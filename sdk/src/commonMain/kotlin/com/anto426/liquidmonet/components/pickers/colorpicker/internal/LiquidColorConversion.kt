package com.anto426.liquidmonet.components.pickers.colorpicker.internal

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/** Converte valori HSV (h 0..360, s 0..1, v 0..1) in Compose Color. */
internal fun hsvToColor(h: Float, s: Float, v: Float, alpha: Float = 1f): Color {
    val c = v * s
    val hPrime = (h % 360f) / 60f
    val x = c * (1f - abs((hPrime % 2f) - 1f))
    val m = v - c

    val (r1, g1, b1) =
        when {
            hPrime < 1f -> Triple(c, x, 0f)
            hPrime < 2f -> Triple(x, c, 0f)
            hPrime < 3f -> Triple(0f, c, x)
            hPrime < 4f -> Triple(0f, x, c)
            hPrime < 5f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = alpha.coerceIn(0f, 1f),
    )
}

/** Converte Compose Color in HSV (Hue 0..360, Saturation 0..1, Value 0..1). */
internal fun colorToHsv(color: Color): Triple<Float, Float, Float> {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, maxOf(g, b))
    val min = minOf(r, minOf(g, b))
    val delta = max - min

    val h =
        when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }

    val s = if (max == 0f) 0f else delta / max
    val v = max
    return Triple(h, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))
}

/** Formatta Compose Color in formato stringa esadecimale #RRGGBB o #AARRGGBB. */
internal fun colorToHex(color: Color, includeAlpha: Boolean = false): String {
    val a = (color.alpha * 255).toInt().coerceIn(0, 255)
    val r = (color.red * 255).toInt().coerceIn(0, 255)
    val g = (color.green * 255).toInt().coerceIn(0, 255)
    val b = (color.blue * 255).toInt().coerceIn(0, 255)
    return if (includeAlpha && a < 255) {
        "#${a.toHexByte()}${r.toHexByte()}${g.toHexByte()}${b.toHexByte()}"
    } else {
        "#${r.toHexByte()}${g.toHexByte()}${b.toHexByte()}"
    }
}

internal fun parseHexToColor(hex: String): Color? {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> {
                val r = clean.substring(0, 2).toInt(16) / 255f
                val g = clean.substring(2, 4).toInt(16) / 255f
                val b = clean.substring(4, 6).toInt(16) / 255f
                Color(r, g, b, 1f)
            }
            8 -> {
                val a = clean.substring(0, 2).toInt(16) / 255f
                val r = clean.substring(2, 4).toInt(16) / 255f
                val g = clean.substring(4, 6).toInt(16) / 255f
                val b = clean.substring(6, 8).toInt(16) / 255f
                Color(r, g, b, a)
            }
            3 -> {
                val r = clean.substring(0, 1).repeat(2).toInt(16) / 255f
                val g = clean.substring(1, 2).repeat(2).toInt(16) / 255f
                val b = clean.substring(2, 3).repeat(2).toInt(16) / 255f
                Color(r, g, b, 1f)
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun Int.toHexByte(): String = toString(16).uppercase().padStart(2, '0')
