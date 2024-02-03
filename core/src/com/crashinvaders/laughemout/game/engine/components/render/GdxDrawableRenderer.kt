package com.crashinvaders.laughemout.game.engine.components.render

import com.badlogic.gdx.graphics.g2d.Batch
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.components.GdxDrawableContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.github.quillraven.fleks.Entity
import ktx.collections.gdxArrayOf
import ktx.math.component1
import ktx.math.component2

object GdxDrawableRenderer : EntityRenderer {

    private val requiredComponents = gdxArrayOf(
        Transform,
        GdxDrawableContainer,
        DrawableVisibility,
        DrawableOrigin,
        DrawableDimensions)

    override fun FleksWorld.validate(entity: Entity) {
        checkRequiredComponents(entity, requiredComponents)
    }

    override fun FleksWorld.render(entity: Entity, batch: Batch) {
        val visible = entity[DrawableVisibility].isVisible
        if (!visible) {
            return
        }

        val origin = entity[DrawableOrigin]

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

        val drawable = entity[GdxDrawableContainer].drawable

        drawable.draw(batch, posX + shiftX, posY + shiftY, originX, originY, width, height, scaleX, scaleY, rotation)
    }
}