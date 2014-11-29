package hack.game

/**
 * Created by michael on 29/11/14.
 */


class Tile(val id:Int,
           val name:String,
           val canBeWalkedOn:Boolean = true) {

}

object Tile {
  // fuck, do we even need ids if we're never going to serialize *anything*
  val tiles = new Array[Tile](256)
  private var _nextId = 0
  def count = _nextId
  def nextId : Int = {
    val id = _nextId
    _nextId += 1
    id
  }
  def register[T <: Tile](t:T) : T = {
    tiles(t.id) = t
    t
  }

  val standardGround = register(new Tile(nextId, "ground"))
  val impassableGround = register(new Tile(nextId, "impassable", canBeWalkedOn = false))
}