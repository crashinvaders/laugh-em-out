package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.randomBoolean
import com.badlogic.gdx.math.Vector2
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.SkeletonContainer
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.esotericsoftware.spine.*
import com.esotericsoftware.spine.utils.SkeletonActor
import com.github.quillraven.fleks.Entity
import com.crashinvaders.common.CommonUtils.random
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.components.AudienceMember
import ktx.app.gdxError

object AudienceMemberHelper {

    private const val ANIM_TRACK_HEIGHT_LEVEL = 100

    private val tmpVec2 = Vector2()

    fun create(world: FleksWorld, x: Float, y: Float, placementIndex: Int): Entity {
        val skelRenderer = world.inject<SkeletonRenderer>()
        val assets = world.inject<AssetManager>()
        val atlasCharacters = assets.get<TextureAtlas>("skeletons/characters.atlas")

        val skelData = SkeletonBinary(atlasCharacters)
            .apply { scale = UPP }
            .readSkeletonData(com.badlogic.gdx.Gdx.files.internal("skeletons/audience-char.skel"))

        val skeleton = Skeleton(skelData)
        skeleton.rootBone.scaleX = -1f
        val animState = AnimationState(AnimationStateData(skelData))
        val skelActor = SkeletonActor(skelRenderer, skeleton, animState)

        animState.setAnimation(0, "test-idle", true)
        animState.update(MathUtils.random() * 10f)

        val entity = world.entity {
            it += Info("AudienceMember")
            it += generatedAppearance(world, placementIndex)
            it += Transform().apply {
                localPositionX = x
                localPositionY = y
            }

            it += SkeletonContainer(skeleton, animState)

            it += ActorContainer(skelActor)
            it += DrawableOrder(order = if (isFrontRow(placementIndex)) GameDrawOrder.AUDIENCE_FRONT_ROW else GameDrawOrder.AUDIENCE_BACK_ROW)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions(0f)
            it += DrawableOrigin(com.badlogic.gdx.utils.Align.bottom)
        }

        with(world) {
            entity[AudienceMember].emoLevel = MathUtils.random(-3, 3)
        }

        EmoMeterHelper.create(world, entity)

        setUpSkeleton(world, entity)

        return entity
    }

    fun evalSpawnPosition(index: Int): Vector2 {
        val startX = 45f * UPP
        val startY = -24f * UPP
        val stepX = 26f * UPP
        val backRowShiftY = 16 * UPP
        val isFrontRow = isFrontRow(index)
        val x = startX + stepX * index
        val y = startY + if (isFrontRow) 0f else backRowShiftY
        return tmpVec2.set(x, y)
    }

    fun isFrontRow(index: Int): Boolean {
        return index % 2 == 0
    }

    private fun generatedAppearance(world: FleksWorld, placementIndex: Int): AudienceMember {
        val race: Race = Race.values().random()
        val gender: Gender = Gender.values().random()
        val hat: Hat? = if (randomBoolean(0.7f)) null else Hat.values().random()
        val hairStyle: HairStyle? = if (hat != null) HairStyle.Hair0 else
            if (randomBoolean(0.1f)) null else
                HairStyle.values().filter { it.targetGender == null || it.targetGender == gender }.random()
        val hairColor: HairColor = HairColor.values().random()
        val heightLevel: HeightLevel = HeightLevel.values().random()
        val bodyStyle: BodyStyle = BodyStyle.values().filter { it.gender == gender }.random()
        val glasses: Glasses? = if (randomBoolean(0.8f)) null else Glasses.values().random()
        val neck: Neck? = if (randomBoolean(0.8f)) null else Neck.values().random()
        val mouth: Mouth? = if (randomBoolean(0.9f)) null else Mouth.values().filter { it.targetGender == null || it.targetGender == gender }.random()

        return AudienceMember(placementIndex, race, gender, hairStyle, hairColor, heightLevel, bodyStyle, glasses, hat, neck, mouth)
    }

