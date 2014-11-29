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


  def assetsPath = "../assets"

  val tileTexture = new Texture(Gdx.files.internal(s"$assetsPath/tiles.png"))

  val mainBatch = new SpriteBatch
  
  val tileSizeTexture = 32
  val tileSizeScreen = tileSizeTexture // 2x upscale


  // from top left
  def tileRegion(x:Int, y:Int) = new TextureRegion(tileTexture, x * tileSizeTexture, y * tileSizeTexture, tileSizeTexture, tileSizeTexture)
  val tileAtlas = new Array[TextureRegion](Tile.count)
  tileAtlas(Tile.standardGround.id) = tileRegion(0, 0)
  tileAtlas(Tile.impassableGround.id) = tileRegion(1, 0)

  def render(world:World) {
    camera.position.set(world.width / 2 * tileSizeScreen, world.height/ 2 * tileSizeScreen, 0 )
    camera.update()

    Gdx.gl.glClearColor(0, 0, 0, 1)
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
