/*
 *  UnrollQuadTree.scala
 *  (Grenzwerte)
 *
 *  Copyright (c) 2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.grenzwerte

import de.sciss.file._
import de.sciss.lucre.bitemp.{SpanLike => SpanLikeEx}
import de.sciss.lucre.expr.Expr
import de.sciss.lucre.geom.{IntDistanceMeasure2D, IntPoint2D}
import de.sciss.lucre.stm
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.mellite.Workspace
import de.sciss.mutagentx.SOMQuadTree.{PlacedNode, QuadGraphDB}
import de.sciss.mutagentx.{SOMQuadTree, Vec}
import de.sciss.processor.Processor
import de.sciss.span.{Span, SpanLike}
import de.sciss.synth.proc.graph.{Attribute, FadeInOut, ScanOut}
import de.sciss.synth.proc.{Confluent, FadeSpec, Obj, ObjKeys, Proc, SynthGraphs, Timeline}
import de.sciss.synth.{Lazy, SynthGraph}
import de.sciss.{numbers, synth}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, blocking}

/** Algorithm:
  *
  * 1. make a copy of the database to a temporary directory
  * 2. open db
  * 3. create or append to mellite session
  * 4. create timeline
  * 5. traverse quad-tree and produce procs on the timeline
  *    - use NN search
  *    - replace special UGens
  *    - wire up ScanOut, fade, mute and gain control
  *    - connect to a useful global proc
  *    - remove item from quad-tree copy
  */
object UnrollQuadTree {
  case class Config(session: String = "", startX: Double = 0.5, startY: Double = 0.5,
                    minSpacing: Double = 0.05, maxSpacing: Double = 60.0, clump: Int = 16384,
                    fadeIn: Double = 0.01, fadeOut: Double = 3.0, maxNodes: Int = 0)

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("UnrollQuadTree") {
      opt[String]('s', "session") required() text "session name" action { (x, c) => c.copy(session = x) }
      opt[Double]('x', "start-x") text "horizontal start coordinate" action {
        (x, c) => c.copy(startX = x) } validate { x =>
        if (x >= 0 && x <= 1) success else failure("0 <= start-x <= 1")
      }
      opt[Double]('y', "start-y") text "vertical start coordinate" action {
        (x, c) => c.copy(startY = x) } validate { x =>
        if (x >= 0 && x <= 1) success else failure("0 <= start-y <= 1")
      }
      opt[Double]("min-spacing") text "minimum spacing in seconds" action {
        (x, c) => c.copy(minSpacing = x) } validate { x =>
        if (x >= 0) success else failure("0 <= min-spacing")
      }
      opt[Double]("max-spacing") text "maximum spacing in seconds" action {
        (x, c) => c.copy(maxSpacing = x) } validate { x =>
        if (x >= 0) success else failure("0 <= max-spacing")
      }
      opt[Int]('c', "clump") text "processing clump size" action {
        (x, c) => c.copy(clump = x) } validate { x =>
        if (x >= 1) success else failure("1 <= clump")
      }
      opt[Double]("fade-in") text "fade-in in seconds" action {
        (x, c) => c.copy(fadeIn = x) } validate { x =>
        if (x >= 0) success else failure("0 <= fade-in")
      }
      opt[Double]("fade-out") text "fade-out in seconds" action {
        (x, c) => c.copy(fadeOut = x) } validate { x =>
        if (x >= 0) success else failure("0 <= fade-out")
      }
      opt[Int]('m', "max-nodes") text "maximum number of nodes (0 for no limit)" action {
        (x, c) => c.copy(maxNodes = x) } validate { x =>
        if (x >= 0) success else failure("0 <= max-nodes")
      }
    }
    parser.parse(args, Config()).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = {
    import ExecutionContext.Implicits.global
    val p = Processor[Unit]("unroll") { self =>
      blocking(body(config = config, self = self))
    }
    p.monitor(printResult = false)

    val sync = new AnyRef
    new Thread {
      override def run() = sync.synchronized(sync.wait())
      start()
    }

    p.onComplete(tr => sys.exit(if (tr.isSuccess) 0 else 1)) // sync.synchronized(sync.notifyAll()))
  }

  val baseName = "derrida1_1_216500"

  private val numCoeff   = 13
  private val extent     = 512

