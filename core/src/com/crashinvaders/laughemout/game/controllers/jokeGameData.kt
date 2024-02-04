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
    val race: audienceMember.Race? = null,
    val gender: audienceMember.Gender? = null,
    val hairStyle: audienceMember.HairStyle? = null,
    val hairColor: audienceMember.HairColor? = null,
    val heightLevel: audienceMember.HeightLevel? = null,
    val bodyStyle: audienceMember.BodyStyle? = null,
    val glasses: audienceMember.Glasses? = null,
    val hat: audienceMember.Hat? = null,
    val neck: audienceMember.Neck? = null,
    val mouth: audienceMember.Mouth? = null,

    val isFancyLooking: Boolean = false,
    val isWearingShades: Boolean = false,
    val isWearingGlasses: Boolean = false,
    val isWearingHat: Boolean = false,
    val isBald: Boolean = false,
    val isSmiling: Boolean = false,
    val isFrowning: Boolean = false,
    val hasFacialHair: Boolean = false,
)

data class JokeConnectorData(
    val text: String,
    val affectionPre: Int,
    val affectionPost: Int,
)

object JokeGameDataHelper {
    val jokeSubjects = gdxArrayOf<JokeSubjectData>(
        JokeSubjectData("BLACK\nPEOPLE", race = audienceMember.Race.Black),
        JokeSubjectData("WHITE\nPEOPLE", race = audienceMember.Race.White),
        JokeSubjectData("ASIAN\nPEOPLE", race = audienceMember.Race.Asian),

        JokeSubjectData("BOYS", gender = audienceMember.Gender.Male),
        JokeSubjectData("GIRLS", gender = audienceMember.Gender.Female),

        JokeSubjectData("BRUNETTES", hairColor = audienceMember.HairColor.Brunette),
        JokeSubjectData("BLONDS", hairColor = audienceMember.HairColor.Blonde),
        JokeSubjectData("GINGERS", hairColor = audienceMember.HairColor.Ginger),
        JokeSubjectData("BLUE\nHEADS", hairColor = audienceMember.HairColor.Painted),
        JokeSubjectData("BALD\nPEOPLE", isBald = true),

        JokeSubjectData("TALL\nPEOPLE", heightLevel = audienceMember.HeightLevel.Tall),
        JokeSubjectData("SHORT\nPEOPLE", heightLevel = audienceMember.HeightLevel.Short),

        JokeSubjectData("SMILING\nPEOPLE", isSmiling = true),
        JokeSubjectData("FROWNING\nPEOPLE", isFrowning = false),

        JokeSubjectData("PEOPLE\nIN HATS", isWearingHat = true),

        JokeSubjectData("MASKED\nPEOPLE", mouth = audienceMember.Mouth.FaceMask0),
        JokeSubjectData("BEARDED\nPEOPLE", hasFacialHair = true),

        JokeSubjectData("PEOPLE IN\nGLASSES", isWearingGlasses = true),
        JokeSubjectData("PEOPLE IN\nSUNGLASSES", isWearingShades = true),
        JokeSubjectData("FANCY\nPEOPLE", isFancyLooking = true),
    )

    val jokeConnectors = gdxArrayOf<JokeConnectorData>(
        JokeConnectorData("fall\nshort\nfor", -1, +1),
        JokeConnectorData("never\nunderstand", -1, +1),
        JokeConnectorData("under-\nestimate", -1, +1),
        JokeConnectorData("think\ntoo much\nabout", -1, +1),
        JokeConnectorData("swear\nworse\nthan", -1, +1),
        JokeConnectorData("feel\ncool\nunlike", +1, -1),
        JokeConnectorData("drive\nbetter\nthan", +1, -1),
        JokeConnectorData("dress\nprettier\nthan", +1, -1),
        JokeConnectorData("jump\nhigher\nthan", +1, -1),
        JokeConnectorData("fart\nlouder\nthan", -1, +1),
        JokeConnectorData("joke\nfunnier\nthan", +1, -1),
        JokeConnectorData("have\nfun\nwith", +1, +1),
        JokeConnectorData("be\nrude\nlike", -1, -1),
    )

