package hack

import hack.game._

/**
 * Created by michael on 29/11/14.
 */
object GameLogic {
  val rand = new scala.util.Random()

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

  def matchingTiles(world:World, pred:Tile => Boolean) : Set[Vec2i] = {
    (for {
      x <- 0 until world.width
      y <- 0 until world.height if pred(world.tileAt(x, y))
    } yield Vec2i(x, y)).toSet
  }

  def calculateFloodFills(world:World) {
    val playerAGoals = matchingTiles(world, tile => tile == Tile.playerBFactory)
    val playerAFloodFill = FloodFill.produceFor(playerAGoals, world)
    world.aggressionFloodFills(0) = playerAFloodFill


//    playerAFloodFill.printDebug("player A aggression")


    val playerBGoals = matchingTiles(world, tile => tile == Tile.playerAFactory)
    val playerBFloodfill = FloodFill.produceFor(playerBGoals, world)
    world.aggressionFloodFills(1) = playerBFloodfill

//    playerBFloodfill.printDebug("player B aggression")

  }

  def updateWorld(world:World) { // update worlds by a tick ... maybe accumulates events
    println(s"game update tick ${world.tick}")

    // calculate floodfillds
    calculateFloodFills(world)

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
        val living = entities(slot)
        if(living != null && living.actionFinishedAtTick <= world.tick) {
          assert(living.actionFinishedAtTick == world.tick) // just trying to sniff errors here
          // idle for 1 tick
          updateLiving(world, living)
        }
        slot += 1
      }
    }

    // clean away dead entities
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val entities = world.livingsAt(x, y)
      var slot = 0; while(slot < world.slotsPerTile) {
        val living = entities(slot)
        if(living != null && living.health <= 0) {
          world.unregisterLivingAt(living.currentLocation, living)
        }
        slot += 1
      }
    }
  }

  def updateLiving(world:World, living:Living) {
    def stampActionDuration(n:Int) {
      living.actionStartedAtTick = world.tick
      living.actionFinishedAtTick = world.tick + n
    }
    living.saveLastLocation()
    def moveTo(v:Vec2i) {
      world.unregisterLivingAt(living.currentLocation, living)

      living.currentLocation = v
      living.currentSlot = world.registerLivingAt(v, living)

      stampActionDuration(1)
    }
    def idle() {
      stampActionDuration(1)
    }
    def strike(at:Vec2i) {
      val enemies = world.validLivingsAt(at).filter{ e =>
        e.playerId != living.playerId && e.health > 0 // only strike enemies with health
      }
      assert(enemies.nonEmpty)
      val enemy = sample(enemies)
      enemy.health -= living.arch.attack
      enemy.lastStruckAt = world.tick

      stampActionDuration(1)
    }


    val ff = world.aggressionFloodFills(living.playerId)
    val currentHeight = ff.get(living.currentLocation)

    // check for person threats

    val directionsWithEnemies = Direction.directions.filter { dir =>
      val neighbour = living.currentLocation + dir
      world.inBounds(neighbour) && world.validLivingsAt(neighbour).exists { l =>
        l.playerId != living.playerId && l.health > 0
      }
    }
    val descendingDirections = Direction.directions.filter{ dir =>
      val neighbour = living.currentLocation + dir
      ff.inBounds(neighbour) &&
        ff.get(neighbour) < currentHeight &&
        world.tileAt(neighbour).canBeWalkedOn &&
        world.hasSpaceAt(neighbour)
    }

    if(directionsWithEnemies.nonEmpty) {
      val neighbour = living.currentLocation + sample(directionsWithEnemies)
      strike(neighbour)
    } else if(descendingDirections.nonEmpty) {
      val neighbour = living.currentLocation + sample(descendingDirections)
      moveTo(neighbour)
    } else {
      idle()
    }
  }

  def spawnNear(world:World, x:Int, y:Int, playerId:Int, arch:Arch){
    val availablePlacementSpots = Direction.directions.filter { dir =>
      val offset = dir.plus(x, y)
      world.inBounds(offset) && world.hasSpaceAt(offset)
    }

    sampleMaybe(availablePlacementSpots) match {
      case Some(dir) =>
        val spawnAt = dir.plus(x, y)
        val living = new Living(world.generateLivingId(), playerId, arch, spawnAt)
        living.actionStartedAtTick = world.tick
        living.actionFinishedAtTick = world.tick + 1 // basically (idle for 1 turn)
        living.currentSlot = world.registerLivingAt(spawnAt, living)
        living.lastSlot = living.currentSlot
      case None =>
    }
  }
}
