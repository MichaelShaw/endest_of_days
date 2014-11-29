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
import hack.game._


object Renderer {
  val pixelsPerTile = 32
}
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

  tileAtlas(Tile.cultistSpawner.id) = tileRegion(2, 0)
  tileAtlas(Tile.impSpawner.id) = tileRegion(3, 2)
  tileAtlas(Tile.captainSpawner.id) = tileRegion(3, 1)
  tileAtlas(Tile.bigBeetleSpawner.id) = tileRegion(4, 1)
//  tileAtlas(Tile.aeFactory.id) = tileRegion(5, 0)


  val cultistSpawners = Array(
    tileRegion(2, 1),
    tileRegion(2, 2)
  )
  val gates = Array(
    tileRegion(5, 1),
    tileRegion(5, 2)
  )

  // 2 players

  val playerACursor = tileRegion(0, 3)
  val playerBCursor = tileRegion(0, 4)

  // soldier, captain, ae, defender

  val cultists = Array(
    new TextureRegion(tileTexture, 224, 32, 16, 16),
    new TextureRegion(tileTexture, 224, 64, 16, 16)
  )
  val archs = new Array[TextureRegion](16)
  archs(Arch.imp.id) = new TextureRegion(tileTexture, 240, 64, 16, 16)
  archs(Arch.captain.id) = new TextureRegion(tileTexture, 240, 32, 16, 16)
  archs(Arch.bigBeetle.id) = new TextureRegion(tileTexture,192, 96, 19, 17)
  archs(Arch.smallBeetle.id) = new TextureRegion(tileTexture,213, 98, 6, 6)

  def render(world : World, simulationAccu : Double, simulationTickSize : Double, delta:Double) {
    camera.position.set(world.width / 2 * tileSizeScreen, world.height / 2 * tileSizeScreen, 0)
    camera.update()



    fbo.begin()

    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    mainBatch.setProjectionMatrix(camera.combined)
    mainBatch.begin()
    mainBatch.setShader(toFrameBufferShader)

    renderTiles(world)
    renderLivings(world, simulationAccu, simulationTickSize, delta)
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
      val textureRegion : TextureRegion = tile match {
        case Tile.gate =>
          gates(owned)
        case Tile.cultistSpawner =>
          cultistSpawners(owned)
        case f:Factory =>
          tileAtlas(f.id)
        case _ =>
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
  def renderLivings(world : World, simulationAccu : Double, simulationTickSize : Double, delta:Double) {
    def flashing(d : Double) : Boolean = (simulationAccu / d).asInstanceOf[Int] % 2 == 1
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val entities = world.livingsAt(x, y)
      var slot = 0; while (slot < world.slotsPerTile) {
        val e = entities(slot)
        if (e != null) {
          val tr : TextureRegion = if(e.arch == Arch.cultist) {
            cultists(e.playerId)
          } else {
            archs(e.arch.id)
          }
          val lastLocation = screenLocation(e.lastLocation, e.lastSlot)
          val currentLocation = screenLocation(e.currentLocation, e.currentSlot)

          val at = Vec2f.lerp(lastLocation, currentLocation, simulationAccu / simulationTickSize)

          val smoothTime = 0.25

          val (newSmoothedPosition, newVelocity) = Spring.smooth(e.smoothedPosition, at, e.velocity, smoothTime, delta)
          e.smoothedPosition = newSmoothedPosition
          e.velocity = newVelocity


          val draw = if (e.lastStruckAt == world.tick - 1 && simulationAccu < 0.4) {
            flashing(0.10)
          } else {
            true
          }
          if (draw) {
            mainBatch.draw(tr, e.smoothedPosition.x, e.smoothedPosition.y, tr.getRegionWidth, tr.getRegionHeight)
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
