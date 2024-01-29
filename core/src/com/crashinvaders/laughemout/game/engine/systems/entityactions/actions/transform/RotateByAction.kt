package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform

import com.badlogic.gdx.math.Interpolation
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.TemporalAction

class RotateByAction() : TemporalAction() {

    var amount: Float = 0f
    var space: TransformSpace = TransformSpace.World

    private var transform: Transform? = null
    private var start = 0f

    constructor(
        amount: Float,
        duration: Float = 0f,
        interpolation: Interpolation = Interpolation.linear,
        space: TransformSpace = TransformSpace.World
    ) : this() {
        this.amount = amount
        this.space = space
        this.duration = duration
        this.interpolation = interpolation
    }

    override fun begin() {
        super.begin()
        with(world!!) {
            transform = entity!![Transform]
            val rotation = when (space) {
                TransformSpace.Local -> transform!!.localRotation
                TransformSpace.World -> transform!!.worldRotation
            }
            start = rotation
        }
    }

    override fun update(percent: Float) {
        val rotation = start + amount * percent
        when (space) {
            TransformSpace.Local -> transform!!.localRotation = rotation
            TransformSpace.World -> transform!!.worldRotation = rotation
        }
    }

    override fun restart() {
        transform = null
        start = 0f
        super.restart()

    }

    override fun reset() {
        amount = 0f
        space = TransformSpace.World
        super.reset()
    }
}
