package com.crashinvaders.laughemout.game.debug.controllers.tests

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedInputMultiplexer
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.debug.controllers.DebugController
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import ktx.actors.onTouchDown
import ktx.app.KtxInputAdapter
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2

class TransformHierarchyTest(private val fleksWorld: FleksWorld) : KtxInputAdapter, DebugController {

    private val camera: Camera = fleksWorld.system<MainCameraStateSystem>().camera

    private val rootEntity: Entity
    private val childEntity: Entity
    private val subChildEntity: Entity

//    private val textureDosTest0 = Texture(Gdx.files.internal("textures/dos-power-stance0.png"))
    private val textureDosTest0 = Texture(Gdx.files.internal("textures/dos-bald0.png"))

    private val inputMultiplexer: OrderedInputMultiplexer = fleksWorld.inject()

    private var currentTransform: Transform? = null

    private var lastScreenX = 0
    private var lastScreenY = 0

    init {
        inputMultiplexer.addProcessor(this@TransformHierarchyTest)

        val (cursorX, cursorY) = screenToWorld(Gdx.input.x, Gdx.input.y)

        with(fleksWorld) {
            rootEntity = entity {entity ->
                entity += Info("Root0")
                entity += Transform().apply {
                    localPositionX = cursorX
                    localPositionY = cursorY
                }

                entity += ActorContainer(Image(textureDosTest0).apply {
                    touchable = Touchable.enabled
                    onTouchDown {
                        debug { "Pew pew" }
                    }
                })
                entity += DrawableOrder()
                entity += DrawableTint().apply {
//                    color.set(0x00ff00ffu.toInt())
                }
                entity += DrawableVisibility()
                entity += DrawableDimensions(2f)
                entity += DrawableOrigin(Align.bottom)

                entity += SodInterpolation(4f, 0.4f, -0.5f).apply {
                    setAccel(0f, 0f,
                        MathUtils.random(-90f, +90f),
                        50f, -20f)
                }
                entity += TransformDebugRenderTag
            }
            childEntity = entity {entity ->
                entity += Info("Child0")
                entity += Transform().apply {
                    parent = rootEntity[Transform]
                    localPositionX = 2f
                    localPositionY = 2f
                    localRotation = 45f
                }

                entity += ActorContainer(Image(textureDosTest0))
                entity += DrawableOrder()
                entity += DrawableTint()
                entity += DrawableVisibility()
                entity += DrawableDimensions(1f)
                entity += DrawableOrigin(Align.center)

                entity += SodInterpolation(4f, 0.4f, -0.5f).apply {
                    setAccel(0f, 0f,
                        MathUtils.random(-90f, +90f),
                        50f, -20f)
                }
                entity += TransformDebugRenderTag
            }
            subChildEntity = entity { entity ->
                entity += Info("SubChild0")
                entity += Transform().apply {
                    parent = childEntity[Transform]
                    localPositionX = 1f
                    localPositionY = 1f
                    localRotation = 45f
                }

                entity += ActorContainer(Image(textureDosTest0))
                entity += DrawableOrder()
                entity += DrawableTint()
                entity += DrawableVisibility()
                entity += DrawableDimensions(1f)
                entity += DrawableOrigin(Align.topRight)

                entity += SodInterpolation(4f, 0.4f, -0.5f).apply {
                    setAccel(0f, 0f,
                        MathUtils.random(-90f, +90f),
                        50f, -20f)
                }
                entity += TransformDebugRenderTag
            }

//            for (i in 0 until 1024*8) {
//                entity { entity ->
//                    entity += Info("SuperSubChild$i")
//                    entity += Transform().apply {
//                        parent = childEntity[Transform]
//                        localPositionX = MathUtils.random(-8f, +8f)
//                        localPositionY = MathUtils.random(-8f, +8f)
//                        localRotation = MathUtils.random(360f)
//                    }
//                    entity += ActorContainer(Image(textureDosTest0))
//                    entity += DrawableOrder()
//                    entity += DrawableTint()
//                    entity += DrawableVisibility()
//                    entity += DrawableDimensions()
//                    entity += DrawableOrigin(Align.center)
//
//                    entity += SodInterpolation(MathUtils.random(0.5f, 4f), MathUtils.random(0.2f, 1f), MathUtils.random(-2f, 2f))
//                }
//            }
        }
    }

