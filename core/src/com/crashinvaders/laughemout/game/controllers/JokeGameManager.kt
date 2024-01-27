package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedDisposableContainer
import com.crashinvaders.common.OrderedDisposableRegistry
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.components.Comedian
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.SkeletonContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.Entity
import ktx.collections.GdxArray

class JokeGameManager(private val world: FleksWorld) :
    OrderedDisposableRegistry by OrderedDisposableContainer() {

    private val skelRenderer = world.inject<SkeletonRenderer>()
    private val assets = world.inject<AssetManager>()
    private val atlasCharacters = assets.get<TextureAtlas>("skeletons/characters.atlas")

    private val comedian: Entity
    private val audienceMembers = GdxArray<Entity>()

    init {
        with(world) {
            comedian = createComedian(0f, 0f)

            apply {
                val audienceMemberCount = 5
                val startX = 45f * UPP
                val startY = -24f * UPP
                val stepX = 26f * UPP
                val backRowShiftY = 16 * UPP
                for (i in 0 until audienceMemberCount) {
                    val isFrontRow = i % 2 != 0
                    val x = startX + stepX * i
                    val y = startY + if (isFrontRow) 0f else backRowShiftY
                    val entity = AudienceMemberHelper.create(world, x, y)
                    audienceMembers.add(entity)
                }
            }
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

//                it += SodInterpolation(4f, 0.4f, -0.5f)
//                it += TransformDebugRenderTag
        }
        return entity
    }
}