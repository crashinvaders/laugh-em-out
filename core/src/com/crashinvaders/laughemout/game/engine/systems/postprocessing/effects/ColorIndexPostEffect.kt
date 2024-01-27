package com.crashinvaders.laughemout.game.engine.systems.postprocessing.effects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.crashinvaders.common.ShaderLoader
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.FullscreenQuad
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.PingPongBuffer
import ktx.graphics.use

class ColorIndexPostEffect(
    private val transparency: TransparencyMode,
    private val palette: Texture,
    order: Int = 0
) : PostEffect(order) {
    init {
        ShaderLoader.pedantic = false
    }

    private val shader = ShaderLoader.fromFile(
        Gdx.files.internal("shaders/screenspace.vert"),
        Gdx.files.internal("shaders/color-index${transparency.fragSuffix}.frag"),
        "#define MAP_LAYERS ${palette.height}")

    private val screenQuad = FullscreenQuad()

    override fun dispose() {
        shader.dispose()
        screenQuad.dispose()
    }

    override fun configure(width: Int, height: Int) {
        super.configure(width, height)

        shader.use {
            it.setUniformi("u_texture0", 0)
            it.setUniformi("u_palette", 1)
        }
    }

    override fun render(buffers: PingPongBuffer): Boolean {
        buffers.textureSrc.bind(0)
        palette.bind(1)
        shader.bind()
        screenQuad.render(shader)
        return true
    }

    enum class TransparencyMode(val fragSuffix: String) {
        None(""),
        Normal("-alpha"),
    }
}
