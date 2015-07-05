package de.sciss.grenzwerte

import de.sciss.file._
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.mellite.gui.ObjView
import de.sciss.mellite.{Color, Workspace}
import de.sciss.synth.proc
import de.sciss.synth.proc.{Scan, Obj, Proc}

import scala.annotation.tailrec

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
  case class Config(session: String = "second", timeline: String = "T1", removeColors: Boolean)

  def main(args: Array[String]): Unit = {

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
      val tl = getTimeline(timeline)

      if (removeColors) {
        tl.iterator.foreach { case (_, xs) =>
          xs.foreach { timed =>
            timed.value.attr.remove(ObjView.attrColor)
          }
        }
      }

      val layerColors = Color.Palette.map { colr =>
        Obj(Color.Elem(Color.Expr.newVar(Color.Expr.newConst[S](colr))))
      }

      val layerOuts = ???

      @tailrec def loop(time: Long): Unit = {
        tl.unplaced(time).foreach { succ =>
          val occupied    = tl.placed(time)
          val freeLayers0 = allLayers diff occupied.map(_.layer)
          val freeLayers  = if (freeLayers0.nonEmpty) freeLayers0 else allLayers
          val succLoc     = succ.quadLoc
          val distances   = freeLayers.map { layer =>
            tl.nearestInLayerBefore(layer, time).fold(Long.MaxValue) { pred =>
              val predLoc = pred.quadLoc
              succLoc distanceSq predLoc
            }
          }
          val bestLayer = distances.zipWithIndex.minBy(_._1)._2
          // assign layer: assign color, disconnect scan-out, reconnect scan-out
          succ.attr.put(ObjView.attrColor, layerColors(bestLayer))
          val succOut = succ.elem.peer.scans.add("out")
          succOut.sinks.foreach {
            case Scan.Link.Scan(that) => succOut ~/> that
            case _ =>
          }
          succOut ~> ???
        }
        tl.nearestEventAfter(time) match {
          case Some(time1) => loop(time1)
          case None =>
        }
      }

      val time0 = tl.nearestEventAfter(-1L).getOrElse(sys.error(s"Timeline is empty?!"))
      loop(time0)
    }
  }
}