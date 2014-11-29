package hack.game

/**
 * Created by michael on 29/11/14.
 */

// all state
class World(val width:Int, val height:Int, val startingTile:Tile) {
  var tick = 0

  // TILE LOGIC
  def cells = width * height
  val tiles = Array.fill[Tile](cells) { startingTile }
  def tileAt(x:Int, y:Int) : Tile = tiles(tileLocation(x, y))
  def tileLocation(x:Int, y:Int) : Int = {
    assert(x < width && x >= 0 && y < height && y >= 0, "asked for out of bounds tile location")
    x * height + y
  }

  // LIVING LOGIC (entities that live on the tiles)


  val player = new Player(0)
}


