package com.crashinvaders.laughemout.game.tilemap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.utils.XmlReader.Element
import ktx.collections.GdxArray

class ObjectGroupLayer : MapLayer() {

    val renderObjects = GdxArray<TiledMapTileMapObject>()

    lateinit var tint: Color

    fun init(map: TiledMap, element: Element) {
        for (obj in objects) {
            if (obj is TiledMapTileMapObject) {
                renderObjects.add(obj)
            }
        }

        // Tiled renders all tiles and objects using the "Right Down" rule.
        renderObjects.sort { ro0, ro1 ->
            val resultY = ro1.y.compareTo(ro0.y)
            if (resultY != 0) {
                return@sort resultY
            }
            return@sort ro0.x.compareTo(ro1.x)
        }

        tint = if (element.hasAttribute("tintcolor")) {
            Color.valueOf(element.get("tintcolor"))
        } else {
            Color.WHITE
        }
    }
}
