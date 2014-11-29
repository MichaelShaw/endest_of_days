package hack.game

/**
 * Created by michael on 30/11/14.
 */
class Particle(var at:Vec2f, var velocity:Vec2f, var aliveFor:Float, var partId:Int) {


}

object Particle {
  val smoke = 0
  val blueStuff = 1
  val redStuff = 2
  val magic = 3

}
