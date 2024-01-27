package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.crashinvaders.common.Box2dWorld
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import ktx.collections.GdxArray
import kotlin.math.min

class PhysWorldSystem : IntervalSystem(), ContactListener {

    private val fixedUpdateListeners = GdxArray<com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.FixedUpdateListener>()
    private val contactListeners = GdxArray<ContactListener>()

    private val box2dWorld: Box2dWorld = inject()

    override fun onInit() {
        super.onInit()
        box2dWorld.setContactListener(this)
    }

    override fun onDispose() {
        super.onDispose()

        box2dWorld.setContactListener(null)
    }

    fun addFixedUpdateListener(listener: com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.FixedUpdateListener) {
        fixedUpdateListeners.add(listener)
    }

    fun removeFixedUpdateListener(listener: com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.FixedUpdateListener) {
        fixedUpdateListeners.removeValue(listener, true)
    }

    fun addContactListener(listener: ContactListener) {
        contactListeners.add(listener)
    }

    fun removeContactListener(listener: ContactListener) {
        contactListeners.removeValue(listener, true)
    }

    override fun onTick() {
        var frameTime: Float = deltaTime
        var stepsPerformed = 0
        while (frameTime > 0.0 && stepsPerformed < com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.Companion.MAXIMUM_NUMBER_OF_STEPS) {
            var deltaTime =
                min(frameTime,
                    com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.Companion.FIXED_TIMESTEP
                )
            frameTime -= deltaTime
            if (frameTime < com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.Companion.MINIMUM_TIMESTEP) {
                deltaTime += frameTime
                frameTime = 0.0f
            }
            for (i in 0 until fixedUpdateListeners.size) {
                fixedUpdateListeners[i].onFixedUpdate(deltaTime)
            }
            box2dWorld.step(
                deltaTime,
                com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.Companion.VELOCITY_ITERATIONS,
                com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.Companion.POSITION_ITERATIONS
            )
            stepsPerformed++
        }
        box2dWorld.clearForces()
    }

    override fun beginContact(contact: Contact) {
        for (i in 0 until contactListeners.size) {
            contactListeners[i].beginContact(contact)
        }
    }

    override fun endContact(contact: Contact) {
        for (i in 0 until contactListeners.size) {
            contactListeners[i].endContact(contact)
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        for (i in 0 until contactListeners.size) {
            contactListeners[i].preSolve(contact, oldManifold)
        }
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
        for (i in 0 until contactListeners.size) {
            contactListeners[i].postSolve(contact, impulse)
        }
    }

    companion object {
        private val GRAVITY = Vector2(0f, -10f)
        private const val MAXIMUM_NUMBER_OF_STEPS = 20
        private const val FIXED_TIMESTEP = 1.0f / 60.0f
        private const val MINIMUM_TIMESTEP = 1.0f / 300.0f
        private const val VELOCITY_ITERATIONS = 6
        private const val POSITION_ITERATIONS = 2

        fun createWorld(): Box2dWorld =
            //TODO refactor it to body.setSleepingAllowed if it will be lacking in performance
            Box2dWorld(com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem.Companion.GRAVITY, false).apply {
                autoClearForces = false
            }
    }

    fun interface FixedUpdateListener {
        fun onFixedUpdate(delta: Float)
    }
}
