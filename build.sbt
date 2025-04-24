import Settings.*

lazy val root = project.aggregate(gol.projectRefs *).in(file("."))

val V = new {
  val Scala = "3.7.0-RC3"
}

lazy val gol = projectMatrix
  .in(file("gol"))
  .defaultAxes(VirtualAxis.js, VirtualAxis.scalaABIVersion(V.Scala))
  .customRow(Seq(V.Scala), Seq(VirtualAxis.js, WasmAxis), Seq.empty)
  .customRow(Seq(V.Scala), Seq(VirtualAxis.js, JSAxis), Seq.empty)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    // Emit ES modules with the Wasm backend
    scalaJSLinkerConfig := {
      var mod = scalaJSLinkerConfig.value

      if (virtualAxes.value.contains(WasmAxis)) {
        mod = mod.withExperimentalUseWebAssembly(true)
      }

      mod.withModuleKind(ModuleKind.ESModule) // required by the Wasm backend
    },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
    scalaJSUseMainModuleInitializer := true
  )

Global / onChangedBuildSource := ReloadOnSourceChanges

val buildFast = taskKey[Unit]("")

buildFast := {
  val dir = (ThisBuild / baseDirectory).value / "build"
  IO.createDirectory(dir)

  def outWasm = gol.finder(WasmAxis)(V.Scala) / Compile / fastLinkJSOutput
  def outJS = gol.finder(JSAxis)(V.Scala) / Compile / fastLinkJSOutput

  IO.copyDirectory(outWasm.value, dir / "wasm")
  IO.copyDirectory(outJS.value, dir / "js")
}

val buildRelease = inputKey[Unit]("")

buildRelease := {
  import complete.DefaultParsers._
  val args: Seq[String] = spaceDelimited("<arg>").parsed

  val command = args.headOption.getOrElse("build")

  val dir = (ThisBuild / baseDirectory).value / "build"
  IO.createDirectory(dir)

  def outWasm = gol.finder(WasmAxis)(V.Scala) / Compile / fullLinkJSOutput
  def outJS = gol.finder(JSAxis)(V.Scala) / Compile / fullLinkJSOutput

  IO.copyDirectory(outWasm.value, dir / "wasm")
  IO.copyDirectory(outJS.value, dir / "js")

  import scala.sys.process.*

  s"npm run $command".!

  val assets = dir.getParentFile() / "dist" / "assets"

  IO.copyFile(outWasm.value / "main.wasm", assets / "main.wasm")
  IO.copyFile(outWasm.value / "main.wasm.map", assets / "main.wasm.map")
}

addCommandAlias("buildForGithubPages", "buildRelease buildForGithubPages")
