package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.branch.Parallel
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.*
import com.crashinvaders.laughemout.App
import com.crashinvaders.laughemout.game.CameraProcessorOrder
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.SodUtils.kickVisually
import com.crashinvaders.laughemout.game.common.camera.Sod3CameraProcessor
import com.crashinvaders.laughemout.game.components.AudienceMember
import com.crashinvaders.laughemout.game.engine.components.*
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.OnWorldInitializedHandler
import com.crashinvaders.laughemout.game.engine.systems.entityactions.ParentAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.*
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.extensions.entityAction
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TextraLabel
import com.github.tommyettinger.textra.TypingLabel
import ktx.ai.*
import ktx.app.gdxError
import ktx.collections.*
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2

class JokeGameManager : IntervalSystem(),
    OrderedDisposableRegistry by OrderedDisposableContainer(), OnWorldInitializedHandler {

    private val camProcessor = Sod3CameraProcessor(2f, 0.8f, 0f,
        CameraProcessorOrder.JOKE_MANAGER,
        readCamValuesWhenAdded = false).apply {
        x = 60f * UPP
        y = 10f * UPP
    }

    private val bBoard = SessionBlackBoard(world, 3)
    private val bTree: BehaviorTree<SessionBlackBoard> = BehaviorTree()

    override fun onWorldInitialized() {
        super.onInit()

        ScreenFadeHelper.createFadeInEffect(world, 0.5f)

        EnvironmentHelper.createObjects(world)
        bBoard.eScoreLabel = createScoreLabel(world)
        updateScoreLabel(world, bBoard.eScoreLabel, bBoard.scoreCount)

        bBoard.eComedian = ComedianHelper.createComedian(world, 0f, 0f)

        world.system<MainCameraStateSystem>().apply {
            cameraEntity[Transform].apply {
                localPositionX = 0f * UPP
                localPositionY = 20f * UPP
            }
            addProcessor(camProcessor)
        }

        bTree.apply {
            setObject(bBoard)

            repeat {
                resetOnCompletion {
                    sequence {
                        runnable {
                            bBoard.maxAudienceCount = when {
                                bBoard.jokeCount < 2 -> 3
                                bBoard.jokeCount < 5 -> 4
                                else -> 5
                            }
                            bBoard.maxAudienceCount += bBoard.maxAudienceCountDelta
                            bBoard.maxAudienceCountDelta = 0
                        }
                        waitLeaf(1f)

                        entityAction(world, bBoard.eComedian) { createSpawnAudienceAction(it) }
                        entityAction(world, bBoard.eComedian) { createFirstJokeAnnouncementAction(it) }
                        waitLeaf(1f)

                        parallelPatched(Parallel.Policy.Selector, Parallel.Orchestrator.Join) {
                            awaitCompletion { bBoard, callback -> showAndAwaitJokeBuilder(bBoard, callback) }
                            // Always fail is to always wait for the joke builder.
                            alwaysFail { add(JokeTimerTask()) }
                            alwaysFail { add(AudienceAffectionIndicatorsTask()) }
                        }

                        runnable { bBoard ->
                            val completedJoke = bBoard.completedJoke!!

                            bBoard.completedJokeView = createResultJokeView(world, completedJoke)

                            SpeechBubbleHelper.createBubble(
                                world, "...",
                                bBoard.eComedian[Transform].worldPositionX,
                                bBoard.eComedian[Transform].worldPositionY + 44f * UPP,
                                2f)

                            evalJokeAffections(world, bBoard)
                        }
                        waitLeaf(2f)

                        entityAction(world, bBoard.eComedian) { createAffectAudienceAction(it) }
                        entityAction(world, bBoard.eComedian) { createAudienceJokeReactionAction(it) }

                        runnable { bBoard ->
                            // Activate score label for the first time.
                            bBoard.eScoreLabel[DrawableVisibility].also {
                                if (!it.isVisible) {
                                    it.isVisible = true
                                    animateScoreLabelPulse(world, bBoard.eScoreLabel)
                                }
                            }
                        }

                        entityAction(world, bBoard.eComedian) { createAudienceRotationAction(it) }
                        waitLeaf(1f)

                        runnable { bBoard ->
                            val isGameOver = bBoard.audienceMembers.any { it[AudienceMember].emoLevel <= -3 }
                            if (isGameOver) {
                                bBoard.isGameOver = true
                                bBoard.eScoreLabel[DrawableVisibility].isVisible = false
                                GameOverHelper.showGameOver(world, bBoard.scoreCount) { App.Inst.restart() }
                            }
                        }
                        waitUntil { bBoard -> !bBoard.isGameOver } // Stuck the tree at this node if the game is over.

                        runnable { bBoard ->
                            // Activate score label for the first time.
                            bBoard.eScoreLabel[DrawableVisibility].also { 
                                if (!it.isVisible) {
                                    it.isVisible
                                    animateScoreLabelPulse(world, bBoard.eScoreLabel)
                                }
                            }

                            if (bBoard.completedJokeView != null) {
                                world -= bBoard.completedJokeView!!
                                bBoard.completedJokeView = null
                            }
                        }

                        runnable { bBoard ->
                            bBoard.jokeCount++
                            bBoard.resetRound()
                        }
                    }
                }
            }
        }
    }

    override fun onTick() {
        bTree.step()
    }

    override fun onDisable() {
        super.onDisable()
        dispose()
    }

    companion object {

        private fun showAndAwaitJokeBuilder(bBoard: SessionBlackBoard, callback: () -> Unit) {
            val subjectCount = 3
            val subjects: GdxArray<JokeSubjectData>
            val world = bBoard.world

            with(world) {
                val audienceMembers = bBoard.audienceMembers.map { it[AudienceMember] }

                val subjectsAll = JokeGameDataHelper.jokeSubjects
                val subjectsFiltered = subjectsAll.filter { subject ->
                    audienceMembers.any { audMemb -> checkSubjectIntersection(audMemb, subject) }
                }.toGdxSet()

                while (subjectsFiltered.size < subjectCount) {
                    subjectsFiltered.add(subjectsAll.random())
                }
                subjects = subjectsFiltered.toGdxArray()
                subjects.shuffle()
                while (subjects.size > subjectCount) {
                    subjects.pop()
                }
            }

            val jokeConnector = JokeGameDataHelper.jokeConnectors.random()

            world.system<JokeBuilderUiController>().show(world, JokeBuilderData(subjects, jokeConnector)) {
                debug {
                    "Complete joke: ${it.subjectPre.text.replace('\n', ' ')} ${
                        it.connector.text.replace(
                            '\n',
                            ' '
                        )
                    } ${it.subjectPost.text.replace('\n', ' ')}"
                }

                bBoard.completedJoke = it
                callback.invoke()
            }
        }

        private fun ParentAction.createSpawnAudienceAction(bBoard: SessionBlackBoard) {
            val membersToSpawn = bBoard.maxAudienceCount - bBoard.audienceMembers.size
            if (membersToSpawn <= 0) {
                return
            }

            sequence {
                for (i in 0 until membersToSpawn) {
                    runnable {
                        val index = nextAvailableAudiencePlacementIndex(world, bBoard.audienceMembers)
                        val (x, y) = AudienceMemberHelper.evalSpawnPosition(index)
                        val entity = AudienceMemberHelper.create(world, x, y, index)
                        bBoard.audienceMembers.add(entity)
                    }
                    delay(0.5f)
                }
            }
        }

        private fun ParentAction.createFirstJokeAnnouncementAction(bBoard: SessionBlackBoard) {
            if (bBoard.jokeCount != 0) {
                return
            }

            val world = bBoard.world
            with(world) {
                val message = "Here goes the\nfirst joke..."
                SpeechBubbleHelper.createBubble(
                    world,
                    message,
                    bBoard.eComedian[Transform].worldPositionX,
                    bBoard.eComedian[Transform].worldPositionY + 44f * UPP,
                    3f
                )
            }
            delay(1f)
        }

        @Suppress("GDXKotlinUnsafeIterator")
        private fun ParentAction.createAffectAudienceAction(bBoard: SessionBlackBoard) {
            sequence {
                val affections = bBoard.jokeAffections.values().toGdxArray()
                affections.shuffle()
                affections.forEachIndexed { index, affection ->
                    val audMemb = affection.audMemb
                    val affectionDelta = affection.affectionSum
                    when {
                        affectionDelta == 0 -> {
                            // Neutral affection.
                            runnable {
                                AudienceMemberHelper.animateJokeReactionNeut(world, audMemb.entity)
                                AudienceMemberHelper.updateFaceEmotion(world, audMemb.entity)
                            }
                            delay(0.5f)
                        }
                        else -> {
                            // Positive/negative affection.
                            runnable {
                                changeAudMemberEmoLevel(world, audMemb, affectionDelta)
                            }
                            delay(1f)
                        }
                    }
                }
            }
        }

        private fun ParentAction.createAudienceJokeReactionAction(bBoard: SessionBlackBoard) {
            val world = bBoard.world
            val affections = bBoard.jokeAffections.values().toGdxArray()

            if (affections.size == 0) {
                return
            }

            // Canceled case (game over).
            affections.firstOrNull { it.audMemb.emoLevel == -3 }.let { affection ->
                if (affection == null) {
                    return@let
                }
                sequence {
                    runnable {
                        val (x, y) = AudienceMemberHelper.getOverheadPos(world, affection.audMemb.entity)
                        SpeechBubbleHelper.createBubble(
                            world,
                            "{SHAKE}[#733547]" + JokeGameDataHelper.audienceReactionsNegative.random(), //TODO We need a very specific reaction here.
                            x,
                            y + 10f * UPP,
                            duration = 3f,
                            affection = SpeechBubbleHelper.Affection.Negative
                        )
                    }
                    delay(1f)
                }
                return
            }

            // Laughed out case.
            affections.firstOrNull { it.audMemb.emoLevel == +3 }.let { affection ->
                if (affection == null) {
                    return@let
                }
                sequence {
                    runnable {
                        val (x, y) = AudienceMemberHelper.getOverheadPos(world, affection.audMemb.entity)
                        SpeechBubbleHelper.createBubble(
                            world,
                            "{RAINBOW}[#544470]" + JokeGameDataHelper.audienceReactionsPositive.random(), //TODO We need a very specific reaction here.
                            x,
                            y + 10f * UPP,
                            duration = 3f,
                            affection = SpeechBubbleHelper.Affection.Positive
                        )
                    }
                    delay(1f)
                }
                return
            }

            // Show message reaction for the random affected audience member.
            affections.random().let { affection ->
                sequence {
                    runnable {
                        if (MathUtils.randomBoolean(0.75f)) {
                            // Create text reaction.
                            val (x, y) = AudienceMemberHelper.getOverheadPos(world, affection.audMemb.entity)
                            val affectionSum = affection.affectionSum
                            val message: String = when {
                                affectionSum > 0 -> "[#544470]" + JokeGameDataHelper.audienceReactionsPositive.random()
                                affectionSum < 0 -> "[#733547]" + JokeGameDataHelper.audienceReactionsNegative.random()
                                else -> JokeGameDataHelper.audienceReactionsNeutral.random()
                            }
                            val bubbleAffection: SpeechBubbleHelper.Affection = when {
                                affectionSum > 0 -> SpeechBubbleHelper.Affection.Positive
                                affectionSum < 0 -> SpeechBubbleHelper.Affection.Negative
                                else -> SpeechBubbleHelper.Affection.Neutral
                            }
                            SpeechBubbleHelper.createBubble(
                                world,
                                message,
                                x,
                                y + 10f * UPP,
                                duration = 3f,
                                affection = bubbleAffection
                            )
                        } else {
                            // Create emoji-only reaction.
                            val (x, y) = AudienceMemberHelper.getOverheadPos(world, affection.audMemb.entity)
                            val affectionSum = affection.affectionSum
                            val emoji: SpeechBubbleHelper.Emoji = when {
                                affectionSum > 0 -> JokeGameDataHelper.audienceReactionsEmojiPositive.random()
                                affectionSum < 0 -> JokeGameDataHelper.audienceReactionsEmojiNegative.random()
                                else -> JokeGameDataHelper.audienceReactionsEmojiNeutral.random()
                            }
                            val bubbleAffection: SpeechBubbleHelper.Affection = when {
                                affectionSum > 0 -> SpeechBubbleHelper.Affection.Positive
                                affectionSum < 0 -> SpeechBubbleHelper.Affection.Negative
                                else -> SpeechBubbleHelper.Affection.Neutral
                            }
                            SpeechBubbleHelper.createEmojiBubble(
                                world,
                                emoji,
                                x,
                                y + 10f * UPP,
                                duration = 2f,
                                affection = bubbleAffection
                            )
                        }
                    }
                    delay(1.0f)
                }
                return
            }
        }

        private fun ParentAction.createAudienceRotationAction(bBoard: SessionBlackBoard) {
            sequence {
                val removedPlacementIndices = GdxIntArray()

                with(bBoard.world) {
                    val completedMembers =
                        bBoard.audienceMembers.filter { it[AudienceMember].emoLevel == 3 }.toGdxArray()
                    completedMembers.forEach { eAudMemb ->
                        removedPlacementIndices.add(eAudMemb[AudienceMember].placementIndex)

                        // Do not spawn a new aud member next turn.
                        bBoard.maxAudienceCountDelta--

                        runnable {
                            bBoard.audienceMembers.removeValue(eAudMemb, true)
                            bBoard.scoreCount++
                            updateScoreLabel(world, bBoard.eScoreLabel, bBoard.scoreCount)
                            animateScoreLabelPulse(world, bBoard.eScoreLabel)

                            AudienceMemberHelper.animateDisappearAndDestroy(world, eAudMemb)
                        }
                        delay(1f)
                    }
                }
            }
        }

        private fun evalJokeAffections(world: FleksWorld, bBoard: SessionBlackBoard) {
            val joke = bBoard.completedJoke!!
            val audienceMembers = bBoard.audienceMembers

            with(world) {
                val affectionMap = bBoard.jokeAffections
                affectionMap.clear()

                val affectionPre = joke.connector.affectionPre
                val affectionPost = joke.connector.affectionPost
                val subjectPre = joke.subjectPre
                val subjectPost = joke.subjectPost

                audienceMembers.forEach { audMemb ->
                    val cAudMemb = audMemb[AudienceMember]
                    if (affectionPre != 0 && checkSubjectIntersection(cAudMemb, subjectPre)) {
                        val entry = affectionMap.getOrPut(cAudMemb) { AudienceJokeAffectionEntry(cAudMemb) }
                        entry.affectionSum += affectionPre
                        entry.triggerSubjects.add(subjectPre)
                    }
                    if (affectionPost != 0 && checkSubjectIntersection(cAudMemb, subjectPost)) {
                        val entry = affectionMap.getOrPut(cAudMemb) { AudienceJokeAffectionEntry(cAudMemb) }
                        entry.affectionSum += affectionPost
                        entry.triggerSubjects.add(subjectPost)
                    }
                }
            }
        }

        private fun checkSubjectIntersection(audMemb: AudienceMember, jokeSubject: JokeSubjectData): Boolean {
            return (jokeSubject.race == audMemb.race) ||
                    (jokeSubject.gender == audMemb.gender) ||
                    (jokeSubject.hairStyle != null && jokeSubject.hairStyle == audMemb.hairStyle) ||
                    (jokeSubject.hairColor == audMemb.hairColor && audMemb.hairStyle != null) ||
                    (jokeSubject.heightLevel == audMemb.heightLevel) ||
                    (jokeSubject.bodyStyle == audMemb.bodyStyle) ||
                    (jokeSubject.glasses != null && jokeSubject.glasses == audMemb.glasses) ||
                    (jokeSubject.hat != null && jokeSubject.hat == audMemb.hat) ||
                    (jokeSubject.neck != null && jokeSubject.neck == audMemb.neck) ||
                    (jokeSubject.mouth != null && jokeSubject.mouth == audMemb.mouth) ||
                    (jokeSubject.isFancyLooking && audMemb.isFancyLooking()) ||
                    (jokeSubject.isWearingShades && audMemb.isWearingShades()) ||
                    (jokeSubject.isWearingHat && audMemb.isWearingHat()) ||
                    (jokeSubject.isWearingGlasses && audMemb.isWearingGlasses()) ||
                    (jokeSubject.isBald && audMemb.isBald()) ||
                    (jokeSubject.isSmiling && audMemb.emoLevel >= 1) ||
                    (jokeSubject.isFrowning && audMemb.emoLevel <= -1) ||
                    (jokeSubject.hasFacialHair && audMemb.mouth != null && audMemb.mouth.isFacialHair)
        }

        fun changeAudMemberEmoLevel(world: FleksWorld, audMemb: AudienceMember, emoLevelDelta: Int) {
            if (emoLevelDelta == 0) {
                return
            }

            val emoLevel = MathUtils.clamp(audMemb.emoLevel + emoLevelDelta, -3, +3)
            audMemb.emoLevel = emoLevel
            audMemb.emoMeter.also {
                it.emoLevel = emoLevel
                EmoMeterHelper.animateValueChange(world, it.entity)
                EmoMeterHelper.animateToken(
                    world, it.entity, when {
                        emoLevel >= 3 -> TokenType.Star
                        emoLevel <= -3 -> TokenType.Cancel
                        emoLevelDelta > 0 -> TokenType.Like
                        emoLevelDelta < 0 -> TokenType.Dislike
                        else -> gdxError("Unexpected case")
                    }
                )
            }
            if (emoLevelDelta > 0) {
                AudienceMemberHelper.animateJokeReactionPos(world, audMemb.entity)
            } else {
                AudienceMemberHelper.animateJokeReactionNeg(world, audMemb.entity)
            }
            AudienceMemberHelper.updateFaceEmotion(world, audMemb.entity)
        }

        fun createResultJokeView(world: FleksWorld, data: JokeStructureData): Entity {
            val font = world.inject<Font>("pixolaCurva")

            return world.entity {
                it += Info("ComedyStage")
                it += Transform().apply {
                    localPositionX = -60f * UPP
                    localPositionY = -54f * UPP
                }

                val text = "[#c8d7eb][#ffedd4]${data.subjectPre.text.replace('\n', ' ')}[] ${data.connector.text.replace('\n', ' ')} [#ffedd4]${data.subjectPost.text.replace('\n', ' ')}[]"
                val actor = TypingLabel(text, font).let {
                    TransformActorWrapper(it, autoLayout = true)
                }
                it += ActorContainer(actor)
                it += DrawableRenderer(ActorEntityRenderer)
                it += DrawableOrder(GameDrawOrder.COMPLETED_JOKE_VIEW)
                it += DrawableTint()
                it += DrawableVisibility()
                it += DrawableDimensions(0f)
                it += DrawableOrigin(Align.left)
            }
        }

        fun createScoreLabel(world: FleksWorld): Entity {
            val font = world.inject<Font>("pixolaCurva")

            return world.entity {
                it += Info("ComedyStage")
                it += Transform().apply {
                    localPositionX = -60f * UPP
                    localPositionY = 64f * UPP
                }

                val actor = TextraLabel("", font).let {
                    TransformActorWrapper(it, autoLayout = false)
                }
                it += ActorContainer(actor)
                it += DrawableRenderer(ActorEntityRenderer)
                it += DrawableOrder(GameDrawOrder.UI_SCORE_LABEL)
                it += DrawableTint()
                it += DrawableVisibility(false)
                it += DrawableDimensions()
                it += DrawableOrigin(Align.center)

                it += SodInterpolation(6f, 0.6f, -0.5f)
            }
        }

        fun updateScoreLabel(world: FleksWorld, eScoreLabel: Entity, scoreCount: Int) {
            with(world) {
                val label = (eScoreLabel[ActorContainer].actor as TransformActorWrapper<TextraLabel>).actor
                label.setText("[#95a4b6]Score: [#ffedd4][125%]${scoreCount}")
            }
        }

        fun animateScoreLabelPulse(world: FleksWorld, eScoreLabel: Entity) {
            with(world) {
                eScoreLabel[SodInterpolation].kickVisually()
            }
        }

        fun nextAvailableAudiencePlacementIndex(world: FleksWorld, audMembers: GdxArray<Entity>): Int {
            var foundIndex: Int
            with(world) {
                var index = 0
                while (true) {
                    var isVacant = true
                    for (i in 0 until audMembers.size) {
                        if (audMembers[i][AudienceMember].placementIndex == index) {
                            index++
                            isVacant = false
                            break
                        }
                    }
                    if (isVacant) {
                        foundIndex = index
                        break
                    }
                }
            }
            return foundIndex
        }
    }

    private class JokeTimerTask : LeafTask<SessionBlackBoard>() {

        private var isTimeUp = false

        private val bBoard: SessionBlackBoard; get() = `object`

        override fun start() {
            super.start()

            val world = bBoard.world

            if (bBoard.jokeCount == 0) {
                isTimeUp = true
                return
            }

            val jokeDuration = when {
                bBoard.jokeCount < 3 -> JokeTimerDuration.Sec25
                bBoard.jokeCount < 5 -> JokeTimerDuration.Sec20
                bBoard.jokeCount < 7 -> JokeTimerDuration.Sec15
                bBoard.jokeCount < 11 -> JokeTimerDuration.Sec10
                else -> JokeTimerDuration.Sec7
            }
            bBoard.eJokeTimer = JokeTimerHelper.createTimer(
                world, 60f * UPP, -47f * UPP, jokeDuration
            ) {
//                Gdx.app.postRunnable { onJokeBuilderTimeUp() }
                onJokeBuilderTimeUp()
            }
        }

        override fun end() {
            super.end()
            hideTimer()
        }

        override fun resetTask() {
            super.resetTask()
            isTimeUp = false
        }

        private fun onJokeBuilderTimeUp() {
            val world = bBoard.world

            hideTimer()
//            // At first, destroy the timer
//            if (bBoard.eJokeTimer != null) {
//                if (bBoard.eJokeTimer!! in world) {
//                    world -= bBoard.eJokeTimer!!
//                }
//                bBoard.eJokeTimer = null
//            }

            val wasFinalized = world.system<JokeBuilderUiController>().tryFinalize()
            if (wasFinalized) {
                return
            }

            with(world) {
                val candidates = bBoard.audienceMembers.filter { it[AudienceMember].emoLevel > -2f }.toGdxArray()
                candidates.shuffle()
                val audMemb = candidates.firstOrNull()
                if (audMemb != null) {
                    changeAudMemberEmoLevel(world, audMemb[AudienceMember], -1)
                }
            }
        }

        private fun hideTimer() {
            val world = bBoard.world
            if (bBoard.eJokeTimer != null) {
                if (bBoard.eJokeTimer!! in world) {
                    world -= bBoard.eJokeTimer!!
                }
                bBoard.eJokeTimer = null
            }
        }

        override fun execute(): Status =
            if (isTimeUp) Status.SUCCEEDED else Status.RUNNING

        override fun copyTo(task: Task<SessionBlackBoard>?): Task<SessionBlackBoard> {
            TODO("Not yet implemented")
        }
    }

    private class AudienceAffectionIndicatorsTask : LeafTask<SessionBlackBoard>() {

        private val onJokeStateChangeHandler: (JokeBuilderUiController.JokeIntermediaryStructure) -> Unit = { joke ->
            val bBoard = `object`
            val world = bBoard.world

            val affectionPre = joke.connector.affectionPre
            val affectionPost = joke.connector.affectionPost
            val subjectPre = joke.subjectPre
            val subjectPost = joke.subjectPost

            bBoard.audienceMembers.forEach { eAudMemb ->
                val cAudMemb = with (world) { eAudMemb[AudienceMember] }
                var affectionDelta = 0
                var isAffected = false
                if (affectionPre != 0 && subjectPre != null && checkSubjectIntersection(cAudMemb, subjectPre)) {
                    affectionDelta += affectionPre
                    isAffected = true
                }
                if (affectionPost != 0 && subjectPost != null && checkSubjectIntersection(cAudMemb, subjectPost)) {
                    affectionDelta += affectionPost
                    isAffected = true
                }

                val affectionIndicatorType = when {
                    //TODO Add more indicators if needed.
                    isAffected -> AudienceMember.AffectionIndicator.Type.Unknown
                    else -> null
                }
                AudienceMemberHelper.setAffectionIndicator(world, eAudMemb, affectionIndicatorType)
            }
        }

        override fun start() {
            super.start()

            val bBoard = `object`
            val jokeBuilder = bBoard.world.system<JokeBuilderUiController>()
            jokeBuilder.onJokeStateChange += onJokeStateChangeHandler
        }

        override fun end() {
            super.end()

            val bBoard = `object`
            val world = bBoard.world

            val jokeBuilder = bBoard.world.system<JokeBuilderUiController>()
            jokeBuilder.onJokeStateChange -= onJokeStateChangeHandler

            // Reset indicators.
            bBoard.audienceMembers.forEach { eAudMemb ->
                AudienceMemberHelper.setAffectionIndicator(world, eAudMemb, null)
            }
        }

        override fun execute(): Status = Status.RUNNING // Never ending task.

        override fun copyTo(task: Task<SessionBlackBoard>?): Task<SessionBlackBoard> {
            TODO("Not yet implemented")
        }
    }

    private data class AudienceJokeAffectionEntry(
        val audMemb: AudienceMember,
    ) {
        val triggerSubjects = GdxSet<JokeSubjectData>(4)
        var affectionSum: Int = 0
    }

    private class SessionBlackBoard(val world: FleksWorld, maxAudienceCount: Int) {

        // Permanent fields, available thought the entire game.
        val audienceMembers = GdxArray<Entity>()
        lateinit var eComedian: Entity
        lateinit var eScoreLabel: Entity
        var jokeCount = 0
        var scoreCount = 0
        var maxAudienceCount: Int = maxAudienceCount
        var maxAudienceCountDelta: Int = 0
        var isGameOver = false

        // These fields get reset every round.
        val jokeAffections = GdxMap<AudienceMember, AudienceJokeAffectionEntry>()
        var eJokeTimer: Entity? = null
        var completedJoke: JokeStructureData? = null
        var completedJokeView: Entity? = null

        fun resetRound() {
            jokeAffections.clear()
            eJokeTimer = null
            completedJoke = null
            completedJokeView = null
        }
    }
}