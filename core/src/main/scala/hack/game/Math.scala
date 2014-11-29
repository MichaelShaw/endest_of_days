package hack.game

/**
 * Created by michael on 29/11/14.
 */

case class Vec2i(x:Int, y:Int) {
  def plus(xa:Int, ya:Int) = Vec2i(x + xa, y + ya)
}
