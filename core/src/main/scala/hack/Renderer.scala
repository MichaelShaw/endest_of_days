package hack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera, Texture}
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch, TextureRegion}
import com.badlogic.gdx.graphics.glutils.{ShaderProgram, FrameBuffer}
import com.badlogic.gdx.math.Matrix4
import hack.game._


object Renderer {
  val pixelsPerTile = 64
}
class Renderer {
  val camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  val font = new BitmapFont() // Gdx.files.internal("data/arial-15.fnt"),false)
  font.setColor(Color.WHITE)

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
  val upScale = 2
  val tileSizeScreen = tileSizeTexture * upScale // 2x upscale

  // from top left
  def tileRegion(x : Int, y : Int) = new TextureRegion(tileTexture, x * tileSizeTexture, y * tileSizeTexture, tileSizeTexture, tileSizeTexture)

  val terrainAtlas = Array.tabulate[Array[TextureRegion]](2) { n =>
    val arr = new Array[TextureRegion](Tile.count)
    arr(Tile.standardGround.id) = tileRegion(0, n + 1)
    arr(Tile.impassableGround.id) = tileRegion(1, n + 1)
    arr
  }

  val tileAtlas = new Array[TextureRegion](Tile.count)
  tileAtlas(Tile.standardGround.id) = tileRegion(0, 0)
  tileAtlas(Tile.impassableGround.id) = tileRegion(1, 0)

  tileAtlas(Tile.impSpawner.id) = tileRegion(3, 2)
  tileAtlas(Tile.wormSpawner.id) = tileRegion(4, 2)
  tileAtlas(Tile.fexSpawner.id) = tileRegion(5, 2)

  tileAtlas(Tile.captainSpawner.id) = tileRegion(3, 1)
  tileAtlas(Tile.eyeBallSpawner.id) = tileRegion(4, 1)
  tileAtlas(Tile.bigBeetleSpawner.id) = tileRegion(5, 1)

  val cultistSpawners = Array(
    tileRegion(2, 1),
    tileRegion(2, 2)
  )
  val gates = Array(
    tileRegion(6, 1),
    tileRegion(6, 2)
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
  archs(Arch.worm.id) = new TextureRegion(tileTexture, 224, 80, 16, 16)
  archs(Arch.fex.id) = new TextureRegion(tileTexture, 224, 96, 24, 20)

  archs(Arch.captain.id) = new TextureRegion(tileTexture, 240, 32, 16, 16)
  archs(Arch.eyeBall.id) = new TextureRegion(tileTexture, 224, 48, 16, 16)
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
    renderHands(world, simulationAccu, simulationTickSize)
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
      val textureRegion = trForTile(world.tileAt(v), world.owned.get(v))
      mainBatch.draw(textureRegion, x * tileSizeScreen, y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
    }
  }

  def trForTile(tile:Tile, owned:Int) : TextureRegion = {
    tile match {
      case Tile.gate =>
        gates(owned)
      case Tile.cultistSpawner =>
        cultistSpawners(owned)
      case f:Factory =>
        tileAtlas(f.id)
      case _ =>
        if(owned >= 0) {
          terrainAtlas(owned)(tile.id)
        } else {
          tileAtlas(tile.id)
        }
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
          if(tr == null) {
            throw new Exception("failed to draw " + e.arch)
          }
          if (draw) {
            if(e.velocity.x > 0.0) {
              mainBatch.draw(tr, e.smoothedPosition.x, e.smoothedPosition.y, tr.getRegionWidth * upScale, tr.getRegionHeight * upScale)
            } else {
              // public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
//              mainBatch.draw(tr, e.smoothedPosition.x, e.smoothedPosition.y, tr.getRegionWidth * upScale, tr.getRegionHeight * upScale)
              mainBatch.draw(tileTexture, e.smoothedPosition.x, e.smoothedPosition.y, tr.getRegionWidth * upScale, tr.getRegionHeight * upScale,
                tr.getU2, tr.getV2, tr.getU, tr.getV)
            }


          }

        }
        slot += 1
      }
    }
  }

  def renderHands(world : World, simulationAccu:Double, simulationTick:Double) : Unit = {
    def renderHand(player : Player, cursorTextureRegion : TextureRegion, xOffset : Int) : Unit = {
      if(player.canPlaceTiles(world)) {

        val yOffset = (if(player.placedTiles == world.placementStage && world.placementTimer == world.ticksPerPlace) {
          // player is up to date and it's the first simulation tick
          val progress = Bias.getBias(Bias.clamp(simulationAccu * 2 / simulationTick), 0.75)
//          println("progress -> " + progress)
          ((1 - progress) * -tileSizeScreen).asInstanceOf[Int]
        } else {
          0
        }) - 15



        for (t <- 0 until player.availableTiles.length) {
          val x : Int = t + xOffset
          val y =  -1

          mainBatch.draw(trForTile(player.availableTiles(t), player.id), x * tileSizeScreen, y * tileSizeScreen + yOffset, tileSizeScreen, tileSizeScreen)

          if (t == player.tile) {
            mainBatch.draw(cursorTextureRegion, x * tileSizeScreen, y * tileSizeScreen + yOffset, tileSizeScreen, tileSizeScreen)
          }
        }
      }
    }

    font.draw(mainBatch, world.placementTimer.toString, world.width * tileSizeScreen / 2, -50)

    renderHand(world.playerA, playerACursor, 0)
    renderHand(world.playerB, playerBCursor, world.width - world.playerB.availableTiles.length)
  }

  def renderCursors(world : World) : Unit = {
    def renderCursor(player : Player, cursorTextureRegion : TextureRegion) : Unit = {
      if(player.canPlaceTiles(world)) {
        mainBatch.draw(trForTile(player.currentTile, player.id), player.cursorPosition.x * tileSizeScreen, player.cursorPosition.y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
      }
      mainBatch.draw(cursorTextureRegion, player.cursorPosition.x * tileSizeScreen, player.cursorPosition.y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
    }

    renderCursor(world.playerA, playerACursor)
    renderCursor(world.playerB, playerBCursor)
  }
}
