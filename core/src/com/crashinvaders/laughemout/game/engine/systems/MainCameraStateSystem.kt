package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.Viewport
import com.crashinvaders.common.CommonUtils
import com.crashinvaders.common.TimeManager
import com.crashinvaders.common.events.Event
import com.crashinvaders.common.events.EventBus
import com.crashinvaders.common.events.EventHandler
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.crashinvaders.laughemout.game.engine.OnResizeEvent
import com.crashinvaders.laughemout.game.engine.TimeMode
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.WorldCamera
import com.crashinvaders.laughemout.game.engine.components.WorldCameraTag
import ktx.collections.GdxArray

class MainCameraStateSystem(
    private val referenceViewport: Viewport
) : IntervalSystem(), EventHandler {

    private val processors = GdxArray<CamProcessor>()
    private val processorsToAdd = GdxArray<CamProcessor>()
    private val processorsToRemove = GdxArray<CamProcessor>()

    val camera = OrthographicCamera()

    private val timeManager = world.inject<TimeManager>()

    lateinit var cameraEntity: Entity; private set
    lateinit var cameraComp: WorldCamera; private set
    lateinit var cameraTransform: Transform; private set

    private var lastOverridingControllerIndex: Int = 0

    private var isDirty = false
    private var isProcessing = false

    override fun onInit() {
        super.onInit()

        referenceViewport.update(Gdx.graphics.width, Gdx.graphics.height)
        val ppu = evalPpu(referenceViewport)

        cameraEntity = world.entity {
            it += Info("MainCamera")
            it += WorldCamera(camera = camera, ppu = ppu)
            it += WorldCameraTag.MAIN
            it += Transform()

            cameraComp = it[WorldCamera]
            cameraTransform = it[Transform]
        }

        world.inject<EventBus>().addHandler<OnResizeEvent>(this)
    }

    override fun onDispose() {
        super.onDispose()

        world.inject<EventBus>().removeHandlers(this)
    }

    fun addProcessor(processor: CamProcessor) {
        if (isProcessing) {
            processorsToAdd.add(processor)
            return
        }
        processors.add(processor)
        isDirty = true

        sharedCamTransform.readWorldFrom(cameraTransform)
        processor.onAdded(sharedCamTransform)
    }

    fun removeProcessor(processor: CamProcessor) {
        if (isProcessing) {
            processorsToRemove.add(processor)
            return
        }
        if (!processors.removeValue(processor, true))
            throw GdxRuntimeException("Processor is not registered: ${processor::class.simpleName}")

        isDirty = true

        sharedCamTransform.readWorldFrom(cameraTransform)
        processor.onRemoved(sharedCamTransform)
    }

    override fun onTick() {
        if (processors.size == 0) {
            return
        }

        if (isDirty) {
            isDirty = false

            processors.sort(processorComparator)

            // Find the last controller that overrides the camera state.
            lastOverridingControllerIndex = 0
            for (i in processors.size - 1 downTo 0) {
                val processor = processors.get(i)
                if (processor.isOverrideState()) {
                    lastOverridingControllerIndex = i
                    break
                }
            }
        }

        isProcessing = true

        val deltaGame = timeManager.delta
        val deltaUnscaled = timeManager.deltaUnscaled

        val camState = MainCameraStateSystem.sharedCamTransform
        sharedCamTransform.readWorldFrom(cameraTransform)

        for (i in lastOverridingControllerIndex until processors.size) {
            val camProcessor = processors[i]
            val deltaTime = when(camProcessor.getTimeMode()) {
                TimeMode.GameTime -> deltaGame
                TimeMode.UnscaledTime -> deltaUnscaled
            }
            camProcessor.process(camState, deltaTime)
        }

        camState.writeWorldTo(cameraTransform)

        isProcessing = false

        while (processorsToRemove.size > 0) {
            val index = processorsToRemove.size - 1
            val processor = processorsToRemove.removeIndex(index)
            if (!processors.removeValue(processor, true))
                throw GdxRuntimeException("Processor is not registered: ${processor::class.simpleName}")
            processor.onRemoved(camState)
            isDirty = true
        }

        while (processorsToAdd.size > 0) {
            val index = processorsToAdd.size - 1
            val processor = processorsToAdd.removeIndex(index)
            processors.add(processor)
            processor.onAdded(camState)
            isDirty = true
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is OnResizeEvent -> {
                referenceViewport.update(event.screenWidth, event.screenHeight)
                cameraComp.ppu = evalPpu(referenceViewport)
            }
        }
    }

    fun screenToWorld(screenX: Int, screenY: Int): Vector3 =
        camera.unproject(tmpVec.set(screenX.toFloat(), screenY.toFloat(), 0f))

    companion object {
        private val tmpVec = Vector3()
        private val sharedCamTransform = Transform.Snapshot()

        private val processorComparator = Comparator<CamProcessor> { p0, p1 ->
            CommonUtils.compare(p0.getOrder(), p1.getOrder()) }

        private fun evalPpu(viewport: Viewport): Float =
            viewport.screenWidth.toFloat() / viewport.worldWidth
    }

//    data class CamState(
//        var x: Float = 0f,
//        var y: Float = 0f,
//        var scale: Float = 0f,
//    ) {
//        fun readTransform(transform: Transform) {
//            x = transform.worldPositionX
//            y = transform.worldPositionY
//            scale = transform.worldScaleX
//        }
//
//        fun writeTransform(transform: Transform) {
//            transform.setWorldPosition(x, y)
//            transform.setWorldScale(scale, scale)
//        }
//    }

    interface CamProcessor {
        fun getOrder(): Int
        fun isOverrideState(): Boolean
        fun getTimeMode(): TimeMode
        fun onAdded(camTransform: Transform.Snapshot)
        fun onRemoved(camTransform: Transform.Snapshot)
        fun process(camTransform: Transform.Snapshot, deltaTime: Float)
    }
}
