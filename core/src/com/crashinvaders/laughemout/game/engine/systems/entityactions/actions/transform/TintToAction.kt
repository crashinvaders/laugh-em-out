package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.crashinvaders.laughemout.game.engine.components.render.DrawableTint
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.TemporalAction
import com.github.tommyettinger.textra.utils.ColorUtils

class TintToAction() : TemporalAction() {

    val end: Color = Color(Color.WHITE)

    private var cTint: DrawableTint? = null
    private val start = Color(Color.WHITE)

    constructor(
        end: Color,
        duration: Float = 0f,
        interpolation: Interpolation = Interpolation.linear
    ) : this() {
        this.end.set(end)
        this.duration = duration
        this.interpolation = interpolation
    }

    override fun begin() {
        super.begin()
        with(world) {
            cTint = entity[DrawableTint]
            start.set(cTint!!.color)
        }
    }

    override fun update(percent: Float) {
        val startBits = start.toIntBits()
        val endBits = end.toIntBits()
        val mixBits = ColorUtils.lerpColors(startBits, endBits, percent)
        cTint!!.color.set(
            MathUtils.lerp(start.r, end.r, percent),
            MathUtils.lerp(start.g, end.g, percent),
            MathUtils.lerp(start.b, end.b, percent),
            MathUtils.lerp(start.a, end.a, percent))
//        cTint!!.color.set(mixBits)
    }

    override fun restart() {
        cTint = null
        start.set(Color.WHITE)
        super.restart()

    }

    override fun reset() {
        end.set(Color.WHITE)
        super.reset()
    }
}
