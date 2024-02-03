package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromDrawablePixels
import com.crashinvaders.laughemout.game.engine.components.GdxDrawableContainer
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*

object EnvironmentHelper {

    fun createObjects(world: FleksWorld) {
        val atlasEnv = world.inject<TextureAtlas>("env")

//        world.entity {
//            it += Info("ComedyStage")
//            it += Transform().apply {
//                localPositionX = -55f * UPP
//                localPositionY = -25f * UPP
//            }
//
//            val actor = Image(atlasEnv.findRegion("stage0"))
//            it += ActorContainer(actor)
//            it += DrawableRendererContainer(ActorRenderer)
//            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK)
//            it += DrawableTint()
//            it += DrawableVisibility()
//            it += DrawableDimensions().fromDrawablePixels(drawable)
//            it += DrawableOrigin(Align.bottomLeft)
//        }
//
//        world.entity {
//            it += Info("GameTitle")
//            it += Transform().apply {
//                localPositionX = +15f * UPP
//                localPositionY = +25f * UPP
//            }
//
//            val actor = Image(atlasEnv.findRegion("title-leo0"))
//            it += ActorContainer(actor)
//            it += DrawableRendererContainer(ActorRenderer)
//            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK - 5)
//            it += DrawableTint()
//            it += DrawableVisibility()
//            it += DrawableDimensions().fromDrawablePixels(drawable)
//            it += DrawableOrigin(Align.bottomLeft)
//        }
//
//        world.entity {
//            it += Info("CrowdGround")
//            it += Transform().apply {
//                localPositionX = -73f * UPP
//                localPositionY = -46f * UPP
//            }
//
//            val actor = Image(atlasEnv.findRegion("ground0"))
//            it += ActorContainer(actor)
//            it += DrawableRendererContainer(ActorRenderer)
//            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK - 10)
//            it += DrawableTint()
//            it += DrawableVisibility()
//            it += DrawableDimensions().fromDrawablePixels(drawable)
//            it += DrawableOrigin(Align.bottomLeft)
//        }

        world.entity {
            it += Info("ComedyStage")
            it += Transform().apply {
                localPositionX = -55f * UPP
                localPositionY = -25f * UPP
            }

            val drawable = TextureRegionDrawable(atlasEnv.findRegion("stage0"))
            it += GdxDrawableContainer(drawable)
            it += DrawableRenderer(GdxDrawableEntityRenderer)
            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromDrawablePixels(drawable)
            it += DrawableOrigin(Align.bottomLeft)
        }

        world.entity {
            it += Info("GameTitle")
            it += Transform().apply {
                localPositionX = +15f * UPP
                localPositionY = +25f * UPP
            }

            val drawable = TextureRegionDrawable(atlasEnv.findRegion("title-leo0"))
            it += GdxDrawableContainer(drawable)
            it += DrawableRenderer(GdxDrawableEntityRenderer)
            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK - 5)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromDrawablePixels(drawable)
            it += DrawableOrigin(Align.bottomLeft)
        }

        world.entity {
            it += Info("CrowdGround")
            it += Transform().apply {
                localPositionX = -73f * UPP
                localPositionY = -46f * UPP
            }

            val drawable = TextureRegionDrawable(atlasEnv.findRegion("ground0"))
            it += GdxDrawableContainer(drawable)
            it += DrawableRenderer(GdxDrawableEntityRenderer)
            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK - 10)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromDrawablePixels(drawable)
            it += DrawableOrigin(Align.bottomLeft)
        }
    }
}