  private def body(config: Config, self: Processor[Unit] with Processor.Body): Unit = {
    val dirIn   = QuadGraphDB.mkDir(baseName)
    require(dirIn.isDirectory)
    val dirOut  = File.createTemp(directory = true)
    dirOut.delete()
    import ExecutionContext.Implicits.global
    self.onComplete { _ =>
      def del(f: File): Unit = if (!f.delete()) f.deleteOnExit()
      def loop(in: File): Unit = {
        in.children.foreach { child =>
          if (child.isFile) del(child) else loop(child)
        }
        del(in)
      }
      loop(dirOut)
    }
    import sys.process._
    println(s"Copying database to temporary file '$dirOut'...")
    val cmdCp = Seq("cp", "-pR", dirIn.path, dirOut.path)
    val resCp = cmdCp.!
    if (resCp != 0) {
      throw new Exception(s"Copy failed with code $resCp")
    }
    println("Begin processing database.")

    val quadCfg   = SOMQuadTree.Config(dbName = "n/a", numCoeff = numCoeff, extent = extent, gridStep = 1, maxNodes = 0)
    val quadDB    = QuadGraphDB.open(quadCfg, somDir = dirOut)

    val wsF       = (file("workspaces") / config.session).replaceExt("mllt")
    val wsExists  = wsF.isDirectory
    val wsFun     = if (wsExists) Workspace.Confluent.read _ else Workspace.Confluent.empty _
    val workspace = wsFun(wsF, BerkeleyDB.Config())

    val fullSize0 = quadDB.system.step { implicit tx => quadDB.handle().size }
    val fullSize  = if (config.maxNodes == 0) fullSize0 else math.min(fullSize0, config.maxNodes)

    println(s"Quad-tree size is $fullSize0; will take $fullSize nodes.")

    def getClump(pt0: IntPoint2D, limit: Int): Vec[PlacedNode] = quadDB.system.step { implicit tx =>
      val q       = quadDB.handle()
      val sz      = math.min(config.clump, limit)

      // println(s"CLUMP SIZE = $sz")

      var pt      = pt0
      var b       = Vector.newBuilder[PlacedNode]
      for (i <- 0 until sz) {
        val nodeOpt = q.nearestNeighborOption(pt, IntDistanceMeasure2D.euclideanSq)
        if (nodeOpt.isEmpty) Console.err.println("QuadTree not empty, but NN returns None")
        nodeOpt.foreach { node =>
          q.remove(node)
          b += node
          pt = node.coord
        }
      }
      b.result()
    }

    type S = Confluent

    val fadeInLen   = (Timeline.SampleRate * config.fadeIn  + 0.5).toLong
    val fadeOutLen  = (Timeline.SampleRate * config.fadeOut + 0.5).toLong

    val timelineH = workspace.cursor.step { implicit tx =>
      val tl        = Timeline[S]
      val tlObj     = Obj(Timeline.Elem(tl))
      // tlObj.name  = ""
      workspace.root.addLast(tlObj)

      tx.newHandle(tl) //  tx.newHandle(fdInObj), tx.newHandle(fdOutObj)
    }

    // ObjKeys.attrFadeIn

    val maxDist = math.sqrt(IntPoint2D(0, 0).distanceSq(IntPoint2D(extent * 2 - 1, extent * 2 - 1)))

    def transformGraph(in: SynthGraph)(fun: PartialFunction[Product, Product]): SynthGraph = {
      val map   = new java.util.IdentityHashMap[Product, Product]
      val lift  = fun.lift

      def process1(in: Product): Product = Option(map.get(in)).getOrElse {
        val out0 = lift(in).getOrElse(in)
        println(s"PROCESS $in YIELDS $out0")
        val argIn   = out0.productIterator.toIndexedSeq

        def mapSeq(xs: IndexedSeq[Any]): IndexedSeq[Any] = xs.map {
          case p: Product => process1(p)
          case child: Seq[_] => mapSeq(child.toIndexedSeq) // tricky thing to cover Vec[GE] args as well
          case other => other
        }

        val argOut  = mapSeq(argIn)
        val out = if (argOut == argIn) out0 else {
          val cs      = s"${out0.getClass.getName}$$"
          val cc      = Class.forName(cs)
          val mApplies = cc.getMethods.filter(_.getName == "apply")
          val mApply  = if (mApplies.length == 1) mApplies(0)
          else mApplies.find(_.getParameterTypes.length == argIn.length)
            .getOrElse(throw new Exception(s"Cannot find apply method in ${out0.productPrefix}"))
          val fModule = cc.getField("MODULE$")
          val c       = fModule.get(null)
          val out     = mApply.invoke(c, argOut.map(_.asInstanceOf[AnyRef]): _*)
          out.asInstanceOf[Product]
        }
        map.put(in, out)
        out
      }

      val sourcesOut = in.sources.map(process1).collect {
        case lz: Lazy => lz
      }
      in.copy(sources = sourcesOut)
    }

    def mkProc(node: PlacedNode)(implicit tx: S#Tx): Proc.Obj[S] = {
      val graph0  = node.node.input.graph
      val p       = Proc[S]
      import synth._
      import ugen._
      val graph1  = transformGraph(graph0) {
        case ConfigOut(in) =>
          import synth._
          val sig = in * FadeInOut.ar * (1 - Attribute.kr(ObjKeys.attrMute, 0)) * Attribute.kr(ObjKeys.attrGain, 1)
          ScanOut(sig)

        case EnvGen_ADSR(attack, decay, sustainLevel, release, peakLevel, gate, levelScale, levelBias, timeScale) =>
          val env = Env.adsr(attack = attack, decay = decay, sustainLevel = sustainLevel, release = release, peakLevel = peakLevel)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)

        case EnvGen_ASR(attack, level, release, gate, levelScale, levelBias, timeScale) =>
          val env = Env.asr(attack = attack, level = level, release = release)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)

        case EnvGen_CutOff(release, level, gate, levelScale, levelBias, timeScale) =>
          val env = Env.cutoff(release = release, level = level)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)

