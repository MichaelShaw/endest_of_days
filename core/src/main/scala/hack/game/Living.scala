package hack.game

/**
 * Created by michael on 29/11/14.
 */

class Living(val id:Int,
             val playerId:Int,
             val arch:Arch,
             var currentLocation: Vec2i) {
  var lastLocation = currentLocation

  var actionStartedAtTick:Int = 0
  var actionFinishedAtTick:Int = 0
}

// describes a unit type
case class Arch(name:String, attack:Int, maxHealth:Int) {

}

object Arch {
  val soldier = Arch("soldier", 1, 4)
}