    private fun setUpSkeleton(world: FleksWorld, audMemb: Entity) {
        with(world) {
            val cSkelContainer = audMemb[SkeletonContainer]
            val cAudMemb = audMemb[AudienceMember]

            val emoLevel = evalEmotionLevelType(cAudMemb.emoLevel)

            val skeleton = cSkelContainer.skeleton
            skeleton.setAttachment("char-body", cAudMemb.bodyStyle.imgName)
            skeleton.setAttachment("char-head", "char-head${cAudMemb.race.imgSuffix}${emoLevel.imgSuffix}")
            skeleton.setAttachment("char-hair", if (cAudMemb.hairStyle == null) null else "${cAudMemb.hairStyle.imgName}${cAudMemb.hairColor.imgSuffix}")
            skeleton.setAttachment("char-hat", if (cAudMemb.hat == null) null else "${cAudMemb.hat.imgName}")
            skeleton.setAttachment("char-glasses", if (cAudMemb.glasses == null) null else "${cAudMemb.glasses.imgName}")
            skeleton.setAttachment("char-neck", if (cAudMemb.neck == null) null else "${cAudMemb.neck.imgName}")
            skeleton.setAttachment("char-mouth", if (cAudMemb.mouth == null) null else when(cAudMemb.mouth) {
                Mouth.Beard0 -> "${cAudMemb.mouth.imgName}${cAudMemb.hairColor.imgSuffix}"
                else -> "${cAudMemb.mouth.imgName}"
            })

            val animState = cSkelContainer.animState
            when(cAudMemb.heightLevel) {
                HeightLevel.Short -> animState.setAnimation(ANIM_TRACK_HEIGHT_LEVEL, "height/low0", false)
                HeightLevel.Tall -> animState.setAnimation(ANIM_TRACK_HEIGHT_LEVEL, "height/high0", false)
                else -> Unit
            }
        }
    }

    private fun evalEmotionLevelType(emotionLevelVal: Int): EmotionLevel {
        return when {
            emotionLevelVal <= -3 -> EmotionLevel.Angry
            emotionLevelVal <= -1 -> EmotionLevel.Dislike
            emotionLevelVal == 0 -> EmotionLevel.Neutral
            emotionLevelVal == 1 -> EmotionLevel.Like
            emotionLevelVal == 2 -> EmotionLevel.Enjoy
            emotionLevelVal >= 3 -> EmotionLevel.Rofl
            else -> gdxError("Unexpected emotion level value: $emotionLevelVal")
        }
    }

    enum class EmotionLevel(val imgSuffix: String) {
        Angry("-s0"),
        Dislike("-s4"),
        Neutral("-s5"),
        Like("-s6"),
        Enjoy("-s8"),
        Rofl("-s9"),
    }

    enum class BodyStyle(val gender: Gender, val imgName: String) {
        M0(Gender.Male, "char-body-m0"),
        M1(Gender.Male, "char-body-m1"),
        M2(Gender.Male, "char-body-m2"),
        M3(Gender.Male, "char-body-m3"),
        M4(Gender.Male, "char-body-m4"),
        M5(Gender.Male, "char-body-m5"),
        M6(Gender.Male, "char-body-m6"),

        F0(Gender.Female, "char-body-f0"),
        F1(Gender.Female, "char-body-f1"),
        F2(Gender.Female, "char-body-f2"),
        F3(Gender.Female, "char-body-f3"),
        F4(Gender.Female, "char-body-f4"),
    }

    enum class Glasses(val imgName: String, val isFancy: Boolean = false) {
        Dredd0("glasses-dredd0"),
        Dredd1("glasses-dredd1"),
        Goggles0("glasses-goggles0"),
        Goggles1("glasses-goggles1"),
        Shades0("glasses-shades0"),
        Shades1("glasses-shades1"),
        Shades2("glasses-shades2"),
    }

    enum class Hat(val imgName: String, val isFancy: Boolean = false) {
        Blinder0("hat-blinder0"),
        Cap0Bw("hat-cap0-bw"),
        Cap0Fw("hat-cap0-fw"),
        Crown0("hat-crown0"),
        Pot0("hat-pot0"),
    }

    enum class Neck(val imgName: String, val isFancy: Boolean = false) {
        Chain0("neck-chain0", isFancy = true),
        Cross0("neck-cross0", isFancy = true),
        Medal0("neck-medal0", isFancy = true),
        Neckless0("neck-neckless0", isFancy = true),
        Scarf0("neck-scarf0"),
    }

    enum class Mouth(val imgName: String, val hideEmotions: Boolean = false, val isFancy: Boolean = false, val targetGender: Gender? = null) {
        Beard0("face-beard0", hideEmotions = true),
        FaceMask0("face-mask0", hideEmotions = true, targetGender = Gender.Male),
    }

    enum class Race(val imgSuffix: String) {
        White("-w"),
        Black("-b"),
        Asian("-c"),
    }

    enum class Gender {
        Male,
        Female,
    }

    enum class HairStyle(val imgName: String, val targetGender: Gender? = null) {
        Hair0("hair0"),
        Hair1("hair1"),
        Hair2("hair2", targetGender = Gender.Female),
        Hair3("hair3", targetGender = Gender.Male),
        Hair4("hair4"),
        Hair5("hair5", targetGender = Gender.Female),
        Hair6("hair6"),
    }

    enum class HairColor(val imgSuffix: String) {
        Brunette("-d"),
        Blonde("-l"),
        Ginger("-g"),
        Painted("-b"),
    }

    enum class HeightLevel {
        Regular,
        Short,
        Tall,
    }
}