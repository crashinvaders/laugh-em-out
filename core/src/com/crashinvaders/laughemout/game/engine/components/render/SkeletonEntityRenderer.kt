package com.crashinvaders.laughemout.game.engine.components.render

import com.badlogic.gdx.graphics.g2d.Batch
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.components.SkeletonContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.esotericsoftware.spine.SkeletonRenderer
import com.github.quillraven.fleks.Entity
import ktx.collections.gdxArrayOf
import ktx.math.component1
import ktx.math.component2

class SkeletonEntityRenderer(
    private val renderer: SkeletonRenderer,
    private val resetBlendFunction: Boolean = true,
) : EntityRenderer {

    private val requiredComponents = gdxArrayOf(
        Transform,
        SkeletonContainer,
        DrawableVisibility)

    override fun FleksWorld.validate(entity: Entity) {
        checkRequiredComponents(entity, requiredComponents)
    }

    override fun FleksWorld.render(entity: Entity, batch: Batch) {
        val visible = entity[DrawableVisibility].isVisible
        if (!visible) {
            return
        }

//        val dimensions = entity[DrawableDimensions]
//        val width = dimensions.width
//        val height = dimensions.height
//
//        val transform = entity[Transform]
//        val (posX, posY) = transform.worldPosition
//        val (scaleX, scaleY) = transform.worldScale
//        val rotation = transform.worldRotation

//        val origin = entity[DrawableOrigin]
//        val shiftX = -width * origin.x
//        val shiftY = -height * origin.y
//
//        val originX = origin.x * width
//        val originY = origin.y * height

        val skeleton = entity[SkeletonContainer].skeleton

        val transform = entity[Transform]
        val (posX, posY) = transform.worldPosition
        val (scaleX, scaleY) = transform.worldScale
        val rotation = transform.worldRotation

        val blendSrc = batch.blendSrcFunc
        val blendDst = batch.blendDstFunc
        val blendSrcAlpha = batch.blendSrcFuncAlpha
        val blendDstAlpha = batch.blendDstFuncAlpha

        skeleton.setPosition(posX, posY)
        skeleton.setScale(scaleX, scaleY)
        skeleton.rootBone.rotation = rotation
        skeleton.updateWorldTransform()
        renderer.draw(batch, skeleton)

        if (resetBlendFunction) {
            batch.setBlendFunctionSeparate(blendSrc, blendDst, blendSrcAlpha, blendDstAlpha)
        }
    }
}