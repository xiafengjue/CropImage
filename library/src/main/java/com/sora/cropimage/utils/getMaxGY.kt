package com.sora.cropimage.utils

/**
 * 求两个数的最大公约数
 * @param other 另一个数
 * @return 最大公约数
 */
object MathUtils {
    fun getMaxGY(up: Int, down: Int): Int {
        var m = up
        var n = down
        return if (m == n) {
            m
        } else {
            while (m % n != 0) {
                val temp = m % n
                m = n
                n = temp
            }
            n
        }
    }
}