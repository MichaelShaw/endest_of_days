package hack.game

/**
 * Created by michael on 29/11/14.
 */


class Tile(val id:Int,
           val name:String,
           val canBeWalkedOn:Boolean = true) {

}

class Factory(id:Int,
              name:String,
              val startingHealth:Int,
              val produceEveryNTicks:Int = 1,
              val produceArch:Arch) extends Tile(id, name, canBeWalkedOn = false)

class Gate(id:Int, name:String) extends Tile(id, name, canBeWalkedOn = true)

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

  val factoryHealth = 24

  val standardGround = register(new Tile(nextId, "ground"))
  val impassableGround = register(new Tile(nextId, "impassable", canBeWalkedOn = false))
  val groundTiles = Array(standardGround, impassableGround)

  val soldierFactory = register(new Factory(nextId, "soldier_factory", factoryHealth, produceEveryNTicks = 1, produceArch = Arch.soldier))
  val captainFactory = register(new Factory(nextId, "captain_factory", factoryHealth, produceEveryNTicks = 3, produceArch = Arch.captain))
  val aeFactory = register(new Factory(nextId, "ae_factory", factoryHealth, produceEveryNTicks = 5, produceArch = Arch.ae))
  val defenderFactory = register(new Factory(nextId, "defender_factory", factoryHealth, produceEveryNTicks = 2, produceArch = Arch.defender))
  val factoryTiles = Array(soldierFactory, captainFactory, aeFactory, defenderFactory)

  val gate = register(new Gate(nextId, "gate"))

}
