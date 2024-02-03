package com.crashinvaders.laughemout.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.crashinvaders.common.OrderedDisposableContainer
import com.crashinvaders.common.OrderedDisposableRegistry
import com.crashinvaders.common.OrderedInputMultiplexer
import com.crashinvaders.common.events.EventBus
import com.crashinvaders.laughemout.App
import com.crashinvaders.laughemout.game.controllers.JokeBuilderUiController
import com.crashinvaders.laughemout.game.controllers.JokeGameManager
import com.crashinvaders.laughemout.game.engine.OnResizeEvent
import com.crashinvaders.common.TimeManager
import com.crashinvaders.laughemout.game.debug.DebugInputProcessor
import com.crashinvaders.laughemout.game.engine.components.render.SkeletonEntityRenderer
import com.crashinvaders.laughemout.game.engine.systems.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.PostProcessingSystem
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.effects.ColorIndexPostEffect
import com.crashinvaders.laughemout.game.hud.GameHudSystem
import com.esotericsoftware.spine.SkeletonRenderer
import com.github.quillraven.fleks.configureWorld
import com.github.tommyettinger.textra.Font
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.freetype.generateFont

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class GameScreen : KtxScreen,
    OrderedDisposableRegistry by OrderedDisposableContainer() {

    val timeManager = TimeManager()

    val eventBus = EventBus()

    val shapeRenderer = ShapeRenderer().alsoRegisterDisposable()

    val batch = PolygonSpriteBatch().alsoRegisterDisposable()

    val b2dWorld = PhysWorldSystem.createWorld().alsoRegisterDisposable()

    val clearColor = Color.valueOf("544470")

    val debugFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/JetBrainsMonoNL-Regular.ttf"))
        .generateFont {
            size = 12
            borderWidth = 2f
            borderColor = Color.BLACK
        }.apply {
            data.markupEnabled = true
        }.alsoRegisterDisposable()

    private val inputMultiplexer = OrderedInputMultiplexer()

    private val fleksWorld = configureWorld {
        val assets = App.Inst.assets

        injectables {
            add(assets)
            add(timeManager)
            add(eventBus)
            add(shapeRenderer)
            add(batch)
            add(b2dWorld)
            add(inputMultiplexer)
            add("clearColor", clearColor)
            add("debugFont", debugFont)
            add("ui", assets.get<TextureAtlas>("atlases/ui.atlas"))
            add("env", assets.get<TextureAtlas>("atlases/env.atlas"))
            add("characters", assets.get<TextureAtlas>("skeletons/characters.atlas"))
            add("bmPixolaCurva", assets.get<BitmapFont>("fonts/pixola-cursiva.fnt"))
            add("pixolaCurva", Font(assets.get<BitmapFont>("fonts/pixola-cursiva.fnt")).also {
                val fontScale = UPP * it.originalCellWidth
                it.scaleTo(fontScale, fontScale)
            }.alsoRegisterDisposable())

            val skeletonRenderer = SkeletonRenderer()
            add(skeletonRenderer)
            add(SkeletonEntityRenderer(skeletonRenderer))
        }

        systems {
            val postProcessingSystem = PostProcessingSystem()

            add(EntityActionSystem())

//region Pre-engine game controllers
//endregion

//region Engine core
            // Physics
            add(TransformToPhysMapperSystem())
            add(PhysWorldSystem())
            add(PhysToTransformMapperSystem())
            add(PhysContactSubscriptionSystem())

            // Skeleton related systems
            add(SkeletonUpdateSystem())
            add(SkeletonBoneParentingSystem())

            add(ActorStageSystem())

            // Update cameras
//            add(MainCameraStateSystem(ScreenViewport().apply { unitsPerPixel = 1f/32f }))
            add(MainCameraStateSystem(object : ExtendViewport(12f, 8f) {
                override fun apply(centerCamera: Boolean) = Unit // We don't need the viewport to update its camera.
            }))
            add(WorldCameraSystem())

            // [Pre Render] Interpolation
            add(SodInterpolationPreRenderSystem())

            // Renderers
            add(postProcessingSystem.captureBeginSubsystem)
            add(DrawableRenderSystem())
            add(postProcessingSystem.captureEndSubsystem)
            add(postProcessingSystem)
            add(PhysDebugRenderSystem())
            add(TransformDebugRenderSystem().apply { enabled = false })
            add(DrawableDebugRenderSystem().apply { enabled = false })

            // [Post Render] Interpolations
            add(SodInterpolationPostRenderSystem())
//endregion

//region Post-engine game controllers

            add(GameHudSystem())

            add(JokeBuilderUiController().apply { enabled = false })
            add(JokeGameManager())
//endregion
        }
    }.also {
        registerDisposable({ it.dispose() }, -1000)
    }

    // Debug input processor.
    init {
        val debugInput = DebugInputProcessor(fleksWorld)
        inputMultiplexer.addProcessor(debugInput, GameInputOrder.DEBUG_KEYS)
        registerDisposable(debugInput)
    }

    // Test effect.
    init {
//        val paletteTexture = Texture(Gdx.files.internal("textures/colorful-cage24-colormap16.png")).alsoRegisterDisposable()
        val paletteTexture = Texture(Gdx.files.internal("textures/palette-lto-colormap16.png")).alsoRegisterDisposable()
        val referenceTexture = Texture(Gdx.files.internal("textures/color-map-reference16.png")).alsoRegisterDisposable()

        fleksWorld.system<PostProcessingSystem>().apply {
//            addEffect(
//                ConvolutionPostEffect(ConvolutionPostEffect.CONV5_BLUR_ROUGH, 5).alsoRegisterDisposable()
//            )
            addEffect(
                ColorIndexPostEffect(
                    ColorIndexPostEffect.TransparencyMode.None,
                    paletteTexture
                ).alsoRegisterDisposable()
            )
        }

//        fleksWorld.entity {
//            it += Info("Palette")
//            it += Transform()
//
//            it += ActorContainer(Image(paletteTexture))
//            it += RendererContainer(ActorRenderer)
//            it += DrawableOrder()
//            it += DrawableTint()
//            it += DrawableVisibility()
//            it += DrawableDimensions(8f, 1f)
//            it += DrawableOrigin()
//        }
//
//        fleksWorld.entity {
//            it += Info("Reference")
//            it += Transform().apply {
//                localPositionY = -1f;
//            }
//
//            it += ActorContainer(Image(referenceTexture))
//            it += RendererContainer(ActorRenderer)
//            it += DrawableOrder()
//            it += DrawableTint()
//            it += DrawableVisibility()
//            it += DrawableDimensions(4f, 1f)
//            it += DrawableOrigin()
//        }
    }

    override fun show() {
        super.show()

        App.Inst.inputMultiplexer.addProcessor(inputMultiplexer, 0)

        // Initialize world.
        with(fleksWorld) {
            // Initialize systems.
            for (i in 0 until systems.size) {
                val system = systems[i]
                if (system is OnWorldInitializedHandler) {
                    system.onWorldInitialized()
                }
            }
        }
    }

    override fun hide() {
        super.hide()

        App.Inst.inputMultiplexer.removeProcessor(inputMultiplexer)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

//        viewport.update(width, height,  true)

        fleksWorld.systems.forEach {
            if (it is OnWorldResizeHandler) {
                it.onResize(width, height)
            }
        }

        eventBus.dispatch(OnResizeEvent(width, height))
    }

    override fun render(delta: Float) {
        clearScreen(clearColor.r, clearColor.g, clearColor.b, clearColor.a, clearDepth = false)

        val deltaProcessed = timeManager.process(delta)

        GdxAI.getTimepiece().update(deltaProcessed)
        fleksWorld.update(deltaProcessed)
    }
}
