package hack.game

import scala.util.Random

// all state

class MetaLayer(val width : Int, val height : Int, val startingValue : Int) {
  def cells = width * height

  def gridLocation(v : Vec2i) : Int = {
    assert(v.x < width && v.x >= 0 && v.y < height && v.y >= 0, "asked for out of bounds tile location")
    v.x * height + v.y
  }

  val meta = Array.fill[Int](cells) {
    startingValue
  }

  def get(v : Vec2i) : Int = {
    meta(gridLocation(v))
  }

  def set(v : Vec2i, m : Int) {
    meta(gridLocation(v)) = m
  }
}

class World(val width : Int, val height : Int, val startingTile : Tile, val slotsPerTile : Int) {
  val rand = new Random()

  var tick = 0


  def ticksPerPlace = 10
  var placementTimer = ticksPerPlace
  var placementStage = 0


  val timer = new MetaLayer(width, height, 0)
  val health = new MetaLayer(width, height, 0)

  val owned = new MetaLayer(width, height, -1) // since 0 is a player id

  def canPlaceTileAt(v : Vec2i, tile : Tile, player : Int) : Boolean = {

    def notOwnedByOtherPlayer : Boolean = {
      inBounds(v) && (owned.get(v) == -1 || owned.get(v) == player)
    }

    def ownedByPlayer(v : Vec2i) : Boolean = {
      inBounds(v) && owned.get(v) == player
    }

    val anyNeighborOwnedByPlayer = Direction.directions.exists { d =>
      ownedByPlayer(d + v)
    }

    notOwnedByOtherPlayer && anyNeighborOwnedByPlayer

    // TODO: disallow placing tile on factory
    // TODO: disallow completely seal off factories from each other?
  }

  def placeTileAt(v : Vec2i, tile : Tile, ownedBy : Int) : Unit = {
    setTileAt(v, tile)

    val startingHealth : Int = tile match {
      case f : Factory => f.startingHealth
      case _ => 0
    }
    health.set(v, startingHealth)
    val startingTimer : Int = tile match {
      case f : Factory => f.produceEveryNTicks
      case _ => 0
    }
    timer.set(v, startingTimer)
    owned.set(v, ownedBy)
  }

  def inBounds(v : Vec2i) : Boolean = {
    v.x >= 0 && v.x < width && v.y >= 0 && v.y < height
  }

  // TILE LOGIC
  def cells = width * height

  val tiles = Array.fill[Tile](cells) {
    startingTile
  }

  def tileAt(v : Vec2i) : Tile = tiles(gridLocation(v.x, v.y))

  def setTileAt(v : Vec2i, tile : Tile) = tiles(gridLocation(v.x, v.y)) = tile

  def gridLocation(v : Vec2i) : Int = gridLocation(v.x, v.y)

  def gridLocation(x : Int, y : Int) : Int = {
    assert(x < width && x >= 0 && y < height && y >= 0, "asked for out of bounds tile location")
    x * height + y
  }

  // LIVING LOGIC (entities that live on the tiles)
  private var _nextLivingId = 0

  def generateLivingId() : Int = {
    val id = _nextLivingId
    _nextLivingId += 1
    id
  }

  val livings = Array.fill[Array[Living]](cells) {
    new Array[Living](slotsPerTile)
  }

  def validLivingsAt(v : Vec2i) = livingsAt(v).filter(_ != null)

  def livingsAt(v : Vec2i) : Array[Living] = livingsAt(v.x, v.y)

  def livingsAt(x : Int, y : Int) : Array[Living] = livings(gridLocation(x, y))

  def hasSpaceAt(v : Vec2i) : Boolean = hasSpaceAt(v.x, v.y)

  def hasSpaceAt(x : Int, y : Int) : Boolean = spaceAt(x, y) > 0

  def spaceAt(x : Int, y : Int) = livings(gridLocation(x, y)).count(_ == null)

  def registerLivingAt(v : Vec2i, living : Living) : Int = registerLivingAt(v.x, v.y, living)

  def registerLivingAt(x : Int, y : Int, living : Living) : Int = {
    // will take first available slot, returning that value
    val spacesFree = spaceAt(x, y)
    var n = rand.nextInt(spacesFree) // n is a counter that counts down the nth free space

    assert(hasSpaceAt(x, y))
    val arr = livings(gridLocation(x, y))
    var sl = 0;
    while (sl < slotsPerTile) {
      if (arr(sl) == null) {
        if(n == 0) { //
          arr(sl) = living
          return sl
        } else {
          n -= 1
        }
      }
      sl += 1
    }
    throw new Exception("impossible")
  }

  def unregisterLivingAt(v : Vec2i, living : Living) {
    unregisterLivingAt(v.x, v.y, living)
  }

  def unregisterLivingAt(x : Int, y : Int, living : Living) {
    val arr = livings(gridLocation(x, y))
    var sl = 0;
    while (sl < slotsPerTile) {
      if (arr(sl) == living) {
        arr(sl) = null
        return
      }
      sl += 1
    }
    throw new Exception("couldnt find living to unregister")
  }

  // TODO: derive from initial tiles
  val playerA = new Player(0, Vec2i(8, 1), 0, Tile.groundTiles, Tile.playerAFactories)
  val playerB = new Player(1, Vec2i(8, 14), 0, Tile.groundTiles, Tile.playerBFactories)
  val players = Array(playerA, playerB)

  // floodfills
  val aggressionFloodFills = new Array[FloodFill](2)
  val summonerFloodFills = new Array[FloodFill](2)
}
