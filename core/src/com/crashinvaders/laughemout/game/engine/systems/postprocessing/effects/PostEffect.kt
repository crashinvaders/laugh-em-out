package com.crashinvaders.laughemout.game.engine.systems.postprocessing.effects

import com.badlogic.gdx.utils.Disposable
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.PingPongBuffer

abstract class PostEffect(val order: Int = 0) : Disposable {

    var pendingConfiguration = true

    open fun configure(width: Int, height: Int) = Unit

    open fun update(deltaTime: Float) = Unit

    abstract fun render(buffers: PingPongBuffer): Boolean

    companion object {
        const val TEXTURE_INDEX0 = 0
        const val TEXTURE_INDEX1 = 1
        const val TEXTURE_INDEX2 = 2
        const val TEXTURE_INDEX3 = 3
    }
}
