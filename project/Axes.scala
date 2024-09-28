import sbt.VirtualAxis.ScalaVersionAxis

import sbt.VirtualAxis

object Settings {
  case class BackendAxis(idSuffix: String, directorySuffix: String)
      extends VirtualAxis.WeakAxis

  lazy val WasmAxis = BackendAxis("_WASM", "wasm")
  lazy val JSAxis = BackendAxis("_JS", "js")
}
