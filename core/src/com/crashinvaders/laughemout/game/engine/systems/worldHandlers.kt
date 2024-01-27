package com.crashinvaders.laughemout.game.engine.systems

interface OnWorldInitializedHandler {
    /** Called after the world is configured and all the essential entities added the engine. */
    fun onWorldInitialized()
}

interface OnWorldResizeHandler {
    fun onResize(width: Int, height: Int)
}
