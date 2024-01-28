package com.crashinvaders.laughemout.game.components

import com.crashinvaders.common.EntityComponent
import com.crashinvaders.laughemout.game.controllers.AudienceMemberHelper
import com.crashinvaders.laughemout.game.controllers.EmoMeter
import com.github.quillraven.fleks.ComponentType

class Comedian : EntityComponent<Comedian>() {

    override fun type() = Comedian
    companion object : ComponentType<Comedian>()
}

class AudienceMember(
    val placementIndex: Int,
    val race: AudienceMemberHelper.Race,
    val gender: AudienceMemberHelper.Gender,
    val hairStyle: AudienceMemberHelper.HairStyle?,
    val hairColor: AudienceMemberHelper.HairColor,
    val heightLevel: AudienceMemberHelper.HeightLevel,
    val bodyStyle: AudienceMemberHelper.BodyStyle,
    val glasses: AudienceMemberHelper.Glasses?,
    val hat: AudienceMemberHelper.Hat?,
    val neck: AudienceMemberHelper.Neck?,
    val mouth: AudienceMemberHelper.Mouth?,

    ) : EntityComponent<AudienceMember>() {

    var emoLevel: Int = 0

    lateinit var emoMeter: EmoMeter

    override fun type() = AudienceMember
    companion object : ComponentType<AudienceMember>()
}