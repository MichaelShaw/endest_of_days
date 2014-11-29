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
    for {
      n <- 1 to 100
    } {
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
  }

  def currentTile = availableTiles(tile)
}
