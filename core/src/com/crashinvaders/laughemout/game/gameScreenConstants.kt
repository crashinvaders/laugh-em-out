@file:JvmName("GameScreenConstants")
package com.crashinvaders.laughemout.game

const val PPU = 32f
const val UPP = 1f/32f

object GameInputOrder {
    const val HUD = 200
    const val DRAWABLES = 400
    const val DEBUG_KEYS = 1000
    const val DEBUG_CONTROLLERS = 1100
}

object GameDrawOrder {
    const val MAP_LAYERS_BACK = -1000

    const val MAIN_CHARACTER = 0

    const val MAP_LAYERS_FRONT = 1000
}

object CameraProcessorOrder {
    const val DEBUG_FREE = Int.MAX_VALUE
}
