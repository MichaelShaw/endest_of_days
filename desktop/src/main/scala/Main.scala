package hack

import com.badlogic.gdx.backends.lwjgl._

object Main extends App {
    val cfg = new LwjglApplicationConfiguration
    cfg.title = "hack"
    cfg.height = 800
    cfg.width = 1200
    cfg.forceExit = false
    new LwjglApplication(new HackGame, cfg)
}
