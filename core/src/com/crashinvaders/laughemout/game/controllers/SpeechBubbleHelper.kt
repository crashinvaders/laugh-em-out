package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActorPixels
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem.Companion.actions
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.*
import com.github.quillraven.fleks.Entity
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TypingLabel
import ktx.app.gdxError

object SpeechBubbleHelper {

    private var cloudId = 0

//    fun createSpeechBubble(
//        world: FleksWorld,
//        message: String,
//        x: Float,
//        y: Float,
//        size: SpeechBubbleSize,
//        duration: Float = -1f
//    ): Entity {
//        val font = world.inject<Font>("pixolaCurva")
//        val atlas = world.inject<TextureAtlas>("ui")
//
//        lateinit var transform: Transform
//
//        val entity = world.entity {
//            it += Info("SpeechCloud")
//            it += Transform().apply {
//                localPositionX = x
//                localPositionY = y
//            }
//
//            val actor: Actor = let {
//                val label = TypingLabel("[#544470]$message", font)
//
//                val container = Container(label)
//                container.background = TextureRegionDrawable(atlas.findRegion("speech-bubble-frame${size.imgSuffix}"))
//                container.top()
//                container.padTop(6f)
//
//                TransformActorWrapper(container)
//            }
//
//            it += ActorContainer(actor)
//            it += DrawableRenderer(ActorEntityRenderer)
//            it += DrawableOrder(GameDrawOrder.UI_SPEECH_BUBBLE)
//            it += DrawableTint()
//            it += DrawableVisibility()
//            it += DrawableDimensions().fromActorPixels(actor)
//            it += DrawableOrigin(Align.bottom)
//
//            it += SodInterpolation(6f, 0.6f, -0.5f).apply {
//                kickVisually()
//            }
//            it += TransformDebugRenderTag
//
//            transform = it[Transform]
//        }
//
//        if (duration > 0f) {
//            world.system<EntityActionSystem>().actions(entity) {
//                sequence {
//                    delay(duration)
//                    runnable {
//                        transform.localScaleX = 0f
//                        transform.localScaleY = 0f
//                    }
//                    delay(0.5f)
//                    removeEntity()
//                }
//            }
////            (
////                entity, SequenceAction(
////                    DelayAction(duration),
////                    RunnableAction {
////                        transform.localScaleX = 0f
////                        transform.localScaleY = 0f
////                    },
////                    DelayAction(0.5f),
////                    RunnableAction { Gdx.app.postRunnable { if (entity in world) world -= entity } },
////                )
////            )
//        }
//
//        return entity
//    }

    fun destroyBubble(world: FleksWorld, eBubble: Entity, animate: Boolean = true) {
        if (!animate) {
            world -= eBubble
            return
        }

        with(world) {
            val cTransform = eBubble[Transform]
            cTransform.localScaleX = 0f
            cTransform.localScaleY = 0f

            actions(eBubble) {
                sequence {
                    sodAccel(0f, 20f, 0f, 2f, 2f)
                    tintFadeOut(0.2f)
                    removeEntity()
                }
            }
        }
    }


    fun createBubble(
        world: FleksWorld,
        message: String,
        x: Float,
        y: Float,
        duration: Float = -1f,
        side: Side = Side.Auto
    ): Entity {
        val font = world.inject<Font>("pixolaCurva")
        val atlas = world.inject<TextureAtlas>("ui")

        val actualSide: Side = if (side != Side.Auto) {
            side
        } else {
            val mainCamSystem = world.system<MainCameraStateSystem>()
            val camX = mainCamSystem.cameraTransform.worldPositionX
            if (x > camX) Side.Right else Side.Left
        }

        val entity = world.entity { entity ->
            entity += Info("SpeechCloud${cloudId++}")
            entity += Transform().apply {
                localPositionX = x + (if (actualSide == Side.Left) -18f * UPP else +18f * UPP)
                localPositionY = y
            }

            val actor: Actor = let {
                val label = TypingLabel("[#544470]$message", font)

                val container = Container(label)
                container.background = NinePatchDrawable(atlas.createPatch("speech-bubble-frame${actualSide.imgSuffix}"))
                container.center()
                container.padLeft(12f)
                container.padRight(12f)
                container.padTop(5f)
                container.padBottom(15f)

                TransformActorWrapper(container)
            }

            entity += ActorContainer(actor)
            entity += DrawableRenderer(ActorEntityRenderer)
            entity += DrawableOrder(GameDrawOrder.UI_SPEECH_BUBBLE)
            entity += DrawableTint()
            entity += DrawableVisibility()
            entity += DrawableDimensions().fromActorPixels(actor)
            entity += DrawableOrigin(when(actualSide) {
                Side.Left -> Align.bottomLeft
                Side.Right -> Align.bottomRight
                else -> gdxError("Unexpected side value: $actualSide")
            })

            entity += SodInterpolation(6f, 0.6f, -0.5f).apply {
//                kickVisually(scale = false)
                setAccel(0f, 20f, 0f, 2f, 2f)
            }
            entity += TransformDebugRenderTag

            if (duration > 0f) {
                world.actions(entity) {
                    sequence {
                        delay(duration)
                        sodAccel(0f, 20f, 0f, 2f, 2f)
                        tintFadeOut(0.2f)
                        removeEntity()
                    }
                }
            }
        }

        return entity
    }

    enum class Side(val imgSuffix: String = "") {
        Auto,
        Left("-l"),
        Right("-r"),
    }
}