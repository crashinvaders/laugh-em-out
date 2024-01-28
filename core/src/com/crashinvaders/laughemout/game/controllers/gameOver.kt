package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.CameraProcessorOrder
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActorPixels
import com.crashinvaders.laughemout.game.common.SodUtils.kickVisually
import com.crashinvaders.laughemout.game.common.camera.Sod3CameraProcessor
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.DelayAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.RunnableAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.SequenceAction
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.Entity
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TypingLabel

object GameOverHelper {

    fun showGameOver(world: FleksWorld, finalScore: Int, onComplete: () -> Unit): Entity {
        val skelRenderer = world.inject<SkeletonRenderer>()
        val atlasCharacters = world.inject<TextureAtlas>("ui")
        val font = world.inject<Font>("pixolaCurva")

        val skelData = SkeletonBinary(atlasCharacters)
            .apply { scale = UPP }
            .readSkeletonData(com.badlogic.gdx.Gdx.files.internal("skeletons/game-over.skel"))

        val skeleton = Skeleton(skelData)
        val animState = AnimationState(AnimationStateData(skelData))
        val skelActor = SkeletonActor(skelRenderer, skeleton, animState)

        animState.setAnimation(0, "animation", false)

        val eGamOver = world.entity {
            it += Info("GameOver")
            it += Transform().apply {
                localPositionX = 0f * UPP
                localPositionY = 20f * UPP
            }

            it += SkeletonContainer(skeleton, animState)

            it += ActorContainer(skelActor)
            it += DrawableOrder(order = GameDrawOrder.UI_GAME_OVER_MAIN)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions(0f)
            it += DrawableOrigin(Align.center)
        }

        world.entity {
            it += Info("JokeConnector")
            it += Transform().apply {
                localPositionX = 0f
                localPositionY = 72f * UPP
            }

            val actor = TypingLabel("[SICK][#7b86e8]FINAL SCORE IS [#c8d7eb][150%]$finalScore", font).apply {
                alignment = Align.center
                pack()
            }
            it += ActorContainer(actor)
            it += DrawableOrder(GameDrawOrder.UI_GAME_OVER_SCORE)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActorPixels(actor)
            it += DrawableOrigin(Align.center)

            it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                kickVisually()
            }
            it += TransformDebugRenderTag
        }

        world.system<MainCameraStateSystem>().addProcessor(Sod3CameraProcessor(
            4f, 0.8f, 0f,
            CameraProcessorOrder.GAME_OVER,
            readCamValuesWhenAdded = false).apply {
            x = 0f * UPP
            y = 20f * UPP
            scale = 0.75f
        })

        world.system<EntityActionSystem>().addAction(eGamOver, SequenceAction(
            DelayAction(3.5f),
            RunnableAction { onComplete() }
        ))

        return eGamOver
    }
}