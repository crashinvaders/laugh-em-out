package com.crashinvaders.laughemout.game.common

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3

object InputUtils {
    private val tmpVec = Vector3()

    fun screenToWorld(camera: Camera, screenX: Int, screenY: Int): Vector3 =
        camera.unproject(tmpVec.set(screenX.toFloat(), screenY.toFloat(), 0f))
}