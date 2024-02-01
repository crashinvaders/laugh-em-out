package com.crashinvaders.laughemout.game.engine.systems

import com.crashinvaders.common.TimeManager
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntity
import com.crashinvaders.laughemout.game.engine.components.SodInterpolation
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.systems.entityactions.Action

class SodInterpolationPreRenderSystem : IteratingSystem(
    family,
    comparator = compareEntity(compareFun = sortComparator())
), FamilyOnAdd {

    private val timeManager = world.inject<TimeManager>()

    override fun onEnable() {
        super.onEnable()
        family.forEach { it[SodInterpolation].pendingReset = true }
    }

    override fun onAddEntity(entity: Entity) {
        val sodIntrpl = entity[SodInterpolation]
        if (sodIntrpl.pendingReset) {
            // Sync SOD position state with the transform.
            // This is preferable over the full SOD reset,
            // since we want to retain the current SOD.acc value.
            val matrix = entity[Transform].localToWorldProj
            sodIntrpl.sodMatrix.posMatrix = matrix
            sodIntrpl.pendingReset = false
        }
    }

    override fun onTick() {
        super.onTick()
        family.forEach { applyInterpolation(it) }
    }

    override fun onTickEntity(entity: Entity) {
        val transform = entity[Transform]
        val sodIntrpl = entity[SodInterpolation]

        resetIfRequired(transform, sodIntrpl)

        // Cache local transform.
        sodIntrpl.apply {
            hasCachedValues = true
            cachedPosX = transform.localPositionX
            cachedPosY = transform.localPositionY
            cachedScaleX = transform.localScaleX
            cachedScaleY = transform.localScaleY
            cachedRotation = transform.localRotation
        }
    }

    private fun applyInterpolation(entity: Entity) {
        val transform = entity[Transform]
        val sodIntrpl = entity[SodInterpolation]

        val deltaTime = when(sodIntrpl.timeMode) {
            SodInterpolation.TimeMode.GameTime -> timeManager.delta
            SodInterpolation.TimeMode.UnscaledTime -> timeManager.deltaUnscaled
        }

        // Update SOD state.
        val sodMatrix = sodIntrpl.sodMatrix
        sodMatrix.update(deltaTime, transform.localToWorldProj)

        // Apply interpolated matrix.
        val interpolatedMatrix = sodMatrix.posMatrix
        transform.setLocalToWorldProj(interpolatedMatrix)
    }

    private fun resetIfRequired(transform: Transform, sodIntrpl: SodInterpolation) {
        if (sodIntrpl.pendingReset) {
            sodIntrpl.pendingReset = false
            sodIntrpl.sodMatrix.resetState(transform.localToWorldProj)
        }
    }
}

class SodInterpolationPostRenderSystem : IteratingSystem(
    family,
    comparator = compareEntity(compareFun = sortComparator())
), FamilyOnRemove {

    override fun onRemoveEntity(entity: Entity) {
        // Make sure to restore transform values before removal.
        onTickEntity(entity)
    }

    override fun onTickEntity(entity: Entity) {
        val sodIntrpl = entity[SodInterpolation]
        if (!sodIntrpl.hasCachedValues) {
            return
        }

        // Restore cached local transform.
        val transform = entity[Transform]
        sodIntrpl.apply {
            hasCachedValues = false
            transform.localPositionX = cachedPosX
            transform.localPositionY = cachedPosY
            transform.localScaleX = cachedScaleX
            transform.localScaleY = cachedScaleY
            transform.localRotation = cachedRotation
        }
    }
}

private val family = family { all(Transform, SodInterpolation) }

// First process the parents, then the children.
private fun sortComparator(): World.(Entity, Entity) -> Int =
    { e0, e1 -> e0[Transform].nestingLevel.compareTo(e1[Transform].nestingLevel) }
