package hack

import hack.game._
import collection.mutable

/**
 * Created by michael on 29/11/14.
 */

trait GameOutcome
case class PlayerWon(playerId:Int) extends GameOutcome
case object Draw extends GameOutcome

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

  def matchingTiles(world:World, pred:(Int, Int) => Boolean) : Set[Vec2i] = {
    (for {
      x <- 0 until world.width
      y <- 0 until world.height if pred(x, y)
    } yield Vec2i(x, y)).toSet
  }

  def calculateFloodFills(world:World) {
    for(player <- world.players) {
      val goals = matchingTiles(world, { (x, y) =>
        val v = Vec2i(x, y)
        val tile = world.tileAt(v)
        tile match {
          case f:Factory => world.owned.get(v) != player.id // opponent factory
          case _ => false
        }
      })
      val floodFill = FloodFill.produceFor(goals, world)
      world.aggressionFloodFills(player.id) = floodFill
    }
  }

  // winning player id
  def gameFinished(world:World) : Option[GameOutcome] = {
    // draw is when there are no factories
    val factoryOwnerShip = new mutable.HashSet[Int]
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      if(world.tileAt(x, y).isInstanceOf[Factory]) {
        factoryOwnerShip += world.owned.get(x, y)
      }
    }

    if(factoryOwnerShip.isEmpty) {
      Some(Draw)
    } else if(factoryOwnerShip.size == 1) {
      Some(PlayerWon(factoryOwnerShip.head))
    } else {
      None
    }
  }

  /*
  trait GameOutcome
case class PlayerWon(playerId:Int) extends GameOutcome
case object Draw extends GameOutcome
   */

  def updateWorld(world:World) { // update worlds by a tick ... maybe accumulates events
    println(s"game update tick ${world.tick}")

    // calculate floodfillds
    calculateFloodFills(world)

    // factory production (anything tile centric)
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val v = Vec2i(x, y)
      val tile = world.tileAt(v)
      tile match {
        case f:Factory =>
          val v = Vec2i(x, y)
          val timer = world.timer.get(v)
          if(timer == 1) {
            // spawn time
            spawnNear(world, x, y, world.owned.get(v), f.produceArch)
            world.timer.set(v, f.produceEveryNTicks)
          } else {
            world.timer.set(v, timer - 1)
          }
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
      def validEnemies = world.validLivingsAt(at).filter{ e =>
        e.playerId != living.playerId && e.health > 0 // only strike enemies with health
      }
      val toAttack:Seq[Living] = if(living.arch.aeAttack) {
        validEnemies
      } else {
        Seq(sample(validEnemies))
      }

      for(enemy <- toAttack) {
//        println(s"${living.arch} striking ${enemy.arch} for ${living.arch.attack}")
        enemy.health -= living.arch.attack
        enemy.lastStruckAt = world.tick
      }

      if(living.arch.explodeOnAttack) {
        living.health = 0
      }

      stampActionDuration(1)
    }

    def strikeBuilding(at:Vec2i): Unit = {
      val healthRemaining = world.health.get(at) - living.arch.attack

      if(healthRemaining <= 0) {
        world.placeTileAt(at, Tile.standardGround, -1)
      } else {
        world.health.set(at, healthRemaining)
      }

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
    val directionsWithEnemyBuildings = Direction.directions.filter { dir =>
      val neighbour = living.currentLocation + dir
      if(ff.inBounds(neighbour)) {
        world.tileAt(neighbour) match {
          case f:Factory => world.owned.get(neighbour) != living.playerId && world.health.get(neighbour) > 0 // meta is health channel for factory
          case _ => false
        }
      } else {
        false
      }
    }
    val descendingDirections = Direction.directions.filter { dir =>
      val neighbour = living.currentLocation + dir
      ff.inBounds(neighbour) &&
        ff.get(neighbour) < currentHeight &&
        world.tileAt(neighbour).canBeWalkedOn &&
        world.hasSpaceAt(neighbour)
    }

    if(directionsWithEnemies.nonEmpty) {
      val neighbour = living.currentLocation + sample(directionsWithEnemies)
      strike(neighbour)
    } else if(directionsWithEnemyBuildings.nonEmpty) {
      val neighbour = living.currentLocation + sample(directionsWithEnemyBuildings)
      strikeBuilding(neighbour)
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
