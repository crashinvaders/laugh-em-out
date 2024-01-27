@file:JvmName("GameScreenConstants")
package com.crashinvaders.laughemout.game

const val PPU = 24f
const val UPP = 1f/PPU

object GameInputOrder {
    const val HUD = 200
    const val JOKE_BUILDER_UI = 300
    const val DRAWABLES = 400
    const val DEBUG_KEYS = 1000
    const val DEBUG_CONTROLLERS = 1100
}

object GameDrawOrder {
    const val MAP_LAYERS_BACK = -1000

    const val ENVIRONMENT_BACK = -100

    const val UI_EMO_METER = -50

    const val COMEDIAN = 0
    const val AUDIENCE_BACK_ROW = 10
    const val AUDIENCE_FRONT_ROW = 11

    const val UI_SPEECH_BUBBLE = 50

    const val ENVIRONMENT_FRONT = 100

    const val MAP_LAYERS_FRONT = 1000

    const val JOKE_BUILDER_UI_BASE = 2000
}

object CameraProcessorOrder {
    const val DEBUG_FREE = Int.MAX_VALUE
}
