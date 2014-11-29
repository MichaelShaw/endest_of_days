package hack

import hack.game.{Living, Vec2i, Tile, World}

/**
 * Created by michael on 29/11/14.
 */
object GameLogic {
  val rand = new scala.util.Random()
  val directions = Array(Vec2i(1, 0), Vec2i(0, 1), Vec2i(-1, 0), Vec2i(1, 0))

  def sample[T](arr:Array[T]) : T = {
    arr(rand.nextInt(arr.length))
  }
  def sampleMaybe[T](arr:Array[T]) : Option[T] = {
    if(arr.length == 0) {
      None
    } else {
      Some(sample(arr))
    }
  }

  def updateWorld(world:World) { // update worlds by a tick ... maybe accumulates events
    println(s"game update tick ${world.tick}")

    def spawnNear(x:Int, y:Int, playerId:Int){
      val availablePlacementSpot = directions.find { dir =>
        val offset = dir.plus(x, y)
        world.inBounds(offset) && world.hasSpaceAt(offset)
      }

      availablePlacementSpot.map { dir =>
        println(s"placing a creature for player $playerId")
        val spawnAt = dir.plus(x, y)
        val living = new Living(world.generateLivingId(), world.player.id, spawnAt)
        world.registerLivingAt(spawnAt, living)
      }
      if(availablePlacementSpot.isEmpty) {
        println("couldnt find spawn spot")
      }
    }

    // factory production
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val tile = world.tileAt(x, y)
      tile match {
        case Tile.playerAFactory => spawnNear(x, y, world.player.id)
        case Tile.playerBFactory => spawnNear(x, y, world.playerB.id)
        case _ =>
      }
    }
  }


}
