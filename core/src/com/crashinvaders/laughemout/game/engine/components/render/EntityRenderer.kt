package com.crashinvaders.laughemout.game.engine.components.render

import com.badlogic.gdx.graphics.g2d.Batch
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.App
import com.crashinvaders.laughemout.game.WorldHelper.getPrintName
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import ktx.app.gdxError
import ktx.collections.GdxArray

interface EntityRenderer {
    fun FleksWorld.validate(entity: Entity)
    fun FleksWorld.render(entity: Entity, batch: Batch)

    /**
     * Utility method to ensure all the required components are present at the entity.
     * Works only when the app is in the debug mode.
     */
    fun FleksWorld.checkRequiredComponents(entity: Entity, requiredComponents: GdxArray<ComponentType<out Component<*>>>) {
        if (App.Inst.isDebug) {
            requiredComponents.forEach {
                if (it !in entity) {
                    val componentName = it::class.qualifiedName.let {
                        it!!.substring(0, it.length - ".Companion".length).let {
                            it.substring(it.lastIndexOf('.') + 1)
                        }
                    }
                    gdxError("Entity \"${entity.getPrintName(this)}\" must have $componentName component to be compatible with ${ActorEntityRenderer::class.simpleName}")
                }
            }
        }
    }
}