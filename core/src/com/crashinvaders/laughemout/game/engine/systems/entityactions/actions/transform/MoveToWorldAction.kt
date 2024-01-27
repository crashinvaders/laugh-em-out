package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform

import com.badlogic.gdx.math.Interpolation
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.TemporalAction

class MoveToWorldAction(
    val endX: Float,
    val endY: Float,
    duration: Float = 0f,
    interpolation: Interpolation = Interpolation.linear
) : TemporalAction(duration, interpolation) {

    private var startX = 0f
    private var startY = 0f

    private lateinit var transform: Transform

    override fun begin() {
        super.begin()
        with(world) {
            transform = entity[Transform]
            val position = transform.worldPosition
            startX = position.x
            startY = position.y
        }
    }

    override fun update(percent: Float) {
        transform.setWorldPosition(
            startX + (endX - startX) * percent,
            startY + (endY - startY) * percent)
    }
}
