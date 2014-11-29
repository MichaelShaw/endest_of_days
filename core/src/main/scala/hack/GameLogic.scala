package hack

import hack.game._
import collection.mutable

/**
 * Created by michael on 29/11/14.
 */

sealed trait GameOutcome
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
      // aggression
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

      val factoriesThatRequireSummoners = matchingTiles(world, {(x, y) =>
        val v = Vec2i(x, y)
        val tile = world.tileAt(v)
        val ownedBy = world.owned.get(v)
        if(ownedBy == player.id) {
          tile match {
            case f:Factory =>
              val summonerCount = world.validLivingsAt(v).count { l =>
                l.playerId == ownedBy && l.arch.canHelpSummon
              }
              summonerCount < f.requiredSummoners
            case _ => false
          }
        } else {
          false
        }
      })

      val summonerFloodFill = FloodFill.produceFor(factoriesThatRequireSummoners, world)

      if(player.id == 0) {
//        summonerFloodFill.printDebug("player 0 summoner")
      }


      world.summonerFloodFills(player.id) = summonerFloodFill
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
      if(world.tileAt(Vec2i(x, y)).isInstanceOf[Factory]) {
        factoryOwnerShip += world.owned.get(Vec2i(x, y))
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
//    println(s"game update tick ${world.tick}")

    // calculate floodfillds
    calculateFloodFills(world)

    // factory production (anything tile centric)
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val v = Vec2i(x, y)
      val tile = world.tileAt(v)
      val ownedBy = world.owned.get(v)
      tile match {
        case g:Gate =>
          val time = world.timer.get(v)
          if(time > 0) {
            world.timer.set(v, time - 1) // countdown gate timer
          }
        case f:Factory =>
          val availableSummoners = world.validLivingsAt(v).count { l =>
            l.playerId == ownedBy && l.arch.canHelpSummon
          }
          if(availableSummoners >= f.requiredSummoners) {
            val timer = world.timer.get(v)
            if(timer == 1) {
              // spawn time
              spawnAt(world, v, world.owned.get(v), f.produceArch)
              world.timer.set(v, f.produceEveryNTicks)
            } else {
              world.timer.set(v, timer - 1)
            }
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
    def stampActionDuration(n: Int) {
      living.actionStartedAtTick = world.tick
      living.actionFinishedAtTick = world.tick + n
    }
    living.saveLastLocation()
    def moveTo(v: Vec2i) {
      world.unregisterLivingAt(living.currentLocation, living)

      living.currentLocation = v
      living.currentSlot = world.registerLivingAt(v, living)

      stampActionDuration(living.arch.ticksToMove)
    }
    def idle() {
      stampActionDuration(1)
    }
    def strike(at: Vec2i) {
      def hitEnemy(damage:Int, enemies:Seq[Living]) {
        for (enemy <- enemies) {
          //        println(s"${living.arch} striking ${enemy.arch} for ${living.arch.attack}")
          enemy.health -= damage
          enemy.lastStruckAt = world.tick

          if(enemy.health <= 0 && living.arch.maxHealth >= 10) {
            world.playMediumHurtSound = true
          } else if(enemy.arch != Arch.cultist) {
            world.playSmallHurtSound = true
          }

          for(arch <- enemy.arch.onHitSpawns) {
            //          println("beetle was hit")
            if(world.hasSpaceAt(at)) {
              // spawn one here
              //            println("spawning one here")
              spawnAt(world, at, enemy.playerId, arch)
            } else {
              val validDirections = Direction.directions.filter { d =>
                val neighbour = at + d
                // is in bounds, has space to spawn something, and can be pathed on
                world.inBounds(neighbour) && world.hasSpaceAt(neighbour) && world.tileAt(neighbour).canBeWalkedOn
              }
              sampleMaybe(validDirections).foreach { d =>
                //              println("spawning one adjacent")
                val neighbour = at + d
                spawnAt(world, neighbour, enemy.playerId, arch)
              }
            }
          }
        }
      }
      def validEnemies = world.validLivingsAt(at).filter { e =>
        e.playerId != living.playerId && e.health > 0 // only strike enemies with health
      }

      val primaryTarget = Seq(sample(validEnemies))

      hitEnemy(living.arch.attack, primaryTarget)


      if (living.arch.aeDamage > 0) {
        val otherEnemies = validEnemies.toSet -- primaryTarget.toSet
        hitEnemy(living.arch.aeDamage, otherEnemies.toSeq)
      }

      if (living.arch.explodeOnAttack) {
        living.health = 0
      }

      stampActionDuration(1)
    }

    def strikeBuilding(at: Vec2i): Unit = {
      val healthRemaining = world.health.get(at) - living.arch.attack

      if (healthRemaining <= 0 || living.arch.instakillBuildings) {
        world.placeTileAt(at, Tile.standardGround, -1)
      } else {
        world.health.set(at, healthRemaining)
      }

      stampActionDuration(1)
    }


    val ff = world.aggressionFloodFills(living.playerId)
    val sff = world.summonerFloodFills(living.playerId)
    val currentHeight = ff.get(living.currentLocation)
    val currentSummonerHeight = sff.get(living.currentLocation)

    // check for person threats

    val directionsWithEnemies = Direction.directions.filter { dir =>
      val neighbour = living.currentLocation + dir
      world.inBounds(neighbour) && world.validLivingsAt(neighbour).exists { l =>
        l.playerId != living.playerId && l.health > 0
      }
    }
    val directionsWithEnemyBuildings = Direction.directions.filter { dir =>
      val neighbour = living.currentLocation + dir
      if (ff.inBounds(neighbour)) {
        world.tileAt(neighbour) match {
          case f: Factory => world.owned.get(neighbour) != living.playerId && world.health.get(neighbour) > 0 // meta is health channel for factory
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

    def descrendingSummonDirections = Direction.directions.filter { dir =>
      val neighbour = living.currentLocation + dir
        sff.inBounds(neighbour) &&
        sff.get(neighbour) < currentSummonerHeight &&
          world.tileAt(neighbour).canBeWalkedOn &&
          world.hasSpaceAt(neighbour)
    }

    def isOnSummoningTile = world.tileAt(living.currentLocation) match {
      case f:Factory => f.requiredSummoners > 0 && world.owned.get(living.currentLocation) == living.playerId
      case _ => false
    }


//    println("arch " + living.arch.name)
//    println(s"can i help summon ${living.arch.canHelpSummon} am i not on a summoning tile ${!isOnSummoningTile} is my loc reachable ${world.summonerFloodFills(living.playerId).reachable(living.currentLocation)}")

    if (directionsWithEnemies.nonEmpty) {
      val neighbour = living.currentLocation + sample(directionsWithEnemies)
      strike(neighbour)
    } else if (directionsWithEnemyBuildings.nonEmpty) {
      val neighbour = living.currentLocation + sample(directionsWithEnemyBuildings)
      strikeBuilding(neighbour)
    } else if(world.hasSpaceAt(living.currentLocation) && living.arch.canHelpSummon && (isOnSummoningTile || world.summonerFloodFills(living.playerId).reachable(living.currentLocation))) {
//      println("summoner")
      if(isOnSummoningTile) {
//        println("i'm on a summoning tile")
        idle()
      } else {
        // i can summon
//        println("perform your summoning duty")
        sampleMaybe(descrendingSummonDirections) match {
          case Some(dir) =>
//            println("i've found the direction to descend")
            val neighbour = living.currentLocation + dir
            moveTo(neighbour)
          case None =>
//            println("can't descend")
            idle()
        }
      }
    } else if(descendingDirections.nonEmpty) {
//      println("descending normally")
      // if we're at an owned gate
      if(!living.arch.canHelpSummon && world.tileAt(living.currentLocation) == Tile.gate && world.owned.get(living.currentLocation) == living.playerId) {
        // if the tile has space and timer is down, don't progress
        if(world.hasSpaceAt(living.currentLocation) && world.timer.get(living.currentLocation) == 0) { // we're full and timer is down
          idle()
        } else {
          if(!world.hasSpaceAt(living.currentLocation)) { // if it was full ... and we moved, chalk up the timer so others can move
            world.timer.set(living.currentLocation, 2) // set timer to 2 ticks
          }

          val neighbour = living.currentLocation + sample(descendingDirections)
          moveTo(neighbour)
        }
      } else {
        val neighbour = living.currentLocation + sample(descendingDirections)
        moveTo(neighbour)
      }
    } else {
//      println("idle catchall")
      idle()
    }
  }

  def spawnAt(world:World, v:Vec2i, playerId:Int, arch:Arch){
    if(world.inBounds(v) && world.hasSpaceAt(v) && world.tileAt(v).canBeWalkedOn) {
      val living = new Living(world.generateLivingId(), playerId, arch, v)
      living.actionStartedAtTick = world.tick
      living.actionFinishedAtTick = world.tick + 1 // basically (idle for 1 turn)
      living.currentSlot = world.registerLivingAt(v, living)
      living.lastSlot = living.currentSlot
    }
  }
}
