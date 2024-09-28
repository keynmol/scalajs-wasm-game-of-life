import Settings.*

lazy val root = project.aggregate(gol.projectRefs*).in(file("."))

lazy val gol = projectMatrix
  .in(file("gol"))
  .defaultAxes(VirtualAxis.js, VirtualAxis.scalaABIVersion("3.5.1"))
  .customRow(Seq("3.5.1"), Seq(VirtualAxis.js, WasmAxis), Seq.empty)
  .customRow(Seq("3.5.1"), Seq(VirtualAxis.js, JSAxis), Seq.empty)
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

    scalaJSUseMainModuleInitializer := true,

    // // Configure Node.js (at least v22) to support the required Wasm features
    // jsEnv := {
    //   val config = NodeJSEnv.Config()
    //     .withArgs(List(
    //       "--experimental-wasm-exnref", // required
    //       "--experimental-wasm-imported-strings", // optional (good for performance)
    //       "--turboshaft-wasm", // optional, but significantly increases stability
    //     ))
    //   new NodeJSEnv(config)
    // },
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
