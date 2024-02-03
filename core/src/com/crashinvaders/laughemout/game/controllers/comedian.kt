package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActorPixels
import com.crashinvaders.laughemout.game.components.Comedian
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.Entity
import ktx.actors.onTouchDown

object ComedianHelper {

    fun createComedian(world: FleksWorld, x: Float, y: Float): Entity {
        val atlasCharacters = world.inject<TextureAtlas>("characters")
        val skelRenderer = world.inject<SkeletonRenderer>()

        val skelData = SkeletonBinary(atlasCharacters)
            .apply { scale = UPP }
            .readSkeletonData(Gdx.files.internal("skeletons/comedian0.skel"))

        val skeleton = Skeleton(skelData)
        val animState = AnimationState(AnimationStateData(skelData))
        val skelActor = SkeletonActor(skelRenderer, skeleton, animState)

        animState.setAnimation(0, "animation", true)

        val eComedian = world.entity {
            it += Info("Comedian")
            it += Comedian()
            it += Transform().apply {
                localPositionX = x
                localPositionY = y
            }

            it += SkeletonContainer(skeleton, animState)

            it += ActorContainer(skelActor)
            it += DrawableOrder(order = GameDrawOrder.COMEDIAN)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions(0f)
            it += DrawableOrigin(Align.bottom)
        }

//        createTestBoneParentingEntity(world, eComedian)

        return eComedian
    }

    private fun createTestBoneParentingEntity(world: FleksWorld, eComedian: Entity) {
        val atlasCharacters = world.inject<TextureAtlas>("characters")

        with(world) {
            eComedian[SkeletonContainer].animState.setAnimation(1, "test-parenting-bone", true)
        }

        world.entity {
            it += Info("test-parenting-bone")
            it += Transform().apply {
                parent = eComedian[Transform]
            }
            it += SkeletonBoneParenting("test-parenting-bone")

            val actor = Image(atlasCharacters.findRegion("char-head-w-s9")!!)

            it += ActorContainer(actor)
            it += DrawableOrder()
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActorPixels(actor)
            it += DrawableOrigin(Align.center)

            it += SodInterpolation(4f, 0.4f, -0.5f)

            it += TransformDebugRenderTag

            actor.apply {
                touchable = Touchable.enabled
                val cSkelParenting = it[SkeletonBoneParenting]
                onTouchDown {
                    if (cSkelParenting.type() in it) {
                        it.configure {
                            it -= cSkelParenting.type()
                        }
                    } else {

                        it.configure {
                            it += cSkelParenting
                        }
                    }
                }
            }
        }
    }
}