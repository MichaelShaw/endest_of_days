package hack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera, Texture}
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch, TextureRegion}
import com.badlogic.gdx.graphics.glutils.{ShaderProgram, FrameBuffer}
import com.badlogic.gdx.math.Matrix4
import hack.game._

object Renderer {
  val tileSizeTexture = 32
  val upScale = 2
  val tileSizeScreen = tileSizeTexture * upScale
  val pixelsPerTile = tileSizeScreen
}

trait AssetLoader {
  def assetsPath = "assets"
  def assetFile(path:String) : FileHandle = Gdx.files.classpath(s"$assetsPath/$path")
}

class Renderer extends AssetLoader {
  val camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  val font = new BitmapFont() // Gdx.files.internal("data/arial-15.fnt"),false)
  font.setColor(Color.WHITE)

  def fboWidth = Gdx.graphics.getWidth
  def fboHeight = Gdx.graphics.getHeight

  val fbo = new FrameBuffer(Format.RGBA8888, fboWidth, fboHeight, false)
  fbo.getColorBufferTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest)

//  def assetsPath = "../assets"



  // TO 
  def toFBOVertexShaderText = assetFile(s"toFbo.vert.glsl").readString()
  def toFBOFragmentShaderText = assetFile(s"toFbo.frag.glsl").readString()
  val toFrameBufferShader = new ShaderProgram(toFBOVertexShaderText,toFBOFragmentShaderText)
  if(!toFrameBufferShader.isCompiled) {
    throw new Exception("couldnt compile shader " + toFrameBufferShader.getLog)
  }

  def postVertexShaderText = assetFile(s"post.vert.glsl").readString()
  def postFragmentShaderText = assetFile(s"post.frag.glsl").readString()
  val postShader = new ShaderProgram(postVertexShaderText,postFragmentShaderText)
  if(!postShader.isCompiled) {
    throw new Exception("couldnt compile shader " + postShader.getLog)
  }


  val tileTexture = new Texture(assetFile(s"tiles.png"))

  val mainBatch = new SpriteBatch
  val postBatch = new SpriteBatch

  // from top left
  def tileRegion(x : Int, y : Int) = new TextureRegion(tileTexture, x * Renderer.tileSizeTexture, y * Renderer.tileSizeTexture, Renderer.tileSizeTexture, Renderer.tileSizeTexture)

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

  val white = new Color(1f, 1f, 1f, 1f)
  val black = new Color(0f, 0f, 0f, 0f)
  val playerColours = Array[Color](
    new Color(144f / 255f,255f / 255f,254f / 255f,0f),
    new Color(142f / 255f,42f / 255f, 39f / 255f,0f)
  )

  val particleRegions = Array[TextureRegion](
    new TextureRegion(tileTexture, 33, 96, 16, 16), // smoke
    new TextureRegion(tileTexture, 32, 112, 16, 16), // blue stuff
    new TextureRegion(tileTexture, 48, 96, 16, 16), // red stuff
    new TextureRegion(tileTexture, 48, 112, 16, 16)  // magic
  )

  def render(world : World, delta:Double, wins:Array[Int]) {
    camera.position.set(world.width / 2 * Renderer.tileSizeScreen, world.height / 2 * Renderer.tileSizeScreen, 0)
    camera.update()

    fbo.begin()

    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    mainBatch.setProjectionMatrix(camera.combined)
    mainBatch.begin()
    mainBatch.setShader(toFrameBufferShader)

    mainBatch.enableBlending()


    renderTiles(world)

    renderLivings(world, delta)

    mainBatch.setColor(black)
    renderHands(world)
    mainBatch.setColor(black)
    renderCursors(world)
    renderParticles(world)



    font.draw(mainBatch, s"${wins(0)} wins" , 15, world.height * Renderer.tileSizeScreen - 15)
    font.draw(mainBatch, s"${wins(1)} wins", world.width * Renderer.tileSizeScreen - 60, world.height * Renderer.tileSizeScreen - 15)

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

  def renderParticles(world:World): Unit = {
    for(p <- world.particles) {
      val tr = particleRegions(p.partId)
//      println("rendering part " + p.partId)
      mainBatch.draw(tr, p.at.x, p.at.y, tr.getRegionWidth * Renderer.upScale, tr.getRegionHeight * Renderer.upScale)
    }
  }

  def renderTiles(world : World) {
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val v = Vec2i(x, y)
      val textureRegion = trForTile(world.tileAt(v), world.owned.get(v))
      mainBatch.draw(textureRegion, x * Renderer.tileSizeScreen, y * Renderer.tileSizeScreen, Renderer.tileSizeScreen, Renderer.tileSizeScreen)
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

  // simulation accu for partial tick
  def renderLivings(world : World,delta:Double) {
    def flashing(d : Double) : Boolean = (world.simulationAccu / d).asInstanceOf[Int] % 2 == 1
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

          val at = world.exactLocationOf(e)

          val smoothTime = 0.25

          val (newSmoothedPosition, newVelocity) = Spring.smooth(e.smoothedPosition, at, e.velocity, smoothTime, delta)
          e.smoothedPosition = newSmoothedPosition
          e.velocity = newVelocity


          val flash = if (e.lastStruckAt == world.tick && world.simulationAccu < 0.4) {
            flashing(0.10)
          } else {
            false
          }
          if(tr == null) {
            throw new Exception("failed to draw " + e.arch)
          }

          val colourToBind = if(flash) {
//            println("FLASH GOD DAMNIT")
            playerColours(e.playerId)
            new Color(0.4f, 0.2f, 0.2f, 0f)
          } else {
            black
          }
          mainBatch.setColor(colourToBind)

          if(e.velocity.x > 0.0) {
            mainBatch.draw(tr, e.smoothedPosition.x, e.smoothedPosition.y, tr.getRegionWidth * Renderer.upScale, tr.getRegionHeight * Renderer.upScale)
          } else {
            // public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
//              mainBatch.draw(tr, e.smoothedPosition.x, e.smoothedPosition.y, tr.getRegionWidth * upScale, tr.getRegionHeight * upScale)
            mainBatch.draw(tileTexture, e.smoothedPosition.x, e.smoothedPosition.y, tr.getRegionWidth * Renderer.upScale, tr.getRegionHeight * Renderer.upScale,
              tr.getU2, tr.getV2, tr.getU, tr.getV)
          }
        }
        slot += 1
      }
    }
  }

  def renderHands(world : World) : Unit = {
    def renderHand(player : Player, cursorTextureRegion : TextureRegion, xOffset : Int) : Unit = {
      if(player.canPlaceTiles(world)) {

        val yOffset = (if(player.placedTiles == world.placementStage && world.placementTimer == world.ticksPerPlace) {
          // player is up to date and it's the first simulation tick
          val progress = Bias.getBias(Bias.clamp(world.simulationAccu * 2 / world.simulationTickEvery), 0.75)
//          println("progress -> " + progress)
          ((1 - progress) * -Renderer.tileSizeScreen).asInstanceOf[Int]
        } else {
          0
        }) - 15



        for (t <- 0 until player.availableTiles.length) {
          val x : Int = t + xOffset
          val y =  -1

          mainBatch.draw(trForTile(player.availableTiles(t), player.id), x * Renderer.tileSizeScreen, y * Renderer.tileSizeScreen + yOffset, Renderer.tileSizeScreen, Renderer.tileSizeScreen)

          if (t == player.tile) {
            mainBatch.draw(cursorTextureRegion, x * Renderer.tileSizeScreen, y * Renderer.tileSizeScreen + yOffset, Renderer.tileSizeScreen, Renderer.tileSizeScreen)
          }
        }
      }
    }

    font.draw(mainBatch, world.placementTimer.toString, world.width * Renderer.tileSizeScreen / 2, -50)

    renderHand(world.playerA, playerACursor, 0)
    renderHand(world.playerB, playerBCursor, world.width - world.playerB.availableTiles.length)
  }

  def renderCursors(world : World) : Unit = {
    def renderCursor(player : Player, cursorTextureRegion : TextureRegion) : Unit = {
      if(player.canPlaceTiles(world)) {
        mainBatch.draw(trForTile(player.currentTile, player.id), player.cursorPosition.x * Renderer.tileSizeScreen, player.cursorPosition.y * Renderer.tileSizeScreen, Renderer.tileSizeScreen, Renderer.tileSizeScreen)
      }
      mainBatch.draw(cursorTextureRegion, player.cursorPosition.x * Renderer.tileSizeScreen, player.cursorPosition.y * Renderer.tileSizeScreen, Renderer.tileSizeScreen, Renderer.tileSizeScreen)
    }

    renderCursor(world.playerA, playerACursor)
    renderCursor(world.playerB, playerBCursor)
  }
}
