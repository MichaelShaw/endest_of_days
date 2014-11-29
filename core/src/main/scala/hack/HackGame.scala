package hack

import com.badlogic.gdx.{Game, Screen}
import hack.game.Vec2i
import hack.game.{Arch, Tile, World}

class HackGame extends Game {
  override def create() {
    this.setScreen(new GameScreen)
  }
}

class GameScreen extends Screen {
  // static initialization hack
  val hck = (Tile.impassableGround, Arch.imp)

  var world = generateWorld()
  val renderer = new Renderer()

  val inputHandler = new InputHandler(world)
  inputHandler.setAsListener()

  def generateWorld() : World = {
    val w = new World(16, 16, Tile.standardGround, 9)

//    w.placeTileAt(6, 15, Tile.playerBSoldierFactory)
//    w.placeTileAt(8, 15, Tile.playerBCaptainFactory)
//    w.placeTileAt(10, 15, Tile.playerBAEFactory)
//    w.placeTileAt(12, 15, Tile.playerBDefenderFactory)

    w.placeTileAt(Vec2i(8, 15), Tile.cultistSpawner, 1)
    w.placeTileAt(Vec2i(12, 15), Tile.impSpawner, 1)

//    w.placeTileAt(Vec2i(2, 13), Tile.gate, 1)

    for {
      x <- 3 to 15
    } {
      w.placeTileAt(Vec2i(x, 7), Tile.impassableGround, -1)
    }
    for {
      y <- 4 to 12
    } {
      w.placeTileAt(Vec2i(3, y), Tile.impassableGround, -1)
    }

//    w.placeTileAt(Vec2i(2, 3), Tile.gate, 0)

//    w.placeTileAt(6, 0, Tile.playerASoldierFactory)
//    w.placeTileAt(8, 0, Tile.playerACaptainFactory)
//    w.placeTileAt(10, 0, Tile.playerAAEFactory)
//    w.placeTileAt(12, 0, Tile.playerADefenderFactory)

    w.placeTileAt(Vec2i(8, 0), Tile.cultistSpawner, 0)
    w.placeTileAt(Vec2i(12, 0), Tile.bigBeetleSpawner, 0)

    w
  }

  def show() = {}

  def hide() = {}

  def pause() = {}

  def resume() = {}

  def dispose() = {}

  var t = 0.0
  var simulationAccu = 0.0
  val simulationTickEvery = 0.15  // every 1 second

  var running = true

  def resetGame() {
    t = 0.0
    simulationAccu = 0.0
    world = generateWorld()
    inputHandler.world = world
  }

  def render(delta : Float) {
    if(!running) {
      resetGame()
      running = true
    }

    t += delta
    if(running) {
      simulationAccu += delta
    }

    // determine game ticks here

    if (simulationAccu >= simulationTickEvery) {
      GameLogic.updateWorld(world)
      world.tick += 1
      simulationAccu -= simulationTickEvery
    }

    GameLogic.gameFinished(world) match {
      case Some(Draw) =>
        println("DRAW")
        running = false
      case Some(PlayerWon(playerId)) =>
        println(s"Player $playerId won")
        running = false
      case None =>

    }

    renderer.render(world, simulationAccu, simulationTickEvery)
  }

  def resize(width : Int, height : Int) {
  }
}
