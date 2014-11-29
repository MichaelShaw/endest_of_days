package hack.game

/**
 * Created by michael on 29/11/14.
 */

case class Vec2i(x : Int, y : Int) {
  def plus(xa : Int, ya : Int) = Vec2i(x + xa, y + ya)

  def *(n : Int) = Vec2i(x * n, y * n)

  def +(other : Vec2i) = Vec2i(x + other.x, y + other.y)
}

case class Vec2f(x : Float, y : Float) {

}

object Vec2f {
  def lerp(a : Vec2i, b : Vec2i, alpha : Double) : Vec2f = {
    val nAlpha = 1.0 - alpha
    Vec2f(
      (a.x * nAlpha + b.x * alpha).asInstanceOf[Float],
      (a.y * nAlpha + b.y * alpha).asInstanceOf[Float]
    )
  }
}

object Direction {
  val e = Vec2i(1, 0)
  val n = Vec2i(0, 1)
  val w = Vec2i(-1, 0)
  val s = Vec2i(0, -1)
  val directions = Array(e, n, w, s)
}