    override fun dispose() {
        fleksWorld -= rootEntity
        fleksWorld -= childEntity
        fleksWorld -= subChildEntity

        textureDosTest0.dispose()

        inputMultiplexer.removeProcessor(this@TransformHierarchyTest)
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.SHIFT_LEFT -> {
                currentTransform = with(fleksWorld) { rootEntity[Transform] }
                return true
            }
            Input.Keys.ALT_LEFT -> {
                currentTransform = with(fleksWorld) { childEntity[Transform] }
                return true
            }
            Input.Keys.CONTROL_LEFT -> {
                currentTransform = with(fleksWorld) { subChildEntity[Transform] }
                return true
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.SHIFT_LEFT, Input.Keys.ALT_LEFT, Input.Keys.CONTROL_LEFT -> {
                currentTransform = null
                return true
            }
        }
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (currentTransform != null) {
            lastScreenX = screenX
            lastScreenY = screenY
            return true
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (currentTransform != null) {
            val prevScreenX = lastScreenX
            val prevScreenY = lastScreenY
            lastScreenX = screenX
            lastScreenY = screenY
            return handleInteraction(currentTransform!!, screenX, screenY, prevScreenX, prevScreenY)
        }
        return false
    }

    private fun handleInteraction(
        transform: Transform,
        screenX: Int,
        screenY: Int,
        prevScreenX: Int,
        prevScreenY: Int
    ): Boolean {
        when {
            Gdx.input.isButtonPressed(Input.Buttons.LEFT) -> {
                moveEntityBy(transform, screenX, screenY, prevScreenX, prevScreenY)
//                moveEntityTo(transform, screenX, screenY, prevScreenX, prevScreenY)
                return true
            }

            Gdx.input.isButtonPressed(Input.Buttons.RIGHT) -> {
                rotateEntity(transform, screenX, screenY, prevScreenX, prevScreenY)
                return true
            }

            Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) -> {
                scaleEntity(transform, screenX, screenY, prevScreenX, prevScreenY)
                return true
            }
        }
        return false
    }

    private fun moveEntityBy(transform: Transform, screenX: Int, screenY: Int, prevScreenX: Int, prevScreenY: Int) {
        val (x, y) = screenToWorld(screenX, screenY)
        val (prevX, prevY) = screenToWorld(prevScreenX, prevScreenY)
        val deltaX = x - prevX
        val deltaY = y - prevY
        val (entityPosX, entityPosY) = transform.worldPosition
        transform.setWorldPosition(entityPosX + deltaX, entityPosY + deltaY)
    }

    private fun moveEntityTo(transform: Transform, screenX: Int, screenY: Int, prevScreenX: Int, prevScreenY: Int) {
        val (x, y) = screenToWorld(screenX, screenY)
        transform.setWorldPosition(x, y)
    }

    private fun rotateEntity(transform: Transform, screenX: Int, screenY: Int, prevScreenX: Int, prevScreenY: Int) {
        val (x, y) = screenToWorld(screenX, screenY)
        val (prevX, prevY) = screenToWorld(prevScreenX, prevScreenY)
        val degreeDelta = (x - prevX) * 15f
        transform.localRotation += degreeDelta
    }

    private fun scaleEntity(transform: Transform, screenX: Int, screenY: Int, prevScreenX: Int, prevScreenY: Int) {
        val (x, y) = screenToWorld(screenX, screenY)
        val (prevX, prevY) = screenToWorld(prevScreenX, prevScreenY)
        val scaleDeltaX = (x - prevX) * 1f
        val scaleDeltaY = (y - prevY) * 1f
        transform.localScaleX += scaleDeltaX
        transform.localScaleY += scaleDeltaY
    }

    private fun screenToWorld(screenX: Int, screenY: Int): Vector3 =
        camera.unproject(tmpVec.set(screenX.toFloat(), screenY.toFloat(), 0f))

    companion object {
        private val tmpVec = Vector3()
    }
}
