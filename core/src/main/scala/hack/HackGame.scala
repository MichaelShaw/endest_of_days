package hack

import com.badlogic.gdx.{Game, Screen}
import hack.game.{Tile, World}

class HackGame extends Game {
  override def create() {
    this.setScreen(new GameScreen)
  }
}

class GameScreen extends Screen {
  // static initialization hack
  val hck = (Tile.impassableGround)

  val inputHandler = new InputHandler()
  val world = generateWorld()
  val renderer = new Renderer()

  def generateWorld(): World = {
    val w = new World(16, 16, Tile.standardGround)

    w.setTileAt(1, 1, Tile.impassableGround)

    w.setTileAt(14, 14, Tile.impassableGround)

    w
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

    // handle input ... modifies world
    renderer.render(world)
  }

  def resize(width: Int, height: Int) {
  }
}
