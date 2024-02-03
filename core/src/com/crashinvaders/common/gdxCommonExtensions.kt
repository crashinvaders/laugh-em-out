package com.crashinvaders.common

import com.badlogic.gdx.graphics.Color

fun Color.set(color: UInt): Color =
    this.set(color.toInt())