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
                explodeOnAttack:Boolean = false, aeDamage:Int = 0,
                canHelpSummon:Boolean = false,
                ticksToMove:Int = 1,
                instakillBuildings:Boolean = false,
                onHitSpawns:Option[Arch] = None) {

}

object Arch {
  val cultist = Arch(0, "cultist", attack = 1, maxHealth = 2, canHelpSummon = true)

  val imp = Arch(1, "imp", attack = 1, maxHealth = 3)
  val worm = Arch(2, "worm", attack = 6, maxHealth = 18, ticksToMove = 2, instakillBuildings = true)
  // due to 2 ticks to move it sits vulnerable for a long time ... so it underperforms relative to it's combat stats
  // this slowness means after winning a standoff, and their friendlies surge forward, they fall behind, weakending the momentum of the wave
  val fex = Arch(3, "fex", attack = 5, maxHealth = 16, aeDamage = 2)

  val captain = Arch(4, "captain", attack = 3, maxHealth = 7)
  val eyeBall = Arch(5, "eye_ball", attack = 3, maxHealth = 7)

  val smallBeetle = Arch(6, "small_beetle", attack = 1, maxHealth = 2)
  val bigBeetle = Arch(7, "big_beetle", attack = 1, maxHealth = 13, onHitSpawns = Some(smallBeetle))

  val count = 8
}

