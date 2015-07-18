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

import de.sciss.file._
import de.sciss.grenzwerte.visual.Visual
import de.sciss.lucre.bitemp.{SpanLike => SpanLikeEx}
import de.sciss.lucre.{stm, data}
import de.sciss.lucre.event.{InMemory, Sys}
import de.sciss.lucre.expr.{Int => IntEx}
import de.sciss.lucre.swing.deferTx
import de.sciss.mutagentx.{Chromosome, Edge, Topology, UGens, Vertex}
import de.sciss.swingplus
import de.sciss.swingplus.CloseOperation
import de.sciss.synth
import de.sciss.synth.ugen.{BinaryOpUGen, Constant, UnaryOpUGen}
import de.sciss.synth.{SynthGraph, UGenSource}

import scala.swing.{Swing, Frame}

object MakeTrace {
  case class Config(session: String = "second", timeline: String = "T-1",
                    width: Int = 1920, height: Int = 1080, fps: Int = 25, speed: Double = 1.0,
                    durationFactor: Double = 8.0)

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("MakeTrace") {
      opt[String]('s', "session") text "session name" action { (x, c) => c.copy(session = x) }
//      opt[Unit]('r', "remove-colors") text "initially clear old color attributes" action {
//        (_, c) => c.copy(removeColors = true)
//      }
      opt[String]('t', "timeline") text "timeline name" action { (x, c) => c.copy(timeline = x) }
    }
    // parser.parse(args, Config()).fold(sys.exit(1))(run)
    testVis()
  }

  implicit object VertexOrd extends data.Ordering[InMemory#Tx, Vertex[InMemory]] {
    type S = InMemory

    def compare(a: Vertex[S], b: Vertex[S])(implicit tx: S#Tx): Int = {
      val aid = stm.Escape.inMemoryID(a.id)
      val bid = stm.Escape.inMemoryID(b.id)
      if (aid < bid) -1 else if (aid > bid) 1 else 0
    }
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
        new Frame { me =>
          contents = vis.component
          pack().centerOnScreen()
          open()
          import swingplus.Implicits._
          me.defaultCloseOperation = CloseOperation.Exit
          // vis.forceSimulator.setSpeedLimit(100f)
          // vis.layout
          vis.animationStep()
          // vis.runAnimation = true
        }
        Swing.onEDT {
          for (i <- 0 to 400) {
            vis.layout.runOnce()
          }
          vis.saveFrameAsPNG(userHome / "Documents" / "temp" / "test_frame.png")
        }
      }
    }
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

    g.sources.foreach(insertProduct)
    t
  }

  val DEBUG = false
/*
  def run(config: Config): Unit = {
    de.sciss.mellite.initTypes()
    import config._
    val wsF = (file("workspaces") / session).replaceExt("mllt")
    require(wsF.isDirectory, s"Session $wsF not found")
    implicit val workspace = Workspace.Confluent.read(wsF, BerkeleyDB.Config())
    import workspace.{S, cursor}

    import proc.Implicits._
    import DSL._

    cursor.step { implicit tx =>
      val tlT   = Try(getTimeline(timeline))
      if (tlT.isFailure) {
        println(workspace.root.iterator.map(_.name).toIndexedSeq.mkString("Objects in root:\n", ", ", ""))
      }
      val tl    = tlT.get
      val tlLen = tl.nearestEventBefore(BiGroup.MaxCoordinate - 2).getOrElse(0L)

      var lastProg = 0

      var lastLayerProc = Map.empty[Int, (Span, Proc.Obj[S])]

      @tailrec def loop(time: Long, movieTime: Double): Unit = {
        val prog = (time.toDouble / tlLen * 100).toInt
        while (lastProg < prog) {
          print('#')
          lastProg += 1
        }

        tl.unplaced(time).foreach { case (succSpan, succ) =>
          if (DEBUG) println(s"\nAT $time WE FIND UNPLACED $succSpan | ${succ.name}")
          succ.quadLoc.foreach { succLoc =>
            val occupied    = tl.placed(time)
            if (DEBUG) println(s"We FIND PLACED ${occupied.map(_._2.name).mkString(", ")}")
            val freeLayers0 = allLayers diff occupied.map(_._2.layer)
            if (DEBUG) println(s"FREE LAYERS ARE ${freeLayers0.mkString(", ")}")
            if (DEBUG) println(s"LAST LAYERS EXIST IN ${lastLayerProc.keys.toList.sorted.mkString(", ")}")
            val freeLayers  = if (freeLayers0.nonEmpty) freeLayers0 else allLayers
            val distances: Vec[Long] = freeLayers.map { layer =>
              lastLayerProc.get(layer).fold(Long.MaxValue) { case (span, pred) =>
                val predLoc = pred.quadLoc
                predLoc.fold(Long.MaxValue)(succLoc distanceSq _)
              }
            }
            if (DEBUG) println(s"DISTANCES ARE ${distances.zipWithIndex.map { case (d, i) => s"$i: $d" }.mkString(", ")}")
            val minDistance = distances.min
            val layer = freeLayers(distances.indexOf(minDistance))
            if (DEBUG) println(s"YIELD A BEST LAYER OF $layer")
            // assign layer: assign color, disconnect scan-out, reconnect scan-out
            succ.attr.put(ObjView.attrColor, layerColors(layer))
            val succOut = succ.elem.peer.scans.add("out")
            succOut.sinks.foreach {
              case Scan.Link.Scan(that) => succOut ~/> that
              case _ =>
            }
            succOut ~> layerOuts(layer).elem.peer.scans.add("in")
            lastLayerProc += layer -> (succSpan, succ)
          }
        }
        // XXX TODO --- apparently we get a time >= input not a time > input
        tl.nearestEventAfter(time + 1) match {
          case Some(time1) =>
            assert(time1 > time)
            loop(time1)
          case None =>
        }
      }

      val time0 = tl.nearestEventAfter(-1L).getOrElse(sys.error(s"Timeline is empty?!"))
      println(f"Start time at ${time0.framesSeconds}%1.1f sec.; end = ${tlLen.framesSeconds}%1.1f sec.")
      println("_" * 100)
      loop(time0, movieTime = 0.0)
    }

    println(" Done.")
    workspace.close()
    sys.exit()
  }
  */
}