package hack

import com.badlogic.gdx.Gdx.{app, input}
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.{Game, Screen}
import hack.game.{Arch, Tile, World}

class HackGame extends Game {
  override def create() {
    this.setScreen(new GameScreen)
  }
}

class GameScreen extends Screen {
  // static initialization hack
  val hck = (Tile.impassableGround, Arch.soldier)

  val inputHandler = new InputHandler()
  val world = generateWorld()
  val renderer = new Renderer()

  def generateWorld() : World = {
    val w = new World(16, 16, Tile.standardGround, 9)

    w.setTileAt(0, 0, Tile.playerAFactory)
    w.setTileAt(1, 1, Tile.impassableGround)

    w.setTileAt(14, 14, Tile.impassableGround)
    w.setTileAt(15, 15, Tile.playerBFactory)

    w
  }

  def handleGlobalInput(): Unit = {
    if (input.isKeyJustPressed(Keys.ESCAPE))
      app.exit()
  }

  def keyboardInput(keys: PlayerKeys): PlayerInput = {
    var x = 0: Int
    var y = 0: Int
    var t = 0: Int
    var a = false: Boolean

    if (input.isKeyJustPressed(keys.left))
      x -= 1

    if (input.isKeyJustPressed(keys.right))
      x += 1

    if (input.isKeyJustPressed(keys.down))
      y -= 1

    if (input.isKeyJustPressed(keys.up))
      y += 1

    if (input.isKeyJustPressed(keys.previous))
      t -= 1

    if (input.isKeyJustPressed(keys.next))
      t += 1

    if (input.isKeyJustPressed(keys.action))
      a = true

    PlayerInput(x, y, t, a)
  }

  def show() = {}

  def hide() = {}

  def pause() = {}

  def resume() = {}

  def dispose() = {}

  var t = 0.0
  var simulationAccu = 0.0
  val simulationTickEvery = 1.0 // every 1 second

  def render(delta: Float) {
    t += delta
    simulationAccu += delta
    // determine game ticks here
    inputHandler.handleFor(world)

    if (simulationAccu >= simulationTickEvery) {
      GameLogic.updateWorld(world)
      world.tick += 1
      simulationAccu -= simulationTickEvery
    }

    handleGlobalInput()
    val input1: PlayerInput = keyboardInput(PlayerKeys.Player1)
    val input2: PlayerInput = keyboardInput(PlayerKeys.Player2)

    renderer.render(world, simulationAccu)
  }

  def resize(width: Int, height: Int) {
  }
}
