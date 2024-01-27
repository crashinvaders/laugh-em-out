package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.math.MathUtils
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.crashinvaders.laughemout.game.engine.components.PhysBody
import com.crashinvaders.laughemout.game.engine.components.PhysTransformMapperTag
import com.crashinvaders.laughemout.game.engine.components.Transform
import ktx.math.component1
import ktx.math.component2

class PhysToTransformMapperSystem : IteratingSystem(
    family { all(PhysBody, Transform, PhysTransformMapperTag.PHYS_TO_TRANSFORM) }
) {
    override fun onTickEntity(entity: Entity) {
        val transform = entity[Transform]
        val physBody = entity[PhysBody]

        val body = physBody.body
        val (posX, posY) = body.position
        val rotationRad = body.angle
        val rotationDeg = rotationRad * MathUtils.radDeg
        transform.setWorldPosition(posX, posY)
        transform.worldRotation = rotationDeg
    }
}

class TransformToPhysMapperSystem : IteratingSystem(
    family { all(PhysBody, Transform, PhysTransformMapperTag.TRANSFORM_TO_PHYS) }
) {
    override fun onTickEntity(entity: Entity) {
        val transform = entity[Transform]
        val physBody = entity[PhysBody]

        val (posX, posY) = transform.worldPosition
        val rotationDeg = transform.worldRotation
        val rotationRad = rotationDeg * MathUtils.degRad

        val body = physBody.body
        body.setTransform(posX, posY, rotationRad)
    }
}
