package hack.game

// all state

class MetaLayer(val width : Int, val height : Int, val startingValue:Int) {
  def cells = width * height

  def gridLocation(v : Vec2i) : Int = gridLocation(v.x, v.y)

  def gridLocation(x : Int, y : Int) : Int = {
    assert(x < width && x >= 0 && y < height && y >= 0, "asked for out of bounds tile location")
    x * height + y
  }

  val meta = Array.fill[Int](cells) {
    startingValue
  }

  def get(v : Vec2i) : Int = get(v.x, v.y)

  def get(x:Int, y:Int) : Int = meta(gridLocation(x, y))

  def set(v : Vec2i, m : Int) {
    set(v.x, v.y, m)
  }

  def set(x : Int, y : Int, m : Int) {
    meta(gridLocation(x, y)) = m
  }
}

class World(val width : Int, val height : Int, val startingTile : Tile, val slotsPerTile : Int) {
  var tick = 0

  val timer = new MetaLayer(width, height, 0)
  val health = new MetaLayer(width, height, 0)

  val owned = new MetaLayer(width, height, -1) // since 0 is a player id

  def canPlaceTileAt(v : Vec2i, tile : Tile) : Boolean = canPlaceTileAt(v.x, v.y, tile)

  def canPlaceTileAt(x : Int, y : Int, tile : Tile) : Boolean = {
    inBounds(x, y)
    // TODO: disallow placing tile on factory
    // TODO: disallow completely seal off factories from each other?
  }

  def placeTileAt(v : Vec2i, tile : Tile) : Unit = placeTileAt(v.x, v.y, tile)

  def placeTileAt(x : Int, y : Int, tile : Tile, ownedBy:Int = -1) : Unit = {
    setTileAt(x, y, tile)

    val startingHealth : Int = tile match {
      case f : Factory => f.startingHealth
      case _ => 0
    }
    health.set(x, y, startingHealth)
    val startingTimer :Int = tile match {
      case f:Factory => f.produceEveryNTicks
      case _ => 0
    }
    timer.set(x, y, startingTimer)
    owned.set(x, y, ownedBy)
  }

  def inBounds(v : Vec2i) : Boolean = inBounds(v.x, v.y)

  def inBounds(x : Int, y : Int) : Boolean = {
    x >= 0 && x < width && y >= 0 && y < height
  }

  // TILE LOGIC
  def cells = width * height

  val tiles = Array.fill[Tile](cells) {
    startingTile
  }

  def tileAt(v : Vec2i) : Tile = tileAt(v.x, v.y)

  def tileAt(x : Int, y : Int) : Tile = tiles(gridLocation(x, y))

  def setTileAt(x : Int, y : Int, tile : Tile) {
    tiles(gridLocation(x, y)) = tile
  }

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
    assert(hasSpaceAt(x, y))
    val arr = livings(gridLocation(x, y))
    var sl = 0;
    while (sl < slotsPerTile) {
      if (arr(sl) == null) {
        arr(sl) = living
        return sl
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
  val playerA = new Player(0, Vec2i(8, 1))
  val playerB = new Player(1, Vec2i(8, 14))
  val players = Array(playerA, playerB)

  // floodfills
  val aggressionFloodFills = new Array[FloodFill](2)
}
