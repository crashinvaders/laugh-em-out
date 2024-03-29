package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.MathUtils.randomBoolean
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.esotericsoftware.spine.*
import com.github.quillraven.fleks.Entity
import com.crashinvaders.common.CommonUtils.random
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromDrawablePixels
import com.crashinvaders.laughemout.game.common.SodUtils.kickVisually
import com.crashinvaders.laughemout.game.components.AudienceMember
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem.Companion.actions
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform.TransformSpace
import ktx.app.gdxError
import ktx.collections.gdxArrayOf
import kotlin.sequences.sequence

object AudienceMemberHelper {

    private const val TRACK_GENERAL = 0
    private const val TRACK_HEIGHT_LEVEL = 100

    private val animationNamesIdle = gdxArrayOf("idle/posing0", "idle/subtle0", "idle/sway0")

    private val tmpVec2 = Vector2()

    fun create(world: FleksWorld, x: Float, y: Float, placementIndex: Int): Entity {
        val assets = world.inject<AssetManager>()
        val atlasCharacters = world.inject<TextureAtlas>("characters")

        val skelData = SkeletonBinary(atlasCharacters)
            .apply { scale = UPP }
            .readSkeletonData(Gdx.files.internal("skeletons/audience-char.skel"))

        val skeleton = Skeleton(skelData)
        skeleton.rootBone.scaleX = -1f
        val animState = AnimationState(AnimationStateData(skelData))

        lateinit var cAudMember: AudienceMember

        val entity = world.entity {
            it += Info("AudienceMember")
            it += generatedAppearance(world, placementIndex)
            it += Transform().apply {
                localPositionX = x
                localPositionY = y
            }

            it += SkeletonContainer(skeleton, animState)

            it += DrawableRenderer(world.inject<SkeletonEntityRenderer>())
            it += DrawableOrder(order = if (isFrontRow(placementIndex)) GameDrawOrder.AUDIENCE_FRONT_ROW else GameDrawOrder.AUDIENCE_BACK_ROW)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions(0f)
            it += DrawableOrigin(Align.bottom)

            it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                kickVisually(rotate = false)
            }

            cAudMember = it[AudienceMember]
        }

        setUpSkeleton(world, entity)
        animState.apply(skeleton)
        skeleton.updateWorldTransform()

        with(world) {
            entity[AudienceMember].emoLevel = MathUtils.random(-1, +1)

            val eEmoMeter = EmoMeterHelper.create(world, entity)
            cAudMember.emoMeter = eEmoMeter[EmoMeter]
        }

        animState.setAnimation(TRACK_GENERAL, "appear0", false)
        animState.addAnimation(TRACK_GENERAL, animationNamesIdle.random(), true, 0f)

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
        val hairStyle: HairStyle? = if (hat != null) when (gender) {
            Gender.Male -> HairStyle.Hair0
            Gender.Female -> HairStyle.Hair7
        } else
            if (randomBoolean(0.1f)) null else
                HairStyle.values().filter { it.targetGender == null || it.targetGender == gender }.random()
        val hairColor: HairColor = HairColor.values().random()
        val heightLevel: HeightLevel = HeightLevel.values().random()
        val bodyStyle: BodyStyle = BodyStyle.values().filter { it.gender == gender }.random()
        val glasses: Glasses? = if (randomBoolean(0.8f)) null else Glasses.values().random()
        val neck: Neck? = if (randomBoolean(0.8f)) null else Neck.values().random()
        val mouth: Mouth? = when {
            randomBoolean(0.1f) -> Mouth.FaceMask0
            gender == Gender.Male && randomBoolean(0.35f) -> Mouth.values().filter { it.targetGender == gender }.random()
            else -> null
        }

