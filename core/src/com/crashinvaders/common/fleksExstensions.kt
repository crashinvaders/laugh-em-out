package com.crashinvaders.common

import com.github.quillraven.fleks.IntervalSystem

typealias FleksWorld = com.github.quillraven.fleks.World

fun IntervalSystem.toggle() {
    enabled = !enabled
}
