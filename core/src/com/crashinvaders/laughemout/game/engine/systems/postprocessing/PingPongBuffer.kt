package com.crashinvaders.laughemout.game.engine.systems.postprocessing

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import ktx.app.gdxError

class PingPongBuffer(
    private val bufferPool: FrameBufferPool
) {
    lateinit var buffSrc: FrameBuffer; private set
    lateinit var buffDst: FrameBuffer; private set

    val textureSrc: Texture
        get() = buffSrc.colorBufferTexture

    val textureDst: Texture
        get() = buffDst.colorBufferTexture

    private var isProcessing = false

    fun configure() {
        if (isProcessing) {
            gdxError("Is in processing state.")
        }
        buffSrc = bufferPool.obtain()
        buffDst = bufferPool.obtain()
    }

    fun swap() {
        if (isProcessing) {
            buffDst.end()
        }

        val tmp = buffSrc
        buffSrc = buffDst
        buffDst = tmp

        if (isProcessing) {
            buffDst.begin()
        }
    }

    fun begin() {
        if (isProcessing) {
            gdxError("Already in processing state.")
        }
        isProcessing = true

        buffDst.begin()
    }

    fun end() {
        if (!isProcessing) {
            gdxError("Not in processing state.")
        }
        isProcessing = false

        buffDst.end()
    }
}
