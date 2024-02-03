@file:JvmName("MathExtensions")

package com.crashinvaders.common

import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.sign
import kotlin.math.sqrt

private val tmpVec2 = Vector2()

val Rectangle.centerX: Float
    get() = x + width * 0.5f
val Rectangle.centerY: Float
    get() = y + height * 0.5f
val Rectangle.right: Float
    get() = x + width
val Rectangle.top: Float
    get() = y + height

fun ceilStep(value: Float, step: Int): Int =
    MathUtils.ceil((value) / step ) * step

//region Affine2
fun Affine2.extractPosition(out: Vector2): Vector2 =
    out.set(extractPositionX(), extractPositionY())

fun Affine2.extractPositionX(): Float =
    m02

fun Affine2.extractPositionY(): Float =
    m12

/** @return Rotation in degrees. */
fun Affine2.extractRotation(): Float {
    var angle = MathUtils.atan2(m10, m00) * MathUtils.radDeg
    if (angle < 0) angle += 360f
    return angle
}

fun Affine2.extractScale(out: Vector2): Vector2 =
    out.set(extractScaleX(), extractScaleY())

fun Affine2.extractScaleX(): Float =
    sqrt(m00 * m00 + m10 * m10)

fun Affine2.extractScaleY(): Float =
    sqrt(m01 * m01 + m11 * m11) * sign(m00 * m11 - m01 * m10)

/** X direction. */
fun Affine2.extractRight(out: Vector2): Vector2 =
    out.set(m00, m10).nor()

/** This gives "real" Y direction.
 * This axis may be skewed and is not suitable for a simple for any conventional rendering, where X and Y are always parallel. */
fun Affine2.extractUpReal(out: Vector2): Vector2 =
    out.set(m01, m11).nor()

/** Gives "flat" Y direction.
 * This is only suitable for 2D sprite art, where X and Y axis perpendicular. */
fun Affine2.extractUp(out: Vector2): Vector2 =
    // Perpendicular to the "right" vector. The orientation depends on the Y "scale" factor.
    if (sign(m00 * m11 - m01 * m10) > 0)
        out.set(-m10, m00).nor()
    else
        out.set(m10, -m00).nor()

fun Affine2.applyToPoint(x: Float, y: Float): Vector2 =
    applyToPoint(tmpVec2.set(x, y))

fun Affine2.applyToPoint(point: Vector2): Vector2 {
    val x = point.x
    val y = point.y
    point.x = m00 * x + m01 * y + m02
    point.y = m10 * x + m11 * y + m12
    return point
}

fun Affine2.applyToVector(x: Float, y: Float): Vector2 =
    applyToVector(tmpVec2.set(x, y))

fun Affine2.applyToVector(point: Vector2): Vector2 {
    val x = point.x
    val y = point.y
    point.x = m00 * x + m01 * y
    point.y = m10 * x + m11 * y
    return point
}
//endregion
