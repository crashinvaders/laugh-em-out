package com.crashinvaders.laughemout.game.common.camera

import com.crashinvaders.common.sod.SecondOrderDynamics2D
import com.crashinvaders.common.sod.SecondOrderDynamicsArray
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem

open class Sod2CameraProcessor(
    val f: Float,
    val z: Float,
    val r: Float,
    private val order: Int = 0,
    private val overridePrevState: Boolean = true,
): MainCameraStateSystem.CamProcessor {

    private val sod = SecondOrderDynamics2D().also {
        it.configure(f, z, r)
    }

    var x: Float = 0f
    var y: Float = 0f

    override fun getOrder(): Int = order

    override fun isOverrideState(): Boolean = overridePrevState

    override fun onAdded(camState: MainCameraStateSystem.CamState) {
        x = camState.x
        y = camState.y
        sod.moveInstant(x, y)
    }

    override fun onRemoved(camState: MainCameraStateSystem.CamState) = Unit

    override fun process(camState: MainCameraStateSystem.CamState, deltaTime: Float) {
        sod.posX = camState.x
        sod.posY = camState.y
        sod.update(deltaTime, x, y)
        camState.x = sod.posX
        camState.y = sod.posY
    }
}

open class Sod3CameraProcessor(
    f: Float,
    z: Float,
    r: Float,
    private val order: Int = 0,
    private val overridePrevState: Boolean = true,
): MainCameraStateSystem.CamProcessor {

    protected val sod = SecondOrderDynamicsArray(3).also {
        it.configure(f, z, r)
    }
    protected val sodValues = FloatArray(3)

    var x: Float = 0f
    var y: Float = 0f
    var scale: Float = 1f

    override fun getOrder(): Int = order

    override fun isOverrideState(): Boolean = overridePrevState

    override fun onAdded(camState: MainCameraStateSystem.CamState) {
        x = camState.x
        y = camState.y
        scale = camState.scale

        sodValues[0] = x
        sodValues[1] = y
        sodValues[2] = scale
        sod.moveInstant(sodValues)
    }

    override fun onRemoved(camState: MainCameraStateSystem.CamState) = Unit

    override fun process(camState: MainCameraStateSystem.CamState, deltaTime: Float) {
        sod.setPos(0, camState.x)
        sod.setPos(1, camState.y)
        sod.setPos(2, camState.scale)

        sodValues[0] = x
        sodValues[1] = y
        sodValues[2] = scale
        sod.update(deltaTime, sodValues)

        camState.x = sod.getPos(0)
        camState.y = sod.getPos(1)
        camState.scale = sod.getPos(2)
    }
}
