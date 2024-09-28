import Settings.*

lazy val root = project.aggregate(gol.projectRefs *).in(file("."))

val V = new {
  val Scala = "3.5.1"
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

      // .withExperimentalUseWebAssembly(true) // use the Wasm backend
      mod.withModuleKind(ModuleKind.ESModule) // required by the Wasm backend
    },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
    scalaJSUseMainModuleInitializer := true
  )

Global / onChangedBuildSource := ReloadOnSourceChanges

val gol_Wasm = gol.finder(WasmAxis)(V.Scala)

val buildRelease = taskKey[Unit]("")

buildRelease := {
  val dir = (ThisBuild / baseDirectory).value / "build"
  IO.createDirectory(dir)

  def outWasm = gol.finder(WasmAxis)(V.Scala) / Compile / fullLinkJSOutput
  def outJS = gol.finder(JSAxis)(V.Scala) / Compile / fullLinkJSOutput

  IO.copyDirectory(outWasm.value, dir / "wasm")
  IO.copyDirectory(outJS.value, dir / "js")

  import scala.sys.process.*

  "npm run build".!

  val assets = dir.getParentFile() / "dist" / "assets"

  IO.copyFile(outWasm.value / "main.wasm", assets / "main.wasm")
}

