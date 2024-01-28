package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.SkeletonContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.DelayAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.RunnableAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.SequenceAction
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.Entity

object JokeTimerHelper {

    fun createTimer(world: FleksWorld, x: Float, y: Float, duration: JokeTimerDuration, onTimeUp: () -> Unit): Entity {
        val skelRenderer = world.inject<SkeletonRenderer>()
        val atlasUi = world.inject<TextureAtlas>("ui")

        val skelData = SkeletonBinary(atlasUi)
            .apply { scale = UPP }
            .readSkeletonData(com.badlogic.gdx.Gdx.files.internal("skeletons/joke-timer.skel"))

        val skeleton = Skeleton(skelData)
        val animState = AnimationState(AnimationStateData(skelData))
        val skelActor = SkeletonActor(skelRenderer, skeleton, animState)

        animState.setAnimation(0, duration.animName, false)

        val entity = world.entity {
            it += Info("AudienceMember")
            it += Transform().apply {
                localPositionX = x
                localPositionY = y
            }

            it += SkeletonContainer(skeleton, animState)

            it += ActorContainer(skelActor)
            it += DrawableOrder(GameDrawOrder.JOKE_TIMER)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions(0f)
            it += DrawableOrigin(Align.center)
        }

        world.system<EntityActionSystem>().addAction(entity, SequenceAction(
            DelayAction(duration.duration),
            RunnableAction {
                onTimeUp.invoke()
            }
        ))

        return entity
    }
}

enum class JokeTimerDuration(val animName: String, val duration: Float) {
    Sec7("timer7", 7f),
    Sec10("timer10", 10f),
    Sec15("timer15", 15f),
    Sec20("timer20", 20f),
    Sec25("timer25", 25f),
}