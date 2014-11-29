package hack

import hack.game._

/**
 * Created by michael on 29/11/14.
 */
object GameLogic {
  val rand = new scala.util.Random()
  val directions = Array(Vec2i(1, 0), Vec2i(0, 1), Vec2i(-1, 0), Vec2i(0, -1))

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


    // factory production (anything tile centric)
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val tile = world.tileAt(x, y)
      tile match {
        case Tile.playerAFactory => spawnNear(world, x, y, world.playerA.id, Arch.soldier)
        case Tile.playerBFactory => spawnNear(world, x, y, world.playerB.id, Arch.soldier)
        case _ =>
      }
    }

    // entities
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val entities = world.livingsAt(x, y)
      var slot = 0; while(slot < world.slotsPerTile) {
        var living = entities(slot)
        if(living != null && living.actionFinishedAtTick <= world.tick) {
          assert(living.actionFinishedAtTick == world.tick) // just trying to sniff errors here
          // idle for 1 tick
          living.actionStartedAtTick = world.tick
          living.actionFinishedAtTick = world.tick + 1

        }
        slot += 1
      }
    }
  }

  def spawnNear(world:World, x:Int, y:Int, playerId:Int, arch:Arch){
    val availablePlacementSpot = directions.find { dir =>
      val offset = dir.plus(x, y)
      world.inBounds(offset) && world.hasSpaceAt(offset)
    }

    availablePlacementSpot.map { dir =>
      //        println(s"placing a creature for player $playerId")
      val spawnAt = dir.plus(x, y)
      val living = new Living(world.generateLivingId(), playerId, arch, spawnAt)
      living.actionStartedAtTick = world.tick
      living.actionFinishedAtTick = world.tick + 1 // basically (idle for 1 turn)
      world.registerLivingAt(spawnAt, living)
    }
    if(availablePlacementSpot.isEmpty) {
      //        println("couldnt find spawn spot")
    }
  }

}
