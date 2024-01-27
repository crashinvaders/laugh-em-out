package com.crashinvaders.laughemout.game.hud

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.utils.Align
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TypingLabel
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.RemovalObserver.Companion.addRemovalListener
import com.crashinvaders.laughemout.game.engine.components.RemovalObserver.Companion.removeRemovalListener
import com.crashinvaders.laughemout.game.engine.components.render.*
import ktx.actors.onTouchDown
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2

class TestHud0(
    hud: GameHudSystem
) : GameHudSystem.Controller(hud),
    DisposableRegistry by DisposableContainer() {

    override fun init() {
        super.init()

        val assets = world.inject<AssetManager>()
//        val font = assets.get<BitmapFont>("fonts/pixola-cursiva.fnt")
//        val font = Font().alsoRegister()
        val font = Font(assets.get<BitmapFont>("fonts/pixola-cursiva.fnt")).alsoRegister()
//        KnownFonts.addEmoji(font)

        fun entityLabel() {
            val fontScale = UPP * font.originalCellWidth
            font.scaleTo(fontScale, fontScale)

            val tWait = "{WAIT=0.25}"
//            val label = TextraLabel("This is a [!][light ORANGE][%125]test[][][][] message", font)
            val label = TypingLabel("{SICK}{EASE}This ${tWait}is ${tWait}a [!][light ORANGE][%125]test[][][][] ${tWait}message", font)
            label.skipToTheEnd()
            label.align = Align.center
            label.pack()

            val group = Group()
            group.addActor(label)
            group.setSize(label.width, label.height)
            group.isTransform = true

            val entity = world.entity {
                it += Info("Label")
                it += Transform().apply {
                    localPositionX = 0f
                    localPositionY = 1f
                }
                it += ActorContainer(group)
                it += DrawableOrder()
                it += DrawableTint()
                it += DrawableVisibility()
                it += DrawableDimensions(group.width, group.height)
                it += DrawableOrigin()

                it += SodInterpolation(4f, 0.6f, 0.5f).apply {
//                    setAccel(0f, 0f, 0f, -10f, 25f)
                    setAccel(0f, 0f,
                        MathUtils.random(-5f, +5f),
                        -20f, +50f)
                }

                it += RemovalObserver {
                    debug { "Bingo!" }
                }
            }

            val removalListener = with(world) {
                addRemovalListener(entity) {
                    debug { "Throw it away!" }
                }
            }

            label.onTouchDown {
                with(world) {
                    removeRemovalListener(entity, removalListener)

                    entity[Transform].also {
                        var (x, y) = it.worldPosition
                        x += MathUtils.random(-1f, 1f)
                        y += MathUtils.random(-1f, 1f)
                        it.setWorldPosition(x, y)
                    }
                    entity[SodInterpolation].also {
                        it.addAccel(0f, 0f, 0f, -20f, +50f)
                    }
                }
                world -= entity
            }
        }

        fun stageLabel() {
            val tWait = "{WAIT=0.25}"
            val label = TypingLabel("{EASE}This ${tWait}is ${tWait}a [!][light ORANGE][%125]test[][][][] ${tWait}message", font)
            label.setScale(1f)

            label.align = Align.center
            val container = Container(label).apply {
                align(Align.bottomRight)
                pad(8f)
            }
            root.add(container)
        }

        entityLabel()
//        stageLabel()
    }
}
