package com.crashinvaders.laughemout.game.debug.controllers

import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector3
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedInputMultiplexer
import com.crashinvaders.laughemout.game.CameraProcessorOrder
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.common.camera.Sod3CameraProcessor
import com.crashinvaders.laughemout.game.engine.TimeMode
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import ktx.app.KtxInputAdapter
import ktx.math.component1
import ktx.math.component2

class FreeCameraDebugController(fleksWorld: FleksWorld) : KtxInputAdapter, DebugController {

    private val inputMultiplexer: OrderedInputMultiplexer = fleksWorld.inject()

    private var lastScreenX = 0
    private var lastScreenY = 0

    private var pressedButton = false

    private val camSystem: MainCameraStateSystem = fleksWorld.system<MainCameraStateSystem>()
    private val camProcessor: FreeCamProcessor

    init {
        inputMultiplexer.addProcessor(this@FreeCameraDebugController, GameInputOrder.DEBUG_CONTROLLERS)

        camProcessor = FreeCamProcessor()
        camSystem.addProcessor(camProcessor)
    }

    override fun dispose() {
        inputMultiplexer.removeProcessor(this@FreeCameraDebugController)

        camSystem.removeProcessor(camProcessor)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!pressedButton && button == 0) {
            pressedButton = true
            lastScreenX = screenX
            lastScreenY = screenY
            return true
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (pressedButton) {
            val prevScreenX = lastScreenX
            val prevScreenY = lastScreenY
            lastScreenX = screenX
            lastScreenY = screenY
            moveEntity(screenX, screenY, prevScreenX, prevScreenY)
            return true
        }
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pressedButton && button == 0) {
            pressedButton = false
            return true
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.RIGHT -> camProcessor.x += 1f
            Input.Keys.LEFT -> camProcessor.x -= 1f
            Input.Keys.UP -> camProcessor.y += 1f
            Input.Keys.DOWN -> camProcessor.y -= 1f
        }
        return false
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        changeEntityScale(amountY)
        return true
    }

    private fun moveEntity(screenX: Int, screenY: Int, prevScreenX: Int, prevScreenY: Int) {
        val (x, y) = screenToWorld(screenX, screenY)
        val (prevX, prevY) = screenToWorld(prevScreenX, prevScreenY)
        val deltaX = x - prevX
        val deltaY = y - prevY
        camProcessor.x -= deltaX
        camProcessor.y -= deltaY
    }

    private fun changeEntityScale(amount: Float) {
        val scaleFactor = if (amount > 0f) SCALE_FACTOR else SCALE_FACTOR_INV
        camProcessor.relativeScale *= scaleFactor
    }

    private fun screenToWorld(screenX: Int, screenY: Int): Vector3 =
        camSystem.camera.unproject(tmpVec.set(screenX.toFloat(), screenY.toFloat(), 0f))

    companion object {
        private const val SCALE_FACTOR = 1.1f
        private const val SCALE_FACTOR_INV = 1f/ SCALE_FACTOR

        private val tmpVec = Vector3()
    }

    private class FreeCamProcessor : Sod3CameraProcessor(5f, 0.9f, 0f,
        CameraProcessorOrder.DEBUG_FREE,
        timeMode = TimeMode.UnscaledTime) {

        private var initialScale: Float = 1f
        var relativeScale: Float = 1f

        override fun onAdded(camTransform: Transform.Snapshot) {
            super.onAdded(camTransform)
            initialScale = camTransform.scaleX
        }

        override fun process(camTransform: Transform.Snapshot, deltaTime: Float) {
            this.scale = initialScale * relativeScale
            super.process(camTransform, deltaTime)
        }
    }
}
