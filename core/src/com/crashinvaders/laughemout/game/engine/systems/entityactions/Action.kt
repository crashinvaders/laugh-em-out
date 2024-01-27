package com.crashinvaders.laughemout.game.engine.systems.entityactions

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pool.Poolable
import com.crashinvaders.common.FleksWorld
import com.github.quillraven.fleks.Entity


abstract class Action : Poolable {

    protected lateinit var world: FleksWorld

    /** The entity this action is attached to, or null if it is not attached.  */
    lateinit var entity: Entity
        public get
        protected set

    /** @return null if the action is not attached to system.
     */
    /** Indicates whether entity is attached to processing system.  */
    var isAttached = false
        public get
        protected set

//    /** Sets the pool that the action will be returned to when removed from the actor.
//     * @param pool May be null.
//     * @see .addedToSystem
//     */
//    var pool: Pool<Action>? = null

    /** Action added to precessing system  */
    open fun addedToSystem(world: FleksWorld, entity: Entity) {
        isAttached = true
        this.world = world
        this.entity = entity
    }

    /** Action removed from precessing system  */
    open fun removedFromSystem() {
        isAttached = false
//        if (pool != null) {
//            pool!!.free(this)
//            pool = null
//        }
    }
    //TODO move parts of that javadoc to addedToSystem/removedFromSystem
    /** Sets the actor this action is attached to. This also sets the [target][.setTarget] actor if it is null. This
     * method is called automatically when an action is added to an actor. This method is also called with null when an action is
     * removed from an actor.
     *
     *
     * When set to null, if the action has a [pool][.setPool] then the action is [returned][Pool.free] to
     * the pool (which calls [.reset]) and the pool is set to null. If the action does not have a pool, [.reset] is
     * not called.
     *
     *
     * This method is not typically a good place for an action subclass to query the actor's state because the action may not be
     * executed for some time, eg it may be [delayed][DelayAction]. The actor's state is best queried in the first call to
     * [.act]. For a [TemporalAction], use TemporalAction#begin().  */
    //    public void setEntity(Entity entity) {
    //        this.entity = entity;
    //        if (target == null) setTarget(actor);
    //        if (actor == null) {
    //            if (pool != null) {
    //                pool.free(this);
    //                pool = null;
    //            }
    //        }
    //    }

    override fun reset() {
//        world = null
//        entity = null
//        pool = null
        restart()
    }

    /** Sets the state of the action so it can be run again.  */
    open fun restart() {}

    /** Updates the action based on time.
     * @param delta Time in seconds since the last frame.
     * @return true if the action is done. This method may continue to be called after the action is done.
     */
    abstract fun act(delta: Float): Boolean
}