        return AudienceMember(placementIndex, race, gender, hairStyle, hairColor, heightLevel, bodyStyle, glasses, hat, neck, mouth)
    }

    private fun setUpSkeleton(world: FleksWorld, audMemb: Entity) {
        with(world) {
            val cAudMemb = audMemb[AudienceMember]
            val cSkelContainer = audMemb[SkeletonContainer]

            val emoLevel = evalEmotionLevelType(cAudMemb.emoLevel)

            val skeleton = cSkelContainer.skeleton
            skeleton.setAttachment("char-body", cAudMemb.bodyStyle.imgName)
            skeleton.setAttachment("char-head", "char-head${cAudMemb.race.imgSuffix}${emoLevel.imgSuffix}")
            skeleton.setAttachment("char-hair", if (cAudMemb.hairStyle == null) null else "${cAudMemb.hairStyle.imgName}${cAudMemb.hairColor.imgSuffix}")
            skeleton.setAttachment("char-hat", if (cAudMemb.hat == null) null else "${cAudMemb.hat.imgName}")
            skeleton.setAttachment("char-glasses", if (cAudMemb.glasses == null) null else "${cAudMemb.glasses.imgName}")
            skeleton.setAttachment("char-neck", if (cAudMemb.neck == null) null else "${cAudMemb.neck.imgName}")
            skeleton.setAttachment("char-mouth", if (cAudMemb.mouth == null) null else when {
                cAudMemb.mouth.isFacialHair -> "${cAudMemb.mouth.imgName}${cAudMemb.hairColor.imgSuffix}"
                else -> cAudMemb.mouth.imgName
            })

            val animState = cSkelContainer.animState
            when(cAudMemb.heightLevel) {
                HeightLevel.Short -> animState.setAnimation(TRACK_HEIGHT_LEVEL, "height/low0", false)
                HeightLevel.Tall -> animState.setAnimation(TRACK_HEIGHT_LEVEL, "height/high0", false)
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

    fun animateJokeReactionPos(world: FleksWorld, entity: Entity) {
        with(world) {
            val cAudMemb = entity[AudienceMember]
            val cSkeleton = entity[SkeletonContainer]

            cSkeleton.animState.apply {
                setAnimation(TRACK_GENERAL, "reactions/positive0", false)
                addAnimation(TRACK_GENERAL, animationNamesIdle.random(), true, 0f)
            }
        }
    }

    fun animateJokeReactionNeg(world: FleksWorld, entity: Entity) {
        with(world) {
            val cAudMemb = entity[AudienceMember]
            val cSkeleton = entity[SkeletonContainer]

            cSkeleton.animState.apply {
                setAnimation(TRACK_GENERAL, "reactions/negative0", false)
                addAnimation(TRACK_GENERAL, animationNamesIdle.random(), true, 0f)
            }
        }
    }

    fun animateJokeReactionNeut(world: FleksWorld, entity: Entity) {
        with(world) {
            val cAudMemb = entity[AudienceMember]
            val cSkeleton = entity[SkeletonContainer]

            cSkeleton.animState.apply {
                setAnimation(TRACK_GENERAL, "reactions/neutral0", false)
                addAnimation(TRACK_GENERAL, animationNamesIdle.random(), true, 0f)
            }
        }
    }

    fun animateDisappearAndDestroy(world: FleksWorld, entity: Entity) {
        with(world) {
            val cAudMemb = entity[AudienceMember]
            val cSkeleton = entity[SkeletonContainer]

            cSkeleton.animState.apply {
                setAnimation(TRACK_GENERAL, "roll-away0", false)
            }
            world.system<EntityActionSystem>().addAction(entity, SequenceAction(
                DelayAction(1.5f),
                RunnableAction { Gdx.app.postRunnable { world -= entity } }
            ))
        }
    }

    fun updateFaceEmotion(world: FleksWorld, eAudMemb: Entity) {
        with(world) {
            val cAudMemb = eAudMemb[AudienceMember]
            val cSkelContainer = eAudMemb[SkeletonContainer]

            val emoType = evalEmotionLevelType(cAudMemb.emoLevel)
            val skeleton = cSkelContainer.skeleton
            skeleton.setAttachment("char-head", "char-head${cAudMemb.race.imgSuffix}${emoType.imgSuffix}")
        }
    }

    fun getOverheadPos(world: FleksWorld, eAudMemb: Entity): Vector2 =
        with(world) {
            return eAudMemb[SkeletonContainer].getBonePosition("overhead-anchor")
        }

    fun setAffectionIndicator(
        world: FleksWorld,
        eAudMemb: Entity,
        type: AudienceMember.AffectionIndicator.Type?) {

        val cAudMemb = with(world) { eAudMemb[AudienceMember] }

        val currentIndicator = when {
            cAudMemb.cAffectionIndicator == null -> null
            else -> cAudMemb.cAffectionIndicator!!.type
        }
        if (type == currentIndicator) {
            return
        }

        destroyAffectionIndicator(world, cAudMemb)

        if (type != null) {
            createAffectionIndicator(world, cAudMemb, type)
        }
    }

    private fun createAffectionIndicator(
        world: FleksWorld,
        cAudMemb: AudienceMember,
        type: AudienceMember.AffectionIndicator.Type) {

        val eIndicator = world.entity {
            it += Info("AffectionIndicator")
            it += Transform().apply {
                parent = cAudMemb.entity[Transform]
                localPositionX = 0f
                localPositionY = -8f * UPP
            }
            it += AudienceMember.AffectionIndicator(type)

            val atlas = world.inject<TextureAtlas>("ui")
            val drawable = TextureRegionDrawable(atlas.findRegion(type.imgName))
            it += GdxDrawableContainer(drawable)
            it += DrawableRenderer(GdxDrawableEntityRenderer)
            it += DrawableOrder(GameDrawOrder.UI_AFFECTION_INDICATOR - 5)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromDrawablePixels(drawable)
            it += DrawableOrigin(Align.center)

            cAudMemb.cAffectionIndicator = it[AudienceMember.AffectionIndicator]
        }

        with(world) {
            eIndicator[Transform].apply { localScaleX = 2.0f; localScaleY = 2.0f }
            actions(eIndicator) {
                scaleTo(1.0f, 1.0f, 0.5f, Interpolation.elasticOut, TransformSpace.Local)
            }
        }
    }

    private fun destroyAffectionIndicator(
        world: FleksWorld,
        cAudMemb: AudienceMember) {

        if (cAudMemb.cAffectionIndicator == null) {
            return
        }

        val eIndicator = cAudMemb.cAffectionIndicator!!.entity
        cAudMemb.cAffectionIndicator = null

        with(world) {
            actions(eIndicator) {
                sequence {
                    tintFadeOut(0.2f)
                    removeEntity()
                }
            }
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
        F5(Gender.Female, "char-body-f5"),
    }

    enum class Glasses(val imgName: String, val isFancy: Boolean = false, val isShades: Boolean = false) {
        Dredd0("glasses-dredd0", isShades = true),
        Dredd1("glasses-dredd1", isShades = true),
        Goggles0("glasses-goggles0"),
        Goggles1("glasses-goggles1"),
        Wayfarer0("glasses-wfr0"),
        Shades0("glasses-shades0", isShades = true),
        Shades1("glasses-shades1", isShades = true),
        Shades2("glasses-shades2", isShades = true),
        SportRed0("glasses-sport-red0", isShades = true),
    }

    enum class Hat(val imgName: String, val isFancy: Boolean = false) {
        Crown0("hat-crown0", isFancy = true),
        Blinder0("hat-blinder0"),
        Cap0Bw("hat-cap0-bw"),
        Cap0Fw("hat-cap0-fw"),
        Pot0("hat-pot0"),
    }

    enum class Neck(val imgName: String, val isFancy: Boolean = false) {
        Chain0("neck-chain0", isFancy = true),
        Cross0("neck-cross0", isFancy = true),
        Medal0("neck-medal0", isFancy = true),
        Neckless0("neck-neckless0", isFancy = true),
        Scarf0("neck-scarf0"),
    }

    enum class Mouth(val imgName: String, val hideEmotions: Boolean = false,
                     val isFancy: Boolean = false, val isFacialHair: Boolean = false, val targetGender: Gender? = null) {
        FaceMask0("face-mask0", hideEmotions = true),

        GrandMustache0("face-grand-mustache0", hideEmotions = true, targetGender = Gender.Male, isFacialHair = true),
        BeardJack0("face-beard-jack0", hideEmotions = true, targetGender = Gender.Male, isFacialHair = true),

        BeardChinCurtain0("face-beard-chin-curtain0", targetGender = Gender.Male, isFacialHair = true),
        BeardDoubleChin0("face-beard-doublechin0", targetGender = Gender.Male, isFacialHair = true),
        BeardGoat0("face-beard-goat0", targetGender = Gender.Male, isFacialHair = true),
        BeardSides0("face-beard-sides0", targetGender = Gender.Male, isFacialHair = true),
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
        Hair0("hair0", targetGender = Gender.Male),
        Hair1("hair1"),
        Hair2("hair2", targetGender = Gender.Female),
        Hair3("hair3", targetGender = Gender.Male),
        Hair4("hair4"),
        Hair5("hair5", targetGender = Gender.Female),
        Hair6("hair6"),
        Hair7("hair7", targetGender = Gender.Female),
        Hair8("hair8", targetGender = Gender.Female),
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