package hack

import com.badlogic.gdx.Gdx.app
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.math.Vector3
import hack.game.Player
import hack.game.Tile
import hack.game.Vec2i
import hack.game.World

import scala.util.Random

class InputHandler(world : World) extends InputProcessor with ControllerListener {
  def movePlayer(player : Int, dx : Int, dy : Int) : Unit = movePlayer(player, Vec2i(dx, dy))

  def movePlayer(player : Int, deltaPosition : Vec2i) : Unit = {
    val p : Player = world.players(player)
    val cp = p.cursorPosition + deltaPosition

    if (world.inBounds(cp)) {
      p.cursorPosition = cp
    }
  }

  def selectTile(player : Int, dt : Int) : Unit = {
    // TODO
  }

  def placeTile(player : Int) : Unit = {
    val p : Player = world.players(player)
    val ts : Array[Tile] = Tile.groundTiles
    val t : Tile = ts(Random.nextInt(ts.size))

    world.placeTileAt(p.cursorPosition, t)
  }

  def setAsListener() : Unit = {
    input.setInputProcessor(this)
    Controllers.addListener(this)
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

  override def connected(controller : Controller) : Unit = {
  }

  override def disconnected(controller : Controller) : Unit = {
  }

  override def xSliderMoved(controller : Controller, i : Int, b : Boolean) : Boolean = {
    false
  }

  override def povMoved(controller : Controller, i : Int, povDirection : PovDirection) : Boolean = {
    false
  }

  override def buttonDown(controller : Controller, i : Int) : Boolean = {
    val p : Int = if (controller == Controllers.getControllers.first()) 0 else 1

    i match {
      case 2 => movePlayer(p, -1, 0)
      case 3 => movePlayer(p, 1, 0)
      case 1 => movePlayer(p, 0, -1)
      case 0 => movePlayer(p, 0, 1)
      case 8 => selectTile(p, -1)
      case 9 => selectTile(p, 1)
      case 11 => placeTile(p)

      case _ =>
    }

    true
  }

  override def buttonUp(controller : Controller, i : Int) : Boolean = {
    false
  }

  override def accelerometerMoved(controller : Controller, i : Int, vector3 : Vector3) : Boolean = {
    false
  }

  override def ySliderMoved(controller : Controller, i : Int, b : Boolean) : Boolean = {
    false
  }

  override def axisMoved(controller : Controller, i : Int, v : Float) : Boolean = {
    false
  }
}
