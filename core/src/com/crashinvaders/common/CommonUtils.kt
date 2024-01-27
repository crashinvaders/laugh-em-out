package com.crashinvaders.common;

object CommonUtils {
    /** This method actually replicate Integer.compare() to support Android API less than 19 */
    @JvmStatic
    fun compare(x: Int, y: Int): Int =
        if (x < y) -1 else (if (x == y) 0 else 1)

    /** This method actually replicate Float.compare() to support Android API less than 19 */
    @JvmStatic
    fun compare(x: Float, y: Float): Int =
        if (x < y) -1 else (if(x == y) 0 else 1)

    /** This method actually replicate Boolean.compare() to support Android API less than 19 */
    @JvmStatic
    fun compare(x: Boolean, y: Boolean): Int =
        if (x == y) 0 else (if (x) 1 else -1)
}
