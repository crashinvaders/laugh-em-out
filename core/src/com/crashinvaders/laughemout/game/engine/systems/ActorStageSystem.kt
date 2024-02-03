package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.crashinvaders.common.OrderedInputMultiplexer
import com.crashinvaders.common.TimeManager
import com.crashinvaders.common.events.Event
import com.crashinvaders.common.events.EventBus
import com.crashinvaders.common.events.EventHandler
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.engine.OnResizeEvent
import com.crashinvaders.laughemout.game.engine.TimeMode
import com.crashinvaders.laughemout.game.engine.components.ActorContainer
import com.crashinvaders.laughemout.game.engine.components.WorldCamera
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import ktx.actors.minusAssign
import ktx.actors.plusAssign

class ActorStageSystem : IteratingSystem(
    family { all(ActorContainer) }
),
    FamilyOnAdd,
    FamilyOnRemove,
    OnWorldInitializedHandler,
    EventHandler {

    private val viewport = ScreenViewport()

    private val timeManager = world.inject<TimeManager>()

    val stage = Stage(viewport, inject<PolygonSpriteBatch>())

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
        world.inject<OrderedInputMultiplexer>().addProcessor(stage, GameInputOrder.STAGE_ACTORS)

        val eventBus: EventBus = world.inject()
        eventBus.addHandler<OnResizeEvent>(this)
    }

    override fun onDisable() {
        super.onDisable()
        world.inject<OrderedInputMultiplexer>().removeProcessor(stage)

        val eventBus: EventBus = world.inject()
        eventBus.removeHandlers(this)
    }

    override fun onAddEntity(entity: Entity) {
        val actor = entity[ActorContainer].actor
        actor.userObject = entity
        stage += actor
    }

    override fun onRemoveEntity(entity: Entity) {
        stage -= entity[ActorContainer].actor
    }

    override fun onTickEntity(entity: Entity) {
        val cActorContainer = entity[ActorContainer]
        val deltaTime = when(cActorContainer.timeMode) {
            TimeMode.GameTime -> timeManager.delta
            TimeMode.UnscaledTime -> timeManager.deltaUnscaled
        }
        val actor = cActorContainer.actor
        actor.act(deltaTime)
    }

    override fun onEvent(event: Event) {
        when (event) {
            is OnResizeEvent -> {
                stage.viewport.update(event.screenWidth, event.screenHeight, true)
            }
        }
    }
}
