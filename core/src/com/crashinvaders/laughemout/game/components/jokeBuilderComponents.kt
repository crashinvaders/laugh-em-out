package com.crashinvaders.laughemout.game.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class JokeSubjectCard : Component<JokeSubjectCard> {

    lateinit var entity: Entity private set

    var currentRosterPlacement: JokeSubjectCardRosterPlacement? = null
    var currentJokeSubjPlaceholder: JokeSubjectCardPlaceholder? = null

    override fun World.onAdd(entity: Entity) {
        this@JokeSubjectCard.entity = entity
    }

    override fun type() = JokeSubjectCard
    companion object : ComponentType<JokeSubjectCard>()
}

class JokeSubjectCardPlaceholder : Component<JokeSubjectCardPlaceholder> {

    lateinit var entity: Entity private set

    var attachedCard: JokeSubjectCard? = null
        set(value) {
            field?.currentJokeSubjPlaceholder = null
            field = value
            value?.currentJokeSubjPlaceholder = this
        }

    override fun World.onAdd(entity: Entity) {
        this@JokeSubjectCardPlaceholder.entity = entity
    }

    override fun type() = JokeSubjectCardPlaceholder
    companion object : ComponentType<JokeSubjectCardPlaceholder>()
}

class JokeSubjectCardRosterPlacement : Component<JokeSubjectCardRosterPlacement> {

    lateinit var entity: Entity private set

    var attachedCard: JokeSubjectCard? = null
        set(value) {
            field?.currentRosterPlacement = null
            field = value
            value?.currentRosterPlacement = this
        }

    override fun World.onAdd(entity: Entity) {
        this@JokeSubjectCardRosterPlacement.entity = entity
    }

    override fun type() = JokeSubjectCardRosterPlacement
    companion object : ComponentType<JokeSubjectCardRosterPlacement>()
}