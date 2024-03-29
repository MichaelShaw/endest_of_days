package hack.game

import hack.{GameLogic, InputHandler}

/**
 * Created by michael on 30/11/14.
 */
object AI {
  val rand = new scala.util.Random()

  def evaluate(world:World, inputHandler:InputHandler) {
    def attemptToPlace(t:Tile, player:Player) : Option[Vec2i] = {
      t match {
        case f:Factory => placeFactory(world, f, player)
        case g:Gate => placeGate(world, player)
        case _ =>
          if(t == Tile.standardGround) {
            placeGrassLand(world, player)
          } else {
            placeMountain(world, player)
          }
      }
    }
    val aiPlayers = world.players.filter { player =>
      player.ai
    }
    aiPlayers.foreach { player =>
      if(player.canPlaceTiles(world) && world.placementTimer <= 3) {
        // factory first preference
        player.availableTiles.sortBy {
          case f:Factory => -200 + rand.nextInt(25)
          case g:Gate => -100
//          case t:Tile if t == Tile.impassableGround => -50 // attempt mountains first
          case _ => -15
        }.find { tile =>
          attemptToPlace(tile, player).isDefined
        }.foreach { tile =>
          player.tile = player.availableTiles.indexOf(tile)
          assert(player.tile >= 0)
          attemptToPlace(tile, player).map { pos =>
            player.cursorPosition = pos
            inputHandler.placeTile(player.id)
          }
        }
      }
    }
  }

  def isNotAGateOrFactory(tile:Tile) = !tile.isInstanceOf[Gate] && !tile.isInstanceOf[Factory]

  def placeFactory(world:World, f:Factory, player:Player) : Option[Vec2i] = {
    val homeBase = GameLogic.matchingTiles(world, { (x, y) =>
      val v = Vec2i(x, y)
      world.tileAt(v) == Tile.cultistSpawner && world.owned.get(v) == player.id
    })

    val canPlaceLegally = GameLogic.matchingTiles(world, { (x, y) =>
      val v = Vec2i(x, y)
      world.canPlaceTileAt(v, player.id) && isNotAGateOrFactory(world.tileAt(v))
    })

    GameLogic.sampleMaybe(canPlaceLegally.toArray)
  }

  // THIS NEEDS TO BE FLOODFILL BASED, DESCEND THE FLOODFILL (OR ITS USELESS)
  def placeGate(world:World, player:Player) : Option[Vec2i] = {
    val homeBase = GameLogic.matchingTiles(world, { (x, y) =>
      val v = Vec2i(x, y)
      world.tileAt(v) == Tile.cultistSpawner && world.owned.get(v) == player.id
    })

    val canPlaceLegally = GameLogic.matchingTiles(world, { (x, y) =>
      val v = Vec2i(x, y)
      world.canPlaceTileAt(v, player.id) && isNotAGateOrFactory(world.tileAt(v))
    })

    homeBase.headOption.flatMap { homeBase =>
      val path = world.aggressionFloodFills(player.id).descendFrom(homeBase)
//      println("full descent path to enemy factory " + path)
      val optionsOnPath = path.drop(2).take(3)
      optionsOnPath.find { v =>
        canPlaceLegally.contains(v)
      }
    }
  }



  def placeMountain(world:World, player:Player ) : Option[Vec2i] = {
    val canPlaceLegally = GameLogic.matchingTiles(world, { (x, y) =>
      val v = Vec2i(x, y)
      world.canPlaceTileAt(v, player.id) && isNotAGateOrFactory(world.tileAt(v))
    })
//    canPlaceLegally.find { v =>
//
//    }


    GameLogic.sampleMaybe(canPlaceLegally.toArray)
  }

  def placeGrassLand(world:World, player:Player): Option[Vec2i] = {
    val canPlaceLegally = GameLogic.matchingTiles(world, { (x, y) =>
      val v = Vec2i(x, y)
      world.canPlaceTileAt(v, player.id) && isNotAGateOrFactory(world.tileAt(v))
    })
    val andWeDontOwn = canPlaceLegally.filter { v =>
      world.owned.get(v) != player.id

    }
    GameLogic.sampleMaybe(andWeDontOwn.toArray).orElse(GameLogic.sampleMaybe(canPlaceLegally.toArray))
  }
}
