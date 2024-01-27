package com.crashinvaders.laughemout.game.engine.systems.postprocessing

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.utils.SnapshotArray
import com.crashinvaders.common.ceilStep
import com.crashinvaders.common.useIndexed
import com.crashinvaders.laughemout.game.PPU
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.effects.PostEffect
import ktx.app.clearScreen
import ktx.graphics.use
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2

class PostProcessingSystem : IntervalSystem() {

    val captureBeginSubsystem = object : IntervalSystem() {
        override fun onTick() = onBeginCapture()
    }
    val captureEndSubsystem = object : IntervalSystem() {
        override fun onTick() = onEndCapture()
    }

    private val frameBufferPool = FrameBufferPool()

    private val pingPongBuffer = PingPongBuffer(frameBufferPool)

    private val effects = SnapshotArray<PostEffect>()

    private val batch = inject<PolygonSpriteBatch>()

    private val clearColor = inject<Color>("clearColor")

    private var fbPosShiftX = 0f
    private var fbPosShiftY = 0f

    private val vertices = FloatArray(5 * 4)
    private val triangles = shortArrayOf(
        0, 1, 2,
        2, 3, 0
    )

    private lateinit var mainCamera: Camera

    override fun onInit() {
        super.onInit()

        mainCamera = world.system<MainCameraStateSystem>().camera
    }

    override fun onDispose() {
        super.onDispose()

        frameBufferPool.dispose()

//        if (::frameBuffer.isInitialized) {
//            frameBuffer.dispose()
//        }
    }

    fun addEffect(postEffect: PostEffect) {
        effects.add(postEffect)
        effects.sort(effectComparator)
        postEffect.pendingConfiguration = true
    }

    fun removeEffect(postEffect: PostEffect) {
        if (!effects.removeValue(postEffect, true))
            ktx.log.error { "The effect is not registered within the system: $postEffect" }
    }

    fun rebind() {
        // Mark all effects as dirty.
        for (i in 0 until effects.size) {
            effects[i].pendingConfiguration = true
        }

        TODO("Call it and invalidate buffers (if needed).")
    }

    private fun onBeginCapture() {
        syncWithMainCam()

        pingPongBuffer.begin()
        clearScreen(clearColor.r, clearColor.g, clearColor.b, clearColor.a, clearDepth = false)
    }

    private fun onEndCapture() {
        pingPongBuffer.end()
    }

    override fun onTick() {
//        // Do we need this?
//        Gdx.gl.glDisable(GL20.GL_CULL_FACE)
//        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST)

        // Apply effects.
        if (!effects.isEmpty) {
            val fbWidth = frameBufferPool.width
            val fbHeight = frameBufferPool.height
            val deltaTime = this.deltaTime

            Gdx.gl.glViewport(0, 0, fbWidth, fbHeight)

            pingPongBuffer.swap()
            pingPongBuffer.begin()
            val effectCount = effects.size
            effects.useIndexed { effect, index ->
                if (effect.pendingConfiguration) {
                    effect.pendingConfiguration = false
                    effect.configure(fbWidth, fbHeight)
                }
                effect.update(deltaTime)
                val wasRendered = effect.render(pingPongBuffer)
                if (wasRendered && index < effectCount - 1) { // Do not swap after the last effect.
                    pingPongBuffer.swap()
                }
            }
            pingPongBuffer.end()

            // Ensure the default texture unit #0 is active.
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)
        }

        // Render the result on screen.
        //TODO Render to screen using FullscreenQuad.
        HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        batch.setColor(Color.WHITE)
        batch.use(mainCamera) {
            val (camX, camY) = mainCamera.position
            val camWidth = mainCamera.viewportWidth
            val camHeight = mainCamera.viewportHeight

            updateVertices(
                camX - camWidth * 0.5f + fbPosShiftX,
                camY - camHeight * 0.5f + fbPosShiftY,
                camWidth,
                camHeight)

            val texture = pingPongBuffer.textureDst
            batch.draw(texture, vertices, 0, vertices.size, triangles, 0, triangles.size)
        }
    }

    override fun onEnable() {
        super.onEnable()
        captureBeginSubsystem.enabled = true
        captureEndSubsystem.enabled = true
    }

    override fun onDisable() {
        super.onDisable()
        captureBeginSubsystem.enabled = false
        captureEndSubsystem.enabled = false
    }

    private fun syncWithMainCam() {
        val camera = mainCamera

        val exactWidth: Float = camera.viewportWidth * PPU
        val exactHeight: Float = camera.viewportHeight * PPU
        val fbWidth: Int = ceilStep(exactWidth, 2)
        val fbHeight: Int = ceilStep(exactHeight, 2)

        val wasReconfigured = frameBufferPool.tryConfigure(fbWidth, fbHeight)
        if (!wasReconfigured) {
            return
        }

//        this.fbPosShiftX = ((fbWidth - exactWidth) * -0.5f) / ppu
//        this.fbPosShiftY = ((fbHeight - exactHeight) * -0.5f) / ppu
        this.fbPosShiftX = 0f
        this.fbPosShiftY = 0f
//        debug { "fbPosShiftX = $fbPosShiftX fbPosShiftY = $fbPosShiftY" }
        debug { "fbWidth = $fbWidth fbHeight = $fbHeight" }

        pingPongBuffer.configure()

        // Mark all effects as dirty.
        for (i in 0 until effects.size) {
            effects[i].pendingConfiguration = true
        }
    }

    private fun updateVertices(x: Float, y: Float, width: Float, height: Float) {
        val color = Color.WHITE_FLOAT_BITS
        val fx2 = x + width
        val fy2 = y + height
        val u = 0f
        val v = 0f
        val u2 = 1f
        val v2 = 1f
        var idx = 0

        vertices[idx++] = x
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v

        vertices[idx++] = x
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = fy2
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v2

        vertices[idx++] = fx2
        vertices[idx++] = y
        vertices[idx++] = color
        vertices[idx++] = u2
        vertices[idx++] = v
    }

    companion object {
        private val effectComparator = Comparator<PostEffect> { e0, e1 -> e0.order.compareTo(e1.order) }
    }
}
