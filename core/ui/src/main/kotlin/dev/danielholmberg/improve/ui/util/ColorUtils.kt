package dev.danielholmberg.improve.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Parses a hex color string (e.g. "#FF0000" or "FF0000") into a Compose [Color].
 *
 * Supports the following formats:
 * - "#RRGGBB"
 * - "#AARRGGBB"
 * - "RRGGBB"
 * - "AARRGGBB"
 *
 * @throws IllegalArgumentException if the string is not a valid hex color.
 */
fun String.toComposeColor(): Color {
    val hex = this.trimStart('#')
    return when (hex.length) {
        6 -> Color(("FF$hex").toLong(16).toInt())
        8 -> Color(hex.toLong(16).toInt())
        else -> throw IllegalArgumentException("Invalid hex color string: '$this'")
    }
}

/**
 * Converts a Compose [Color] to a hex string in "#RRGGBB" format.
 *
 * The alpha component is discarded. If you need alpha, adapt to "#AARRGGBB" by
 * also formatting `(color.alpha * 255).toInt()`.
 */
fun Color.toHexString(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "#%02X%02X%02X".format(red, green, blue)
}

/**
 * Returns either [Color.White] or [Color.Black] depending on which provides
 * sufficient contrast against [backgroundColor], using the WCAG relative
 * luminance formula.
 *
 * The threshold of 0.179 (≈ midpoint on the perceptual scale) is a common
 * heuristic; adjust if your design requires stricter contrast ratios.
 */
fun getContrastingTextColor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.179f) Color.Black else Color.White
}
