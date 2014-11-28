package hack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}

/**
 * Created by michael on 29/11/14.
 */
class Renderer {
  val camera = new OrthographicCamera(Gdx.graphics.getWidth, Gdx.graphics.getHeight)

  def assetsPath = "../assets"

  def render(world:World) {
    Gdx.gl.glClearColor(1, 1, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)


  }

}
