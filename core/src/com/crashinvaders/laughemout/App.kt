package com.crashinvaders.laughemout

import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.GdxRuntimeException
import com.crashinvaders.common.OrderedInputMultiplexer
import com.github.tommyettinger.textra.KnownFonts
import com.crashinvaders.laughemout.common.audio.GameMusicController
import com.crashinvaders.laughemout.game.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.assets.load
import ktx.collections.GdxArray

class App(val params: Params) : KtxGame<KtxScreen>() {

    val inputMultiplexer = OrderedInputMultiplexer()

    /** Time since the game starts. */
    var gameTime: Float = 0f; private set

    lateinit var assets: AssetManager; private set

    lateinit var gameMusic: GameMusicController; private set

    val isDebug: Boolean get() = params.isDebug

    override fun create() {
        if (params.isDebug) {
            Gdx.app.logLevel = Application.LOG_DEBUG
        }
        Gdx.app.input.inputProcessor = inputMultiplexer

        if (params.isDebug) {
            inputMultiplexer.addProcessor(createDebugInputProcessor(), Int.MIN_VALUE)
        }

        KnownFonts.setAssetPrefix("textratypist/")

        //TODO Turn this to a loader screen.
        assets = AssetManager()
        loadAssets(assets)

        gameMusic = GameMusicController(assets)

        App.instance = this

        addScreen(GameScreen())
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        inputMultiplexer.clear()
        screens.clear()

        gameMusic.dispose()
        assets.dispose()

        App.instance = null
    }

    override fun render() {
        val deltaTime = Gdx.graphics.deltaTime

        gameTime += deltaTime

        gameMusic.update(deltaTime)

        super.render()
    }

    override fun <Type : KtxScreen> setScreen(type: Class<Type>) {
        //AC: Same as base.setScreen(), but the resize() moved after the screen.show() call.
        currentScreen.hide()
        currentScreen = getScreen(type)
        currentScreen.show()
        currentScreen.resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    private fun createDebugInputProcessor(): InputProcessor = object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            when (keycode) {
                Input.Keys.F8 -> {
                    // Restart the game.
                    Gdx.app.postRunnable {
                        dispose()
                        create()
                    }
                }
            }
            return false
        }
    }

    private fun loadAssets(assets: AssetManager) {
        assets.load<BitmapFont>("fonts/pixola-cursiva.fnt")
        assets.load<TextureAtlas>("atlases/ui.atlas")

        assets.finishLoading()

        // Configure all the fonts.
        assets.getAll(BitmapFont::class.java, GdxArray()).forEach {
            val data = it.data

            // Set up missing glyph.
            val missingGlyph = data.getGlyph('\uFFFD')
            if (missingGlyph != null) {
                data.missingGlyph = missingGlyph
            }

            // Enable markup for all the fonts.
            data.markupEnabled = true
        }
    }

    companion object {
        private var instance: App? = null
        public val Inst: App get() {
            return instance ?: throw GdxRuntimeException("App is not initialized yet.")
        }
    }

    data class Params(
        var isDebug: Boolean = false
    )
}
