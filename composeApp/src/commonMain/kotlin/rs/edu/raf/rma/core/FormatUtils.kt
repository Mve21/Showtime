package rs.edu.raf.rma.core

import kotlin.math.roundToInt

fun Float.format1d(): String {
    val v = (this * 10).roundToInt()
    return "${v / 10}.${v % 10}"
}
