package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform

import com.badlogic.gdx.math.Interpolation
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.TemporalAction

class ScaleByAction() : TemporalAction() {

    var amountX: Float = 0f
    var amountY: Float = 0f
    var space: TransformSpace = TransformSpace.World

    private var transform: Transform? = null
    private var startX = 0f
    private var startY = 0f

    constructor(
        amountX: Float,
        amountY: Float,
        duration: Float = 0f,
        interpolation: Interpolation = Interpolation.linear,
        space: TransformSpace = TransformSpace.World
    ) : this() {
        this.amountX = amountX
        this.amountY = amountY
        this.space = space
        this.duration = duration
        this.interpolation = interpolation
    }

    override fun begin() {
        super.begin()
        with(world!!) {
            transform = entity!![Transform]
            val scale = when (space) {
                TransformSpace.Local -> transform!!.localScale
                TransformSpace.World -> transform!!.worldScale
            }
            startX = scale.x
            startY = scale.y
        }
    }

    override fun update(percent: Float) {
        val x = startX + amountX * percent
        val y = startY + amountY * percent
        when (space) {
            TransformSpace.Local -> transform!!.apply { localScaleX = x; localScaleY = y }
            TransformSpace.World -> transform!!.setWorldScale(x, y)
        }
    }

    override fun restart() {
        transform = null
        startX = 0f
        startY = 0f
        super.restart()

    }

    override fun reset() {
        amountX = 0f
        amountY = 0f
        space = TransformSpace.World
        super.reset()
    }
}
