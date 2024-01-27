package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.EntityComponent
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.components.AudienceMember
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.SkeletonContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.crashinvaders.common.CommonUtils.random
import ktx.app.gdxError

object EmoMeterHelper {

    private const val TRACK_FRAME = 0
    private const val TRACK_TOKEN = 1

    fun create(world: FleksWorld, audMemb: Entity): Entity {
        val cAmTransform: Transform
        val cAmAudMember: AudienceMember
        with(world) {
            cAmTransform = audMemb[Transform]
            cAmAudMember = audMemb[AudienceMember]
        }

        val skelRenderer = world.inject<SkeletonRenderer>()
        val assets = world.inject<AssetManager>()
        val atlasCharacters = assets.get<TextureAtlas>("atlases/ui.atlas")

        val skelData = SkeletonBinary(atlasCharacters)
            .apply { scale = UPP }
            .readSkeletonData(com.badlogic.gdx.Gdx.files.internal("skeletons/emotion-meter.skel"))

        val skeleton = Skeleton(skelData)
        val animState = AnimationState(AnimationStateData(skelData))
        val skelActor = SkeletonActor(skelRenderer, skeleton, animState)

        animState.update(MathUtils.random() * 10f)

        val entity = world.entity {
            it += Info("AudienceMember")
            it += EmoMeter(cAmAudMember)
            it += Transform().apply {
                parent = cAmTransform
                localPositionX = 0f
                localPositionY = 56f * UPP
            }

            it += SkeletonContainer(skeleton, animState)

            it += ActorContainer(skelActor)
            it += DrawableOrder(order = GameDrawOrder.UI_EMO_METER)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions(0f)
            it += DrawableOrigin(Align.bottom)

//                it += SodInterpolation(4f, 0.4f, -0.5f)
//                it += TransformDebugRenderTag
        }

        with(world) {
            entity[EmoMeter].emoValue = MathUtils.random(-3, 3)
        }

        animateValueChange(world, entity)
        animateToken(world, entity, TokenType.values().random())

        return entity
    }

    fun animateValueChange(world: FleksWorld, emoMeter: Entity) {
        val cEmoMeter: EmoMeter
        val cSkeleton: SkeletonContainer
        with(world) {
            cEmoMeter = emoMeter[EmoMeter]
            cSkeleton = emoMeter[SkeletonContainer]
        }

//        if (!cEmoMeter.isDirty) return
//        cEmoMeter.isDirty = false

        val emoValue = cEmoMeter.emoValue
        val imgNameFrame: String = when {
            cEmoMeter.isUnknown -> "unknown"
            emoValue <= -3 -> "neg3"
            emoValue == -2 -> "neg2"
            emoValue == -1 -> "neg1"
            emoValue == 0 -> "empty"
            emoValue == +1 -> "pos1"
            emoValue == +2 -> "pos2"
            emoValue >= +3 -> "pos3"
            else -> gdxError("Unexpected emo meter state")
        }

        cSkeleton.skeleton.setAttachment("frame", imgNameFrame)
        cSkeleton.animState.apply {
            setAnimation(TRACK_FRAME, "frame-change", false)
            addEmptyAnimation(TRACK_FRAME, 0f, 0f)
        }
    }

    fun animateToken(world: FleksWorld, emoMeter: Entity, tokenType: TokenType) {
        val cEmoMeter: EmoMeter
        val cSkeleton: SkeletonContainer
        with(world) {
            cEmoMeter = emoMeter[EmoMeter]
            cSkeleton = emoMeter[SkeletonContainer]
        }

        val imgName = tokenType.imgName
        val animName = if (tokenType.keepAfterHighlight) "token-highlight-keep" else "token-highlight-clear"
        cSkeleton.skeleton.setAttachment("token", imgName)
        cSkeleton.animState.setAnimation(TRACK_TOKEN, animName, false)
    }
}

class EmoMeter(val audMemb: AudienceMember) : EntityComponent<EmoMeter>() {

    var isDirty: Boolean = false

    var isUnknown: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            field = value
            isDirty = true
        }

    var emoValue: Int = 0
        set(value) {
            if (value == field) {
                return
            }
            field = value
            isDirty = true
        }

    override fun type() = EmoMeter
    companion object : ComponentType<EmoMeter>()
}

enum class TokenType(val imgName: String, val keepAfterHighlight: Boolean) {
    Like("pos-minor", keepAfterHighlight = false),
    Dislike("neg-minor", keepAfterHighlight = false),
    Star("gold-star", keepAfterHighlight = true),
    Cancel("canceled", keepAfterHighlight = true),
}