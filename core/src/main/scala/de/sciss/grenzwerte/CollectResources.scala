package de.sciss.grenzwerte

import de.sciss.file._
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.mellite.{ProcActions, Workspace}
import de.sciss.synth.proc.{AudioGraphemeElem, Proc, Timeline}

object CollectResources {
  case class Config(session: File = file("workspaces")/"second.mllt")

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("CollectResources") {
      opt[File]('s', "session") required() text "session file" action { (x, c) => c.copy(session = x) }
    }
    parser.parse(args, Config()).fold(sys.exit(1)) { config =>
      run(config.session)
    }
  }

  def run(session: File): Unit = {
    de.sciss.mellite.initTypes()
    val wsF = session // (file("workspaces") / session).replaceExt("mllt")
    require(wsF.isDirectory, s"Session $wsF not found")
    implicit val workspace = Workspace.Confluent.read(wsF, BerkeleyDB.Config())

    println(s"Processing ${session.name}...")

    val res = workspace.cursor.step { implicit tx =>
      val timelines = workspace.collectObjects {
        case Timeline.Obj(tl) => tl.elem.peer
      }

      var set = Set.empty[File]

      timelines.foreach { tl =>
        tl.iterator.foreach { case (span, xs) =>
          xs.foreach { timed =>
            timed.value match {
              case Proc.Obj(proc) =>
                ProcActions.getAudioRegion(proc).foreach { case (_, g) =>
                    set += g.artifact.value
                }
            }

            timed.value.attr.iterator.foreach {
              case (_, AudioGraphemeElem.Obj(obj)) =>
                set += obj.elem.peer.artifact.value
              case _ =>
            }
          }
        }
      }

      set
    }

    val vec = res.toIndexedSeq
    println(s"\n======== FOUND ${vec.size} AUDIO FILES IN ${session.name} ========")
    vec.sortBy(_.name.toLowerCase).foreach(println)
    println()

    workspace.close()
    sys.exit()
  }
}
