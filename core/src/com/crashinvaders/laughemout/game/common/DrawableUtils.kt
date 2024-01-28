package com.crashinvaders.laughemout.game.common

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.applyToPoint
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.DrawableDimensions
import com.crashinvaders.laughemout.game.engine.components.render.DrawableOrigin
import com.github.quillraven.fleks.Entity
import ktx.math.component1
import ktx.math.component2

object DrawableUtils {
    private val tmpVec2 = Vector2()
    private val tmpRect = Rectangle()

    fun DrawableDimensions.fromActorPixels(actor: Actor): DrawableDimensions {
        this.width = actor.width * UPP
        this.height = actor.height * UPP
        return this
    }

    fun DrawableDimensions.fromActorUnits(actor: Actor): DrawableDimensions {
        this.width = actor.width
        this.height = actor.height
        return this
    }

    fun checkHit(world: FleksWorld, entity: Entity, hitWorldX: Float, hitWorldY: Float): Boolean {
        with(world) {
            val transform = entity[Transform]
            val w2lMat = transform.worldToLocalProj
            val (hitLocalX, hitLocalY) = w2lMat.applyToPoint(tmpVec2.set(hitWorldX, hitWorldY))

            val dimensions = entity[DrawableDimensions]
            val width = dimensions.width
            val height = dimensions.height
            val origin = entity[DrawableOrigin]
            val xLeft = -width * origin.x
            val yBot = -height * origin.y


            tmpRect.set(xLeft, yBot, width, height)
            return tmpRect.contains(hitLocalX, hitLocalY)
        }

//        val xRight = xLeft + width
//        val yTop = yBot + height
////        val (x0, y0) = l2wMat.applyToPoint(tmpVec2.set(xLeft, yBot))
////        val (x1, y1) = l2wMat.applyToPoint(tmpVec2.set(xRight, yTop))
////
////        val xMin: Float
////        val xMax: Float
////        val yMin: Float
////        val : Float
////        if (x0 > x1) x0 else x1
    }
}