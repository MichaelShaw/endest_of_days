package hack.game

import hack.Renderer

import scala.util.Random
import collection.mutable

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

  var simulationAccu = 0.0
  var simulationTickEvery = 0.5  // every 1 second

  var playSmallHurtSound = false
  var playMediumHurtSound = false

  var playBuildingDestroyed = false

  def ticksPerPlace = 5
  var placementTimer = ticksPerPlace
  var placementStage = 1 // so you can place 2 tiles immediately

  val particles = new mutable.HashSet[Particle]()

  val innerTileLocations = (for {
    x <- 0 to 2
    y <- 0 to 2
  } yield Vec2i(4 + x * 8, 4 + y * 8)).toArray // need better logic here

  def screenLocation(loc : Vec2i, slot : Int) : Vec2i = {
    (loc * Renderer.pixelsPerTile) + innerTileLocations(slot)
  }

  def exactLocationOf(e:Living) : Vec2f = {
    val lastLocation = screenLocation(e.lastLocation, e.lastSlot)
    val currentLocation = screenLocation(e.currentLocation, e.currentSlot)

    val actionDuration = e.actionFinishedAtTick - e.actionStartedAtTick
    val progressAbs = tick - e.actionStartedAtTick + simulationAccu / simulationTickEvery
    val progressAlpha = progressAbs / actionDuration

    Vec2f.lerp(lastLocation, currentLocation, progressAlpha) // simulationAccu / simulationTickSize
  }

  def spawnAtTile(v:Vec2i, partId:Int, count:Int = 8) {
    for(n <- 1 to count) {
      val at = Vec2f(v.x + rand.nextFloat(), v.y + rand.nextFloat()) * Renderer.pixelsPerTile
      val velocity = Vec2f(rand.nextFloat() * 2.0f - 1.0f, rand.nextFloat() * 2.0f - 1.0f) * Renderer.pixelsPerTile

      particles += new Particle(at, velocity, 0.5f + rand.nextFloat(), partId)
    }
  }
  
  def spawnNear(v:Vec2f, partId:Int, count:Int = 8): Unit = {
    for(n <- 1 to count) {
      val at = v + Vec2f(8, 8)
      val velocity = Vec2f(rand.nextFloat() - 0.5f, -rand.nextFloat()) * Renderer.pixelsPerTile
      particles += new Particle(at, velocity, 0.5f + rand.nextFloat(), partId)
    }
  }

  def simulateParticles(delta:Float) {
    particles.retain { part =>
      part.aliveFor -= delta

      part.at += part.velocity * delta // move by velocity
      part.velocity *= (1 - delta * 3f) // dampen velocity

      part.aliveFor > 0
    }
  }

  val timer = new MetaLayer(width, height, 0)
  val health = new MetaLayer(width, height, 0)

  val owned = new MetaLayer(width, height, -1) // since 0 is a player id

  def canPlaceTileAt(v : Vec2i, player : Int) : Boolean = {

    def notOwnedByOtherPlayer : Boolean = {
      inBounds(v) && (owned.get(v) == -1 || owned.get(v) == player)
    }

    def ownedByPlayer(v : Vec2i) : Boolean = {
      inBounds(v) && owned.get(v) == player
    }

    val anyNeighborOwnedByPlayer = Direction.directions.exists { d =>
      ownedByPlayer(d + v)
    }

    def notCultistSpawner : Boolean = {
      tileAt(v) != Tile.cultistSpawner
    }

    notOwnedByOtherPlayer && anyNeighborOwnedByPlayer && notCultistSpawner

    // TODO: disallow placing tile on factory
    // TODO: disallow completely seal off factories from each other?
  }

  def placeTileAt(v : Vec2i, tile : Tile, ownedBy : Int) : Unit = {
//    println(s"placed tile ${tile.name} at $v with own $ownedBy")
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

  val playerA = new Player(0, Vec2i(0, 0), 0, Tile.groundTiles, Tile.playerAFactories)
  val playerB = new Player(1, Vec2i(0, 0), 0, Tile.groundTiles, Tile.playerBFactories)
  val players = Array(playerA, playerB)

  // floodfills
  val aggressionFloodFills = new Array[FloodFill](2)
  val summonerFloodFills = new Array[FloodFill](2)
}
