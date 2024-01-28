package com.crashinvaders.laughemout.game.controllers

import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf

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

    val isFancyLooking: Boolean = false,
    val isWearingShades: Boolean = false,
    val isWearingGlasses: Boolean = false,
    val isWearingHat: Boolean = false,
    val isBald: Boolean = false,
    val isSmiling: Boolean = false,
    val isFrowning: Boolean = false,
)

data class JokeConnectorData(
    val text: String,
    val affectionPre: Int,
    val affectionPost: Int,
)

object JokeGameDataHelper {
    val jokeSubjects = gdxArrayOf<JokeSubjectData>(
        JokeSubjectData("BLACK\nPEOPLE", race = AudienceMemberHelper.Race.Black),
        JokeSubjectData("WHITE\nPEOPLE", race = AudienceMemberHelper.Race.White),
        JokeSubjectData("ASIAN\nPEOPLE", race = AudienceMemberHelper.Race.Asian),

        JokeSubjectData("BOYS", gender = AudienceMemberHelper.Gender.Male),
        JokeSubjectData("GIRLS", gender = AudienceMemberHelper.Gender.Female),

        JokeSubjectData("BRUNETTES", hairColor = AudienceMemberHelper.HairColor.Brunette),
        JokeSubjectData("BLONDS", hairColor = AudienceMemberHelper.HairColor.Blonde),
        JokeSubjectData("GINGERS", hairColor = AudienceMemberHelper.HairColor.Ginger),
        JokeSubjectData("BLUE\nHEADS", hairColor = AudienceMemberHelper.HairColor.Painted),
        JokeSubjectData("BALD\nPEOPLE", isBald = true),

        JokeSubjectData("TALL\nPEOPLE", heightLevel = AudienceMemberHelper.HeightLevel.Tall),
        JokeSubjectData("SHORT\nPEOPLE", heightLevel = AudienceMemberHelper.HeightLevel.Short),

        JokeSubjectData("SMILING\nPEOPLE", isSmiling = true),
        JokeSubjectData("FROWNING\nPEOPLE", isFrowning = false),

        JokeSubjectData("PEOPLE\nIN HATS", isWearingHat = true),

        JokeSubjectData("BEARDY\nPEOPLE", mouth = AudienceMemberHelper.Mouth.Beard0),
        JokeSubjectData("MASKED\nPEOPLE", mouth = AudienceMemberHelper.Mouth.FaceMask0),

        JokeSubjectData("PEOPLE\nWITH\nGLASSES", isWearingGlasses = true),
        JokeSubjectData("PEOPLE\nWITH\nSHADES", isWearingShades = true),
        JokeSubjectData("FANCY\nPEOPLE", isFancyLooking = true),
    )
}