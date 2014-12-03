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
import hack.game._

class InputHandler(var world : World) extends InputProcessor with ControllerListener {
  var resetWorld:Boolean = false

  var placeTileSound:Boolean = false
  var triggerSound:Boolean = false

  def movePlayer(player : Int, deltaPosition : Vec2i) : Unit = {
    val p : Player = world.players(player)
    val cp = p.cursorPosition + deltaPosition

    if (world.inBounds(cp)) {
      p.cursorPosition = cp
    }
  }

  def selectTile(player : Int, dt : Int) : Unit = {
    val p : Player = world.players(player)
    val t = p.tile + dt

    if (t >= 0 && t < p.availableTiles.size) {
      triggerSound = true
      p.tile = t
    }
  }

  def placeTile(player : Int) : Unit = {
    val p : Player = world.players(player)
    assert(p.id == player)

    if(p.canPlaceTiles(world)) {
      val t : Tile = p.currentTile

      if (world.canPlaceTileAt(p.cursorPosition, p.id)) {
        placeTileSound = true
        p.handlePlacement()
        world.placeTileAt(p.cursorPosition, t, p.id)
        world.spawnAtTile(p.cursorPosition, Particle.smoke)
      }
    }
  }

  def setAsListener() : Unit = {
    input.setInputProcessor(this)
    Controllers.addListener(this)
  }

  override def keyDown(i : Int) : Boolean = {
    i match {
      case Keys.LEFT => movePlayer(1, Direction.w)
      case Keys.RIGHT => movePlayer(1, Direction.e)
      case Keys.DOWN => movePlayer(1, Direction.s)
      case Keys.UP => movePlayer(1, Direction.n)
      case Keys.LEFT_BRACKET => selectTile(1, -1)
      case Keys.RIGHT_BRACKET => selectTile(1, 1)
      case Keys.BACKSLASH => placeTile(1)

      case Keys.A => movePlayer(0, Direction.w)
      case Keys.D => movePlayer(0, Direction.e)
      case Keys.S => movePlayer(0, Direction.s)
      case Keys.W => movePlayer(0, Direction.n)
      case Keys.Z => selectTile(0, -1)
      case Keys.X => selectTile(0, 1)
      case Keys.C => placeTile(0)

      case Keys.Y => world.playerA.ai = !world.playerA.ai
      case Keys.U => world.playerB.ai = !world.playerB.ai

      case Keys.H => resetWorld = true

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
      case 0 => movePlayer(p, Direction.n)
      case 1 => movePlayer(p, Direction.s)
      case 2 => movePlayer(p, Direction.w)
      case 3 => movePlayer(p, Direction.e)
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
