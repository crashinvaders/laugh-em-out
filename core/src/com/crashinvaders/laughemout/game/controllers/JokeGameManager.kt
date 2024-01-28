package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.*
import com.crashinvaders.laughemout.game.CameraProcessorOrder
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActor
import com.crashinvaders.laughemout.game.common.camera.Sod3CameraProcessor
import com.crashinvaders.laughemout.game.components.AudienceMember
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.EntityActionSystem
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.DelayAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.RunnableAction
import com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.SequenceAction
import com.esotericsoftware.spine.SkeletonRenderer
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TypingLabel
import ktx.ai.add
import ktx.ai.repeat
import ktx.ai.sequence
import ktx.ai.waitLeaf
import ktx.app.gdxError
import ktx.collections.*
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2

class JokeGameManager : IntervalSystem(),
    OrderedDisposableRegistry by OrderedDisposableContainer() {

    private val skelRenderer = world.inject<SkeletonRenderer>()
    private val assets = world.inject<AssetManager>()
    private val atlasCharacters = world.inject<TextureAtlas>("characters")
    private val atlasEnv = world.inject<TextureAtlas>("env")

    private val camProcessor = Sod3CameraProcessor(4f, 0.8f, 0f,
        CameraProcessorOrder.JOKE_MANAGER,
        readCamValuesWhenAdded = false).apply {
        x = 60f * UPP
        y = 10f * UPP
    }

    private val bBoard = BTreeBlackBoard(world)
    private val bTree: BehaviorTree<BTreeBlackBoard> = BehaviorTree()

    override fun onInit() {
        super.onInit()

        createEnvironment(world)

        bBoard.comedian = ComedianHelper.createComedian(world, 0f, 0f)

        val audienceMemberCount = 5
        for (i in 0 until audienceMemberCount) {
            val (x, y) = AudienceMemberHelper.evalSpawnPosition(i)
            val entity = AudienceMemberHelper.create(world, x, y, i)
            bBoard.audienceMembers.add(entity)
        }

        world.system<MainCameraStateSystem>().addProcessor(camProcessor)

        bTree.apply {
            setObject(bBoard)
            var counter = 0

            repeat { resetOnCompletion { sequence {
                waitLeaf(2f)
                add(JokeBuilderStateTask(world))
                runnable {
                    val completedJoke = bBoard.completedJoke!!

                    bBoard.completedJokeView = createResultJokeView(world, completedJoke)

                    SpeechBubbleHelper.createSpeechBubble(
                        world, "...",
                        bBoard.comedian[Transform].worldPositionX,
                        bBoard.comedian[Transform].worldPositionY + 56f * UPP,
                        SpeechBubbleSize.Small, 2f
                    )

                    evalJokeAffections(world, bBoard)
                }
                waitLeaf(2f)

                add(AffectAudienceTask(world))

                waitLeaf(2f)

                runnable {
                    if (bBoard.completedJokeView != null) {
                        world -= bBoard.completedJokeView!!
                        bBoard.completedJokeView = null
                    }
                }

                runnable {
                    bBoard.reset()
                }
            }
        } } }
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
            val actor = TypingLabel(text, font)
            it += ActorContainer(actor)
            it += DrawableOrder(GameDrawOrder.COMPLETED_JOKE_VIEW)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActor(actor)
            it += DrawableOrigin(Align.center)
        }
    }

    override fun onTick() {
        bTree.step()
    }

    override fun onDisable() {
        super.onDisable()
        dispose()
    }

    private fun createEnvironment(world: FleksWorld) {
        world.entity {
            it += Info("ComedyStage")
            it += Transform().apply {
                localPositionX = -55f * UPP
                localPositionY = -25f * UPP
            }

            val actor = Image(atlasEnv.findRegion("stage0"))
            it += ActorContainer(actor)
            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActor(actor)
            it += DrawableOrigin(Align.bottomLeft)
        }

        world.entity {
            it += Info("CrowdGround")
            it += Transform().apply {
                localPositionX = -73f * UPP
                localPositionY = -46f * UPP
            }

            val actor = Image(atlasEnv.findRegion("ground0"))
            it += ActorContainer(actor)
            it += DrawableOrder(GameDrawOrder.ENVIRONMENT_BACK - 10)
            it += DrawableTint()
            it += DrawableVisibility()
            it += DrawableDimensions().fromActor(actor)
            it += DrawableOrigin(Align.bottomLeft)
        }
    }

    companion object {
        private fun evalJokeAffections(world: FleksWorld, bBoard: BTreeBlackBoard) {
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
//                        if (entry.affectionSum == 0) {
//                            affectionMap.remove(cAudMemb)
//                        }
                    }
                    if (affectionPost != 0 && checkSubjectIntersection(cAudMemb, subjectPost)) {
                        val entry = affectionMap.getOrPut(cAudMemb) { AudienceJokeAffectionEntry(cAudMemb) }
                        entry.affectionSum += affectionPost
                        entry.triggerSubjects.add(subjectPost)
//                        if (entry.affectionSum == 0) {
//                            affectionMap.remove(cAudMemb)
//                        }
                    }
                }
            }
        }

        private fun checkSubjectIntersection(audMemb: AudienceMember, jokeSubject: JokeSubjectData): Boolean {
            return (audMemb.race == jokeSubject.race) ||
                    (audMemb.gender == jokeSubject.gender) ||
                    (audMemb.hairStyle != null && audMemb.hairStyle == jokeSubject.hairStyle) ||
                    (audMemb.hairColor == jokeSubject.hairColor) ||
                    (audMemb.heightLevel == jokeSubject.heightLevel) ||
                    (audMemb.bodyStyle == jokeSubject.bodyStyle) ||
                    (audMemb.glasses != null && audMemb.glasses == jokeSubject.glasses) ||
                    (audMemb.hat != null && audMemb.hat == jokeSubject.hat) ||
                    (audMemb.neck != null && audMemb.neck == jokeSubject.neck) ||
                    (audMemb.mouth != null && audMemb.mouth == jokeSubject.mouth)
        }
    }

    private class IntroStateTask(val world: FleksWorld): LeafTask<BTreeBlackBoard>() {

        private val actionSystem = world.system<EntityActionSystem>()

        private var isCompleted = false

        private val actionHost = world.entity()

        override fun start() {
            super.start()

            actionSystem.addAction(actionHost, SequenceAction(
                RunnableAction {

                },
                DelayAction(2f),
                RunnableAction {
                    debug { "Done!" }
                    isCompleted = true
                }
            ))
        }

        override fun end() {
            super.end()
            world -= actionHost
        }

        override fun execute(): Status {
            return if (isCompleted) Status.SUCCEEDED else Status.RUNNING
        }

        override fun copyTo(p0: Task<BTreeBlackBoard>?): Task<BTreeBlackBoard> {
            TODO("Not yet implemented")
        }
    }

    private class JokeBuilderStateTask(val world: FleksWorld): LeafTask<BTreeBlackBoard>() {

        var isCompleted = false
            private set

        override fun start() {
            super.start()

            world.system<JokeBuilderUiController>().show(world, JokeBuilderData(
                gdxArrayOf(
                    JokeSubjectData("BLACK\nPEOPLE", race = AudienceMemberHelper.Race.Black),
                    JokeSubjectData("BRUNETTES", hairColor = AudienceMemberHelper.HairColor.Brunette),
                    JokeSubjectData("TALL\nPEOPLE", heightLevel = AudienceMemberHelper.HeightLevel.Tall),
                ),
                JokeConnectorData("drive\nbetter\nthan", 1, -1))
            ) {
                debug { "Complete joke: ${it.subjectPre.text.replace('\n', ' ')} ${it.connector.text.replace('\n', ' ')} ${it.subjectPost.text.replace('\n', ' ')}" }

                getObject().completedJoke = it
                isCompleted = true
            }
        }

        override fun resetTask() {
            super.resetTask()
            isCompleted = false
        }

        override fun execute(): Status {
            return if (isCompleted) Status.SUCCEEDED else Status.RUNNING
        }

        override fun copyTo(p0: Task<BTreeBlackBoard>?): Task<BTreeBlackBoard> {
            TODO("Not yet implemented")
        }
    }

    private class AffectAudienceTask(val world: FleksWorld): LeafTask<BTreeBlackBoard>() {

        private val actionSystem = world.system<EntityActionSystem>()

        var isCompleted = false
            private set

        override fun start() {
            super.start()

            val sequenceAction = SequenceAction()

            val affections = `object`.jokeAffections.values().toGdxArray()
            affections.shuffle()
            affections.forEachIndexed { index, affection ->
                val audMemb = affection.audMemb

//                if (affection.affectionSum == 0) {
//                    gdxError("There should not be records with 0 affect delta.")
//                }

                val affectionDelta = affection.affectionSum
                if (affectionDelta != 0) {
                    val emoLevel = MathUtils.clamp(audMemb.emoLevel + affectionDelta, -3, +3)
                    sequenceAction.addAction(RunnableAction {
                        audMemb.emoLevel = emoLevel
                        audMemb.emoMeter.also {
                            it.emoLevel = emoLevel
                            EmoMeterHelper.animateValueChange(world, it.entity)
                            EmoMeterHelper.animateToken(
                                world, it.entity, when {
                                    emoLevel >= 3 -> TokenType.Star
                                    emoLevel <= -3 -> TokenType.Cancel
                                    affectionDelta > 0 -> TokenType.Like
                                    affectionDelta < 0 -> TokenType.Dislike
                                    else -> gdxError("Unexpected case")
                                }
                            )

                            if (affectionDelta > 0) {
                                AudienceMemberHelper.animateJokeReactionPos(world, audMemb.entity)
                            } else {
                                AudienceMemberHelper.animateJokeReactionNeg(world, audMemb.entity)
                            }
                        }
                    })
                }
                sequenceAction.addAction(DelayAction(1.0f))
            }

            sequenceAction.addAction(RunnableAction {
                isCompleted = true
            })

            actionSystem.addAction(getObject().comedian, sequenceAction)
        }

        override fun resetTask() {
            super.resetTask()
            isCompleted = false
        }

        override fun execute(): Status {
            return if (isCompleted) Status.SUCCEEDED else Status.RUNNING
        }

        override fun copyTo(p0: Task<BTreeBlackBoard>?): Task<BTreeBlackBoard> {
            TODO("Not yet implemented")
        }
    }

    data class AudienceJokeAffectionEntry(
        val audMemb: AudienceMember,
    ) {
        val triggerSubjects = GdxSet<JokeSubjectData>(4)
        var affectionSum: Int = 0
    }

    private class BTreeBlackBoard(val world: FleksWorld) {

        val audienceMembers = GdxArray<Entity>()
        lateinit var comedian: Entity

        var completedJoke: JokeStructureData? = null
        var completedJokeView: Entity? = null
        var jokeAffections = GdxMap<AudienceMember, AudienceJokeAffectionEntry>()

        fun reset() {
            completedJoke = null
            completedJokeView = null

            jokeAffections.clear()
        }
    }
}