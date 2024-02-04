package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.set
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.engine.components.GdxDrawableContainer
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem.Companion.actions
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.removeEntity
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.runnable
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.sequence
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.tintTo

object ScreenFadeHelper {

    fun createFadeInEffect(world: FleksWorld, duration: Float) {
        val eFade = world.entity {
            it += Info("FadeIn")
            it += Transform().apply {
                    parent = world.system<MainCameraStateSystem>().cameraTransform
            }

            val atlas = world.inject<TextureAtlas>("ui")
            val drawable = TextureRegionDrawable(atlas.findRegion("white8"))

            it += GdxDrawableContainer(drawable)
            it += DrawableRenderer(GdxDrawableEntityRenderer)
            it += DrawableOrder(GameDrawOrder.SCREEN_FADE)
            it += DrawableTint(Color.BLACK)
            it += DrawableVisibility()
            it += DrawableDimensions(128f)
            it += DrawableOrigin()
        }

        world.actions(eFade) {
            sequence {
                tintTo(0x00000000_u, duration)
                removeEntity()
            }
        }
    }

    fun createFadeOutEffect(world: FleksWorld, duration: Float, onComplete: (() -> Unit)? = null) {
        val eFade = world.entity {
            it += Info("FadeIn")
            it += Transform().apply {
                    parent = world.system<MainCameraStateSystem>().cameraTransform
            }

            val atlas = world.inject<TextureAtlas>("ui")
            val drawable = TextureRegionDrawable(atlas.findRegion("white8"))

            it += GdxDrawableContainer(drawable)
            it += DrawableRenderer(GdxDrawableEntityRenderer)
            it += DrawableOrder(GameDrawOrder.SCREEN_FADE)
            it += DrawableTint().apply { color.set(0x00000000_u) }
            it += DrawableVisibility()
            it += DrawableDimensions(128f)
            it += DrawableOrigin()
        }

        world.actions(eFade) {
            sequence {
                tintTo(0x000000ff_u, duration)
                runnable { onComplete?.invoke() }
                removeEntity()
            }
        }
    }
}