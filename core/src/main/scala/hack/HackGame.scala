package hack

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

  val world = generateWorld()
  val renderer = new Renderer()

  val inputHandler = new InputHandler(world)
  inputHandler.setAsListener()

  def generateWorld() : World = {
    val w = new World(16, 16, Tile.standardGround, 9)

//    w.placeTileAt(6, 15, Tile.playerBSoldierFactory)
//    w.placeTileAt(8, 15, Tile.playerBCaptainFactory)
//    w.placeTileAt(10, 15, Tile.playerBAEFactory)
//    w.placeTileAt(12, 15, Tile.playerBDefenderFactory)

    w.placeTileAt(6, 15, Tile.playerBCaptainFactory)

    w.placeTileAt(7, 7, Tile.impassableGround)
    w.placeTileAt(8, 7, Tile.impassableGround)
    w.placeTileAt(9, 7, Tile.impassableGround)

//    w.placeTileAt(6, 0, Tile.playerASoldierFactory)
//    w.placeTileAt(8, 0, Tile.playerACaptainFactory)
//    w.placeTileAt(10, 0, Tile.playerAAEFactory)
//    w.placeTileAt(12, 0, Tile.playerADefenderFactory)

    w.placeTileAt(6, 0, Tile.playerASoldierFactory)

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

  def render(delta : Float) {
    t += delta
    simulationAccu += delta
    // determine game ticks here

    if (simulationAccu >= simulationTickEvery) {
      GameLogic.updateWorld(world)
      world.tick += 1
      simulationAccu -= simulationTickEvery
    }

    renderer.render(world, simulationAccu)
  }

  def resize(width : Int, height : Int) {
  }
}
