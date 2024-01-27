package com.crashinvaders.laughemout.game.engine

import com.crashinvaders.common.events.Event

data class OnResizeEvent(
    val screenWidth: Int,
    val screenHeight: Int
) : Event
