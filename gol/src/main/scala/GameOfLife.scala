import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import scala.scalajs.js.Date

@main def hello =
  val prefix = if scalajs.LinkingInfo.isWebAssembly then "wasm" else "js"
  val canvas = dom.document
    .getElementById(
      s"${prefix}_canvas"
    )
    .asInstanceOf[dom.HTMLCanvasElement]

  val tickButton = dom.document.getElementById(s"${prefix}_tick")
  val playButton = dom.document.getElementById(s"${prefix}_play")
  val perf = dom.document.getElementById(s"${prefix}_perf")

  val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  val side = canvas.width
  val gridWidth = 1
  val cells = 30
  val cellSidePixel = side / cells

  ctx.fillStyle = "black"
  val actualSide = cells * cellSidePixel
  ctx.fillRect(0, 0, actualSide, actualSide)

  def rectStart(row: Int, col: Int) =
    val y = row * cellSidePixel
    val x = col * cellSidePixel

    (x, y)

  def renderLive(row: Int, col: Int) =
    val (x, y) = rectStart(row, col)

    ctx.fillStyle = "white"
    ctx.fillRect(x, y, cellSidePixel, cellSidePixel)

  def renderDead(row: Int, col: Int) =
    val (x, y) = rectStart(row, col)

    ctx.fillStyle = "black"
    ctx.fillRect(x, y, cellSidePixel, cellSidePixel)

  // ctx.strokeStyle = "white"
  // ctx.lineWidth = 1
  // for i <- 0 until cells
  // do
  //   ctx.moveTo(0, i * cellSidePixel)
  //   ctx.lineTo(actualSide, i * cellSidePixel)
  //   ctx.stroke()
  //   ctx.moveTo(i * cellSidePixel, 0)
  //   ctx.lineTo(i * cellSidePixel, actualSide)
  //   ctx.stroke()

  var which = 0

  val buffers = Array.fill(2)(Array.fill(cells * cells)(false))

  def offset(row: Int, col: Int) = row * cells + col

  def isLive(row: Int, col: Int) = active(offset(row, col))
  def willSurvive(row: Int, col: Int) = inactive(offset(row, col))

  inline def active = buffers(which)
  inline def inactive = buffers((which + 1) % 2)

  def rotateBuffer() =
    which = (which + 1) % 2

  def render() =
    for
      row <- 0 until cells
      col <- 0 until cells
    do
      if isLive(row, col) then renderLive(row, col)
      else renderDead(row, col)

  var generations = 0

  def draw(s: String, row: Int, col: Int) =
    process(s, row, col)
      .map(offset(_, _))
      .foreach: offset =>
        active(offset) = true

  draw(glider, 5, 5)
  draw(blinker, 1, 1)
  draw(square, 10, 10)
  draw(beacon, 15, 10)
  draw(rPentomino, 22, 15)

  render()

  def countNeighbours(row: Int, col: Int) =
    var live = 0
    for
      nRow <- (row - 1) to (row + 1)
      nCol <- (col - 1) to (col + 1)
      if nRow >= 0 && nRow < cells && nCol >= 0 && nCol < cells && (row != nRow || col != nCol)
    do if isLive(nRow, nCol) then live += 1

    live

  def makeDead(row: Int, col: Int) =
    inactive(offset(row, col)) = false

  def makeAlive(row: Int, col: Int) =
    inactive(offset(row, col)) = true

  def make(row: Int, col: Int, value: Boolean) =
    inactive(offset(row, col)) = value

  def tick() =
    val t0 = Performance.now()
    val buffer = inactive
    for
      row <- 0 until cells
      col <- 0 until cells
    do
      val alive = isLive(row, col)
      val aliveNeighbours = countNeighbours(row, col)

      if alive && aliveNeighbours < 2 then makeDead(row, col)
      else if alive && (aliveNeighbours == 2 || aliveNeighbours == 3) then
        makeAlive(row, col)
      else if alive && aliveNeighbours > 3 then makeDead(row, col)
      else if !alive && aliveNeighbours == 3 then makeAlive(row, col)
      else make(row, col, alive)

    generations += 1

    rotateBuffer()
    render()
    val t1 = Performance.now()

    val fmt = f"${(t1-t0).toInt}%4d"

    perf.innerText = s"[${fmt}ms per generation]"
  end tick

  enum GameState:
    case Paused
    case Running(timeout: Int)

  var prevGameState = Option.empty[GameState]
  var gameState = GameState.Paused
  val onClick: scalajs.js.Function1[dom.Event, ?] = _ => tick()
  tickButton.addEventListener("click", onClick)

  def handle(state: GameState) =
    state match
      case GameState.Paused =>
        tickButton.removeAttribute("disabled")
        playButton.innerText = "Play"
        prevGameState.collect:
          case GameState.Running(timeout) =>
            dom.window.clearTimeout(timeout)
      case GameState.Running(timeout) =>
        tickButton.setAttribute("disabled", "true")
        playButton.innerText = "Stop"

  val onPlayClick: scalajs.js.Function1[dom.Event, ?] = _ =>
    gameState match
      case GameState.Paused =>
        prevGameState = Some(gameState)
        gameState = GameState.Running(dom.window.setInterval(() => tick(), 100))
        handle(gameState)

      case GameState.Running(timeout) =>
        prevGameState = Some(gameState)
        gameState = GameState.Paused
        handle(gameState)

  playButton.addEventListener("click", onPlayClick)
  handle(gameState)

val glider =
  """
|  *
|* *
| **
""".trim.stripMargin

val square =
  """
|**
|**
""".trim().stripMargin

val blinker =
  """
|***
""".trim().stripMargin

val beacon =
  """
|**
|**
|  **
|  **
""".trim().stripMargin

val rPentomino =
  """
| **
|**
| *
""".trim().stripMargin

def process(s: String, row: Int, col: Int) =
  val coords = List.newBuilder[(Int, Int)]
  s.linesIterator.zipWithIndex.toList.foreach: (line, idx) =>
    line
      .toCharArray()
      .zipWithIndex
      .foreach: (char, offset) =>
        if char == '*' then coords.addOne((row + idx, col + offset))

  coords.result()

import scalajs.js

@js.native
trait Performance extends js.Object:
  def now(): Double = js.native

@js.native
@js.annotation.JSGlobal("performance")
object Performance extends Performance
