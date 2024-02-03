package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.applyToPoint
import com.crashinvaders.laughemout.game.WorldHelper.evalEntityDebugColor
import com.crashinvaders.laughemout.game.WorldHelper.getPrintName
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.WorldCamera
import com.crashinvaders.laughemout.game.engine.components.WorldCameraTag
import com.crashinvaders.laughemout.game.engine.components.render.DrawableDimensions
import com.crashinvaders.laughemout.game.engine.components.render.DrawableOrigin
import com.crashinvaders.laughemout.game.engine.components.render.DrawableRenderer
import com.crashinvaders.laughemout.game.engine.components.render.DrawableVisibility
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import ktx.collections.GdxArray
import ktx.math.component1
import ktx.math.component2

class DrawableDebugRenderSystem : IteratingSystem(
    World.family { all(Transform, DrawableRenderer) }
), OnWorldInitializedHandler {

    private val highlightedDrawables = GdxArray<Entity>(8)

    private val glyphLayout = GlyphLayout()

    private val shapes = World.inject<ShapeRenderer>()
    private val batch = World.inject<PolygonSpriteBatch>()
    private val debugFont: BitmapFont = World.inject("debugFont")

    private var eDebugCam: Entity? = null
    private val debugCam = OrthographicCamera()

    private lateinit var worldCamSystem: MainCameraStateSystem

    override fun onWorldInitialized() {
        worldCamSystem = world.system<MainCameraStateSystem>()

        if (enabled) {
            onEnable()
        }
    }

    override fun onEnable() {
        super.onEnable()

        eDebugCam = world.entity {
            it += Info("DrawableDebugRenderSystemCamera")
            it += WorldCamera(camera = debugCam, ppu = 1f)
            it += WorldCameraTag.DEBUG_DRAWABLES
            it += Transform()
        }
    }

    override fun onDisable() {
        super.onDisable()

        if (eDebugCam != null) {
            world -= eDebugCam!!
            eDebugCam = null
        }
    }

    override fun onTick() {
        shapes.projectionMatrix = worldCamSystem.camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Line)

        val (pointerWorldX, pointerWorldY) =
            worldCamSystem.screenToWorld(Gdx.input.x, Gdx.input.y)

        family.forEach {
            onTickEntity(it, pointerWorldX, pointerWorldY)
        }

        shapes.end()
        shapes.transformMatrix.idt() // Reset the transform matrix.

        if (highlightedDrawables.size != 0) {

            val (pointerDebugX, pointerDebugY) =
                debugCam.unproject(tmpVec3.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))

            batch.projectionMatrix = debugCam.combined
            batch.color.set(Color.WHITE)
            batch.begin()
            highlightedDrawables.forEachIndexed { index, entity ->
                val printName = entity.getPrintName(world)
                val textColor = evalEntityDebugColor(entity, saturation = 50f)
                val shiftY = debugFont.lineHeight * index
                glyphLayout.setText(debugFont, printName, textColor, 0f, Align.left, false)
                debugFont.draw(batch, glyphLayout, pointerDebugX, pointerDebugY + 16f + shiftY)

            }
            batch.end()

            highlightedDrawables.clear()
        }
    }

    private fun onTickEntity(entity: Entity, pointerWorldX: Float, pointerWorldY: Float) {
        if (DrawableVisibility in entity && !entity[DrawableVisibility].isVisible) {
            return
        }

        var isHighlighted = false

        val cTransform = entity[Transform]
        val l2wMat = cTransform.localToWorldProj
        val w2lMat = cTransform.worldToLocalProj

        val (pointerLocalX, pointerLocalY) = w2lMat.applyToPoint(pointerWorldX, pointerWorldY)

        shapes.color.set(evalEntityDebugColor(entity))
        shapes.transformMatrix = tmpMat4.set(l2wMat)
        shapes.line(CROSS_H_SIZE * 2f, 0f, CROSS_H_SIZE, 0f)
        shapes.line(-CROSS_H_SIZE * 2f, 0f, -CROSS_H_SIZE, 0f)
        shapes.line(0f, -CROSS_H_SIZE * 2f, 0f, -CROSS_H_SIZE)
        shapes.line(0f, CROSS_H_SIZE * 2f, 0f, CROSS_H_SIZE)
        shapes.circle(0f, 0f, CROSS_H_SIZE, 6)

        if (!isHighlighted &&
            pointerLocalX > -CROSS_SIZE && pointerLocalX < CROSS_SIZE &&
            pointerLocalY > -CROSS_SIZE && pointerLocalY < CROSS_SIZE) {
            highlightedDrawables.add(entity)
            isHighlighted = true
        }

        if (DrawableDimensions in entity && DrawableOrigin in entity) {
            val cDimens = entity[DrawableDimensions]
            val cOrigin = entity[DrawableOrigin]
            val width = cDimens.width
            val height = cDimens.height
            val x = -width * cOrigin.x
            val y = -height * cOrigin.y
            shapes.rect(x, y, width, height)

            if (!isHighlighted &&
                pointerLocalX > x && pointerLocalX < x + width &&
                pointerLocalY > y && pointerLocalY < y + height) {
                highlightedDrawables.add(entity)
                isHighlighted = true
            }
        }
    }

    override fun onTickEntity(entity: Entity) = Unit

    companion object {
        private const val CROSS_SIZE = 0.2f
        private const val CROSS_H_SIZE = CROSS_SIZE * 0.5f

        private val tmpMat4 = Matrix4()
        private val tmpVec3 = Vector3()
    }
}