package com.crashinvaders.laughemout.game.tilemap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.math.MathUtils
import com.crashinvaders.common.*
import ktx.tiled.property

class GameTileMapRenderer(map: TiledMap, unitScale: Float, batch: Batch) :
    OrthogonalTiledMapRenderer(map, unitScale, batch) {

    // Make it public.
    public override fun renderMapLayer(layer: MapLayer) {
        super.renderMapLayer(layer)
    }

    override fun renderObjects(mapLayer: MapLayer) {
        val layer = mapLayer as ObjectGroupLayer

        val color = layer.tint
        batch.setColor(color.r, color.g, color.b, color.a * layer.opacity)

        val layerOffsetX = layer.renderOffsetX * unitScale - viewBounds.centerX * (layer.parallaxX - 1)
        // offset in tiled is y down, so we flip it
        val layerOffsetY = -layer.renderOffsetY * unitScale - viewBounds.centerY * (layer.parallaxY - 1)

        val renderObjects = layer.renderObjects
        for (i in 0 until renderObjects.size) {
            val obj = renderObjects.get(i)
            val region = obj.textureRegion

            val x = layerOffsetX + (obj.x * unitScale)
            val y = layerOffsetY + (obj.y * unitScale)
            val width = region.regionWidth * unitScale
            val height = region.regionHeight * unitScale
            val originX = obj.originX * unitScale
            val originY = obj.originY * unitScale
            val scaleX = obj.scaleX
            val scaleY = obj.scaleY
            val rotation = obj.rotation

            batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, -rotation)
        }
    }

    override fun renderImageLayer(mapLayer: TiledMapImageLayer) {
        val layer = mapLayer as ExtTiledMapImageLayer

        //TODO Hashmap lookup is pretty slow, find a way to cache the value.
        val color = layer.property("tintcolor", Color.WHITE)
        val colorBits = Color.toFloatBits(color.r, color.g, color.b, color.a * layer.opacity)

        val vertices = this.vertices

        val region = layer.textureRegion ?: return

        fun renderTile(x: Float, y: Float, width: Float, height: Float) {
            val x1 = x;
            val y1 = y;
            val x2 = x + width
            val y2 = y + height

            val u1 = region.u
            val v1 = region.v2
            val u2 = region.u2
            val v2 = region.v

            vertices[Batch.X1] = x1
            vertices[Batch.Y1] = y1
            vertices[Batch.C1] = colorBits
            vertices[Batch.U1] = u1
            vertices[Batch.V1] = v1

            vertices[Batch.X2] = x1
            vertices[Batch.Y2] = y2
            vertices[Batch.C2] = colorBits
            vertices[Batch.U2] = u1
            vertices[Batch.V2] = v2

            vertices[Batch.X3] = x2
            vertices[Batch.Y3] = y2
            vertices[Batch.C3] = colorBits
            vertices[Batch.U3] = u2
            vertices[Batch.V3] = v2

            vertices[Batch.X4] = x2
            vertices[Batch.Y4] = y1
            vertices[Batch.C4] = colorBits
            vertices[Batch.U4] = u2
            vertices[Batch.V4] = v1

            batch.draw(region.texture, vertices, 0, NUM_VERTICES)
        }

        val x = layer.renderOffsetX * unitScale - viewBounds.centerX * (layer.parallaxX - 1f)
        val y = layer.renderOffsetY * unitScale - viewBounds.centerY * (layer.parallaxY - 1f)
        val width = region.regionWidth * unitScale
        val height = region.regionHeight * unitScale

        var leftMostX: Float = x
        var repeatsX: Int = 1
        if (layer.repeatX) {
            if (x > viewBounds.x) {
                leftMostX = x - width * MathUtils.ceil((x - viewBounds.x) / width)
            } else if ((x + width) < viewBounds.right) {
                leftMostX = x + width * MathUtils.floor((viewBounds.x - x) / width)
            }
            repeatsX = MathUtils.ceil((viewBounds.right - leftMostX) / width)
        }
        var bottomMostY: Float = y
        var repeatsY: Int = 1
        if (layer.repeatY) {
            if (y > viewBounds.y) {
                bottomMostY = y - height * MathUtils.ceil((y - viewBounds.y) / height)
            } else if ((y + height) < viewBounds.top) {
                bottomMostY = y + height * MathUtils.floor((viewBounds.y - y) / height)
            }
            repeatsY = MathUtils.ceil((viewBounds.top - bottomMostY) / height)
        }

        for (i in 0 until repeatsX) {
            val tileX = leftMostX + i * width
            for (j in 0 until repeatsY) {
                val tileY = bottomMostY + j * height
                renderTile(tileX, tileY, width, height)
            }
        }
    }

    override fun beginRender() {
        AnimatedTiledMapTile.updateAnimationBaseTime()
    }

    override fun endRender() {

    }
}
