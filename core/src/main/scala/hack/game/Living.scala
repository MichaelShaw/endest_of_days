package hack.game

import hack.Renderer

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

  // DISPLAY FLAGS (render state)
  var lastStruckAt:Int = -1
  var velocity = Vec2f.zero
  var smoothedPosition = (Vec2f.from(currentLocation) + Vec2f(0.5f, 0.5f)) * Renderer.pixelsPerTile

//  println(s"${arch.id} is spawning at $currentLocation smoothedposition will be $smoothedPosition")

  def moving = lastLocation != currentLocation

  def saveLastLocation() {
    lastLocation = currentLocation
    lastSlot = currentSlot
  }
}


// describes a unit type
case class Arch(id:Int, name:String, attack:Int, maxHealth:Int,
                explodeOnAttack:Boolean = false, aeAttack:Boolean = false,
                canHelpSummon:Boolean = false,
                onHitSpawns:Option[Arch] = None) {

}

object Arch {
  val cultist = Arch(0, "cultist", 1, 2, canHelpSummon = true)

  val imp = Arch(1, "imp", attack = 1, maxHealth = 4)
  val worm = Arch(2, "worm", attack = 5, maxHealth = 6)
  val fex = Arch(3, "fex", attack = 3, maxHealth = 18, aeAttack = true)

  val captain = Arch(4, "captain", attack = 3, maxHealth = 10)
  val eyeBall = Arch(5, "eye_ball", attack = 2, maxHealth = 4)
  val smallBeetle = Arch(6, "small_beetle", attack = 1, maxHealth = 2)
  val bigBeetle = Arch(7, "big_beetle", attack = 1, maxHealth = 12, onHitSpawns = Some(smallBeetle))



  val count = 8
}

