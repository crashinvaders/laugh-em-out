package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.ArrayMap
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.SnapshotArray
import com.crashinvaders.common.use
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.IntervalSystem
import ktx.assets.pool
import ktx.collections.GdxArray
import ktx.collections.getOrPut

class PhysContactSubscriptionSystem : IntervalSystem(), ContactListener {

    private val contactInfoPool = pool { ContactInfo() }

    private val familyListeners = ArrayMap<Family, SnapshotArray<EntityContactListener>>()

    private val pendingContacts = GdxArray<ContactInfo>()

    fun addListener(family: Family, listener: EntityContactListener): EntityContactListener {
        val listeners = familyListeners.getOrPut(family) { SnapshotArray() }
        listeners.add(listener)

        return listener
    }

    fun removeListener(family: Family, listener: EntityContactListener) {
        val listeners = familyListeners.get(family)
            ?: throw IllegalArgumentException("The listener $listener hasn't been registered to the family: $family")

        if (!listeners.removeValue(listener, true))
            throw IllegalArgumentException("The listener $listener hasn't been registered to the family: $family")

        if (listeners.size == 0) {
            familyListeners.removeKey(family)
        }
    }

    override fun onInit() {
        super.onInit()
        world.system<com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem>().addContactListener(this)
    }

    override fun onDispose() {
        super.onDispose()
        world.system<com.crashinvaders.laughemout.game.engine.systems.PhysWorldSystem>().removeContactListener(this)

        contactInfoPool.freeAll(pendingContacts)
        pendingContacts.clear()
    }

    override fun onTick() {
        if (familyListeners.size == 0)
            return

        for (i in 0 until pendingContacts.size) {
            val contact = pendingContacts[i]

            for (j in 0 until familyListeners.size) {
                val family = familyListeners.getKeyAt(j)
                val listeners = familyListeners.getValueAt(j)

                if (family.contains(contact.entityA!!)) {
                    listeners.use {
                        it.onEntityContact(
                            contact.contactType, contact.entityA!!,
                            contact.entityB!!, contact.fixtureA!!,
                            contact.fixtureB!!,
                            contact.contactPos.x, contact.contactPos.y
                        )
                    }
                }
                if (family.contains(contact.entityB!!)) {
                    listeners.use {
                        it.onEntityContact(
                            contact.contactType, contact.entityB!!,
                            contact.entityA!!, contact.fixtureB!!,
                            contact.fixtureA!!,
                            contact.contactPos.x, contact.contactPos.y
                        )
                    }
                }
            }

            contactInfoPool.free(contact)
        }
        pendingContacts.clear()
    }

    override fun beginContact(contact: Contact) {
        registerContact(contact, CONTACT_BEGIN)
    }

    override fun endContact(contact: Contact) {
        registerContact(contact, CONTACT_END)
    }

    private fun registerContact(
        contact: Contact,
        contactType: Int
    ) {
        val fixtureA = contact.fixtureA
        val fixtureB = contact.fixtureB
        val aObject = fixtureA.body.userData
        val bObject = fixtureB.body.userData
        if (aObject == null || bObject == null) {
            return
        }
        val aEntity = aObject as Entity
        val bEntity = bObject as Entity
        val contactPos = contact.getWorldManifold().points[0]
        val contactInfo = contactInfoPool.obtain()
        contactInfo.init(contactType, aEntity, bEntity, fixtureA, fixtureB, contactPos)
        pendingContacts.add(contactInfo)
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        // Do not handle.
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
        // Do not handle.
    }

    companion object {
        const val CONTACT_BEGIN = 0
        const val CONTACT_END = 1
    }

    fun interface EntityContactListener {
        fun onEntityContact(
            contactType: Int,
            entityOwn: Entity,
            entityOther: Entity,
            fixtureOwn: Fixture,
            fixtureOther: Fixture,
            contactX: Float,
            contactY: Float
        )
    }

    private class ContactInfo : Pool.Poolable {
        var contactType: Int = 0
        var entityA: Entity? = null
        var entityB: Entity? = null
        var fixtureA: Fixture? = null
        var fixtureB: Fixture? = null
        val contactPos = Vector2()

        fun init(
            contactType: Int,
            entityA: Entity,
            entityB: Entity,
            fixtureA: Fixture,
            fixtureB: Fixture,
            contactPos: Vector2
        ) {
            this.entityA = entityA
            this.entityB = entityB
            this.fixtureA = fixtureA
            this.fixtureB = fixtureB
            this.contactType = contactType
            this.contactPos.set(contactPos)
        }

        override fun reset() {
            entityA = null
            entityB = null
            fixtureA = null
            fixtureB = null
            contactType = 0
            contactPos.setZero()
        }
    }
}
