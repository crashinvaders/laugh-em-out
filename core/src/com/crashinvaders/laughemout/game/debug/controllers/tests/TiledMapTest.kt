package com.crashinvaders.laughemout.game.debug.controllers.tests

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.crashinvaders.common.FleksWorld
import com.crashinvaders.common.OrderedInputMultiplexer
import com.github.quillraven.fleks.Entity
import com.crashinvaders.laughemout.game.GameInputOrder
import com.crashinvaders.laughemout.game.debug.controllers.DebugController
import com.crashinvaders.laughemout.game.engine.components.Info
import com.crashinvaders.laughemout.game.engine.components.Transform
import com.crashinvaders.laughemout.game.engine.components.render.*
import com.crashinvaders.laughemout.game.engine.systems.MainCameraStateSystem
import com.crashinvaders.laughemout.game.tilemap.GameTileMapLoader
import com.crashinvaders.laughemout.game.tilemap.GameTileMapRenderer
import ktx.app.KtxInputAdapter
import ktx.tiled.property

class TiledMapTest(private val fleksWorld: FleksWorld) : KtxInputAdapter, DebugController {

    private val camera: Camera
    private val mapEntity: Entity

    private val atlas = TextureAtlas(Gdx.files.internal("skeletons/spineboy-pro.atlas"))

    private val inputMultiplexer: OrderedInputMultiplexer = fleksWorld.inject()

    private val tileMap : TiledMap

    init {
        camera = fleksWorld.system<MainCameraStateSystem>().camera

        tileMap = GameTileMapLoader().load("maps/test0.tmx", TmxMapLoader.Parameters())

        val batch = fleksWorld.inject<PolygonSpriteBatch>()
        val tileMapRenderer = GameTileMapRenderer(tileMap, 1f/32f, batch)

        with(fleksWorld) {
            mapEntity = entity { entity ->
                entity += Info("TileMapRoot")
                entity += Transform()
            }

            for (layer in tileMap.layers) {
                if (!layer.isVisible)
                    continue

                val drawOrder: Int = layer.property<Int>(GameTileMapLoader.LAYER_PROP_DRAW_ORDER)

                entity { entity ->
                    entity += Info("TileMapLayer[${layer.name}]")
                    entity += Transform().apply {
                        parent = mapEntity[Transform]
                    }

                    entity += ActorContainer(TileMapLayerActor(camera, tileMapRenderer, layer))
                    entity += DrawableOrder(drawOrder)
                    entity += DrawableTint()
                    entity += DrawableVisibility()
                    entity += DrawableDimensions(0f)
                    entity += DrawableOrigin()
                }
            }
        }

        inputMultiplexer.addProcessor(this@TiledMapTest, GameInputOrder.DEBUG_CONTROLLERS)
    }

    override fun dispose() {
        fleksWorld -= mapEntity

        atlas.dispose()

        tileMap.dispose()

        inputMultiplexer.removeProcessor(this@TiledMapTest)
    }

//    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
//        val (x, y) = screenToWorld(screenX, screenY)
//        transform.setWorldPosition(x, y)
//        return true
//    }

    private fun screenToWorld(screenX: Int, screenY: Int): Vector3 =
        camera.unproject(tmpVec.set(screenX.toFloat(), screenY.toFloat(), 0f))

    companion object {
        private val tmpVec = Vector3()
    }

    class TileMapLayerActor(
        val camera: OrthographicCamera,
        val renderer: GameTileMapRenderer,
        val layer: MapLayer,
    ) : Actor() {

//        init {
//            this.color = layer.property("tintcolor", Color.WHITE)
//        }

        override fun draw(batch: Batch, parentAlpha: Float) {
            super.draw(batch, parentAlpha)

//            val ownColor = color
//            batch.color.set(ownColor.r, ownColor.g, ownColor.b, ownColor.a * parentAlpha)

            AnimatedTiledMapTile.updateAnimationBaseTime()

            renderer.setView(camera)
            renderer.renderMapLayer(layer)
        }
    }
}
