package hack

import com.badlogic.gdx.{Game, Screen}
import hack.game._

class HackGame extends Game {
  override def create() {
    this.setScreen(new GameScreen)
  }
}

class GameScreen extends Screen {
  // static initialization hack
  val hck = (Tile.impassableGround, Arch.imp)

  var simulationTickEvery = 0.2

  var world = generateWorld()
  val renderer = new Renderer()
  val audio = new AudioRenderer()

  val inputHandler = new InputHandler(world)
  inputHandler.setAsListener()

  def generateWorld() : World = {
    val top = 10
//    val w = new World(42, 24, Tile.standardGround, 9)
    val w = new World(18, 11, Tile.standardGround, 9)

    def placeHomeBaseBackage(v:Vec2i, playerId:Int) {
      for {
        x <- v.x - 1 to v.x + 1
        y <- v.y - 1 to v.y + 1
      } {
        val at = Vec2i(x, y)
        if(w.inBounds(at)) {
          w.placeTileAt(at, Tile.standardGround, playerId)
        }
      }
      w.placeTileAt(v, Tile.cultistSpawner, playerId)
      w.players(playerId).cursorPosition = v
    }

    w.simulationTickEvery = simulationTickEvery

    WorldGen.terraform(w, seed, spreadStepMin = 40)

    val halfHeight = w.height / 2

    placeHomeBaseBackage(Vec2i(0, halfHeight), 0)
    placeHomeBaseBackage(Vec2i(w.width - 1, halfHeight), 1)

//    w.placeTileAt(Vec2i(1, halfHeight), Tile.eyeBallSpawner, 0)
//    w.placeTileAt(Vec2i(2, halfHeight), Tile.bigBeetleSpawner, 0)
//    w.placeTileAt(Vec2i(w.width - 2, halfHeight), Tile.impSpawner, 1)

//    w.placeTileAt(Vec2i(1, halfHeight), Tile.bigBeetleSpawner, 0)
//    w.placeTileAt(Vec2i(w.width - 2, halfHeight), Tile.fexSpawner, 1)

//        w.placeTileAt(Vec2i(1, halfHeight), Tile.eyeBallSpawner, 0)
//   w.placeTileAt(Vec2i(w.width - 2, halfHeight), Tile.fexSpawner, 1)
//    w.placeTileAt(Vec2i(w.width - 3, halfHeight), Tile.wormSpawner, 1)

    val withGate = true
    if(withGate) {
      w.placeTileAt(Vec2i(4, halfHeight), Tile.gate, 0)
      w.placeTileAt(Vec2i(w.width - 4, halfHeight), Tile.gate, 1)
    }

    w
  }



  def show() = {}

  def hide() = {}

  def pause() = {}

  def resume() = {}

  def dispose() = {}

  var t = 0.0





  var running = true

  var seed = 0L



  def resetGame() {
//    simulationTickEvery *= 0.5
    t = 0.0
    seed += 1
    world = generateWorld()
    world.simulationTickEvery = simulationTickEvery
    inputHandler.world = world
  }

  val wins = Array.fill[Int](2){0}

  def render(delta : Float) {
    if(!running || inputHandler.resetWorld) {
      resetGame()
      inputHandler.resetWorld = false
      running = true
    }



    t += delta
    if(running) {
      world.simulationAccu += delta
    }

    // determine game ticks here

    if (world.simulationAccu >= world.simulationTickEvery) {
      AI.evaluate(world, inputHandler)

      world.tick += 1
      GameLogic.updateWorld(world)


      if(world.placementTimer == 0) {
        world.placementStage += 1
        world.placementTimer = world.ticksPerPlace
      } else {
        world.placementTimer -= 1
      }


      world.simulationAccu -= world.simulationTickEvery
    }

    world.simulateParticles(delta)

    val playFinishedSound = GameLogic.gameFinished(world) match {
      case Some(Draw) =>
        println("DRAW")
        running = false
        false
      case Some(PlayerWon(playerId)) =>
        println(s"Player $playerId won")
        running = false
        wins(playerId) += 1
        true
      case None =>
        false
    }

    renderer.render(world, delta, wins) // t

    // SOUND
    if(inputHandler.placeTileSound) {
      audio.placeTile()
      inputHandler.placeTileSound = false
    }
    if(inputHandler.triggerSound) {
      audio.shoulderButton()
      inputHandler.triggerSound = false
    }
    if(world.playMediumHurtSound) {
      audio.mediumHurt()
    } else if(world.playSmallHurtSound) {
      audio.smallHurt()
    }
    world.playMediumHurtSound = false
    world.playSmallHurtSound = false
    if(playFinishedSound) {
      audio.finalDestruction()
    } else if(world.playBuildingDestroyed) {
      audio.mediumDestruction()
    }
    world.playBuildingDestroyed = false
  }

  def resize(width : Int, height : Int) {
  }
}
