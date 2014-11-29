package hack.game

import hack.GameLogic

import scala.util.Random
import collection.mutable

/**
 * Created by michael on 30/11/14.
 */
object WorldGen {
  def terraform(world:World, seed:Long, initialPointsMin:Int = 3, initialPointsRange:Int = 4, spreadStepMin:Int = 6, spreadStepRange:Int = 16) {
    val rand = new Random(seed)

    val xMin = 2; val xMax = world.width - 2
    val yMin = 0; val yMax = world.height

    def randomPointInBounds() : Vec2i = {
      Vec2i(
        xMin + rand.nextInt(xMax - xMin),
        yMin + rand.nextInt(yMax - yMin)
      )
    }

    def inBounds(v:Vec2i) : Boolean = {
      v.x >= xMin && v.x < xMax && v.y >= yMin && v.y < yMax
    }

    val toGen = initialPointsMin + rand.nextInt(initialPointsRange)

    val spreadable = new mutable.HashSet[Vec2i]()

    for(i <- 1 to toGen) {
      val loc = randomPointInBounds()
      if(world.tileAt(loc) == Tile.standardGround) {
        world.placeTileAt(loc, Tile.impassableGround, -1)
        spreadable += loc

        val randomDirections = Direction.directions.filter { d =>
          val neighbour = loc + d
          inBounds(neighbour) && world.tileAt(neighbour) == Tile.standardGround
        }

        GameLogic.sampleMaybe(randomDirections).foreach { d =>
          val neighbour = loc + d
          world.placeTileAt(neighbour, Tile.impassableGround, -1)
          spreadable += neighbour
        }
      }
    }

    val spreadAttempts = spreadStepMin + rand.nextInt(spreadStepRange)
    for(i <- 1 to spreadAttempts) {
      GameLogic.sampleMaybe(spreadable.toArray).foreach { loc =>
        val randomDirections = Direction.directions.filter { d =>
          val neighbour = loc + d
          inBounds(neighbour) && world.tileAt(neighbour) == Tile.standardGround
        }
        GameLogic.sampleMaybe(randomDirections).foreach { d =>
          val neighbour = loc + d
          world.placeTileAt(neighbour, Tile.impassableGround, -1)
          spreadable += neighbour
        }
      }
    }




  }
}
