package com.crashinvaders.laughemout.game.engine.systems

import com.crashinvaders.common.TimeManager
import com.crashinvaders.laughemout.game.engine.TimeMode
import com.crashinvaders.laughemout.game.engine.components.SkeletonContainer
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class SkeletonUpdateSystem : IteratingSystem(
    family { all(SkeletonContainer) }
), FamilyOnAdd {

    private val timeManager = world.inject<TimeManager>()

    override fun onAddEntity(entity: Entity) {
        entity[SkeletonContainer].apply {
            animState.apply(skeleton)
            skeleton.updateWorldTransform()
        }
    }

    override fun onTickEntity(entity: Entity) {
        entity[SkeletonContainer].apply {
            val deltaTime = when(timeMode) {
                TimeMode.GameTime -> timeManager.delta
                TimeMode.UnscaledTime -> timeManager.deltaUnscaled
            }

            animState.update(deltaTime)
            animState.apply(skeleton)
            skeleton.updateWorldTransform()
        }
    }
}