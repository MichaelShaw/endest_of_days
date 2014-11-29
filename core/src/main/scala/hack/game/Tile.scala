package hack.game

/**
 * Created by michael on 29/11/14.
 */


class Tile(val id:Int,
           val name:String,
           val canBeWalkedOn:Boolean = true) {

}

class Factory(id:Int,
              val playerId:Int,
              val startingHealth:Int,
              val produceEveryNTicks:Int = 1,
              val produceArch:Arch) extends Tile(id, s"factory_$playerId", canBeWalkedOn = false)

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

  val neutralStandardGround = register(new Tile(nextId, "ground"))
  val neutralImpassableGround = register(new Tile(nextId, "impassable", canBeWalkedOn = false))

  val playerAStandardGround = register(new Tile(nextId, "ground"))
  val playerAImpassableGround = register(new Tile(nextId, "impassable", canBeWalkedOn = false))
  val playerAGroundTiles = Array(playerAStandardGround, playerAImpassableGround)

  val playerASoldierFactory = register(new Factory(nextId, 0, factoryHealth, produceEveryNTicks = 1, produceArch = Arch.soldier))
  val playerACaptainFactory = register(new Factory(nextId, 0, factoryHealth, produceEveryNTicks = 3, produceArch = Arch.captain))
  val playerAAEFactory = register(new Factory(nextId, 0, factoryHealth, produceEveryNTicks = 5, produceArch = Arch.ae))
  val playerADefenderFactory = register(new Factory(nextId, 0, factoryHealth, produceEveryNTicks = 2, produceArch = Arch.defender))
  val playerAFactoryTiles = Array(playerASoldierFactory, playerACaptainFactory, playerAAEFactory, playerADefenderFactory)

  val playerBStandardGround = register(new Tile(nextId, "ground"))
  val playerBImpassableGround = register(new Tile(nextId, "impassable", canBeWalkedOn = false))
  val playerBGroundTiles = Array(playerBStandardGround, playerBImpassableGround)

  val playerBSoldierFactory = register(new Factory(nextId, 1, factoryHealth, produceEveryNTicks = 1, produceArch = Arch.soldier))
  val playerBCaptainFactory = register(new Factory(nextId, 1, factoryHealth, produceEveryNTicks = 3, produceArch = Arch.captain))
  val playerBAEFactory = register(new Factory(nextId, 1, factoryHealth, produceEveryNTicks = 5, produceArch = Arch.ae))
  val playerBDefenderFactory = register(new Factory(nextId, 1, factoryHealth, produceEveryNTicks = 2, produceArch = Arch.defender))
  val playerBFactoryTiles = Array(playerBSoldierFactory, playerBCaptainFactory, playerBAEFactory, playerBDefenderFactory)
}
