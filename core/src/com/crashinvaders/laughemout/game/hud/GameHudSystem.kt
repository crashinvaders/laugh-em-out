package com.crashinvaders.laughemout.game.hud

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedInputMultiplexer
import com.crashinvaders.common.events.Event
import com.crashinvaders.common.events.EventBus
import com.crashinvaders.common.events.EventHandler
import com.github.quillraven.fleks.IntervalSystem
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.engine.OnResizeEvent
import ktx.collections.GdxArrayMap
import kotlin.reflect.KClass

class GameHudSystem : IntervalSystem(), EventHandler {

    private val controllers = GdxArrayMap<KClass<out Controller>, Controller>()

    val stage = Stage(
        ExtendViewport(320f, 200f),
        world.inject<PolygonSpriteBatch>())

    override fun onInit() {
        super.onInit()

        val eventBus: EventBus = world.inject()
        eventBus.addHandler<OnResizeEvent>(this)

        val inputMultiplexer: OrderedInputMultiplexer = world.inject()
        inputMultiplexer.addProcessor(stage, GameInputOrder.HUD)

        fun addController(controller: Controller) =
            controllers.put(controller::class, controller)

        addController(TestHud0(this))
        //TODO Add controllers

        for (i in 0 until controllers.size) {
            controllers.getValueAt(i).init()
        }
    }

    override fun onDispose() {
        super.onDispose()
        stage.dispose()

        val eventBus: EventBus = world.inject()
        eventBus.removeHandler<OnResizeEvent>(this)

        for (i in 0 until controllers.size) {
            controllers.getValueAt(i).dispose()
        }
    }

    override fun onEvent(event: Event) {
        when (event) {
            is OnResizeEvent -> {
                stage.viewport.update(event.screenWidth, event.screenHeight, true)
            }
        }
    }

    override fun onTick() {
        val deltaTime = this.deltaTime

        for (i in 0 until controllers.size) {
            controllers.getValueAt(i).update(deltaTime)
        }

        stage.act(deltaTime)
        stage.draw()
    }

    inline fun <reified T : Controller> controller(): T =
        controller(T::class)

    fun <T : Controller> controller(type: KClass<T>): T =
        (controllers.get(type) ?:
            throw GdxRuntimeException("There's no controller with type: ${type.simpleName}"))
            as T

    open abstract class Controller(
        protected val hud: GameHudSystem
    ): Disposable {

        protected val world: FleksWorld; get() = hud.world

        protected val root = Stack().also {
            it.setFillParent(true)
            it.touchable = Touchable.childrenOnly
        }

        var enabled: Boolean
            get() = root.isVisible
            set(value) {
                root.isVisible = value
            }

        open fun init() {
            hud.stage.addActor(root)
        }

        open fun update(deltaTime: Float) = Unit

        override fun dispose() = Unit

        fun toggle() {
            enabled = !enabled
        }
    }
}
