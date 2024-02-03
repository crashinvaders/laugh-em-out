package com.crashinvaders.laughemout.game

import com.badlogic.gdx.graphics.Color
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.WorldHelper.getEntityNames
import com.github.quillraven.fleks.*
import com.crashinvaders.laughemout.game.engine.components.Info
import com.kotcrab.vis.ui.util.ColorUtils
import ktx.app.gdxError

object WorldHelper {
    private val tmpColor = Color()

    //TODO AC: This might be slow and ugly. Find another way to find the entity of the component.
    inline fun <reified T : Component<*>> FleksWorld.findEntity(type: ComponentType<T>, component: T): Entity =
        family { all(type) }.entities
            .find { entity -> entity.getOrNull(type) == component }
            ?: gdxError("Failed to find entity for the provided transform.")

    fun Entity.getPrintName(world: FleksWorld): String {
        val entity = this
        with(world) {
            return entity.getOrNull(Info)?.name ?: "[${entity.id}:${entity.version}]"
        }
    }

    fun Family.getEntityNames(world: FleksWorld, separator: String, nameExtractor: World.(Entity) -> String): String {
        val sb = StringBuilder()
        this.forEach {
            if (sb.length != 0) sb.append(separator)
            val entityName = world.nameExtractor(it)
            sb.append(entityName)
        }
        return sb.toString()
    }

    fun Family.getEntityNames(world: FleksWorld): String =
        getEntityNames(world, ", ") { it.getPrintName(world) }

    fun evalEntityDebugColor(entity: Entity, saturation: Float = 100f, value: Float = 100f): Color =
        ColorUtils.HSVtoRGB(
            ((entity.hashCode() and 0xff) / 0xff.toFloat()) * 360f,
            saturation, value, tmpColor
        ).also { it.a = 1f }
}