    val audienceReactionsNegative = gdxArrayOf<String>(
        "Swing and a miss.",
        "Haha... Nope.",
        "Awkward silence...",
        "That's a nope.",
        "Speechless here.",
        "Gross!",
        "Ew, no!",
        "That's vile.",
        "Yuck!",
        "Really?",
        "Not cool.",
        "Too far!",
        "Disturbing.",
        "Unpleasant.",
        "Revolting!",
        "No thanks.",
        "Crossed the line.",
        "Dislike.",
        "Nasty!",
        "Unacceptable!",
        "Cringe!",
        "Offensive!",
        "Bad taste...",
        "That's wrong.",
        "Please stop.",
        "Awful, just awful.",
        "Regret hearing that.",
        "Despicable!",
        "Not okay.",
        "That's offensive.",
        "Highly inappropriate.",
        "Not funny.",
        "That's too much.",
        "Way overboard!",
        "Respect, please!"
    )

    val audienceReactionsNeutral = gdxArrayOf<String>(
        "Erm, okay...",
        "Next, please!",
        "Was that it?",
        "I'm confused.",
        "Huh?",
        "I'm lost.",
        "Explain?",
        "Clueless here.",
        "Pardon me?",
        "Missed it.",
        "Again, please?",
        "I don't get it.",
        "Come again?",
        "What do you mean?",
        "Lost me.",
        "Confused.",
        "Can you repeat?",
        "Elaborate?",
        "Didn't catch that.",
        "I'm not sure I follow.",
        "Need more context.",
        "Not clicking, sorry.",
        "Joke's on me?",
        "Missed the mark.",
        "Context, please?",
        "Not ringing a bell.",
        "I'm out of touch.",
        "Explain a bit?",
        "Flew over my head.",
        "I'm baffled.",
        "What's the catch?",
    )

    val audienceReactionsPositive = gdxArrayOf<String>(
        "Hilarious!",
        "Good one!",
        "Cracking up!",
        "So funny!",
        "I'm dying!",
        "Spot on!",
        "Pure gold!",
        "That's rich!",
        "Laugh riot!",
        "Killer joke!",
        "Roaring!",
        "Comedy gold!",
        "Side-splitting!",
        "Belly laughs!",
        "Too good!",
        "Nailed it!",
        "Priceless!",
        "Classic!",
        "Epic!",
        "Hysterical!",
        "Brilliant!",
        "Can't stop laughing!",
        "Top notch!",
        "A+ humor!",
        "On point!",
        "Rolling!",
        "Tears of joy!",
        "Slayed it!",
        "Best yet!",
        "Genius!",
        "Masterpiece!",
        "LOL",
        "Standing ovation!",
        "Speechless!",
        "Mind-blown!",
        "Gut-buster!",
        "Screaming!",
        "Outdone yourself!",
        "Unbeatable!",
        "Laughing fit!",
        "Comic genius!",
        "In stitches!",
        "Non-stop laughter!",
        "Breakthrough comedy!",
        "Unforgettable!",
        "You win!",
        "Laughing nonstop!",
        "Comic relief!",
        "Joke of the day!",
    )

    val audienceReactionsEmojiPositive = gdxArrayOf(
        speechBubble.Emoji.Laugh,
        speechBubble.Emoji.Smile,
        speechBubble.Emoji.Rofl,
    )

    val audienceReactionsEmojiNegative = gdxArrayOf(
        speechBubble.Emoji.Angry,
        speechBubble.Emoji.Scared,
        speechBubble.Emoji.Disappointment,
        speechBubble.Emoji.Dislike,
    )

    val audienceReactionsEmojiNeutral = gdxArrayOf(
        speechBubble.Emoji.Surprise,
        speechBubble.Emoji.Neutral,
    )
}