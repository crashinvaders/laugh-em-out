package com.crashinvaders.laughemout.game.engine.components;

import com.badlogic.gdx.math.Affine2
import com.crashinvaders.common.sod.SecondOrderDynamicsAffine2D
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class SodInterpolation(
    f: Float = 2f,
    z: Float = 1f,
    r: Float = 0f
) : Component<SodInterpolation> {

    val sodMatrix = SecondOrderDynamicsAffine2D()

    var hasCachedValues = false
    var cachedPosX = 0f
    var cachedPosY = 0f
    var cachedScaleX = 0f
    var cachedScaleY = 0f
    var cachedRotation = 0f

    var pendingReset = true

    init {
        configure(f, z, r)
    }

    fun configure(f: Float, z: Float, r: Float) {
        sodMatrix.configure(f, z, r)
    }

    fun setAccel(x: Float, y: Float, rotation: Float, scaleX: Float, scaleY: Float) =
        setAccel(tmpMatrix.setToTrnRotScl(x, y, rotation, scaleX, scaleY))

    fun setAccel(matrix: Affine2) {
        sodMatrix.accMatrix = matrix
    }

    fun addAccel(x: Float, y: Float, rotation: Float, scaleX: Float, scaleY: Float) =
        addAccel(tmpMatrix.setToTrnRotScl(x, y, rotation, scaleX, scaleY))

    fun addAccel(matrix: Affine2) {
        sodMatrix.addAccMatrix(matrix)
    }

    override fun type() = SodInterpolation

    companion object : ComponentType<SodInterpolation>() {
        private val tmpMatrix = Affine2()
    }
}
