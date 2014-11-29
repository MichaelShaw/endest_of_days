package hack.game

/**
 * Created by michael on 29/11/14.
 */

class Living(val id:Int,
             val playerId:Int,
             val arch:Arch,
             var currentLocation: Vec2i) {

  var currentSlot = -1 // chicken and egg problem :-/

  var health = arch.maxHealth

  var lastLocation = currentLocation
  var lastSlot = currentSlot

  var actionStartedAtTick:Int = 0
  var actionFinishedAtTick:Int = 0

  // DISPLAY FLAGS
  var lastStruckAt:Int = -1

  def moving = lastLocation != currentLocation

  def saveLastLocation() {
    lastLocation = currentLocation
    lastSlot = currentSlot
  }
}


// describes a unit type
case class Arch(id:Int, name:String, attack:Int, maxHealth:Int, explodeOnAttack:Boolean = false, aeAttack:Boolean = false, canHelpSummon:Boolean = false) {

}

object Arch {
  val cultist = Arch(0, "cultist", 1, 2, canHelpSummon = true)

  val imp = Arch(1, "imp", attack = 1, maxHealth = 4)
  val captain = Arch(2, "captain", attack = 3, maxHealth = 10)

  val count = 4
}

