package com.crashinvaders.laughemout.game.engine.components;

import com.badlogic.gdx.physics.box2d.Body
import com.crashinvaders.common.Box2dWorld
import com.github.quillraven.fleks.*

class PhysBody(val body: Body) : Component<PhysBody> {
    companion object : ComponentType<PhysBody>() {
//        @OptIn(ExperimentalContracts::class)
//        inline fun PhysBody.create(init: () -> Body): PhysBody {
//            contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
//
//        }
    }

    override fun type() = PhysBody

    override fun World.onAdd(entity: Entity) {
        body.userData = entity
    }

    override fun World.onRemove(entity: Entity) {
        val world = inject<Box2dWorld>()
        world.destroyBody(body)
    }
}

enum class PhysTransformMapperTag : EntityTags by entityTagOf() {
    PHYS_TO_TRANSFORM,
    TRANSFORM_TO_PHYS
}


