lazy val baseName = "Grenzwerte"

def baseNameL = baseName.toLowerCase

lazy val projectVersion   = "0.2.0-SNAPSHOT"

lazy val mutagenVersion   = "0.2.0"
lazy val melliteVersion   = "1.7.0"
lazy val ugensVersion     = "1.13.3"

lazy val commonSettings = Seq(
  version            := projectVersion,
  organization       := "de.sciss",
  scalaVersion       := "2.11.7",
  homepage           := Some(url(s"https://github.com/Sciss/$baseName")),
  licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt")),
  scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature", "-Xfuture")
)

lazy val root = Project(id = baseNameL, base = file(".")).
  aggregate(core, visual).
  dependsOn(core, visual).
  settings(commonSettings).
  settings(
    publishArtifact in (Compile, packageBin) := false, // there are no binaries
    publishArtifact in (Compile, packageDoc) := false, // there are no javadocs
    publishArtifact in (Compile, packageSrc) := false  // there are no sources
  )

lazy val core = Project(id = s"$baseNameL-core", base = file("core")).
  settings(commonSettings).
  settings(
    name        := s"$baseName-sound",
    libraryDependencies ++= Seq(
      "de.sciss"  %% "mutagentx"  % mutagenVersion,
      "de.sciss"  %% "mellite"    % melliteVersion,
      "de.sciss"  %  "scalacolliderugens-spec" % ugensVersion
    )
  )

lazy val visual = Project(id = s"$baseNameL-visual", base = file("visual")).
  dependsOn(core).
  settings(commonSettings).
  settings(
    name := s"$baseName-visual",
    libraryDependencies ++= Seq(
    )
  )
