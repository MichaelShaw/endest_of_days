package hack.game

import com.badlogic.gdx.Gdx

import scala.util.Random

/**
 * Created by michael on 30/11/14.
 */
class AudioRenderer {
  def assetsPath = "../assets/sound"

  val shoulderButtonSound = Gdx.audio.newSound(Gdx.files.internal(s"$assetsPath/select.wav"))
  val placeTileSound = Gdx.audio.newSound(Gdx.files.internal(s"$assetsPath/place_tile.wav"))
  val smallHurtSound = Gdx.audio.newSound(Gdx.files.internal(s"$assetsPath/small_hurt.wav"))
  val mediumHurtSound = Gdx.audio.newSound(Gdx.files.internal(s"$assetsPath/medium_hurt.wav"))
  val mediumDestructionSound = Gdx.audio.newSound(Gdx.files.internal(s"$assetsPath/medium_destruction.wav"))
  val finalDestructionSound = Gdx.audio.newSound(Gdx.files.internal(s"$assetsPath/final_destruction.wav"))

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
    smallHurtSound.play(randomPitch(0.2f), randomPitch(0.2f), 0f)
  }

  def mediumDestruction() {
    mediumDestructionSound.play(1f, randomPitch(0.2f), 0.0f)
  }

  def finalDestruction() {
    finalDestructionSound.play(0.6f, 1f, 0f)
  }
}
