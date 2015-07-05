/*
 *  MakeLayers.scala
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
import de.sciss.kollflitz.Vec
import de.sciss.lucre.bitemp.{BiGroup, SpanLike => SpanLikeEx}
import de.sciss.lucre.expr.{Int => IntEx}
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.mellite.gui.ObjView
import de.sciss.mellite.{Color, Workspace}
import de.sciss.span.Span
import de.sciss.synth
import de.sciss.synth.proc.graph.{Attribute, ScanInFix}
import de.sciss.synth.{SynthGraph, proc}
import de.sciss.synth.proc.{IntElem, SynthGraphs, ObjKeys, Scan, Obj, Proc}

import scala.annotation.tailrec
import scala.util.Try

/*
  - take an existing timeline
  - arrange regions into layers so that within each layer
    there are no overlapping regions
  - distinguish each layer by color and target sink.
    thus we have to disconnect from previous sinks and
    create new ones.
  - how to make sense of the inner-layer groupings?
    a simple idea is to use quad-tree locality in the
    sense that when a region is placed, it will pick
    among the layers that have free space the one
    with the closest predecessor.

 */
object MakeLayers {
  case class Config(session: String = "second", timeline: String = "T-1", removeColors: Boolean = false)

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("MakeLayers") {
      opt[String]('s', "session") text "session name" action { (x, c) => c.copy(session = x) }
      opt[Unit]('r', "remove-colors") text "initially clear old color attributes" action {
        (_, c) => c.copy(removeColors = true)
      }
      opt[String]('t', "timeline") text "timeline name" action { (x, c) => c.copy(timeline = x) }
    }
    parser.parse(args, Config()).fold(sys.exit(1))(run)
  }

  def run(config: Config): Unit = {
    import config._
    val wsF = (file("workspaces") / session).replaceExt("mllt")
    require(wsF.isDirectory, s"Session $wsF not found")
    implicit val workspace = Workspace.Confluent.read(wsF, BerkeleyDB.Config())
    import workspace.{S, cursor}

    import proc.Implicits._
    import DSL._

    val allLayers = Color.Palette.indices

    cursor.step { implicit tx =>
      val tlT   = Try(getTimeline(timeline))
      if (tlT.isFailure) {
        println(workspace.root.iterator.map(_.name).toIndexedSeq.mkString("Objects in root:\n", ", ", ""))
      }
      val tl    = tlT.get
      val tlLen = tl.nearestEventBefore(BiGroup.MaxCoordinate).getOrElse(0L)

      if (removeColors) {
        tl.iterator.foreach { case (_, xs) =>
          xs.foreach { timed =>
            timed.value.attr.remove(ObjView.attrColor)
          }
        }
      }

      val layerColors: Vec[Obj[S]] = Color.Palette.map { colr =>
        Obj(Color.Elem(Color.Expr.newVar(Color.Expr.newConst[S](colr))))
      }

      val layerOuts: Vec[Proc.Obj[S]] = allLayers.map { layer =>
        val name = s"layer-${layer + 1}"
        val existing = tl.intersect(BiGroup.MinCoordinate).flatMap(_._2.map(_.value)).toList.collectFirst {
          case Proc.Obj(objT) if objT.name == name => objT
        }
        existing.getOrElse {
          val global    = Proc[S]
          val glGraph   = SynthGraph {
            import synth._
            import ugen._
            val in  = ScanInFix(numChannels = 1)
            val sig = in * (1 - Attribute.kr(ObjKeys.attrMute, 0)) * Attribute.kr(ObjKeys.attrGain, 1)
            val bus = Attribute.kr(ObjKeys.attrBus, 0)
            // val pan = Attribute.kr("pan", 0)
            Out.ar(bus, sig) // Pan2.ar(sig, pos = pan))
          }

          global.graph()  = SynthGraphs.newConst(glGraph)
          global.scans.add("in")

          val glObj       = Obj(Proc.Elem(global))
          glObj.attr.put(ObjKeys.attrBus, Obj(IntElem(IntEx.newVar(IntEx.newConst[S](layer)))))
          glObj.name      = name
          tl.add(SpanLikeEx.newConst[S](Span.All), glObj)
          glObj
        }
      }

      var lastProg = 0
      println("_" * 100)

      @tailrec def loop(time: Long): Unit = {
        val prog = (time.toDouble / tlLen * 100).toInt
        while (lastProg < prog) {
          print('#')
          lastProg += 1
        }

        tl.unplaced(time).foreach { succ =>
          succ.quadLoc.foreach { succLoc =>
            val occupied    = tl.placed(time)
            val freeLayers0 = allLayers diff occupied.map(_.layer)
            val freeLayers  = if (freeLayers0.nonEmpty) freeLayers0 else allLayers
            val distances: Vec[Long] = freeLayers.map { layer =>
              tl.nearestInLayerBefore(layer, time).fold(Long.MaxValue) { pred =>
                val predLoc = pred.quadLoc
                predLoc.fold(Long.MaxValue)(succLoc distanceSq _)
              }
            }
            val minDistance = distances.min
            val layer = distances.indexOf(minDistance)
            // assign layer: assign color, disconnect scan-out, reconnect scan-out
            succ.attr.put(ObjView.attrColor, layerColors(layer))
            val succOut = succ.elem.peer.scans.add("out")
            succOut.sinks.foreach {
              case Scan.Link.Scan(that) => succOut ~/> that
              case _ =>
            }
            succOut ~> layerOuts(layer).elem.peer.scans.add("in")
          }
        }
        tl.nearestEventAfter(time) match {
          case Some(time1) => loop(time1)
          case None =>
        }
      }

      val time0 = tl.nearestEventAfter(-1L).getOrElse(sys.error(s"Timeline is empty?!"))
      loop(time0)
    }

    println(" Done.")
    workspace.close()
    sys.exit()
  }
}