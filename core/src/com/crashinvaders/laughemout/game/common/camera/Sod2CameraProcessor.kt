package com.crashinvaders.laughemout.game.common.camera

import com.crashinvaders.common.sod.SecondOrderDynamics2D
import com.crashinvaders.common.sod.SecondOrderDynamicsArray
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem

open class Sod2CameraProcessor(
    val f: Float,
    val z: Float,
    val r: Float,
    private val order: Int = 0,
    private val overridePrevState: Boolean = true,
    private val timeMode: MainCameraStateSystem.TimeMode = MainCameraStateSystem.TimeMode.GameTime,
    private val readCamValuesWhenAdded: Boolean = true,
): MainCameraStateSystem.CamProcessor {

    private val sod = SecondOrderDynamics2D().also {
        it.configure(f, z, r)
    }

    var x: Float = 0f
    var y: Float = 0f

    var isDisabled = false

    override fun getOrder(): Int = order

    override fun isOverrideState(): Boolean = overridePrevState

    override fun getTimeMode(): MainCameraStateSystem.TimeMode = timeMode

    override fun onAdded(camTransform: Transform.Snapshot) {
        if (readCamValuesWhenAdded) {
            x = camTransform.positionX
            y = camTransform.positionY
        }
        sod.moveInstant(x, y)
    }

    override fun onRemoved(camTransform: Transform.Snapshot) = Unit

    override fun process(camTransform: Transform.Snapshot, deltaTime: Float) {
        if (isDisabled) {
            return
        }
        sod.posX = camTransform.positionX
        sod.posY = camTransform.positionY
        sod.update(deltaTime, x, y)
        camTransform.positionX = sod.posX
        camTransform.positionY = sod.posY
    }
}

open class Sod3CameraProcessor(
    f: Float,
    z: Float,
    r: Float,
    private val order: Int = 0,
    private val overridePrevState: Boolean = true,
    private val timeMode: MainCameraStateSystem.TimeMode = MainCameraStateSystem.TimeMode.GameTime,
    private val readCamValuesWhenAdded: Boolean = true
): MainCameraStateSystem.CamProcessor {

    protected val sod = SecondOrderDynamicsArray(3).also {
        it.configure(f, z, r)
    }
    protected val sodValues = FloatArray(3)

    var x: Float = 0f
    var y: Float = 0f
    var scale: Float = 1f

    var isDisabled = false

    override fun getOrder(): Int = order

    override fun isOverrideState(): Boolean = overridePrevState

    override fun getTimeMode(): MainCameraStateSystem.TimeMode = timeMode

    override fun onAdded(camTransform: Transform.Snapshot) {
        if (readCamValuesWhenAdded) {
            x = camTransform.positionX
            y = camTransform.positionY
            scale = camTransform.scaleX
        }

        sodValues[0] = x
        sodValues[1] = y
        sodValues[2] = scale
        sod.moveInstant(sodValues)
    }

    override fun onRemoved(camTransform: Transform.Snapshot) = Unit

    override fun process(camTransform: Transform.Snapshot, deltaTime: Float) {
        if (isDisabled) {
            return
        }

        sod.setPos(0, camTransform.positionX)
        sod.setPos(1, camTransform.positionY)
        sod.setPos(2, camTransform.scaleX)

        sodValues[0] = x
        sodValues[1] = y
        sodValues[2] = scale
        sod.update(deltaTime, sodValues)

        camTransform.positionX = sod.getPos(0)
        camTransform.positionY = sod.getPos(1)
        camTransform.scaleX = sod.getPos(2)
        camTransform.scaleY = sod.getPos(2)
    }
}
