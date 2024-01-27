package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedDisposableContainer
import com.crashinvaders.common.OrderedDisposableRegistry
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActor
import com.crashinvaders.laughemout.game.components.Comedian
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.Entity
import ktx.collections.GdxArray
import ktx.math.component1
import ktx.math.component2

class JokeGameManager(private val world: FleksWorld) :
    OrderedDisposableRegistry by OrderedDisposableContainer() {

    private val skelRenderer = world.inject<SkeletonRenderer>()
    private val assets = world.inject<AssetManager>()
    private val atlasCharacters = assets.get<TextureAtlas>("skeletons/characters.atlas")
    private val atlasEnv = assets.get<TextureAtlas>("atlases/env.atlas")

    private val comedian: Entity
    private val audienceMembers = GdxArray<Entity>()

    init {
        with(world) {
            createEnvironment()

            comedian = createComedian(0f, 0f)

            apply {
                val audienceMemberCount = 5
                for (i in 0 until audienceMemberCount) {
                    val (x, y) = AudienceMemberHelper.evalSpawnPosition(i)
                    val entity = AudienceMemberHelper.create(world, x, y)
                    audienceMembers.add(entity)
                }
            }
        }
    }

    private fun FleksWorld.createEnvironment() {
        entity {
            it += Info("ComedyStage")
            it += Transform().apply {
                localPositionX = -55f * UPP
                localPositionY = -25f * UPP
            }

            val actor = Image(atlasEnv.findRegion("stage0"))
            it += ActorContainer(actor)
            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActor(actor)
            it += DrawableOrigin(Align.bottomLeft)
        }

        entity {
            it += Info("CrowdGround")
            it += Transform().apply {
                localPositionX = -73f * UPP
                localPositionY = -46f * UPP
            }

            val actor = Image(atlasEnv.findRegion("ground0"))
            it += ActorContainer(actor)
            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK - 10)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActor(actor)
            it += DrawableOrigin(Align.bottomLeft)
        }
    }

    private fun FleksWorld.createComedian(x: Float, y: Float): Entity {
        val skelData = SkeletonBinary(atlasCharacters)
            .apply { scale = UPP }
            .readSkeletonData(Gdx.files.internal("skeletons/comedian0.skel"))
//        skelData.defaultSkin = skelData.skins[0]

        val skeleton = Skeleton(skelData)
        val animState = AnimationState(AnimationStateData(skelData))
        val skelActor = SkeletonActor(skelRenderer, skeleton, animState)

        animState.setAnimation(0, "animation", true)

        val entity = world.entity {
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
        return entity
    }
}