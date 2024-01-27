package com.crashinvaders.laughemout.game.tilemap

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.ImageResolver
import com.badlogic.gdx.maps.MapLayers
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.crashinvaders.laughemout.game.GameDrawOrder
import ktx.collections.getOrPut
import ktx.collections.toGdxArray

// I FUCKING HATE LIBGDX'S TILEMAP IMPLEMENTATION
class GameTileMapLoader : TmxMapLoader() {

    override fun load(fileName: String, parameter: Parameters): TiledMap {
        val tmxFile = resolve(fileName)

        this.root = xml.parse(tmxFile)

        val textures = ObjectMap<String, Texture>()
        val map = loadTiledMap(tmxFile, parameter) {
            val assetPath = "textures/tilemap/" + extractName(it)
            val texture = textures.getOrPut(assetPath) {
                val fileHandle = Gdx.files.internal(assetPath)
                val texture = Texture(fileHandle, parameter.generateMipMaps)
                texture.setFilter(parameter.textureMinFilter, parameter.textureMagFilter)
                return@getOrPut texture
            }
            TextureRegion(texture)
        }
        map.setOwnedResources(textures.values().toGdxArray())

        // Add draw order properties to every layer.
        val layers = map.layers
        val gameContentLayerIndex = layers.getIndex(GAME_CONTENT_LAYER_NAME)
        if (gameContentLayerIndex < 0) {
            throw GdxRuntimeException("Cannot find layer with name \"$GAME_CONTENT_LAYER_NAME\"")
        }
        for (i in 0 until layers.size()) {
            // Offset the front and back layers according to the draw order constants.
            val drawOrder: Int = when {
                i < gameContentLayerIndex -> GameDrawOrder.MAP_LAYERS_BACK + (i - gameContentLayerIndex)
                i > gameContentLayerIndex -> GameDrawOrder.MAP_LAYERS_FRONT + (i - gameContentLayerIndex)
                else -> 0
            }
            layers[i].properties.put(LAYER_PROP_DRAW_ORDER, drawOrder)
        }

        return map
    }

    override fun loadLayer(
        map: TiledMap,
        parentLayers: MapLayers,
        element: XmlReader.Element,
        tmxFile: FileHandle,
        imageResolver: ImageResolver
    ) {
        super.loadLayer(map, parentLayers, element, tmxFile, imageResolver)

        val layer = parentLayers[parentLayers.size() - 1] // Obtain the freshly created layer.

        if (element.hasAttribute("tintcolor")) {
            val color = Color.valueOf(element.get("tintcolor"))
            layer.properties.put("tintcolor", color)
        }
    }

    override fun loadImageLayer(
        map: TiledMap,
        parentLayers: MapLayers,
        element: XmlReader.Element,
        tmxFile: FileHandle,
        imageResolver: ImageResolver
    ) {
        if (element.name == "imagelayer") {
            val x = if (element.hasAttribute("offsetx")) {
                element.getFloatAttribute("offsetx")
            } else {
                element.getFloatAttribute("x", 0f)
            }
            var y = if (element.hasAttribute("offsety")) {
                element.getFloatAttribute("offsety")
            } else {
                element.getFloatAttribute("y", 0f)
            }
            if (flipY) y = mapHeightInPixels - y

            var texture: TextureRegion? = null
            val image = element.getChildByName("image")
            if (image != null) {
                val source = image.getAttribute("source")
                val handle = getRelativeFileHandle(tmxFile, source)
                texture = imageResolver.getImage(handle.path())
                y -= texture.regionHeight.toFloat()
            }

            val repeatX = element.getInt("repeatx", 0) > 0
            val repeatY = element.getInt("repeaty", 0) > 0

            val layer = ExtTiledMapImageLayer(texture, x, y, repeatX, repeatY) // Use our own layer implementation.

            loadBasicLayerInfo(layer, element)

            val properties = element.getChildByName("properties")
            if (properties != null) {
                loadProperties(layer.properties, properties)
            }

            parentLayers.add(layer)
        }
    }

    override fun loadObjectGroup(map: TiledMap, parentLayers: MapLayers, element: XmlReader.Element) {
        if (element.name == "objectgroup") {
            val layer = ObjectGroupLayer() // Use our own layer implementation.
            loadBasicLayerInfo(layer, element)
            val properties = element.getChildByName("properties")
            if (properties != null) {
                loadProperties(layer.properties, properties)
            }

            for (objectElement in element.getChildrenByName("object")) {
                loadObject(map, layer, objectElement)
            }

            // The most important bit.
            layer.init(map, element)

            parentLayers.add(layer)
        }
    }

    companion object {
        private const val GAME_CONTENT_LAYER_NAME = "[GAME_CONTENT]"
        const val LAYER_PROP_DRAW_ORDER = "draw_order"

        private fun extractExtension(filePath: String): String {
            val dotIndex = filePath.lastIndexOf('.')
            if (dotIndex == -1) return ""
            return filePath.substring(dotIndex + 1)
        }

        private fun extractName(filePath: String): String {
            val path: String = filePath.replace('\\', '/')
            val slashIndex = path.lastIndexOf('/')
            if (slashIndex == -1) return path
            return path.substring(slashIndex + 1)
        }
    }
}
