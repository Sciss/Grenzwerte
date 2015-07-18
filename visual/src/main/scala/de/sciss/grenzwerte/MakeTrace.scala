/*
 *  MakeTrace.scala
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

import java.awt.EventQueue

import de.sciss.file._
import de.sciss.grenzwerte.visual.Visual
import de.sciss.lucre.bitemp.BiGroup
import de.sciss.lucre.event.{InMemory, Sys}
import de.sciss.lucre.geom.IntPoint2D
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.lucre.swing.deferTx
import de.sciss.lucre.{data, stm}
import de.sciss.mellite.Workspace
import de.sciss.mutagentx.SOMQuadTree.Coord
import de.sciss.mutagentx.{Chromosome, Edge, Topology, UGens, Vec, Vertex}
import de.sciss.span.Span
import de.sciss.swingplus.CloseOperation
import de.sciss.{swingplus, synth}
import de.sciss.synth.proc.{Obj, Proc}
import de.sciss.synth.ugen.{BinaryOpUGen, Constant, UnaryOpUGen}
import de.sciss.synth.{audio, UGenSpec, GE, SynthGraph, UGenSource, proc}

import scala.annotation.tailrec
import scala.swing.{Component, Frame, Swing}
import scala.util.Try

object MakeTrace {
  case class Config(session : File = file("workspaces")/"second.mllt",
                    output  : File = file("image_out"),
                    timeline: String = "T-1",
                    test: Boolean = false
                    /* width: Int = 1920, height: Int = 1080, fps: Int = 25, speed: Double = 1.0, */
                    /* durationFactor: Double = 8.0 */)

  implicit object VertexOrd extends data.Ordering[InMemory#Tx, Vertex[InMemory]] {
    type S = InMemory

    def compare(a: Vertex[S], b: Vertex[S])(implicit tx: S#Tx): Int = {
      val aid = stm.Escape.inMemoryID(a.id)
      val bid = stm.Escape.inMemoryID(b.id)
      if (aid < bid) -1 else if (aid > bid) 1 else 0
    }
  }

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("MakeTrace") {
      opt[File]('s', "session") text "session file"     action { (x, c) => c.copy(session = x) }
      opt[File]('d', "output" ) text "output directory" action { (x, c) => c.copy(output  = x) }
      opt[Unit]('t', "test"   ) text "run test routine" action {
        (_, c) => c.copy(test = true)
      }
      opt[String]('t', "timeline") text "timeline name" action { (x, c) => c.copy(timeline = x) }
    }
    parser.parse(args, Config()).fold(sys.exit(1)) { config =>
      if (config.test) testVis() else {
        type S = InMemory
        implicit val system = InMemory()
        val vis = system.step { implicit tx => Visual[S] }
        Swing.onEDT(mkFrame(vis.component))
        run(config, vis)
      }
    }
    // testVis()
  }

  def testVis(): Unit = {
    val graph = SynthGraph {
      import synth._
      import ugen._
      val f   = LFSaw.kr(0.4).madd(24, LFSaw.kr(Seq(8.0, 7.23)).madd(3, 80)).midicps
      val sig = CombN.ar(SinOsc.ar(f)*0.04, 0.2, 0.2, 4)
      Out.ar(0, sig)
    }

    type S = InMemory
    val system = InMemory()
    system.step { implicit tx =>
      val c   = mkChromosome[S](graph)
      val vis = Visual[S]
      vis.insertChromosome(c)
      deferTx {
        /* val f = */ mkFrame(vis.component)
        vis.animationStep()
        Swing.onEDT {
          vis.visualization.synchronized {
            for (i <- 1 to 400) {
              vis.layout.runOnce()
            }
          }
          vis.saveFrameAsPNG(userHome / "Documents" / "temp" / "test_frame.png")
        }
      }
    }
  }

  def mkFrame(c: Component): Frame =
    new Frame { me =>
      contents = c
      pack().centerOnScreen()
      open()
      import swingplus.Implicits._
      me.defaultCloseOperation = CloseOperation.Exit
    }

  private def mkSeqSpec(n: Int) = {
    val out = UGenSpec.Output(name = None, shape = UGenSpec.SignalShape.Generic, variadic = None)
    val args = (0 until n).map { i =>
      UGenSpec.Argument(name = s"in$i", tpe = UGenSpec.ArgumentType.GE(UGenSpec.SignalShape.Generic),
        defaults = Map.empty, rates = Map.empty)
    }
    val inputs = args.map(a => UGenSpec.Input(arg = a.name, tpe = UGenSpec.Input.Single))
    UGenSpec(name = "Mix", attr = Set.empty,
      rates = UGenSpec.Rates.Implied(audio, UGenSpec.RateMethod.Custom("apply")),
      args = args, inputs = inputs, outputs = Vec(out), doc = None)
  }

  /** Note -- this is not a reliable or complete reproduction right now. */
  def mkChromosome[S <: Sys[S]](g: SynthGraph)(implicit tx: S#Tx, ord: data.Ordering[S#Tx, Vertex[S]]): Chromosome[S] = {
    val ugenMap   = UGens.map // UGenSpec.standardUGens
    val vertexMap = new java.util.IdentityHashMap[Product, Vertex.UGen[S]]
    var constMap  = Map.empty[Float, Vertex.Constant[S]]

    val t = Topology.empty[S, Vertex[S], Edge[S]]

    def insertProduct(p: Product): Option[Vertex[S]] = p match {
      case c @ Constant(f) =>
        val v = constMap.getOrElse(f, {
          val _v = Vertex.Constant[S](f)
          constMap += f -> _v
          t.addVertex(_v)
          _v
        })
        Some(v)

      case _ =>
        Option(vertexMap.get(p)).orElse {
          p match {
            case sq if sq.productPrefix == "GESeq" =>
              val elems = sq.productElement(0).asInstanceOf[Vec[GE]]
              val spec  = mkSeqSpec(elems.size)
              val v     = Vertex.UGen(spec)
              vertexMap.put(p, v)
              t.addVertex(v)
              elems.zipWithIndex.foreach {
                case (child: Product, idx) =>
                  insertProduct(child).foreach(cv => t.addEdge(Edge(v, cv, idx)))
                case _ =>
              }
              Some(v)

            case _ =>
              val name = p match {
                case u: BinaryOpUGen  => s"Bin_${u.selector.id}"
                case u: UnaryOpUGen   => s"Un_${u.selector.id}"
                case u: UGenSource[_] => u.name

                case _ => "???"
              }
              val res = ugenMap.get(name).map(Vertex.UGen[S])
              res.foreach { v =>
                vertexMap.put(p, v)
                t.addVertex(v)
                p.productIterator.zipWithIndex.foreach {
                  case (child: Product, idx) =>
                    insertProduct(child).foreach(cv => t.addEdge(Edge(v, cv, idx)))
                  case _ =>
                }
              }
              res
          }
        }
    }

    g.sources.foreach(insertProduct)
    t
  }

  val DEBUG = false

  def mkImage[I <: Sys[I]](dir: File, pc: ProcCoord, time: Long, count: Int, vis: Visual[I])
                          (implicit cursor: stm.Cursor[I], ord: data.Ordering[I#Tx, Vertex[I]]): Unit = {
    cursor.step { implicit tx =>
      val c = mkChromosome[I](pc.graph)
      vis.clearChromosomes()
      vis.insertChromosome(c)
    }

    def edtSync(code: => Unit): Unit =
      if (EventQueue.isDispatchThread) code
      else EventQueue.invokeAndWait(new Runnable { def run() = code })

    val name = s"frame${count}_${time}_${pc.coord.x}_${pc.coord.y}.png"

    edtSync {
      vis.visualization.synchronized {
        for (i <- 1 to 400) {
          vis.layout.runOnce()
        }
        vis.saveFrameAsPNG(dir / name)
      }
    }
  }

  def run[I <: Sys[I]](config: Config, vis: Visual[I])(implicit visCursor: stm.Cursor[I],
                                                       ord: data.Ordering[I#Tx, Vertex[I]]): Unit = {
    de.sciss.mellite.initTypes()
    import config._
    val wsF = session // (file("workspaces") / session).replaceExt("mllt")
    require(wsF.isDirectory, s"Session $wsF not found")
    implicit val workspace = Workspace.Confluent.read(wsF, BerkeleyDB.Config())
    import DSL._
    import proc.Implicits._
    import workspace.cursor

    if (!output.exists()) output.mkdirs()

    val (tlLen, tlH) = cursor.step { implicit tx =>
      val tlT = Try(getTimeline(timeline))
      if (tlT.isFailure) {
        println(workspace.root.iterator.map(_.name).toIndexedSeq.mkString("Objects in root:\n", ", ", ""))
      }
      val tl = tlT.get
      val _tlLen = tl.nearestEventBefore(BiGroup.MaxCoordinate - 2).getOrElse(0L)
      (_tlLen, tx.newHandle(tl))
    }
    var lastProg = 0

    @tailrec def loop(time: Long, count: Int): Unit = {
      val prog = (time.toDouble / tlLen * 100).toInt
      while (lastProg < prog) {
        print('#')
        lastProg += 1
      }

      val pcs: Vec[ProcCoord] = cursor.step { implicit tx =>
        val tl = tlH()
        tl.intersect(time).flatMap[ProcCoord] {
          case (span@Span(`time`, _), xs) =>
            val objs = xs.map(_.value)
            objs.collect {
              case ProcCoord(pc) => pc: ProcCoord
            }
          case _ => Vector.empty[ProcCoord]

        }.toIndexedSeq
      }

      val countOut = (count /: pcs) { case (c1, pc) =>
        mkImage(dir = output, pc = pc, time = time, count = count, vis = vis)
        c1 + 1
      }

      // XXX TODO --- apparently we get a time >= input not a time > input
      val timeOut = cursor.step { implicit tx =>
        val tl = tlH()
        tl.nearestEventAfter(time + 1)
      }

      timeOut match {
        case Some(time1) =>
          assert(time1 > time)
          loop(time1, count = countOut)
        case None =>
      }
    }

    val time0 = cursor.step { implicit tx =>
      val tl = tlH()
      tl.nearestEventAfter(-1L).getOrElse(sys.error(s"Timeline is empty?!"))
    }
    println(f"Start time at ${time0.framesSeconds}%1.1f sec.; end = ${tlLen.framesSeconds}%1.1f sec.")
    println("_" * 100)
    loop(time0, count = 0)

    println(" Done.")
    workspace.close()
    sys.exit()
  }

  object ProcCoord {
    private val RegExp = """\((\d+),(\d+)\).*""".r

    def unapply[S <: Sys[S]](obj: Obj[S])(implicit tx: S#Tx): Option[ProcCoord] =
      Proc.Obj.unapply(obj).flatMap { procObj =>
        import proc.Implicits._
        // procObj.name = s"(${node.coord.x},${node.coord.y})"
        if (procObj.muted) None else procObj.name match {
          case RegExp(a, b) => Try {
              val pt = IntPoint2D(a.toInt, b.toInt)
              new ProcCoord(pt, procObj.elem.peer.graph.value)
            } .toOption
          case _ => None
        }
      }
  }
  class ProcCoord(val coord: Coord, val graph: SynthGraph)
}