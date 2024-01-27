package com.crashinvaders.laughemout.game.controllers

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.crashinvaders.common.*
import com.crashinvaders.laughemout.game.GameDrawOrder
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.UPP
import com.crashinvaders.laughemout.game.common.DrawableUtils
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.SodInterpolation
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.TransformDebugRenderTag
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.common.DrawableUtils.fromActor;
import com.crashinvaders.laughemout.game.common.SodUtils.kickVisually
import com.crashinvaders.laughemout.game.components.JokeSubjectCard
import com.crashinvaders.laughemout.game.components.JokeSubjectCardPlaceholder
import com.crashinvaders.laughemout.game.components.JokeSubjectCardRosterPlacement
import com.crashinvaders.laughemout.game.engine.systems.DrawableRenderSystem
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.EntityComparator
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.TextraLabel
import com.github.tommyettinger.textra.TypingLabel
import ktx.actors.onClick
import ktx.app.KtxInputAdapter
import ktx.app.gdxError
import ktx.log.debug
import ktx.math.component1
import ktx.math.component2

class JokeBuilderUiController : IteratingSystem(family { all(
    JokeSubjectCard,
    Transform,
    DrawableOrigin,
    DrawableDimensions,
) }), FamilyOnAdd, KtxInputAdapter {

//    private val subjCardFamily: Family = world.family { all(
//        JokeSubjectCard,
//        Transform,
//        DrawableOrigin,
//        DrawableDimensions,
//    ) }
    private val subjCardPlaceholderFamily: Family = world.family { all(
        JokeSubjectCardPlaceholder,
        Transform,
    ) }
    private val subjCardRosterPlacementFamily: Family = world.family { all(
        JokeSubjectCardRosterPlacement,
        Transform,
    ) }

    private val rosterPlacementComparator: (Entity, Entity) -> Int = { p0, p1 ->
        CommonUtils.compare(p1[Transform].localPositionX, p0[Transform].localPositionX)
    }

    private lateinit var camSystem: MainCameraStateSystem

    private var uiController: UiController? = null
    private var isEnabled: Boolean = false

    private var eDraggingSubjCard: Entity? = null
    private var lastTouchScreenX: Int = 0
    private var lastTouchScreenY: Int = 0

    override fun onInit() {
        super.onInit()

        camSystem = world.system<MainCameraStateSystem>()

        if (isEnabled) {
            onEnable()
        }
    }

    override fun onEnable() {
        super.onEnable()
        isEnabled = true

        uiController = UiController(world).also {
            it.onJokeItButtonClick += {
                debug { "Click" }
            }
        }

        world.inject<OrderedInputMultiplexer>().addProcessor(this, GameInputOrder.JOKE_BUILDER_UI)
    }

    override fun onDisable() {
        super.onDisable()
        isEnabled = false

        world.inject<OrderedInputMultiplexer>().removeProcessor(this)

        if (uiController != null) {
            uiController!!.dispose()
            uiController= null
        }
    }

    override fun onTickEntity(entity: Entity) = Unit

    override fun onAddEntity(entity: Entity) {
        with(world) {
            moveCardToRoster(entity)
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val (x, y) = camSystem.screenToWorld(screenX, screenY)

        eDraggingSubjCard = family.firstOrNull {
            DrawableUtils.checkHit(world, it, x, y)
        }
        if (eDraggingSubjCard != null) {
            with(world) {
                eDraggingSubjCard!![Transform].localRotation = -15f
                eDraggingSubjCard!![SodInterpolation].kickVisually(0.5f, rotate = false)
                eDraggingSubjCard!![DrawableOrder].order = DRAW_ORDER_CARD_DRAGGED
            }
            lastTouchScreenX = screenX
            lastTouchScreenY = screenY
        }
        return eDraggingSubjCard != null
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (eDraggingSubjCard == null) {
            return false
        }

        eDraggingSubjCard!![Transform].also { transform ->
            val prevScreenX = lastTouchScreenX
            val prevScreenY = lastTouchScreenY
            lastTouchScreenX = screenX
            lastTouchScreenY = screenY
            val (worldX, worldY) = camSystem.screenToWorld(screenX, screenY)
            val (prevWorldX, prevWorldY) = camSystem.screenToWorld(prevScreenX, prevScreenY)
            val deltaX = worldX - prevWorldX
            val deltaY = worldY - prevWorldY
            val (entityPosX, entityPosY) = transform.worldPosition
            transform.setWorldPosition(entityPosX + deltaX, entityPosY + deltaY)
        }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (eDraggingSubjCard == null) {
            return false
        }
        val card = eDraggingSubjCard!!
        val cJokeSubjectCard = card[JokeSubjectCard]

        with(world) {
            val cardTransform = card[Transform]
            val (cardX, cardY) = cardTransform.worldPosition
            val placeholder = subjCardPlaceholderFamily.firstOrNull {
                val (phX, phY) = it[Transform].worldPosition
                val sqDist = tmpVec2.set(cardX, cardY).dst2(phX, phY)
                sqDist < 1.25f
            }
            if (placeholder != null) {
                moveCardToSubjPlaceholder(card, placeholder)
            } else {
                moveCardToRoster(card)
            }

            cardTransform.localRotation = 0f
//            eDraggingSubjCard!![SodInterpolation].kickVisually(0.5f, rotate = false)
            eDraggingSubjCard = null

            handleJokeCardPlacementChange()
        }
        return true
    }

    private fun FleksWorld.moveCardToRoster(card: Entity) {
        val cJokeSubjectCard = card[JokeSubjectCard]
        cJokeSubjectCard.currentJokeSubjPlaceholder?.attachedCard = null

        subjCardRosterPlacementFamily.sort(rosterPlacementComparator)
        val placement = subjCardRosterPlacementFamily.firstOrNull {
            val placement = it[JokeSubjectCardRosterPlacement]
            placement.attachedCard == cJokeSubjectCard || placement.attachedCard == null
        }
        if (placement == null) {
            gdxError("No free placement available.")
        }
        placement[JokeSubjectCardRosterPlacement].attachedCard = cJokeSubjectCard

        card[DrawableOrder].order = DRAW_ORDER_CARD_REGULAR

        val transform = card[Transform]
        transform.parent = placement[Transform]
        transform.localPositionX = 0f
        transform.localPositionY = 0f
    }

    private fun FleksWorld.moveCardToSubjPlaceholder(card: Entity, placeholder: Entity) {
        val cJokeSubjectCard = card[JokeSubjectCard]
        cJokeSubjectCard.currentJokeSubjPlaceholder?.attachedCard = null

        val cPlaceholder = placeholder[JokeSubjectCardPlaceholder]
        if (cPlaceholder.attachedCard != null) {
            val oldCard = cPlaceholder.attachedCard!!.entity
            moveCardToRoster(oldCard)
        }

        card[DrawableOrder].order = DRAW_ORDER_CARD_REGULAR

        cPlaceholder.attachedCard = cJokeSubjectCard
        val cTransform = card[Transform]
        cTransform.parent = placeholder[Transform]
        cTransform.localPositionX = 0f
        cTransform.localPositionY = 0f
    }

    private fun FleksWorld.handleJokeCardPlacementChange() {
        val isJokeReady: Boolean = !let {
            var hasEmptyJokeSubjects = false
            subjCardPlaceholderFamily.forEach {
                if (it[JokeSubjectCardPlaceholder].attachedCard == null) {
                    hasEmptyJokeSubjects = true
                    return@forEach
                }
            }
            return@let hasEmptyJokeSubjects
        }

        debug { "isJokeReady: $isJokeReady" }

        uiController!!.apply {
            eTitle[DrawableVisibility].isVisible = !isJokeReady
            eTitle[SodInterpolation].kickVisually()
            eJokeItButton[DrawableVisibility].isVisible = isJokeReady
            eJokeItButton[SodInterpolation].kickVisually()
        }
    }

    private class UiController(val world: FleksWorld): Disposable {

        val eRoot: Entity
        val ePlaceholderL: Entity
        val ePlaceholderR: Entity
        val eConnector: Entity
        val eTitle: Entity
        val eJokeItButton: Entity

        val onJokeItButtonClick = BlankSignal()

        init {
            val font = world.inject<Font>("pixolaCurva")
            val bmFont = world.inject<BitmapFont>("bmPixolaCurva")
            val atlas = world.inject<TextureAtlas>("ui")

            eRoot = world.entity {
                it += Info("JokeBuilderRoot")
                it += Transform()
            }

            eTitle = world.entity {
                it += Info("JokeBuilderTitle")
                it += Transform().apply {
                    parent = eRoot[Transform]
                    localPositionX = 0f
                    localPositionY = 22f * UPP
                }

                val actor = TypingLabel("[#8da8f2]JOKE ABOUT", font).apply {
                    alignment = Align.center
                    pack()
                }
                it += ActorContainer(actor)
                it += DrawableOrder(DRAW_ORDER_MISC)
                it += DrawableTint()
                it += DrawableVisibility(true)
                it += DrawableDimensions().fromActor(actor)
                it += DrawableOrigin(Align.center)

                it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                    kickVisually()
                }
                it += TransformDebugRenderTag
            }

            eJokeItButton = world.entity {
                it += Info("JokeItButton")
                it += Transform().apply {
                    parent = eRoot[Transform]
                    localPositionX = 0f
                    localPositionY = 32f * UPP
                }

                val actor = Button(
                        TextureRegionDrawable(atlas.findRegion("btn-joke-it-up")),
                        TextureRegionDrawable(atlas.findRegion("btn-joke-it-down"))).apply {
                    isTransform = true
                    touchable = Touchable.enabled
                    onClick { onJokeItButtonClick() }
                    pack()
                }
                it += ActorContainer(actor)
                it += DrawableOrder(DRAW_ORDER_JOKE_IT_BUTTON)
                it += DrawableTint()
                it += DrawableVisibility(false)
                it += DrawableDimensions().fromActor(actor)
                it += DrawableOrigin(Align.center)

                it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                    kickVisually()
                }
                it += TransformDebugRenderTag
            }

            eConnector = world.entity {
                it += Info("JokeConnector")
                it += Transform().apply {
                    parent = eRoot[Transform]
                    localPositionX = 0f
                    localPositionY = 0f
                }

                val actor = TypingLabel("drive\nbetter\nthan", font).apply {
                    alignment = Align.center
                    pack()
                }
                it += ActorContainer(actor)
                it += DrawableOrder(DRAW_ORDER_MISC)
                it += DrawableTint()
                it += DrawableVisibility()
                it += DrawableDimensions().fromActor(actor)
                it += DrawableOrigin(Align.center)

                it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                    kickVisually()
                }
                it += TransformDebugRenderTag
            }

            ePlaceholderL = world.entity {
                it += Info("JokeSubjPlaceholderLeft")
                it += JokeSubjectCardPlaceholder()
                it += Transform().apply {
                    parent = eRoot[Transform]
                    localPositionX = -56f * UPP
                    localPositionY = 0f
                }

                val actor = Image(atlas.findRegion("joke-subj-placeholder"))
                it += ActorContainer(actor)
                it += DrawableOrder(DRAW_ORDER_PLACEHOLDER)
                it += DrawableTint()
                it += DrawableVisibility()
                it += DrawableDimensions().fromActor(actor)
                it += DrawableOrigin(Align.center)

                it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                    kickVisually()
                }
                it += TransformDebugRenderTag
            }

            ePlaceholderR = world.entity {
                it += Info("JokeSubjPlaceholderRight")
                it += JokeSubjectCardPlaceholder()
                it += Transform().apply {
                    parent = eRoot[Transform]
                    localPositionX = 56f * UPP
                    localPositionY = 0f
                }

                val actor = Image(atlas.findRegion("joke-subj-placeholder"))
                it += ActorContainer(actor)
                it += DrawableOrder(DRAW_ORDER_PLACEHOLDER)
                it += DrawableTint()
                it += DrawableVisibility()
                it += DrawableDimensions().fromActor(actor)
                it += DrawableOrigin(Align.center)

                it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                    kickVisually()
                }
                it += TransformDebugRenderTag
            }

            fun createRosterPlacement(index: Int, x: Float, y: Float) {
                world.entity {
                    it += Info("JokeCardRosterPlacement$index")
                    it += JokeSubjectCardRosterPlacement()
                    it += Transform().apply {
                        parent = eRoot[Transform]
                        localPositionX = x
                        localPositionY = y
                    }
                    it += TransformDebugRenderTag
                }
            }

            fun createCard(task: String) {
                world.entity {
                    it += Info("JokeSubjCard")
                    it += JokeSubjectCard()
                    it += Transform().apply {
                        parent = eRoot[Transform]
                        localPositionX = 0f
                        localPositionY = -48f * UPP
                    }

                    val actor = let {
                        val label = TextraLabel("[#ffedd4]$task", font).apply {

                            alignment = Align.center
                        }
                        Container(label).apply {
                            background = TextureRegionDrawable(atlas.findRegion("joke-subj-frame"))
//                        setBackground(TextureRegionDrawable(atlas.findRegion("joke-subj-frame")), true)
                            isTransform = true
                            fill()
                            center()
                            pack()
                            debug()
                        }
                    }
                    it += ActorContainer(actor)
                    it += DrawableOrder(DRAW_ORDER_CARD_REGULAR)
                    it += DrawableTint()
                    it += DrawableVisibility()
                    it += DrawableDimensions().fromActor(actor)
                    it += DrawableOrigin(Align.center)

                    it += SodInterpolation(6f, 0.6f, -0.5f).apply {
                        kickVisually()
                    }
                    it += TransformDebugRenderTag
                }
            }

            val rosterSize = 4
            val rosterItemWidth = 70f * UPP
            for (i in 0 until rosterSize) {
                val x = -0.5f * (rosterSize * rosterItemWidth) + i * rosterItemWidth + rosterItemWidth * 0.5f
                val y = if (i % 2 == 0) -40 * UPP else -48 * UPP
                createRosterPlacement(i, x, y)
            }

            createCard("BLACK\nPEOPLE")
            createCard("BRUNETTES")
            createCard("TALL\nPEOPLE")
        }

        override fun dispose() {
            world -= eRoot
        }
    }

    companion object {
        private val tmpVec2 = Vector2()

        private const val DRAW_ORDER_BASE = GameDrawOrder.JOKE_BUILDER_UI_BASE
        private const val DRAW_ORDER_CARD_DRAGGED =   -10 + DRAW_ORDER_BASE
        private const val DRAW_ORDER_CARD_REGULAR =   -5  + DRAW_ORDER_BASE
        private const val DRAW_ORDER_JOKE_IT_BUTTON = -1  + DRAW_ORDER_BASE
        private const val DRAW_ORDER_PLACEHOLDER =    +5  + DRAW_ORDER_BASE
        private const val DRAW_ORDER_MISC =           +10 + DRAW_ORDER_BASE
    }
}