        case EnvGen_DADSR(delay, attack, decay, sustainLevel, release, peakLevel, gate, levelScale, levelBias, timeScale) =>
          val env = Env.dadsr(delay = delay, attack = attack, decay = decay, sustainLevel = sustainLevel, release = release, peakLevel = peakLevel)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)

        case EnvGen_Linen(attack, sustain, release, level, gate, levelScale, levelBias, timeScale) =>
          val env = Env.linen(attack = attack, sustain = sustain, release = release, level = level)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)

        case EnvGen_Perc(attack, release, level, gate, levelScale, levelBias, timeScale) =>
          val env = Env.perc(attack = attack, release = release, level = level)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)

        case EnvGen_Sine(dur, level, gate, levelScale, levelBias, timeScale) =>
          val env = Env.sine(dur = dur, level = level)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)

        case EnvGen_Triangle(dur, level, gate, levelScale, levelBias, timeScale) =>
          val env = Env.triangle(dur = dur, level = level)
          EnvGen.ar(env, gate = gate, levelScale = levelScale, levelBias = levelBias, timeScale = timeScale)
      }
      val graph = graph1.copy(sources = graph1.sources.filterNot { lz =>
        lz.productPrefix == "RandSeed"
      })
      p.graph() = SynthGraphs.newConst[S](graph)
      val procObj = Obj(Proc.Elem(p))
      import proc.Implicits._
      procObj.name = s"(${node.coord.x},${node.coord.y})"

      val fdIn      = FadeSpec(numFrames = fadeInLen )
      val fdOut     = FadeSpec(numFrames = fadeOutLen)
      def mkFdObj(fd: FadeSpec) = Obj(FadeSpec.Elem(FadeSpec.Expr.newVar(FadeSpec.Expr.newConst[S](fd))))

      val fdInObj   = mkFdObj(fdIn )
      val fdOutObj  = mkFdObj(fdOut)

      procObj.attr.put(ObjKeys.attrFadeIn , fdInObj )
      procObj.attr.put(ObjKeys.attrFadeOut, fdOutObj)
      procObj
    }

    def putClump(pt0: IntPoint2D, prevH: Option[stm.Source[S#Tx, Expr[S, SpanLike]]],
                 nodes: Vec[PlacedNode]): (IntPoint2D, Option[stm.Source[S#Tx, Expr[S, SpanLike]]]) = {
      workspace.cursor.step { implicit tx =>
        val tl    = timelineH()
        var prev  = prevH.map(_.apply())
        var pt    = pt0
        nodes.foreach { node =>
          import numbers.Implicits._
          val dist    = math.sqrt(node.coord.distanceSq(pt))
          val distN   = dist.toDouble / maxDist
          val dt      = distN.linlin(0, 1, config.minSpacing, config.maxSpacing)
          val dFrames = (dt * Timeline.SampleRate + 0.5).toLong

          val startTime = prev.fold(0L) { prevObj =>
            val res = (prevObj.value match {
                case hs: Span.HasStart => hs.start
                case _ => 0L
              }) + dFrames

            prevObj match {
              case Expr.Var(vr) =>
                val pStart = vr().value match {
                  case phs: Span.HasStart => phs.start
                  case _ => 0L
                }
                vr() = SpanLikeEx.newConst(Span(pStart, pStart + math.min(fadeInLen, dFrames) + fadeOutLen))
            }

            res
          }

          pt = node.coord

          val span    = Span(startTime, startTime + fadeInLen + fadeOutLen)
          val spanEx  = SpanLikeEx.newVar(SpanLikeEx.newConst[S](span))
          val procObj = mkProc(node)

          // println(s"Add at $startTime.")

          val next = tl.add(spanEx, procObj)
          prev = Some(next.span)
        }
        implicit val spanSer = SpanLikeEx.serializer[S]
        val nextH = prev.map(tx.newHandle(_)): Option[stm.Source[S#Tx, Expr[S, SpanLike]]]
        (pt, nextH)
      }
    }

    @tailrec def loop(pt0: IntPoint2D, prevH: Option[stm.Source[S#Tx, Expr[S, SpanLike]]], done: Int): Unit = {
      val nodes = getClump(pt0, limit = fullSize - done)

      println(s"getClump($pt0, limit = ${fullSize - done}) -> ${nodes.size} nodes")

      val done1 = done + nodes.size
      self.progress = done1.toDouble / fullSize
      self.checkAborted()

      if (nodes.nonEmpty) {
        val (pt1, nextH) = putClump(pt0, prevH, nodes)
        loop(pt0 = pt1, prevH = nextH, done = done1)
      }
    }

    val startPt = IntPoint2D((config.startX * extent * 2 + 0.5).toInt,
                             (config.startY * extent * 2 + 0.5).toInt)

    loop(pt0 = startPt, prevH = None, done = 0)

    workspace     .close()
    quadDB.system .close()
  }
}
