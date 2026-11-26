package com.geedkajannada.feature.tasbeeh.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.geedkajannada.R
import com.geedkajannada.feature.tasbeeh.TasbeehSound

/** SoundPool wrapper: dhammaan dhawaaqyada waa la sii raraa si aanay u daahin. Mute-ka ViewModel-ka ayaa maamula. */
class TasbeehSoundPlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids: Map<TasbeehSound, Int> = mapOf(
        TasbeehSound.TAP to R.raw.tap_wood,
        TasbeehSound.BEAD to R.raw.bead_glass,
        TasbeehSound.CHIME to R.raw.chime_33,
        TasbeehSound.NEXT to R.raw.bell_next,
        TasbeehSound.COMPLETE to R.raw.complete,
    ).mapValues { pool.load(context, it.value, 1) }

    fun play(sound: TasbeehSound) {
        val volume = if (sound == TasbeehSound.TAP || sound == TasbeehSound.BEAD) 0.7f else 1f
        pool.play(ids.getValue(sound), volume, volume, 1, 0, 1f)
    }

    fun release() = pool.release()
}
