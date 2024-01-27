package com.crashinvaders.laughemout.game.engine.systems.postprocessing

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.Disposable
import ktx.app.gdxError
import ktx.collections.GdxArray

class FrameBufferPool(
    private val pixelFormat: Pixmap.Format = Pixmap.Format.RGB888,
    private val textureFilter: Texture.TextureFilter = Texture.TextureFilter.Nearest,
    private val textureWrap: Texture.TextureWrap = Texture.TextureWrap.ClampToEdge,
    private val hasDepth: Boolean = false,
): Disposable {

    private val allBuffers = GdxArray<FrameBuffer>()
    private val freeBuffers = GdxArray<FrameBuffer>()

    var width: Int = 0; private set
    var height: Int = 0; private set

    fun tryConfigure(width: Int, height: Int): Boolean {
        if (this.width == width && this.height == height) {
            return false
        }

        disposeAllBuffers()

        this.width = width
        this.height = height

        return true
    }

    fun obtain(): FrameBuffer {
        if (freeBuffers.size > 0) {
            return freeBuffers.pop()
        }

        val frameBuffer = create()
        allBuffers.add(frameBuffer)
        return frameBuffer
    }

    private fun create(): FrameBuffer {
        if (width == 0 || height == 0) {
            gdxError("Width or height is zero. Seems like the pool was never configured with tryReconfigure()")
        }

        val frameBuffer = FrameBuffer(pixelFormat, width, height, hasDepth)
        frameBuffer.colorBufferTexture.setFilter(textureFilter, textureFilter)
        frameBuffer.colorBufferTexture.setWrap(textureWrap, textureWrap)
        return frameBuffer
    }

    private fun free(frameBuffer: FrameBuffer) {
        freeBuffers.add(frameBuffer)
    }

    private fun disposeAllBuffers() {
        allBuffers.forEach {
            it.dispose()
        }
        allBuffers.clear()
        freeBuffers.clear()
    }

    override fun dispose() =
        disposeAllBuffers()
}
