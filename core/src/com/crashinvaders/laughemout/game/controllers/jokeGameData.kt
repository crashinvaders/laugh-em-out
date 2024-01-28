package com.crashinvaders.laughemout.game.controllers

import ktx.collections.GdxArray

data class JokeStructureData(
    val subjectPre: JokeSubjectData,
    val subjectPost: JokeSubjectData,
    val connector: JokeConnectorData,
)

data class JokeBuilderData(
    val subjects: GdxArray<JokeSubjectData>,
    val connector: JokeConnectorData,
)

data class JokeSubjectData(
    val text: String,
    val race: AudienceMemberHelper.Race? = null,
    val gender: AudienceMemberHelper.Gender? = null,
    val hairStyle: AudienceMemberHelper.HairStyle? = null,
    val hairColor: AudienceMemberHelper.HairColor? = null,
    val heightLevel: AudienceMemberHelper.HeightLevel? = null,
    val bodyStyle: AudienceMemberHelper.BodyStyle? = null,
    val glasses: AudienceMemberHelper.Glasses? = null,
    val hat: AudienceMemberHelper.Hat? = null,
    val neck: AudienceMemberHelper.Neck? = null,
    val mouth: AudienceMemberHelper.Mouth? = null,
)

data class JokeConnectorData(
    val text: String,
    val affectionPre: Int,
    val affectionPost: Int,
)