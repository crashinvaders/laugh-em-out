package com.crashinvaders.laughemout.game.components

import com.crashinvaders.common.EntityComponent
import com.crashinvaders.laughemout.game.controllers.AudienceMemberHelper
import com.github.quillraven.fleks.ComponentType

class Comedian : EntityComponent<Comedian>() {

    override fun type() = Comedian
    companion object : ComponentType<Comedian>()
}

class AudienceMember(
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

    var emotionLevel: AudienceMemberHelper.EmotionLevel = AudienceMemberHelper.EmotionLevel.Neutral

    override fun type() = AudienceMember
    companion object : ComponentType<AudienceMember>()
}