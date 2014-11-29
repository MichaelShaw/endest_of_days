package hack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.{SpriteBatch, TextureRegion}
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera, Texture}
import hack.game.{Tile, Vec2i, World, Vec2f}

class Renderer {
  val camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)


  def assetsPath = "../assets"

  val tileTexture = new Texture(Gdx.files.internal(s"$assetsPath/tiles.png"))

  val mainBatch = new SpriteBatch

  val tileSizeTexture = 32
  val tileSizeScreen = tileSizeTexture // 2x upscale


  // from top left
  def tileRegion(x : Int, y : Int) = new TextureRegion(tileTexture, x * tileSizeTexture, y * tileSizeTexture, tileSizeTexture, tileSizeTexture)

  val tileAtlas = new Array[TextureRegion](Tile.count)
  tileAtlas(Tile.standardGround.id) = tileRegion(0, 0)
  tileAtlas(Tile.impassableGround.id) = tileRegion(1, 0)

  tileAtlas(Tile.soldierFactory.id) = tileRegion(2, 0)
  tileAtlas(Tile.captainFactory.id) = tileRegion(3, 0)
  tileAtlas(Tile.aeFactory.id) = tileRegion(4, 0)
  tileAtlas(Tile.defenderFactory.id) = tileRegion(5, 0)

  val ownedTileAtlas:Array[Array[TextureRegion]] = (1 to 2).map { n =>
    (0 to 5).map { t =>
      tileRegion(t, n)
    }.toArray
  }.toArray

  // 2 players



  val playerA = tileRegion(2, 1)

  val playerAGuys = Array(
    new TextureRegion(tileTexture, 224, 32, 16, 16),
    new TextureRegion(tileTexture, 240, 32, 16, 16),
    new TextureRegion(tileTexture, 224, 48, 16, 16),
    new TextureRegion(tileTexture, 240, 48, 16, 16)
  )

  // soldier, captain, ae, defender
  val playerB = tileRegion(2, 2)
  val playerBGuys = Array(
    new TextureRegion(tileTexture, 224, 64, 16, 16),
    new TextureRegion(tileTexture, 240, 64, 16, 16),
    new TextureRegion(tileTexture, 224, 80, 16, 16),
    new TextureRegion(tileTexture, 240, 80, 16, 16)
  )

  def render(world : World, simulationAccu : Double) {
    camera.position.set(world.width / 2 * tileSizeScreen, world.height / 2 * tileSizeScreen, 0)
    camera.update()

    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    mainBatch.setProjectionMatrix(camera.combined)
    mainBatch.begin()

    renderTiles(world)
    renderLivings(world, simulationAccu)
    renderPlayers(world)

    mainBatch.end()
  }

  def renderTiles(world : World) {
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val tile = world.tileAt(x, y)
      val owned = world.owned.get(x, y)
      val textureRegion : TextureRegion = if(owned >= 0) {
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

  def screenLocation(loc:Vec2i, slot:Int) : Vec2i = {
    (loc * tileSizeScreen) + innerTileLocations(slot)
  }

  // simulation accu for partial tick
  def renderLivings(world : World, simulationAccu : Double) {
    def flashing(d:Double) : Boolean = (simulationAccu / d).asInstanceOf[Int] % 2 == 1
    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val entities = world.livingsAt(x, y)
      var slot = 0; while (slot < world.slotsPerTile) {
        val e = entities(slot)
        if (e != null) {
          val tr : TextureRegion = if (e.playerId == 0) {
            playerAGuys(e.arch.id)
          } else {
            playerBGuys(e.arch.id)
          }
          val lastLocation = screenLocation(e.lastLocation, e.lastSlot)
          val currentLocation = screenLocation(e.currentLocation, e.currentSlot)

          val at = Vec2f.lerp(lastLocation, currentLocation, simulationAccu)

          val draw = if(e.lastStruckAt == world.tick - 1 && simulationAccu < 0.4) {
            flashing(0.10)
          } else {
            true
          }
          if(draw) {
            mainBatch.draw(tr, at.x , at.y, 16, 16)
          }

        }
        slot += 1
      }
    }
  }

  def renderPlayers(world : World) : Unit = {
    mainBatch.draw(playerA, world.playerA.x * tileSizeScreen, world.playerA.y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
    mainBatch.draw(playerB, world.playerB.x * tileSizeScreen, world.playerB.y * tileSizeScreen, tileSizeScreen, tileSizeScreen)
  }
}
