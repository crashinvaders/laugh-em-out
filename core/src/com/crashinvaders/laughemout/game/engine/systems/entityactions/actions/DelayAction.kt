package com.crashinvaders.laughemout.game.engine.systems.entityactions.actions

class DelayAction(): TemporalAction() {

    constructor(duration: Float) : this() {
        this.duration = duration
    }

    override fun update(percent: Float) = Unit
}