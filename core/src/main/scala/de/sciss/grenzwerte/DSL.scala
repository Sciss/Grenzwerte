/*
 *  DSL.scala
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

import de.sciss.kollflitz.Vec
import de.sciss.lucre.event.Sys
import de.sciss.lucre.geom.IntPoint2D
import de.sciss.mellite.gui.ObjView
import de.sciss.mellite.{Color, Workspace}
import de.sciss.synth.proc.Implicits._
import de.sciss.synth.proc.{Folder, FolderElem, Obj, Proc, Timeline}

import scala.annotation.tailrec
import scala.collection.breakOut

object DSL {
  /** Retrieves or creates (if not found) a folder in the workspace root. */
  def mkFolder[S <: Sys[S]](name: String)(implicit tx: S#Tx, workspace: Workspace[S]): Folder[S] = {
    val r = workspace.root
    (r / name).fold[Folder[S]] {
      val f     = Folder[S]
      val fObj  = Obj(FolderElem(f))
      fObj.name = name
      r.addLast(fObj)
      f
    } {
      case FolderElem.Obj(f) => f.elem.peer
    }
  }

  def getTimeline[S <: Sys[S]](name: String)(implicit tx: S#Tx, workspace: Workspace[S]): Timeline.Modifiable[S] = {
    val r = workspace.root
    (r / name).fold[Timeline.Modifiable[S]] {
      sys.error(s"Timeline '$name' not found")
    } {
      case Timeline.Obj(objT) => objT.elem.peer.modifiableOption.getOrElse(
        sys.error(s"Timeline '$name' is not modifiable")
      )
      case other => sys.error(s"Object named '$name' is not a timeline: $other")
    }
  }

  implicit class MyObjectOps[S <: Sys[S]](private val obj: Obj[S]) extends AnyVal {
    def color(implicit tx: S#Tx): Option[Color] = obj.attr[Color.Elem](ObjView.attrColor).map(_.value)
    def layer(implicit tx: S#Tx): Int           = color.fold(-1)(Color.Palette.indexOf)
    def quadLoc(implicit tx: S#Tx): Option[IntPoint2D] = {
      val n = obj.name
      // s"(${node.coord.x},${node.coord.y})"
      val i = n.indexOf(')')
      if (i < 0) None else {
        val x :: y :: Nil = n.substring(1, i).split(',').map(_.toInt)(breakOut): List[Int]
        Some(IntPoint2D(x, y))
      }
    }
  }

  implicit class MyTimelineOps[S <: Sys[S]](private val tl: Timeline[S]) extends AnyVal {
    private def procs(time: Long)(filter: Option[Color] => Boolean)
                     (implicit tx: S#Tx): Vec[Proc.Obj[S]] =
      tl.intersect(time).flatMap { case (span, xs) =>
        xs.map(_.value).collect {
          case Proc.Obj(objT) if filter(objT.color) => objT
        }
      } .toIndexedSeq

    def placed  (time: Long)(implicit tx: S#Tx): Vec[Proc.Obj[S]] =  procs(time)(_.isDefined)
    def unplaced(time: Long)(implicit tx: S#Tx): Vec[Proc.Obj[S]] =  procs(time)(_.isEmpty  )

    def nearestInLayerBefore(layer: Int, time: Long)(implicit tx: S#Tx): Option[Proc.Obj[S]] = {
      @tailrec def loop(time0: Long): Option[Proc.Obj[S]] = {
        val time1Opt = tl.nearestEventBefore(time0 - 1)
        time1Opt match {
          case Some(time1) =>
            assert(time1 < time0)
            val inLayer = tl.placed(time1).filter(_.layer == layer)
            if (inLayer.nonEmpty) inLayer.headOption /* we drop span info, so what */ else loop(time1)

          case None => None
        }
      }
      loop(time)
    }
  }

  implicit class FramesSeconds(private val n: Long) extends AnyVal { me =>
    /** Interprets the number as a frame number, and converts it to seconds,
      * based on the standard `Timeline` sample-rate.
      */
    def framesSeconds: Double = n.toDouble / Timeline.SampleRate
  }
}