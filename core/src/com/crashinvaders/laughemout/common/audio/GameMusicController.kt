package com.crashinvaders.laughemout.common.audio

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import com.crashinvaders.laughemout.App
import ktx.collections.GdxArray
import ktx.collections.GdxMap
import ktx.collections.contains
import ktx.collections.gdxArrayOf

class GameMusicController(assets: AssetManager) : Disposable {

    private val musicPlayback = MusicPlayback()

    private val themeStateMap = GdxMap<ThemeType, ThemeState>()
    private var currentState: ThemeState? = null
    private var scheduledState: ThemeState? = null
    private var scheduledTimeLeft: Float = 0f

    init {
        fun registerState(state: ThemeState) {
            if (themeStateMap.put(state.type, state) != null) {
                throw GdxRuntimeException("A state for the theme ${state.type} has already been added.")
            }
        }

//        val musicBarLength = 3.116f //TODO Calculate using music BPM
//
//        registerState(ThemeState(ThemeType.Menu,
//            gdxArrayOf(
//                assets.get<Music>("music/MenuLoop0.ogg"),
//            ),
//            1f, true, musicBarLength
//        ))
//        registerState(ThemeState(ThemeType.Action,
//            gdxArrayOf(
//                assets.get<Music>("music/BattleLoop0.ogg"),
//                assets.get<Music>("music/BattleLoop1.ogg"),
//            ),
//            1f, true, musicBarLength
//        ))
//        registerState(ThemeState(ThemeType.Intermission,
//            gdxArrayOf(
//                assets.get<Music>("music/IntermissionLoop0.ogg"),
//                assets.get<Music>("music/IntermissionLoop1.ogg"),
//                assets.get<Music>("music/IntermissionLoop2.ogg"),
//                assets.get<Music>("music/IntermissionLoop3.ogg"),
//                assets.get<Music>("music/IntermissionLoop4.ogg"),
//            ),
//            1f, true, musicBarLength
//        ))
//        registerState(ThemeState(ThemeType.Outro,
//            gdxArrayOf(
//                assets.get<Music>("music/Outro0.ogg"),
//                assets.get<Music>("music/Outro1.ogg"),
//                assets.get<Music>("music/Outro2.ogg"),
//                assets.get<Music>("music/Outro3.ogg"),
//            ),
//            1f, false, 0f
//        ))

        // Validate states.
        for (themeType in ThemeType.entries) {
            if (!themeStateMap.contains(themeType)) {
                throw GdxRuntimeException("Cannot find a theme state for the type: $themeType. Forgot to set up?")
            }
        }
    }

    override fun dispose() {
        musicPlayback.stopMusic(0f)
    }

    fun playMusic(theme: ThemeType, instant: Boolean = false) {
        scheduledState = null

        val state = themeStateMap.get(theme)
        if (state == currentState) {
            return
        }

        // No schedule is required.
        if (instant ||
            currentState == null ||
            !currentState!!.loop ||
            MathUtils.isZero(currentState!!.barLength)) {
            currentState = state
            playState(state, useCrossfade = false)
            return
        }

        // Schedule state change.
        val gameTime = App.Inst.gameTime
        val barTimeLeft = currentState!!.barLength - (gameTime - currentState!!.lastPlayTime) % currentState!!.barLength
        scheduledState = state
        scheduledTimeLeft = barTimeLeft
    }

    fun stopMusic(instant: Boolean = false) {
        scheduledState = null

        if (currentState == null) {
            return
        }

        val crossfadeDuration = if (instant) 0f else currentState!!.barLength
        musicPlayback.stopMusic(crossfadeDuration)
        currentState = null
    }

    fun update(deltaTime: Float) {
        if (scheduledState != null) {
            scheduledTimeLeft -= deltaTime
            if (scheduledTimeLeft <= 0f) {
                val state = scheduledState!!
                scheduledState = null
                currentState = state
                playState(state, useCrossfade = true)
            }
        }

        musicPlayback.update(deltaTime)
    }

    private fun playState(state: ThemeState, useCrossfade: Boolean) {
        val music = state.tracks.random()
        val volume = state.volume
        val loop = state.loop
        val crossfadeDuration = if (useCrossfade) 0.1f else 0f
        val useFadeIn = useCrossfade
        state.lastPlayTime = App.Inst.gameTime
        musicPlayback.playMusic(music, volume, loop, crossfadeDuration, useFadeIn)
    }

    enum class ThemeType {

    }

    private data class ThemeState(
        val type: ThemeType,
        val tracks: GdxArray<Music>,
        val volume: Float,
        val loop: Boolean,
        val barLength: Float
    ) {
        var lastPlayTime = 0f
    }
}
