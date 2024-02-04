package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActorPixels
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem.Companion.actions
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.*
import com.github.quillraven.fleks.Entity
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TypingLabel
import ktx.app.gdxError
import ktx.scene2d.container
import ktx.scene2d.image
import ktx.scene2d.scene2d
import ktx.scene2d.stack

object speechBubble {

    private var cloudId = 0

    fun createBubble(
        world: FleksWorld,
        message: String,
        x: Float,
        y: Float,
        duration: Float = -1f,
        side: Side = Side.Auto,
        affection: Affection = Affection.Neutral,
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
                container.background = NinePatchDrawable(atlas.createPatch("speech-bubble-frame${affection.imgSuffix}${actualSide.imgSuffix}"))
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

    fun createEmojiBubble(
        world: FleksWorld,
        emoji: Emoji,
        x: Float,
        y: Float,
        duration: Float = -1f,
        affection: Affection = Affection.Neutral,
    ): Entity {
        val atlas = world.inject<TextureAtlas>("ui")

        val entity = world.entity { entity ->
            entity += Info("SpeechCloud${cloudId++}")
            entity += Transform().apply {
                localPositionX = x + 4f * UPP
                localPositionY = y
            }

            val actor: Actor = TransformActorWrapper(scene2d {
                stack {
                    image(atlas.findRegion("emoji-bubble-frame${affection.imgSuffix}"))
                    container {
                        center()
                        padBottom(11f)
                        image(atlas.findRegion(emoji.imgName)) {
                            setScaling(Scaling.none)
                        }
                    }
                }
//                val image = Image(atlas.findRegion(emoji.imgName))
//                image.setScaling(Scaling.none)
//
//                val container = Container(image)
//                container.setBackground(TextureRegionDrawable(atlas.findRegion("emoji-bubble-frame${affection.imgSuffix}")), false)
//                container.fill()
//                container.pad(0f, 0f, 8f, 0f)
            })

            entity += ActorContainer(actor)
            entity += DrawableRenderer(ActorEntityRenderer)
            entity += DrawableOrder(GameDrawOrder.UI_SPEECH_BUBBLE)
            entity += DrawableTint()
            entity += DrawableVisibility()
            entity += DrawableDimensions().fromActorPixels(actor)
            entity += DrawableOrigin(Align.bottom)

            entity += SodInterpolation(6f, 0.6f, -0.5f).apply {
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

    enum class Side(val imgSuffix: String = "") {
        Auto,
        Left("-l"),
        Right("-r"),
    }

    enum class Affection(val imgSuffix: String) {
        Neutral(""),
        Positive("-pos"),
        Negative("-neg"),
    }

    enum class Emoji(val imgName: String) {
        Angry("emoji-angry"),
        Disappointment("emoji-disappointment"),
        Dislike("emoji-dislike"),
        Laugh("emoji-laugh"),
        Neutral("emoji-neutral"),
        Rofl("emoji-rofl"),
        Scared("emoji-scared"),
        Smile("emoji-smile"),
        Surprise("emoji-surprise"),
    }
}