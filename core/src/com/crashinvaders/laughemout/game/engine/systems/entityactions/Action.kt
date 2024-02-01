package com.crashinvaders.laughemout.game.engine.systems.entityactions

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pool.Poolable
import com.crashinvaders.common.FleksWorld
import com.github.quillraven.fleks.Entity

abstract class Action : Poolable {

    private var _world: FleksWorld? = null
    val world: FleksWorld get() = _world!!

    /** The entity this action is attached to, or null if it is not attached.  */
    private var _entity: Entity? = null
    val entity: Entity get() = _entity!!

    /** Indicates whether entity is attached to processing system.  */
    var isAttached = false
        public get
        protected set

    /** Sets the pool that the action will be returned to when removed from the actor.
     * @see addedToSystem
     */
    var pool: Pool<Any>? = null

    /** Applied only to the root action (the one that has been added to the system). */
    var timeMode: TimeMode = TimeMode.GameTime

    /** Action added to precessing system  */
    open fun addedToSystem(world: FleksWorld, entity: Entity) {
        isAttached = true
        this._world = world
        this._entity = entity
    }

    /** Action removed from precessing system  */
    open fun removedFromSystem() {
        isAttached = false
        if (pool != null) {
            pool!!.free(this)
            pool = null
        }
    }

    override fun reset() {
        _world = null
        _entity = null
        pool = null
        timeMode = TimeMode.GameTime
        restart()
    }

    /** Sets the state of the action, so it can be run again.  */
    open fun restart() = Unit

    /** Updates the action based on time.
     * @param delta Time in seconds since the last frame.
     * @return true if the action is done. This method may continue to be called after the action is done.
     */
    abstract fun act(delta: Float): Boolean

    enum class TimeMode {
        GameTime,
        UnscaledTime,
    }
}

