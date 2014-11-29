package hack

import com.badlogic.gdx.Gdx.{app, input}
import com.badlogic.gdx.Input.Keys
import hack.game.World

class InputHandler {
  def handleGlobalInput(): Unit = {
    if (input.isKeyJustPressed(Keys.ESCAPE))
      app.exit()
  }

  def keyboardInput(keys: PlayerKeys): PlayerInput = {
    var x = 0: Int
    var y = 0: Int
    var t = 0: Int
    var a = false: Boolean

    if (input.isKeyJustPressed(keys.left))
      x -= 1

    if (input.isKeyJustPressed(keys.right))
      x += 1

    if (input.isKeyJustPressed(keys.down))
      y -= 1

    if (input.isKeyJustPressed(keys.up))
      y += 1

    if (input.isKeyJustPressed(keys.previous))
      t -= 1

    if (input.isKeyJustPressed(keys.next))
      t += 1

    if (input.isKeyJustPressed(keys.action))
      a = true

    PlayerInput(x, y, t, a)
  }

  def handleFor(world: World) {
    handleGlobalInput()
    val input1: PlayerInput = keyboardInput(PlayerKeys.Player1)
    val input2: PlayerInput = keyboardInput(PlayerKeys.Player2)

    // TODO: apply to world
  }
}
