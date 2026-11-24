package com.geedkajannada.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.geedkajannada.R

/** SoundPool: tick marka la tirinayo, chime marka yoolka la gaaro. */
class SoundPlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val tick = pool.load(context, R.raw.tick, 1)
    private val chime = pool.load(context, R.raw.chime, 1)

    fun playTick() { pool.play(tick, 0.7f, 0.7f, 1, 0, 1f) }
    fun playChime() { pool.play(chime, 1f, 1f, 1, 0, 1f) }
    fun release() { pool.release() }
}
