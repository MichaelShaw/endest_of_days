package hack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.{TextureRegion, SpriteBatch}
import com.badlogic.gdx.graphics.{Texture, GL20, OrthographicCamera}
import hack.game.{Tile, World}

/**
 * Created by michael on 29/11/14.
 */
class Renderer {
  val camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
  camera.update()

  def assetsPath = "../assets"

  val tileTexture = new Texture(Gdx.files.internal(s"$assetsPath/tiles.png"))

  val mainBatch = new SpriteBatch
  
  val tileSizeTexture = 32
  val tileSizeScreen = tileSizeTexture * 2 // 2x upscale

  val tileAtlas = new Array[TextureRegion](Tile.count)
  tileAtlas(Tile.standardGround.id) = new TextureRegion(tileTexture, 0, 0, tileSizeTexture, tileSizeTexture)
  tileAtlas(Tile.impassableGround.id) = new TextureRegion(tileTexture, 16, 0, tileSizeTexture, tileSizeTexture)

  def render(world:World) {
    Gdx.gl.glClearColor(1, 1, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    mainBatch.setProjectionMatrix(camera.combined)
    mainBatch.begin()

    for {
      x <- 0 until world.width
      y <- 0 until world.height
    } {
      val tile = world.tileAt(x, y)
      val textureRegion = tileAtlas(tile.id)
      mainBatch.draw(textureRegion, x * tileSizeScreen, y * tileSizeScreen, tileSizeScreen, tileSizeScreen )
    }

    mainBatch.end()


  }

  def renderTiles(world:World) {

  }

}
