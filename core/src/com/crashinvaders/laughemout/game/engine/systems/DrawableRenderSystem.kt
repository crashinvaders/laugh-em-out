package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.crashinvaders.laughemout.game.engine.components.render.DrawableOrder
import com.crashinvaders.laughemout.game.engine.components.render.DrawableRenderer
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.quillraven.fleks.collection.compareEntityBy

class DrawableRenderSystem : IteratingSystem(
    family { all(DrawableRenderer, DrawableOrder) },
    comparator = compareEntityBy(DrawableOrder),
    sortingType = Manual
),
    FamilyOnAdd,
    FamilyOnRemove,
    OnWorldInitializedHandler {

    private val batch = inject<PolygonSpriteBatch>()

    private lateinit var mainCam: OrthographicCamera

    init {
        doSort = true
    }

    override fun onWorldInitialized() {
        mainCam = world.system<MainCameraStateSystem>().camera
    }

    override fun onAddEntity(entity: Entity) {
        doSort = true

        val renderer = entity[DrawableRenderer].renderer
        with(renderer) {
            world.validate(entity)
        }

        entity[DrawableOrder].onOrderChange += {
            doSort = true
        }
    }

    override fun onRemoveEntity(entity: Entity) {
        doSort = true
    }

    override fun onTick() {
        batch.projectionMatrix = mainCam.combined
        batch.begin()
        super.onTick()
        batch.end()
    }

    override fun onSort() {
        if (doSort) {
            doSort = sortingType == Automatic
            family.sort(comparator)

//            debug {
//                "SORTED: " + family.getEntityNames(world, ", ") { "${it.getPrintName(world)}:${it[DrawableOrder].order}" }
//            }
        }
    }

    override fun onTickEntity(entity: Entity) {
//        if (!entity[DrawableVisibility].isVisible) {
//            return
//        }

        val renderer = entity[DrawableRenderer].renderer
        with(renderer) {
            world.render(entity, batch)
        }
    }
}
