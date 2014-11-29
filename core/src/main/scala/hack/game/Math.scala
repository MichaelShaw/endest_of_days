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
  def -(other:Vec2f) = Vec2f(x - other.x, y - other.y)
  def *(n:Float) = Vec2f(x * n, y * n)
  def +(other:Vec2f) = Vec2f(x + other.x, y + other.y)
}

object Vec2f {
  val zero = Vec2f(0, 0)
  def from(v:Vec2i) = Vec2f(v.x, v.y)
  def lerp(a : Vec2i, b : Vec2i, alpha : Double) : Vec2f = {
    val nAlpha = 1.0 - alpha
    Vec2f(
      (a.x * nAlpha + b.x * alpha).asInstanceOf[Float],
      (a.y * nAlpha + b.y * alpha).asInstanceOf[Float]
    )
  }
}

object Spring {
  // smoothtime 0.1
  // )
  def smooth(from:Vec2f, to:Vec2f, velocity:Vec2f, smoothTime:Double, timeDelta:Double) : (Vec2f,Vec2f) = {
    val omega = (2.0 / smoothTime).asInstanceOf[Float]
    val x = omega * timeDelta
    val exp = (1.0 / (1.0 + x + 0.48 * x * x + 0.235 * x * x * x)).asInstanceOf[Float]
    val change = from - to

    val temp = (Vec2f(0f, 0f) + velocity + (change * omega.asInstanceOf[Float])) * timeDelta.asInstanceOf[Float]
    // we're modifying the velocity here

    val newVelocity = velocity - (temp * omega)

    val result = (change + temp) * exp + to

    (result, newVelocity)
  }
}

object Direction {
  val e = Vec2i(1, 0)
  val n = Vec2i(0, 1)
  val w = Vec2i(-1, 0)
  val s = Vec2i(0, -1)
  val directions = Array(e, n, w, s)

  val z = Vec2i(0, 0)
  val directionsWithZero = directions ++ Array(z)
}
