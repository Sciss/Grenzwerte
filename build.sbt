lazy val baseName = "Grenzwerte"

def baseNameL = baseName.toLowerCase

lazy val projectVersion   = "0.1.0-SNAPSHOT"

lazy val mutagenVersion   = "0.1.1-SNAPSHOT"
lazy val melliteVersion   = "1.6.0-SNAPSHOT"
lazy val ugensVersion     = "1.13.3-SNAPSHOT"

lazy val commonSettings = Seq(
  version            := projectVersion,
  organization       := "de.sciss",
  scalaVersion       := "2.11.7",
  homepage           := Some(url(s"https://github.com/Sciss/$baseName")),
  licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt")),
  scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture")
)

lazy val core = Project(
  id        = s"$baseNameL-core",
  base      = file("core"),
  settings  = commonSettings ++ Seq(
    name        := s"$baseName-sound",
    libraryDependencies ++= Seq(
      "de.sciss"  %% "mutagentx"  % mutagenVersion,
      "de.sciss"  %% "mellite"    % melliteVersion,
      "de.sciss"  %  "scalacolliderugens-spec" % ugensVersion
    )
  )
)
