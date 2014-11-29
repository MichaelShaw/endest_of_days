package hack

/**
 * Created by michael on 29/11/14.
 */

import com.badlogic.gdx.{Screen, Game}
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
  val world = new World(16, 16, Tile.standardGround)
  val renderer = new Renderer()

  def show() = {}
  def hide() = {}
  def pause() = {}
  def resume() = {}
  def dispose() = {}

  var t = 0.0
  var simulationAccu = 0.0
  val simulationTickEvery = 1.0 // every 1 second

  def render(delta:Float) {
    t += delta
    simulationAccu += delta
    // determine game ticks here
    inputHandler.handleFor(world)

    if(simulationAccu >= simulationTickEvery) {
      GameLogic.updateWorld(world)
      world.tick += 1
      simulationAccu -= simulationTickEvery
    }

    // handle input ... modifies world
    renderer.render(world)
  }
  def resize(width:Int, height:Int) {

  }
}