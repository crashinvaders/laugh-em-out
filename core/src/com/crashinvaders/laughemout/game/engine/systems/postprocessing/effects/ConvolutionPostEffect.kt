package com.crashinvaders.laughemout.game.engine.systems.postprocessing.effects

import com.badlogic.gdx.Gdx
import com.crashinvaders.common.ShaderLoader
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.FullscreenQuad
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.PingPongBuffer
import ktx.graphics.use

class ConvolutionPostEffect(
    private val convMatrix: FloatArray,
    private val matrixSide: Int,
    order: Int = 0
) : PostEffect(order) {

    private val shader = ShaderLoader.fromFile(
            Gdx.files.internal("shaders/convolution.vert"),
            Gdx.files.internal("shaders/convolution.frag"),
            "#define MAT_SIDE $matrixSide")

    private val screenQuad = FullscreenQuad()

    override fun dispose() {
        shader.dispose()
        screenQuad.dispose()
    }

    override fun configure(width: Int, height: Int) {
        super.configure(width, height)

        val texelWidth = 1f / width
        val texelHeight = 1f / height

        shader.use {
            it.setUniformi("u_texture0", TEXTURE_INDEX0)
            it.setUniformf("u_texelSize", texelWidth, texelHeight)
//            it.setUniformMatrix("u_convMat", convMatrix)
            it.setUniform1fv("u_convMat", convMatrix, 0, matrixSide*matrixSide)
        }
    }

    override fun render(buffers: PingPongBuffer): Boolean {
        buffers.textureSrc.bind(TEXTURE_INDEX0)
        shader.bind()
        screenQuad.render(shader)
        return true
    }

    companion object {
        @JvmStatic val CONV3_IDENTITY = floatArrayOf(
            0f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 0f)

        @JvmStatic val CONV3_GLOW = floatArrayOf(
            +0.25f, +0.25f, +0.25f,
            +0.25f, +0.25f, +0.25f,
            +0.25f, +0.25f, +0.25f)

        @JvmStatic val CONV3_BLUR = floatArrayOf(
            +0.05f, +0.20f, +0.05f,
            +0.20f, +0.00f, +0.20f,
            +0.05f, +0.20f, +0.05f)

        @JvmStatic val CONV3_DRUNK = normMatrix(floatArrayOf(
            +3f, +0f, +3f,
            +0f, -2f, +0f,
            +3f, +0f, +3f))

        @JvmStatic val CONV3_PIXEL_BLEED = floatArrayOf(
            +0.0f, -0.1f, +0.0f,
            -0.3f, +1.0f, +0.3f,
            +0.0f, +0.1f, +0.0f)

        @JvmStatic val CONV5_BLUR_ROUGH = normMatrix(floatArrayOf(
            0f, 1f, 2f, 1f, 0f,
            1f, 4f, 8f, 4f, 1f,
            2f, 8f, 0f, 8f, 2f,
            1f, 4f, 8f, 4f, 1f,
            0f, 1f, 2f, 1f, 0f))

        @JvmStatic val CONV5_BLUR_SMOOTH = normMatrix(floatArrayOf(
            1f, 2f, 3f, 2f, 1f,
            2f, 3f, 4f, 3f, 2f,
            3f, 4f, 5f, 4f, 3f,
            2f, 3f, 4f, 3f, 2f,
            1f, 2f, 3f, 2f, 1f))

        @JvmStatic fun conv3Sharpen(strength: Float): FloatArray = floatArrayOf(
            +0f, -strength, +0f,
            -strength, +1f + strength * 4f, -strength,
            +0f, -strength, +0f)

        @JvmStatic fun normMatrix(values: FloatArray, normSum: Float = 1f): FloatArray {
            var sum = 0f
            for (i in 0 until values.size) {
                sum += values[i]
            }
            val mul = normSum / sum
            for (i in 0 until values.size) {
                values[i] *= mul
            }
            return values
        }
    }
}

