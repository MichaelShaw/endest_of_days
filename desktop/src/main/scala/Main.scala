package hack

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

object Main extends App {
  val cfg = new LwjglApplicationConfiguration
  cfg.title = "hack"
  cfg.width = 1440
  cfg.height = 840
  cfg.resizable = false
  new LwjglApplication(new HackGame, cfg)
}
