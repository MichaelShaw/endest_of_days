package hack

import hack.game.World

/**
 * Created by michael on 29/11/14.
 */
object GameLogic {

  def updateWorld(world:World) { // update worlds by a tick ... maybe accumulates events
    println(s"game update tick ${world.tick}")
  }
}
