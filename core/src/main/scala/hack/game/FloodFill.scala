package hack.game

import hack.GameLogic

import collection.mutable
import scala.util.Random

/**
 * Created by michael on 29/11/14.
 */
class FloodFill(val width:Int, val height:Int) {
  val rand = new Random()
  def cells = width * height
  val distance = Array.fill[Int](cells) { FloodFill.unreachable }
  def descendFrom(v:Vec2i) : Seq[Vec2i] = {
    val steps = new mutable.ArrayBuffer[Vec2i]()
    steps += v


    var at = v

    while(get(at) > 0) {
      val validDirections = Direction.directions.filter { dir =>
        val neighbour = at + dir
        inBounds(neighbour) && get(neighbour) < get(at)
      }
      GameLogic.sampleMaybe(validDirections) match {
        case Some(dir) =>
          val neighbour = at + dir
          steps += neighbour
          at = neighbour
        case None =>
          return steps
      }
    }

    steps
  }

  def gridLocation(v:Vec2i) : Int = gridLocation(v.x, v.y)
  def gridLocation(x:Int, y:Int) : Int = {
    assert(x < width && x >= 0 && y < height && y >= 0, "asked for out of bounds tile location")
    x * height + y
  }
  def set(vec:Vec2i, v:Int) { set(vec.x, vec.y, v) }
  def set(x:Int, y:Int, v:Int) {
    distance(gridLocation(x, y)) = v
  }
  def setIfLower(vec:Vec2i, v:Int) : Boolean = setIfLower(vec.x, vec.y, v)
  def setIfLower(x:Int, y:Int, v:Int) : Boolean = {
    if(get(x, y) > v) {
      set(x, y, v)
      true
    } else {
      false
    }
  }
  def get(vec:Vec2i) : Int = get(vec.x, vec.y)
  def get(x:Int, y:Int) : Int = distance(gridLocation(x, y))
  def inBounds(v:Vec2i) : Boolean = inBounds(v.x, v.y)
  def inBounds(x:Int, y:Int) : Boolean = x < width && x >= 0 && y < height && y >= 0

  def reachable(v:Vec2i) : Boolean = reachable(v.x, v.y)
  def reachable(x:Int, y:Int) :Boolean = inBounds(x, y) && get(x, y) < FloodFill.unreachable

  def printDebug(name:String) {
    println(s" ===== floodfill $name -- $width x $height ===== ")
    for {
      y <- height- 1 to 0 by -1
    } {
      val line = (0 until width).map { x =>
        val d = get(x, y)
        if(d == FloodFill.unreachable) {
          ".."
        } else {
          d.toString().padTo(2, ' ')
        }
      }.mkString(" ")
      println(line)
    }
  }

}

object FloodFill {
  val unreachable = Int.MaxValue - 1000 // (to be safe from overflow)
  type CostFunction = Vec2i => Int

  def produceFor(goals:Set[Vec2i], world:World, playerId:Int) : FloodFill = {
    val ff = new FloodFill(world.width, world.height)

    val spreadFrom = new mutable.Queue[Vec2i]()
    for {
      loc <- goals
    } {
      ff.set(loc, 0)
      spreadFrom.enqueue(loc)
    }

    // ok spread it in a loop

    while(spreadFrom.nonEmpty) {
      val loc = spreadFrom.dequeue()
      val d = ff.get(loc)

      val fullOfFriendlies = world.validLivingsAt(loc).count(_.playerId == playerId) == 9
      val cost = if(fullOfFriendlies) 4 else 1
      val nd = d + cost

      for(d <- Direction.directions) {
        val neighbour = d + loc
        if(ff.inBounds(neighbour) && world.tileAt(neighbour).canBeWalkedOn && ff.setIfLower(neighbour, nd)) {
          spreadFrom += neighbour
        }
      }
    }

    ff
  }
}
