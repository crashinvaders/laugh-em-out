package com.crashinvaders.common

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pool.Poolable

class KtxPool<T>(initialCapacity: Int = 16, max: Int = Int.MAX_VALUE, private val objectCreator: () -> T) :
    Pool<T>(initialCapacity, max) {

    override fun newObject(): T = objectCreator.invoke()
}
