package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.crashinvaders.common.OrderedInputMultiplexer
import com.crashinvaders.common.events.Event
import com.crashinvaders.common.events.EventBus
import com.crashinvaders.common.events.EventHandler
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.FamilyOnRemove
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.compareEntityBy
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.engine.OnResizeEvent
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import ktx.actors.minusAssign
import ktx.actors.plusAssign
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2

class DrawableRenderSystem : IteratingSystem(
    family {
        all(
            Transform,
            ActorContainer,
            DrawableOrder,
            DrawableTint,
            DrawableVisibility,
            DrawableDimensions,
            DrawableOrigin
        )
    },
    comparator = compareEntityBy(DrawableOrder)
),
    FamilyOnAdd,
    FamilyOnRemove,
    OnWorldInitializedHandler,
    EventHandler {

    private val viewport = ScreenViewport()

    val stage = Stage(viewport, world.inject<PolygonSpriteBatch>())

    private var isPendingActorSort: Boolean = false

    init {
        stage.root.debugAll()

//        stage.addListener(object : InputListener() {
//            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
//                debug { "This is a click on stage!" }
//                return super.touchDown(event, x, y, pointer, button)
//            }
//        })
    }

    override fun onWorldInitialized() {
        val mainCamUpp = 1f / world.system<MainCameraStateSystem>().cameraEntity[WorldCamera].ppu
        viewport.unitsPerPixel = mainCamUpp
        viewport.camera = world.system<MainCameraStateSystem>().camera

        this.onEnable()
    }

    override fun onDispose() {
        super.onDispose()
        stage.dispose()
    }

    override fun onEnable() {
        super.onEnable()
        world.inject<OrderedInputMultiplexer>().addProcessor(stage, GameInputOrder.DRAWABLES)

        val eventBus: EventBus = world.inject()
        eventBus.addHandler<OnResizeEvent>(this)
    }

    override fun onDisable() {
        super.onDisable()
        world.inject<OrderedInputMultiplexer>().removeProcessor(stage)

        val eventBus: EventBus = world.inject()
        eventBus.removeHandler<OnResizeEvent>(this)
    }

    override fun onAddEntity(entity: Entity) {
        val actor = entity[ActorContainer].actor
        actor.userObject = entity
        stage += actor
//        stage.actors.sort { actor0, actor1 ->
//            val order0 = (actor0.userObject as Entity)[DrawableOrder].order
//            val order1 = (actor1.userObject as Entity)[DrawableOrder].order
//            order0.compareTo(order1)
//        }
    }

    override fun onRemoveEntity(entity: Entity) {
        stage -= entity[ActorContainer].actor
    }

    override fun onTick() {
        super.onTick()

        if (isPendingActorSort) {
            isPendingActorSort = false
            stage.actors.sort { a0, a1 ->
                (a0.userObject as Entity)[DrawableOrder].compareTo((a1.userObject as Entity)[DrawableOrder])
            }
        }

        stage.act(deltaTime)
        stage.draw()
    }

    override fun onTickEntity(entity: Entity) {
        val actor = entity[ActorContainer].actor
        val visible = entity[DrawableVisibility].isVisible
        actor.isVisible = visible

        if (!visible) {
            return
        }

        val drawableOrder = entity[DrawableOrder]
        if (drawableOrder.isDirty) {
            drawableOrder.isDirty = false
            isPendingActorSort = true
        }

        val origin = entity[DrawableOrigin]
        val tint = entity[DrawableTint]

        val dimensions = entity[DrawableDimensions]
        val width = dimensions.width
        val height = dimensions.height

        val transform = entity[Transform]
        val (posX, posY) = transform.worldPosition
        val (scaleX, scaleY) = transform.worldScale
        val rotation = transform.worldRotation

        val shiftX = -width * origin.x
        val shiftY = -height * origin.y

        val originX = origin.x * width
        val originY = origin.y * height

        actor.setSize(width, height)
        actor.setPosition(posX + shiftX, posY + shiftY)
        actor.setScale(scaleX, scaleY)
        actor.setOrigin(originX, originY)
        actor.rotation = rotation
        actor.color = tint.color
    }

    override fun onEvent(event: Event) {
        when (event) {
            is OnResizeEvent -> {
                stage.viewport.update(event.screenWidth, event.screenHeight, true)
            }
        }
    }
}
