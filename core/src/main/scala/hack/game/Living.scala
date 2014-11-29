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
case class Arch(id:Int, name:String, attack:Int, maxHealth:Int, explodeOnAttack:Boolean = false, aeAttack:Boolean = false) {

}

object Arch {
  val soldier = Arch(0, "soldier", 1, 4)
  val captain = Arch(1, "captain", 2, 5)
  val ae = Arch(2, "ae", 3, 4, explodeOnAttack = true, aeAttack = true)
  val defender = Arch(3, "defender", 1, 8)

  val count = 4
}

