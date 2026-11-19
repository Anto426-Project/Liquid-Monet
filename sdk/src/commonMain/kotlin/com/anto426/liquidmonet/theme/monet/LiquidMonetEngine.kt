package com.anto426.liquidmonet.theme.monet

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

internal val White = Color(0xFFFFFFFF)
internal val Black = Color(0xFF000000)

fun contentColorFor(color: Color): Color {
    return if (color.luminance() > 0.179f) Black else White
}

fun blend(base: Color, overlay: Color, amount: Float): Color {
    val safeAmount = amount.coerceIn(0f, 1f)
    val inverse = 1f - safeAmount
    return Color(
        red = base.red * inverse + overlay.red * safeAmount,
        green = base.green * inverse + overlay.green * safeAmount,
        blue = base.blue * inverse + overlay.blue * safeAmount,
        alpha = 1f
    )
}

data class LiquidMonetSeed(
    val lightPrimary: Color,
    val darkPrimary: Color,
    val lightSecondary: Color,
    val darkSecondary: Color,
    val lightTertiary: Color,
    val darkTertiary: Color
) {
    companion object {
        fun fromColor(color: Color): LiquidMonetSeed {
            val lightPrimary = blend(color, Black, 0.15f)
            val darkPrimary = blend(color, White, 0.40f)
            val lightSecondary = blend(color, Color(0xFF535F70), 0.30f)
            val darkSecondary = blend(color, Color(0xFFBBC7DB), 0.35f)
            val lightTertiary = blend(color, Color(0xFF6B5778), 0.30f)
            val darkTertiary = blend(color, Color(0xFFD6BAE4), 0.35f)
            return LiquidMonetSeed(
                lightPrimary = lightPrimary,
                darkPrimary = darkPrimary,
                lightSecondary = lightSecondary,
                darkSecondary = darkSecondary,
                lightTertiary = lightTertiary,
                darkTertiary = darkTertiary
            )
        }
    }
}

object LiquidMonetPresets {
    val Sapphire = LiquidMonetSeed(
        lightPrimary = Color(0xFF0061A4),
        darkPrimary = Color(0xFF9ECAFF),
        lightSecondary = Color(0xFF535F70),
        darkSecondary = Color(0xFFBBC7DB),
        lightTertiary = Color(0xFF6B5778),
        darkTertiary = Color(0xFFD6BAE4)
    )

    val Emerald = LiquidMonetSeed(
        lightPrimary = Color(0xFF006C4C),
        darkPrimary = Color(0xFF75DCAC),
        lightSecondary = Color(0xFF4C6356),
        darkSecondary = Color(0xFFB3CCBC),
        lightTertiary = Color(0xFF3D6373),
        darkTertiary = Color(0xFFA5CCE0)
    )

    val Sunset = LiquidMonetSeed(
        lightPrimary = Color(0xFF8B5000),
        darkPrimary = Color(0xFFFFB870),
        lightSecondary = Color(0xFF715B41),
        darkSecondary = Color(0xFFDFC2A4),
        lightTertiary = Color(0xFF53643E),
        darkTertiary = Color(0xFFB8CDA2)
    )

    val Violet = LiquidMonetSeed(
        lightPrimary = Color(0xFF6B4EA2),
        darkPrimary = Color(0xFFD3BBFF),
        lightSecondary = Color(0xFF625B71),
        darkSecondary = Color(0xFFCCC2DC),
        lightTertiary = Color(0xFF7E5260),
        darkTertiary = Color(0xFFEFB8C8)
    )
}

object LiquidMonetEngine {

    @Composable
    fun generateColorScheme(
        darkTheme: Boolean,
        useSystemDynamic: Boolean = true,
        customSeed: LiquidMonetSeed = LiquidMonetPresets.Sapphire
    ): ColorScheme {
        val seed = if (useSystemDynamic) {
            platformMonetSeed(customSeed)
        } else {
            customSeed
        }
        return remember(seed, darkTheme) { seed.toMaterial3ExpressiveScheme(darkTheme) }
    }

