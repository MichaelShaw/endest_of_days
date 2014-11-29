package hack.game

import collection.mutable
import scala.util.Random

class Player(val id : Int, var cursorPosition : Vec2i, var tile : Int, val terrainTiles : Array[Tile], val factoryTiles : Array[Factory]) {
  // tile queue
  var placedTiles = 0

  val tileQueue = new mutable.ArrayBuffer[Seq[Tile]]()

  def generateSequence() {
    val rand = new Random()

    def pushStandard() {
      for {
        n <- 1 to 4
      } {
        tileQueue += terrainTiles
      }
      tileQueue += factoryTiles
    }
    def pushGate() {
      val toShuffle = new mutable.ArrayBuffer[Seq[Tile]]()
      (1 to 3).foreach(n => toShuffle += terrainTiles )
      toShuffle += (Array(Tile.gate) ++ terrainTiles)

      val shuffled = rand.shuffle(toShuffle)
      shuffled += factoryTiles

      tileQueue ++= shuffled
    }

    pushStandard()
    pushGate()
    pushStandard()
    pushStandard()
    pushGate()
    pushStandard()
    pushStandard()
    pushStandard()
    pushGate()

    for {
      n <- 1 to 100
    } {
//      tileQueue += (terrainTiles ++ factoryTiles)
      pushStandard()
    }
  }

  generateSequence()

  def canPlaceTiles(world:World) = placedTiles <= world.placementStage

  def allowedTiles(world:World) : Seq[Tile] = {
    if(canPlaceTiles(world)) {
      availableTiles
    } else {
      Seq.empty[Tile]
    }
  }

  def availableTiles = tileQueue(placedTiles)

  def handlePlacement() {
    placedTiles += 1
    if(tile >= availableTiles.length) {
      tile = availableTiles.length - 1 // clamp it in case the amount of tiles decreased
    }
  }

  def currentTile = availableTiles(tile)
}
