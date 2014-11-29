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
              val produceArch:Arch,
              val requiredSummoners:Int) extends Tile(id, name, canBeWalkedOn = true)

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

  val gate = register(new Gate(nextId, "gate"))

  val cultistSpawner = register(new Factory(nextId, "cultist_spawner", factoryHealth, produceEveryNTicks = 2, produceArch = Arch.cultist, requiredSummoners = 0))

  val impSpawner = register(new Factory(nextId, "imp_spawner", factoryHealth, produceEveryNTicks = 1, produceArch = Arch.imp, requiredSummoners = 5))
  val wormSpawner = register(new Factory(nextId, "worm_spawner", factoryHealth, produceEveryNTicks = 4, produceArch = Arch.worm, requiredSummoners = 6))
  val fexSpawner = register(new Factory(nextId, "fex_spawner", factoryHealth, produceEveryNTicks = 12, produceArch = Arch.fex, requiredSummoners = 7))

  val captainSpawner = register(new Factory(nextId, "captain_spawner", factoryHealth, produceEveryNTicks = 3, produceArch = Arch.captain, requiredSummoners = 5))
  val eyeBallSpawner = register(new Factory(nextId, "eyeBall_spawner", factoryHealth, produceEveryNTicks = 6, produceArch = Arch.eyeBall, requiredSummoners = 6))
  val bigBeetleSpawner = register(new Factory(nextId, "bigBeetle_spawner", factoryHealth, produceEveryNTicks = 8, produceArch = Arch.bigBeetle, requiredSummoners = 7))

  val playerAFactories = Array(captainSpawner, eyeBallSpawner, bigBeetleSpawner)
  val playerBFactories = Array(impSpawner, wormSpawner, fexSpawner)
}
