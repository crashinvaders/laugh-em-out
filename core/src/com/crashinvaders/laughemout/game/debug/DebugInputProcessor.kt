package com.crashinvaders.laughemout.game.debug

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.TimeManager
import com.crashinvaders.common.toggle
import com.crashinvaders.laughemout.App
import com.crashinvaders.laughemout.game.controllers.GameOverHelper
import com.crashinvaders.laughemout.game.controllers.SpeechBubbleHelper
import com.crashinvaders.laughemout.game.debug.controllers.DebugController
import com.crashinvaders.laughemout.game.debug.controllers.FreeCameraDebugController
import com.crashinvaders.laughemout.game.debug.controllers.tests.*
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.systems.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.*
import com.crashinvaders.laughemout.game.engine.systems.postprocessing.PostProcessingSystem
import com.crashinvaders.laughemout.game.hud.GameHudSystem
import ktx.app.KtxInputAdapter
import ktx.collections.gdxMapOf
import ktx.collections.set
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2
import kotlin.reflect.KClass

class DebugInputProcessor(private val fleksWorld: FleksWorld) : KtxInputAdapter, Disposable {

    private val debugControllers = gdxMapOf<KClass<out DebugController>, DebugController>()

    private val timeManager = fleksWorld.inject<TimeManager>()

    private var pauseToken: TimeManager.Token? = null
    private val speedUpToken = TimeManager.Token("DebugSpeedUp", 0, false, 4f)

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.Q -> {
                // Toggle transform debug renderer.
                if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    fleksWorld.system<TransformDebugRenderSystem>().toggle()
                    return true
                }
                // Toggle physics debug renderer.
                if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
                    fleksWorld.system<PhysDebugRenderSystem>().toggle()
                    return true
                }
                // Toggle drawable debug renderer.
                fleksWorld.system<DrawableDebugRenderSystem>().toggle()
                return true
            }
            Keys.W -> {
                // Toggle HUD debug renderer.
                if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    fleksWorld.system<GameHudSystem>().toggle()
                    return true
                }

                // Toggle HUD visibility.
                val stage = fleksWorld.system<GameHudSystem>().stage
                stage.isDebugAll = !stage.isDebugAll
                return true
            }
            Keys.E -> {
                // Toggle post-processing.
                fleksWorld.system<PostProcessingSystem>().toggle()
                return true
            }
            Keys.T -> {
                // Free camera.
                toggleDebugController { FreeCameraDebugController(fleksWorld) }
                return true
            }

            // Debug controllers.
            Keys.F -> {
                //Toggle game pause.
                if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    val timeManager = fleksWorld.inject<TimeManager>()
                    if (pauseToken == null) {
                        pauseToken = TimeManager.Token("DebugPause", Int.MAX_VALUE, true, 0f)
                        timeManager.addToken(pauseToken!!)
                    } else {
                        timeManager.removeToken(pauseToken!!)
                        pauseToken = null
                    }
                    return true
                }
                // Game time speed-up.
                timeManager.addToken(speedUpToken)
                return true
            }

            // Test world setups.
            Keys.S -> {
                toggleDebugController { TransformHierarchyTest(fleksWorld) }
                return true
            }
            Keys.D -> {
                toggleDebugController { SimplePhysicsTest(fleksWorld) }
                return true
            }
            Keys.G -> {
                toggleDebugController { EntityActionSystemTest(fleksWorld) }
                return true
            }
//            Keys.H -> {
//                toggleDebugController { SpineSkeletonTest(fleksWorld) }
//                return true
//            }
//            Keys.J -> {
//                toggleDebugController { TiledMapTest(fleksWorld) }
//                return true
//            }

            Keys.NUM_1 -> {
                // Toggle SOD system.
                if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    fleksWorld.system<SodInterpolationPreRenderSystem>().toggle()
                    fleksWorld.system<SodInterpolationPostRenderSystem>().toggle()
                    return true
                }
                return false
            }

//            // Music theme test.
//            Keys.NUM_6 -> {
//                App.Inst.gameMusic.playMusic(GameMusicController.ThemeType.Menu)
//            }
//            Keys.NUM_7 -> {
//                App.Inst.gameMusic.playMusic(GameMusicController.ThemeType.Intermission)
//            }
//            Keys.NUM_8 -> {
//                App.Inst.gameMusic.playMusic(GameMusicController.ThemeType.Action)
//            }
//            Keys.NUM_9 -> {
//                App.Inst.gameMusic.playMusic(GameMusicController.ThemeType.Outro, instant = true)
//            }
//            Keys.NUM_0 -> {
//                App.Inst.gameMusic.stopMusic()
//            }

            Keys.N -> {
                val (x, y) = mouseWorldPos()
                SpeechBubbleHelper.createBubble(fleksWorld, "Pew pew\nshakalaka\npew pew meow", x, y, 2.0f)
                return true
            }

            Keys.M -> {
                GameOverHelper.showGameOver(fleksWorld, MathUtils.random(0, 11)) {
                    App.Inst.restart()
                }
                return true
            }

            Keys.B -> {
                debug { "Pew" }
                val entity = fleksWorld.entity {
                    it += Info("ActionHost")
                }

                fleksWorld.system<EntityActionSystem>().actions(entity) {
                    parallel {
                        repeat(4) {
                            sequence {
                                delay(0.5f)
                                runnable { debug { "Hello world!" } }
                                removeEntity()
                                runnable { with(it.world) { debug { "Entity name is ${it.entity[Info].name}" } } }
                            }
                        }
                        repeat {
                            sequence {
                                delay(0.1f)
                                runnable { debug { "HHH" } }
                            }
                        }
                    }
                }

//                fleksWorld.system<EntityActionSystem>().addAction(entity, SequenceAction(
//                    DelayAction(0.5f),
//                    RunnableAction { debug { "Before entity removed" } },
//
//                    ParallelAction(
//                        RunnableAction { fleksWorld -= entity },
//                        RunnableAction { with(fleksWorld) { debug { "Parallel action: ${it[Info].name}" } } },
//                    ),
//                    DelayAction(0.5f),
//                    RunnableAction { debug { "After entity removed" } },
//                ))

                return true
            }

            else -> return false
        }
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Keys.F -> {
                // Game time speed-up.
                timeManager.removeToken(speedUpToken)
                return true
            }
            else -> return false
        }
    }

    private fun mouseWorldPos(): Vector3 {
        return fleksWorld.system<MainCameraStateSystem>().screenToWorld(Gdx.input.x, Gdx.input.y)
    }

    private inline fun <reified T : DebugController> toggleDebugController(controllerCreator: () -> T) {
        val controller = debugControllers.remove(T::class)
        if (controller != null) {
            controller.dispose()
            return
        }
        debugControllers[T::class] = controllerCreator()
    }

    override fun dispose() {
        debugControllers.forEach { it.value.dispose() }
        debugControllers.clear()
    }
}
