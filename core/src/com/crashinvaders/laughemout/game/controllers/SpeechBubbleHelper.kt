package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActor
import com.crashinvaders.laughemout.game.common.SodUtils.kickVisually
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.SodInterpolation
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.TransformDebugRenderTag
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.DelayAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.RunnableAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.SequenceAction
import com.github.quillraven.fleks.Entity
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TypingLabel

object SpeechBubbleHelper {

    fun createSpeechBubble(
        world: FleksWorld,
        message: String,
        x: Float,
        y: Float,
        size: SpeechBubbleSize,
        duration: Float = -1f
    ): Entity {
        val font = world.inject<Font>("pixolaCurva")
        val atlas = world.inject<TextureAtlas>("ui")

        lateinit var transform: Transform

        val entity = world.entity {
            it += Info("SpeechCloud")
            it += Transform().apply {
                localPositionX = x
                localPositionY = y
            }

            val actor: Actor = let {
                val label = TypingLabel("[#544470]$message", font).apply {
                    alignment = Align.center
                }

                val imgBubble = Image(atlas.findRegion("speech-bubble-frame${size.imgSuffix}")).apply {
                }
                val bubbleWidth = imgBubble.prefWidth * UPP
                val bubbleHeight = imgBubble.prefHeight * UPP
                imgBubble.setSize(bubbleWidth, bubbleHeight)
                imgBubble.setPosition(0f, 0f, Align.bottom)

                Group().apply {
                    addActor(imgBubble)
                    addActor(Group().apply {
                        addActor(label)
                        val shiftY = 12f * UPP
                        label.setPosition(0f, (bubbleHeight - shiftY) * 0.5f + shiftY, Align.center)
                    })
                    isTransform = true
                }
            }

            it += ActorContainer(actor)
            it += DrawableOrder(GameDrawOrder.UI_SPEECH_BUBBLE)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActor(actor)
            it += DrawableOrigin(Align.bottom)

            it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                kickVisually()
            }
            it += TransformDebugRenderTag

            transform = it[Transform]
        }

        if (duration > 0f) {
            world.system<EntityActionSystem>().addAction(
                entity, SequenceAction(
                    DelayAction(duration),
                    RunnableAction {
                        transform.localScaleX = 0f
                        transform.localScaleY = 0f
                    },
                    DelayAction(0.5f),
                    RunnableAction { Gdx.app.postRunnable { if (entity in world) world -= entity } },
                )
            )
        }

        return entity
    }

    fun destroy(world: FleksWorld, bubble: Entity, animate: Boolean = true) {
        if (!animate) {
            world -= bubble
            return
        }

        with(world) {
            val cTransform = bubble[Transform]
            cTransform.localScaleX = 0f
            cTransform.localScaleY = 0f
            world.system<EntityActionSystem>().addAction(SequenceAction(
                DelayAction(0.5f),
                RunnableAction { Gdx.app.postRunnable { if (bubble in world) world -= bubble } },
            ))
        }
    }
}

enum class SpeechBubbleSize(val imgSuffix: String) {
    Small("-s"),
    Medium("-m"),
    Large("-l"),
    XLarge("-xl"),
}