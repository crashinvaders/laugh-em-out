package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

class DelayAction(duration: Float): TemporalAction(duration) {
    override fun update(percent: Float) = Unit
}