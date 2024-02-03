package com.crashinvaders.laughemout.game.engine.components.render;

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.crashinvaders.common.BlankSignal
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class DrawableRenderer(
    val renderer: EntityRenderer
) : Component<DrawableRenderer> {

    override fun type() = DrawableRenderer
    companion object : ComponentType<DrawableRenderer>()
}

class DrawableOrder(
    order: Int = 0
) : Component<DrawableOrder>, Comparable<DrawableOrder> {

    val onOrderChange = BlankSignal()

    var order: Int = order
        set(value) {
            field = value
            isDirty = true
            onOrderChange.invoke()
        }

    @Deprecated("Use onOrderChange signal instead.")
    var isDirty: Boolean = true

    override fun compareTo(other: DrawableOrder): Int =
        order.compareTo(other.order)

    override fun type() = DrawableOrder

    companion object : ComponentType<DrawableOrder>()
}

class DrawableTint(
    val color: Color = Color(1f, 1f, 1f, 1f)
) : Component<DrawableTint> {
    override fun type() = DrawableTint
    companion object : ComponentType<DrawableTint>()
}

class DrawableVisibility(
    var isVisible: Boolean = true
) : Component<DrawableVisibility> {
    override fun type() = DrawableVisibility
    companion object : ComponentType<DrawableVisibility>()
}

class DrawableDimensions(
    var width: Float,
    var height: Float,
) : Component<DrawableDimensions> {
    override fun type() = DrawableDimensions
    companion object : ComponentType<DrawableDimensions>()

    constructor() : this(0f, 0f)

    constructor(size: Float) : this(size, size)
}

class DrawableOrigin() : Component<DrawableOrigin> {

    var x: Float = 0.5f
    var y: Float = 0.5f

    constructor(x: Float, y: Float) : this() {
        set(x, y)
    }

    /** Use [align][Align] constants to set up origin. */
    constructor(align: Int) : this() {
        set(align)
    }

    fun set(origin: Vector2) {
        this.x = origin.x
        this.y = origin.y
    }

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    /** Use [align][Align] constants to set up origin. */
    fun set(align: Int) {
        x = when {
            align and Align.left != 0 -> 0.0f
            align and Align.right != 0 -> 1.0f
            else -> 0.5f
        }
        y = when {
            align and Align.bottom != 0 -> 0.0f
            align and Align.top != 0 -> 1.0f
            else -> 0.5f
        }
    }

    override fun type() = DrawableOrigin

    companion object : ComponentType<DrawableOrigin>()
}