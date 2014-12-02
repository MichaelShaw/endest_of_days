package hack.game

import com.badlogic.gdx.Gdx
import hack.AssetLoader

import scala.util.Random

/**
 * Created by michael on 30/11/14.
 */
class AudioRenderer extends AssetLoader {

  val shoulderButtonSound = Gdx.audio.newSound(assetFile(s"sound/select.wav"))
  val placeTileSound = Gdx.audio.newSound(assetFile(s"sound/place_tile.wav"))
  val smallHurtSound = Gdx.audio.newSound(assetFile(s"sound/small_hurt.wav"))
  val mediumHurtSound = Gdx.audio.newSound(assetFile(s"sound/medium_hurt.wav"))
  val mediumDestructionSound = Gdx.audio.newSound(assetFile(s"sound/medium_destruction.wav"))
  val finalDestructionSound = Gdx.audio.newSound(assetFile(s"sound/final_destruction.wav"))

  val rand = new Random()

  def randomPitch(f:Float) = 1f - f + (2 * f * rand.nextFloat())

  def shoulderButton() {
    shoulderButtonSound.play()
  }

  def placeTile() {
    placeTileSound.play()
  }

  def smallHurt() {
//    smallHurtSound.play(0.1f, randomPitch(0.2f), 0f)
  }

  def mediumHurt() {
    smallHurtSound.play(0.2f, randomPitch(0.2f), 0f)
  }

  def mediumDestruction() {
    mediumDestructionSound.play(1f, randomPitch(0.2f), 0.0f)
  }

  def finalDestruction() {
    finalDestructionSound.play(0.6f, 1f, 0f)
  }
}
