package hack

import com.badlogic.gdx.Gdx.app
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import hack.game.World

class InputHandler(world : World) extends InputProcessor {
  def movePlayer(player : Int, dx : Int, dy : Int) : Unit = {
    // TODO
  }

  def selectTile(player : Int, dt : Int) : Unit = {
    // TODO
  }

  def placeTile(player : Int) : Unit = {
    // TODO
  }

  override def keyDown(i : Int) : Boolean = {
    i match {
      case Keys.LEFT => movePlayer(0, -1, 0)
      case Keys.RIGHT => movePlayer(0, 1, 0)
      case Keys.DOWN => movePlayer(0, 0, -1)
      case Keys.UP => movePlayer(0, 0, 1)
      case Keys.LEFT_BRACKET => selectTile(0, -1)
      case Keys.RIGHT_BRACKET => selectTile(0, 1)
      case Keys.BACKSLASH => placeTile(0)

      case Keys.A => movePlayer(1, -1, 0)
      case Keys.D => movePlayer(1, 1, 0)
      case Keys.S => movePlayer(1, 0, -1)
      case Keys.W => movePlayer(1, 0, 1)
      case Keys.Z => selectTile(1, -1)
      case Keys.X => selectTile(1, 1)
      case Keys.C => placeTile(1)

      case Keys.ESCAPE => app.exit()

      case _ =>
    }

    true
  }

  override def keyTyped(c : Char) : Boolean = {
    false
  }

  override def mouseMoved(i : Int, i1 : Int) : Boolean = {
    false
  }

  override def touchDown(i : Int, i1 : Int, i2 : Int, i3 : Int) : Boolean = {
    false
  }

  override def keyUp(i : Int) : Boolean = {
    false
  }

  override def scrolled(i : Int) : Boolean = {
    false
  }

  override def touchUp(i : Int, i1 : Int, i2 : Int, i3 : Int) : Boolean = {
    false
  }

  override def touchDragged(i : Int, i1 : Int, i2 : Int) : Boolean = {
    false
  }
}
