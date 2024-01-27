package com.crashinvaders.laughemout.game.tilemap

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer

// Support for repeat X/Y attributes.
class ExtTiledMapImageLayer(
    region: TextureRegion?,
    x: Float,
    y: Float,
    val repeatX: Boolean,
    val repeatY: Boolean
) : TiledMapImageLayer(region, x, y)
