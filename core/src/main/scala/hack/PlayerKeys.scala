package hack

import com.badlogic.gdx.Input.Keys

case class PlayerKeys(left: Int, right: Int, down: Int, up: Int, previous: Int, next: Int, action: Int)

object PlayerKeys {
  val Player1 = PlayerKeys(Keys.LEFT, Keys.RIGHT, Keys.DOWN, Keys.UP, Keys.LEFT_BRACKET, Keys.RIGHT_BRACKET, Keys.BACKSLASH)
  val Player2 = PlayerKeys(Keys.A, Keys.D, Keys.S, Keys.W, Keys.Z, Keys.X, Keys.C)
}
