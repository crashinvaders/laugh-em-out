package com.crashinvaders.laughemout.game.debug.controllers

import com.badlogic.gdx.utils.Disposable

interface DebugController: Disposable {
    override fun dispose() = Unit
}
