package com.crashinvaders.laughemout.common.audio

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils

class MusicPlayback {

    private var currentMusic: Music? = null
    private var crossfade: MusicCrossfade? = null

    fun playMusic(music: Music, volume: Float, loop: Boolean = true, crossfadeDuration: Float = 0f, useFadeIn: Boolean = true) {
        // The track is already playing. Do nothing.
        if (currentMusic == music) {
            return
        }

        cancelCrossfade()

        // No crossfade is required.
        if (currentMusic == null || MathUtils.isZero(crossfadeDuration, CROSSFADE_ZERO_TOLERANCE)) {
            currentMusic?.stop()
            music.isLooping = loop
            music.volume = volume
            music.position = 0f
            music.play()
            currentMusic = music
            return
        }

        // Start crossfade.
        music.isLooping = loop
        music.volume = if (useFadeIn) 0f else volume
        music.position = 0f
        music.play()
        val prevMusic = currentMusic!!
        currentMusic = music
        crossfade = MusicCrossfade(prevMusic, currentMusic, volume, crossfadeDuration)
    }

    fun stopMusic(crossfadeDuration: Float = 0f) {
        if (currentMusic == null)
            return

        cancelCrossfade()

        if (MathUtils.isZero(crossfadeDuration, CROSSFADE_ZERO_TOLERANCE)) {
            currentMusic!!.stop()
            currentMusic = null
            return
        }

        crossfade = MusicCrossfade(currentMusic!!, null, 0f, crossfadeDuration)
        currentMusic = null
    }

    fun update(deltaTime: Float) {
        if (crossfade != null) {
            val isCrossFadeOver = crossfade!!.update(deltaTime)
            if (isCrossFadeOver) {
                cancelCrossfade()
            }
        }
    }

    private fun cancelCrossfade() {
        if (crossfade != null) {
            crossfade!!.musicSrc.stop()
            crossfade = null
        }
    }

    companion object {
        private const val CROSSFADE_ZERO_TOLERANCE = 0.001f
    }

    private class MusicCrossfade(
        val musicSrc: Music,
        val musicDst: Music?,
        val musicDstEndVolume: Float,
        val duration: Float,
        val interpolation: Interpolation = Interpolation.pow2
    ) {
        private var timeAccum = 0f
        private var musicSrcStartVolume = musicSrc.volume

        fun update(deltaTime: Float): Boolean {
            timeAccum += deltaTime
            val progressRaw = MathUtils.clamp(timeAccum / duration, 0f, 1f)
            val progressSmooth = interpolation.apply(progressRaw)
            musicSrc.volume = MathUtils.lerp(musicSrcStartVolume, 0f, progressSmooth)
            if (musicDst != null) {
                musicDst.volume = MathUtils.lerp(0f, musicDstEndVolume, progressSmooth)
            }

            return timeAccum > duration
        }
    }
}
