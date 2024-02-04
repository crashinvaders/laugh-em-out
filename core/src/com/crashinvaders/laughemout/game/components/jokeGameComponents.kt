package com.crashinvaders.laughemout.game.components

import com.crashinvaders.common.EntityComponent
import com.crashinvaders.laughemout.game.controllers.audienceMember
import com.crashinvaders.laughemout.game.controllers.EmoMeter
import com.github.quillraven.fleks.ComponentType

class Comedian : EntityComponent<Comedian>() {

    override fun type() = Comedian
    companion object : ComponentType<Comedian>()
}

class AudienceMember(
    val placementIndex: Int,
    val race: audienceMember.Race,
    val gender: audienceMember.Gender,
    val hairStyle: audienceMember.HairStyle?,
    val hairColor: audienceMember.HairColor,
    val heightLevel: audienceMember.HeightLevel,
    val bodyStyle: audienceMember.BodyStyle,
    val glasses: audienceMember.Glasses?,
    val hat: audienceMember.Hat?,
    val neck: audienceMember.Neck?,
    val mouth: audienceMember.Mouth?,

    ) : EntityComponent<AudienceMember>() {

    var emoLevel: Int = 0

    lateinit var emoMeter: EmoMeter

    fun isFancyLooking(): Boolean {
        return hat?.isFancy == true || glasses?.isFancy == true || neck?.isFancy == true
    }

    fun isWearingShades(): Boolean {
        return glasses?.isShades == true
    }

    fun isWearingGlasses(): Boolean {
        return glasses?.isShades == false
    }

    fun isWearingHat(): Boolean {
        return hat != null
    }

    fun isBald(): Boolean {
        return hairStyle == null
    }

    override fun type() = AudienceMember
    companion object : ComponentType<AudienceMember>()
}