    private fun LiquidMonetSeed.toMaterial3ExpressiveScheme(darkTheme: Boolean): ColorScheme {
        val seedPrimary = (if (darkTheme) darkPrimary else lightPrimary).copy(alpha = 1f)
        val seedSecondary = (if (darkTheme) darkSecondary else lightSecondary).copy(alpha = 1f)
        val seedTertiary = (if (darkTheme) darkTertiary else lightTertiary).copy(alpha = 1f)
        val lightBase = Color(0xFFF3F3F6)
        val lightDim = blend(lightBase, seedPrimary, 0.30f)
        val primary = if (darkTheme) seedPrimary else readableLightAccent(seedPrimary, lightDim)
        val secondary = if (darkTheme) seedSecondary else readableLightAccent(seedSecondary, lightDim)
        val tertiary = if (darkTheme) seedTertiary else readableLightAccent(seedTertiary, lightDim)
        val error = if (darkTheme) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
        val target = if (darkTheme) Black else lightBase

        val primaryContainer = blend(seedPrimary, target, if (darkTheme) 0.42f else 0.73f)
        val secondaryContainer = blend(seedSecondary, target, if (darkTheme) 0.40f else 0.75f)
        val tertiaryContainer = blend(seedTertiary, target, if (darkTheme) 0.40f else 0.75f)
        val errorContainer = blend(error, target, if (darkTheme) 0.40f else 0.78f)

        return if (darkTheme) {
            val neutralSurface = Color(0xFF121212)
            val neutralBackground = Color(0xFF090A0F)
            val neutralOnSurface = Color(0xFFE6E1E5)
            val neutralOnSurfaceVariant = Color(0xFFCAC4D0)

            darkColorScheme(
                primary = primary,
                onPrimary = contentColorFor(primary),
                primaryContainer = primaryContainer,
                onPrimaryContainer = contentColorFor(primaryContainer),
                secondary = secondary,
                onSecondary = contentColorFor(secondary),
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = contentColorFor(secondaryContainer),
                tertiary = tertiary,
                onTertiary = contentColorFor(tertiary),
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = contentColorFor(tertiaryContainer),
                error = error,
                onError = contentColorFor(error),
                errorContainer = errorContainer,
                onErrorContainer = contentColorFor(errorContainer),
                background = neutralBackground,
                onBackground = neutralOnSurface,
                surface = neutralSurface,
                onSurface = neutralOnSurface,
                surfaceDim = Color(0xFF0E0E0E),
                surfaceBright = Color(0xFF323232),
                surfaceContainerLowest = Color(0xFF0C0C0C),
                surfaceContainerLow = Color(0xFF161616),
                surfaceContainer = Color(0xFF1E1E1E),
                surfaceContainerHigh = Color(0xFF262626),
                surfaceContainerHighest = Color(0xFF303030),
                surfaceVariant = Color(0xFF222222),
                onSurfaceVariant = neutralOnSurfaceVariant,
                outline = Color(0xFF49454F),
                outlineVariant = Color(0xFF303030),
                inverseSurface = Color(0xFFE6E1E5),
                inverseOnSurface = Color(0xFF313033),
                inversePrimary = blend(primary, White, 0.34f),
                surfaceTint = Color.Transparent, // ZERO TINTING OF SURFACES
                scrim = Black
            )
        } else {
            val onSurface = blend(Color(0xFF1C1D24), seedPrimary, 0.12f)
            val onSurfaceVariant = readableLightAccent(blend(Color(0xFF434451), seedPrimary, 0.14f), lightDim)

            lightColorScheme(
                primary = primary,
                onPrimary = contentColorFor(primary),
                primaryContainer = primaryContainer,
                onPrimaryContainer = contentColorFor(primaryContainer),
                secondary = secondary,
                onSecondary = contentColorFor(secondary),
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = contentColorFor(secondaryContainer),
                tertiary = tertiary,
                onTertiary = contentColorFor(tertiary),
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = contentColorFor(tertiaryContainer),
                error = error,
                onError = contentColorFor(error),
                errorContainer = errorContainer,
                onErrorContainer = contentColorFor(errorContainer),
                background = blend(lightBase, seedPrimary, 0.17f),
                onBackground = onSurface,
                surface = blend(lightBase, seedPrimary, 0.12f),
                onSurface = onSurface,
                surfaceDim = lightDim,
                surfaceBright = blend(lightBase, seedPrimary, 0.06f),
                surfaceContainerLowest = blend(lightBase, seedPrimary, 0.08f),
                surfaceContainerLow = blend(lightBase, seedPrimary, 0.11f),
                surfaceContainer = blend(lightBase, seedPrimary, 0.15f),
                surfaceContainerHigh = blend(lightBase, seedPrimary, 0.20f),
                surfaceContainerHighest = blend(lightBase, seedPrimary, 0.25f),
                surfaceVariant = blend(lightBase, seedSecondary, 0.23f),
                onSurfaceVariant = onSurfaceVariant,
                outline = blend(Color(0xFF6D707B), seedPrimary, 0.22f),
                outlineVariant = blend(Color(0xFFC5C7CE), seedPrimary, 0.20f),
                inverseSurface = Color(0xFF313033),
                inverseOnSurface = Color(0xFFF4EFF4),
                inversePrimary = blend(primary, Black, 0.22f),
                surfaceTint = Color.Transparent,
                scrim = Black
            )
        }
    }
}

/** Darken only the usable foreground; light surfaces and containers still use the original seed. */
private fun readableLightAccent(color: Color, background: Color): Color {
    val maxForegroundLuminance = (background.luminance() + 0.05f) / 4.5f - 0.05f
    if (color.luminance() <= maxForegroundLuminance) return color

    var low = 0f
    var high = 1f
    repeat(10) {
        val amount = (low + high) / 2f
        if (blend(color, Black, amount).luminance() > maxForegroundLuminance) {
            low = amount
        } else {
            high = amount
        }
    }
    return blend(color, Black, high)
}

@Composable
internal expect fun platformMonetSeed(fallback: LiquidMonetSeed): LiquidMonetSeed
