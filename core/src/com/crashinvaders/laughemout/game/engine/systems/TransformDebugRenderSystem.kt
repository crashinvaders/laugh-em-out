package com.crashinvaders.laughemout.game.engine.systems

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.applyToPoint
import com.crashinvaders.common.applyToVector
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.crashinvaders.laughemout.game.engine.components.*
import ktx.math.component1
import ktx.math.component2
import kotlin.math.abs

class TransformDebugRenderSystem : IteratingSystem(
    family {
        all(Info, Transform, TransformDebugRenderTag)
    }
), OnWorldInitializedHandler {
    private val batch: PolygonSpriteBatch = inject()
    private val shapeRenderer: ShapeRenderer = inject()
    private val debugFont: BitmapFont = inject("debugFont")
//    private val debugFont: BitmapFont = inject<AssetManager>().get("fonts/pixola-cursiva-shadow.fnt")

    private val glyphLayout = GlyphLayout()

    private val debugCam = OrthographicCamera()
    private lateinit var mainCam: Camera

    private val worldToDebugMat = Affine2()

    override fun onWorldInitialized() {
        val mainCamEntity = world.system<MainCameraStateSystem>().cameraEntity
        mainCam = mainCamEntity[WorldCamera].camera

        world.entity {
            it += Info("DebugOverlayCamera")
            it += WorldCamera(camera = debugCam, ppu = 1f)
            it += WorldCameraTag.DEBUG_OVERLAY
            it += Transform().apply {
//                parent = mainCamEntity[Transform]
            }
        }
    }

    override fun onTick() {
        worldToDebugMat.set(tmpMat.set(debugCam.combined).inv().mul(mainCam.combined))

        val combinedMat = debugCam.combined
        shapeRenderer.projectionMatrix = combinedMat
        batch.projectionMatrix = combinedMat

//        HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height) //TODO Do we need that call?

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        batch.begin()
        super.onTick()
        batch.end()
        shapeRenderer.end()
    }

    override fun onTickEntity(entity: Entity) {
        val transform = entity[Transform]
        val (posX, posY) = transform.worldPosition
        val (scaleX, scaleY) = transform.worldScale
        val (fwdX, fwdY) = transform.worldRight
        val (upX, upY) = transform.worldUp

        val (renderPosX, renderPosY) = worldToDebugMat.applyToPoint(tmpVec.set(posX, posY))
        val (renderFwdX, renderFwdY) = worldToDebugMat.applyToVector(tmpVec.set(fwdX, fwdY).scl(abs(scaleX)))
        val (renderUpX, renderUpY) = worldToDebugMat.applyToVector(tmpVec.set(upX, upY).scl(abs(scaleY)))

        shapeRenderer.color = COLOR_FORWARD
        shapeRenderer.line(renderPosX, renderPosY, renderPosX + renderFwdX, renderPosY + renderFwdY)
        shapeRenderer.color = COLOR_UP
        shapeRenderer.line(renderPosX, renderPosY, renderPosX + renderUpX, renderPosY + renderUpY)
        shapeRenderer.color = COLOR_CENTER
        shapeRenderer.circle(renderPosX, renderPosY, CIRCLE_SIZE, 4)

        // Print text info.
        val text = ("${entity[Info].name}\n${toString(transform.localToWorldProj)}")
        glyphLayout.setText(debugFont, text, Color.WHITE, 0f, Align.left, false)
        val textWidth = glyphLayout.width

        debugFont.draw(batch, glyphLayout, renderPosX - textWidth * 0.5f, renderPosY - 8f)


        //TODO Remove me.
        if (entity.has(WorldCamera)) {
            val (camWorldX, camWorldY) = entity[WorldCamera].camera.position
            val (camRenderX, camRenderY) = worldToDebugMat.applyToPoint(tmpVec.set(camWorldX, camWorldY))
            shapeRenderer.color = Color.PURPLE
            shapeRenderer.circle(camRenderX, camRenderY, CIRCLE_SIZE, 8)
        }
    }

    companion object {
        private const val CIRCLE_SIZE = 8f

        private val COLOR_FORWARD = Color.RED
        private val COLOR_UP = Color.GREEN
        private val COLOR_CENTER = Color.WHITE

        private val tmpMat = Matrix4()
        private val tmpVec = Vector2()

        private fun toString(matrix: Affine2): String =
            when (Gdx.app.type) {
                //TODO TeaVM not yet support the "f" formatting symbol. But they should enable it in 0.9.3+ release.
                Application.ApplicationType.WebGL -> ""
                else -> "[#ffb020]%+.1f %+.1f [#ff4040]%+.1f\n[#20ffb0]%+.1f %+.1f [#40ff40]%+.1f"
                    .format(matrix.m00, matrix.m01, matrix.m02, matrix.m10, matrix.m11, matrix.m12)
            }
    }
}
