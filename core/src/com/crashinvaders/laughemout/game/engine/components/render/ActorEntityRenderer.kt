package com.crashinvaders.laughemout.game.engine.components.render;

import com.badlogic.gdx.graphics.g2d.Batch
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.components.ActorContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.github.quillraven.fleks.Entity
import ktx.collections.gdxArrayOf
import ktx.math.component1
import ktx.math.component2

object ActorEntityRenderer : EntityRenderer {

    private val requiredComponents = gdxArrayOf(
        Transform,
        ActorContainer,
        DrawableVisibility,
        DrawableOrigin,
        DrawableTint,
        DrawableDimensions)

    override fun FleksWorld.validate(entity: Entity) {
        checkRequiredComponents(entity, requiredComponents)
    }

    override fun FleksWorld.render(entity: Entity, batch: Batch) {
        val actor = entity[ActorContainer].actor
        val visible = entity[DrawableVisibility].isVisible
        actor.isVisible = visible

        if (!visible) {
            return
        }

        val origin = entity[DrawableOrigin]
        val tint = entity[DrawableTint]

        val dimensions = entity[DrawableDimensions]
        val width = dimensions.width
        val height = dimensions.height

        val transform = entity[Transform]
        val (posX, posY) = transform.worldPosition
        val (scaleX, scaleY) = transform.worldScale
        val rotation = transform.worldRotation

        val shiftX = -width * origin.x
        val shiftY = -height * origin.y

        val originX = origin.x * width
        val originY = origin.y * height

        actor.setSize(width, height)
        actor.setPosition(posX + shiftX, posY + shiftY)
        actor.setScale(scaleX, scaleY)
        actor.setOrigin(originX, originY)
        actor.rotation = rotation
        actor.color = tint.color

//        actor.act(deltaTime)
        actor.draw(batch, 1f)
    }
}