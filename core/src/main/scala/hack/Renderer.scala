package hack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.{ShaderProgram, FrameBuffer}
import com.badlogic.gdx.math.Matrix4
import hack.game.Player
import hack.game.Tile
import hack.game.Vec2f
import hack.game.Vec2i
import hack.game.World

class Renderer {
  val camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)

  def fboWidth = Gdx.graphics.getWidth
  def fboHeight = Gdx.graphics.getHeight

  val fbo = new FrameBuffer(Format.RGBA8888, fboWidth, fboHeight, false)
  fbo.getColorBufferTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest)

  def assetsPath = "../assets"

  // TO 
  def toFBOVertexShaderText = Gdx.files.internal(s"$assetsPath/toFbo.vert.glsl").readString()
  def toFBOFragmentShaderText = Gdx.files.internal(s"$assetsPath/toFbo.frag.glsl").readString()
  val toFrameBufferShader = new ShaderProgram(toFBOVertexShaderText,toFBOFragmentShaderText)
  if(!toFrameBufferShader.isCompiled) {
    throw new Exception("couldnt compile shader " + toFrameBufferShader.getLog)
  }

  def postVertexShaderText = Gdx.files.internal(s"$assetsPath/post.vert.glsl").readString()
  def postFragmentShaderText = Gdx.files.internal(s"$assetsPath/post.frag.glsl").readString()
  val postShader = new ShaderProgram(postVertexShaderText,postFragmentShaderText)
  if(!postShader.isCompiled) {
    throw new Exception("couldnt compile shader " + postShader.getLog)
  }




  val tileTexture = new Texture(Gdx.files.internal(s"$assetsPath/tiles.png"))

  val mainBatch = new SpriteBatch
  val postBatch = new SpriteBatch

  val tileSizeTexture = 32
  val tileSizeScreen = tileSizeTexture // 2x upscale

  // from top left
  def tileRegion(x : Int, y : Int) = new TextureRegion(tileTexture, x * tileSizeTexture, y * tileSizeTexture, tileSizeTexture, tileSizeTexture)

  val tileAtlas = new Array[TextureRegion](Tile.count)
  tileAtlas(Tile.standardGround.id) = tileRegion(0, 0)
  tileAtlas(Tile.impassableGround.id) = tileRegion(1, 0)

  tileAtlas(Tile.workerFactory.id) = tileRegion(2, 0)
  tileAtlas(Tile.soldierFactory.id) = tileRegion(3, 0)
  tileAtlas(Tile.captainFactory.id) = tileRegion(4, 0)
  tileAtlas(Tile.aeFactory.id) = tileRegion(5, 0)


  tileAtlas(Tile.gate.id) = tileRegion(6, 0)

  val ownedTileAtlas : Array[Array[TextureRegion]] = (1 to 2).map { n =>
    (0 to 6).map { t =>
      tileRegion(t, n)
    }.toArray
  }.toArray

  // 2 players

  val playerACursor = tileRegion(0, 3)
  val playerBCursor = tileRegion(0, 4)

  // soldier, captain, ae, defender

  val playerAGuys = Array(
    new TextureRegion(tileTexture, 224, 32, 16, 16),
    new TextureRegion(tileTexture, 240, 32, 16, 16),
    new TextureRegion(tileTexture, 224, 48, 16, 16),
    new TextureRegion(tileTexture, 240, 48, 16, 16)
  )

  val playerBGuys = Array(
    new TextureRegion(tileTexture, 224, 64, 16, 16),
    new TextureRegion(tileTexture, 240, 64, 16, 16),
    new TextureRegion(tileTexture, 224, 80, 16, 16),
    new TextureRegion(tileTexture, 240, 80, 16, 16)
  )

  def render(world : World, simulationAccu : Double, simulationTickSize : Double) {
    camera.position.set(world.width / 2 * tileSizeScreen, world.height / 2 * tileSizeScreen, 0)
    camera.update()



    fbo.begin()

    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    mainBatch.setProjectionMatrix(camera.combined)
    mainBatch.begin()
    mainBatch.setShader(toFrameBufferShader)

    renderTiles(world)
    renderLivings(world, simulationAccu, simulationTickSize)
    renderHands(world)
    renderCursors(world)

    mainBatch.end()

    fbo.end()

    // draw final

    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    val m = new Matrix4()
    m.setToOrtho(0, fbo.getWidth, fbo.getHeight, 0, 0, 1)

    postShader.begin()

    postBatch.setShader(postShader)
    postBatch.setProjectionMatrix(m)
    postBatch.begin()
    postBatch.draw(fbo.getColorBufferTexture, 0, 0)
    postBatch.end()

    postShader.end()
  }

  def renderTiles(world : World) {
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val v = Vec2i(x, y)
      val tile = world.tileAt(v)
      val owned = world.owned.get(v)
      val textureRegion : TextureRegion = if (owned >= 0) {
        ownedTileAtlas(owned)(tile.id)

      } else {
        tileAtlas(tile.id)
      }
      mainBatch.draw(textureRegion, x * tileSizeScreen, y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
    }
  }

  val innerTileLocations = (for {
    x <- 0 to 2
    y <- 0 to 2
  } yield Vec2i(4 + x * 8, 4 + y * 8)).toArray // need better logic here

  def screenLocation(loc : Vec2i, slot : Int) : Vec2i = {
    (loc * tileSizeScreen) + innerTileLocations(slot)
  }

  // simulation accu for partial tick
  def renderLivings(world : World, simulationAccu : Double, simulationTickSize : Double) {
    def flashing(d : Double) : Boolean = (simulationAccu / d).asInstanceOf[Int] % 2 == 1
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val entities = world.livingsAt(x, y)
      var slot = 0; while (slot < world.slotsPerTile) {
        val e = entities(slot)
        if (e != null) {
          val tr : TextureRegion = if(e.playerId == 0) {
            playerAGuys(e.arch.id)
          } else {
            playerBGuys(e.arch.id)
          }
          val lastLocation = screenLocation(e.lastLocation, e.lastSlot)
          val currentLocation = screenLocation(e.currentLocation, e.currentSlot)

          val at = Vec2f.lerp(lastLocation, currentLocation, simulationAccu / simulationTickSize)

          val draw = if (e.lastStruckAt == world.tick - 1 && simulationAccu < 0.4) {
            flashing(0.10)
          } else {
            true
          }
          if (draw) {
            mainBatch.draw(tr, at.x, at.y, 16, 16)
          }

        }
        slot += 1
      }
    }
  }

  def renderHands(world : World) : Unit = {
    def renderHand(player : Player, cursorTextureRegion : TextureRegion, xOffset : Int) : Unit = {
      for (t <- 0 until player.tiles.length) {
        val x : Int = t + xOffset
        val y : Int = -2

        mainBatch.draw(tileAtlas(player.tiles(t).id), x * tileSizeScreen, y * tileSizeScreen, tileSizeScreen, tileSizeScreen)

        if (t == player.tile) {
          mainBatch.draw(cursorTextureRegion, x * tileSizeScreen, y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
        }
      }
    }

    renderHand(world.playerA, playerACursor, 0)
    renderHand(world.playerB, playerBCursor, world.width - world.playerB.tiles.length)
  }

  def renderCursors(world : World) : Unit = {
    def renderCursor(player : Player, cursorTextureRegion : TextureRegion) : Unit = {
      mainBatch.draw(tileAtlas(player.tiles(player.tile).id), player.cursorPosition.x * tileSizeScreen, player.cursorPosition.y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
      mainBatch.draw(cursorTextureRegion, player.cursorPosition.x * tileSizeScreen, player.cursorPosition.y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
    }

    renderCursor(world.playerA, playerACursor)
    renderCursor(world.playerB, playerBCursor)
  }
}
