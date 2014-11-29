package hack.game

/**
 * Created by michael on 29/11/14.
 */

class Living(val id:Int,
             val playerId:Int,
             var currentLocation: Vec2i
              ) {
  var lastLocation = currentLocation

  var actionStartedAtTick:Int = 0
  var actionFinishedAtTick:Int = 0
}