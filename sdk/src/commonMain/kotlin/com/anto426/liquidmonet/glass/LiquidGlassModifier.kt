package com.anto426.liquidmonet.glass

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPreset
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.effectPolicy
import com.anto426.liquidmonet.glass.runtime.surfacePerformance
import com.anto426.liquidmonet.theme.monet.blend
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.adaptiveLegibility
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.isRuntimeShaderSupported
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

/** Applies the canonical stable glass pipeline and clips content with the same [shape]. */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    shape: Shape,
    role: LiquidGlassRole = LiquidGlassRole.Surface,
    containerColor: Color? = null,
    preset: LiquidGlassPreset? = null,
    performance: LiquidGlassPerformanceState = LocalLiquidGlassPerformance.current,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
    exportedBackdrop: LayerBackdrop? = null,
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    interactive: Boolean = layerBlock != null,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
): Modifier =
    liquidGlass(
        backdrop = backdrop,
        shape = shape,
        isCardSurface = false,
        role = role,
        containerColor = containerColor,
        preset = preset,
        performance = performance,
        layerBlock = layerBlock,
        exportedBackdrop = exportedBackdrop,
        backdropPolicy = backdropPolicy,
        interactive = interactive,
        legibility = legibility,
    )

/** Only LiquidCard selects this treatment. The flag stays local to its optical background. */
@Composable
internal fun Modifier.liquidGlass(
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    shape: Shape,
    isCardSurface: Boolean,
    role: LiquidGlassRole = LiquidGlassRole.Surface,
    containerColor: Color? = null,
    preset: LiquidGlassPreset? = null,
    performance: LiquidGlassPerformanceState = LocalLiquidGlassPerformance.current,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
    exportedBackdrop: LayerBackdrop? = null,
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    interactive: Boolean = layerBlock != null,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
): Modifier {
    val colorScheme = MaterialTheme.colorScheme
    val isLightSurface = remember(colorScheme.surface) { colorScheme.surface.luminance() > 0.5f }

    // A shared container already paid for backdrop sampling. Descendants retain their exact
    // shape, semantic tint and press deformation without multiplying blur/refraction passes.
    if (
        LocalLiquidGlassContainerMode.current == LiquidGlassContainerMode.Shared &&
            backdropPolicy == LiquidGlassBackdropPolicy.SceneFirst
    ) {
        val isEssentialSharedControl =
            (performance.opticalQualityTier ?: performance.qualityTier) ==
                LiquidGlassQualityTier.MINIMAL &&
                role == LiquidGlassRole.Control
        val isNestedCardSurface = isCardSurface && containerColor == null
        val groupedSurfaceColor =
            when {
                isEssentialSharedControl ->
                    remember(
                        containerColor,
                        isLightSurface,
                        colorScheme.surface,
                        colorScheme.onSurface,
                        colorScheme.primary,
                    ) {
                        resolveEssentialChromeSurfaceColor(
                            containerColor = containerColor,
                            role = role,
                            isLightSurface = isLightSurface,
                            surface = colorScheme.surface,
                            onSurface = colorScheme.onSurface,
                            primary = colorScheme.primary,
                            sharedControl = true,
                        )
                    }
                isNestedCardSurface ->
                    remember(colorScheme.onSurface, isLightSurface) {
                        colorScheme.onSurface.copy(alpha = if (isLightSurface) 0.055f else 0.075f)
                    }
                else -> containerColor ?: Color.Transparent
            }
        val sharedRim =
            when {
                isEssentialSharedControl && groupedSurfaceColor.alpha > 0f -> {
                    val primary = colorScheme.primary
                    val rimBrush =
                        remember(isLightSurface, primary) {
                            val rimTint = blend(Color.White, primary, 0.22f)
                            Brush.verticalGradient(
                                listOf(
                                    rimTint.copy(alpha = if (isLightSurface) 0.36f else 0.20f),
                                    rimTint.copy(alpha = if (isLightSurface) 0.12f else 0.08f),
                                )
                            )
                        }
                    Modifier.border(width = 1.dp, brush = rimBrush, shape = shape)
                }
                isNestedCardSurface -> {
                    val onSurface = colorScheme.onSurface
                    val rimBrush =
                        remember(isLightSurface, onSurface) {
                            Brush.verticalGradient(
                                listOf(
                                    onSurface.copy(alpha = if (isLightSurface) 0.12f else 0.16f),
                                    onSurface.copy(alpha = if (isLightSurface) 0.04f else 0.06f),
                                )
                            )
                        }
                    Modifier.border(width = 0.75.dp, brush = rimBrush, shape = shape)
                }
                else -> Modifier
            }
        val groupedSurface: ContentDrawScope.() -> Unit =
            remember(groupedSurfaceColor) {
                {
                    if (groupedSurfaceColor.alpha > 0f) drawRect(groupedSurfaceColor)
                    drawContent()
                }
            }
        return this.then(if (layerBlock != null) Modifier.graphicsLayer(layerBlock) else Modifier)
            .clip(shape)
            .then(sharedRim)
            .drawWithContent(groupedSurface)
    }

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val style = remember(role) { LiquidGlassStyleManager.resolve(role) }
    val surfacePerformance = remember(performance, isCardSurface) {
        performance.surfacePerformance(isCardSurface)
    }
    val tokens =
        remember(preset, style.preset, surfacePerformance) {
            (preset ?: style.preset).resolve(surfacePerformance)
        }
    val liquidStrength = performance.liquidIntensity.coerceIn(0f, 1f)
    val isEssentialChrome =
        (surfacePerformance.opticalQualityTier ?: surfacePerformance.qualityTier) ==
            LiquidGlassQualityTier.MINIMAL &&
            role != LiquidGlassRole.Surface &&
            !isCardSurface
    val legibilitySpec =
        remember(
            legibility,
            role,
            isLightSurface,
            colorScheme.surface,
            colorScheme.primary,
            liquidStrength,
        ) {
            LiquidGlassLegibilityPolicy.resolve(
                legibility = legibility,
                role = role,
                isLightSurface = isLightSurface,
                surfaceColor = colorScheme.surface,
                primaryColor = colorScheme.primary,
                liquidStrength = liquidStrength,
            )
        }
    val useAdaptiveLegibilityShader =
        legibilitySpec.useShader && !isEssentialChrome && isRuntimeShaderSupported()
    val useAdaptiveLegibilityFallback =
        legibilitySpec.useShader && !useAdaptiveLegibilityShader
    val ambientMonetTint =
        remember(colorScheme.primary, isLightSurface, liquidStrength, isEssentialChrome) {
            if (isEssentialChrome) {
                Color.Transparent
            } else {
                colorScheme.primary.copy(
                    alpha =
                        LiquidGlassStyleManager.ambientTintAlpha(isLightSurface) *
                            (0.35f + 0.65f * liquidStrength)
                )
            }
        }
    val surfaceColor =
        if (isEssentialChrome) {
            remember(
                containerColor,
                role,
                isLightSurface,
                colorScheme.surface,
                colorScheme.onSurface,
                colorScheme.primary,
            ) {
                resolveEssentialChromeSurfaceColor(
                    containerColor = containerColor,
                    role = role,
                    isLightSurface = isLightSurface,
                    surface = colorScheme.surface,
                    onSurface = colorScheme.onSurface,
                    primary = colorScheme.primary,
                )
            }
        } else {
            val baseSurface =
                containerColor
                    ?: LiquidGlassStyleManager.surfaceColor(role, isLightSurface, colorScheme.surface)
            val minAlpha =
                when {
                    containerColor != null -> 0f
                    useAdaptiveLegibilityShader -> legibilitySpec.shaderMinSurfaceAlpha
                    useAdaptiveLegibilityFallback -> legibilitySpec.fallbackMinSurfaceAlpha
                    else -> 0f
                }
            if (minAlpha > baseSurface.alpha) baseSurface.copy(alpha = minAlpha) else baseSurface
        }
    val essentialRim =
        if (
            isEssentialChrome &&
                surfaceColor.alpha > 0f &&
                role != LiquidGlassRole.TopBar &&
                role != LiquidGlassRole.Menu
        ) {
            val primary = colorScheme.primary
            val rimBrush =
                remember(isLightSurface, primary) {
                    val rimTint = blend(Color.White, primary, 0.22f)
                    Brush.verticalGradient(
                        listOf(
                            rimTint.copy(alpha = if (isLightSurface) 0.40f else 0.22f),
                            rimTint.copy(alpha = if (isLightSurface) 0.14f else 0.08f),
                        )
                    )
                }
            Modifier.border(width = 1.dp, brush = rimBrush, shape = shape)
        } else {
            Modifier
        }

    val effectPolicy =
        remember(surfacePerformance, role, interactive, isCardSurface) {
            surfacePerformance.effectPolicy(role, interactive, isCardSurface)
        }
    val useBlur = effectPolicy.blur && tokens.blurRadius > 0.dp
    val useRefraction =
        effectPolicy.refraction && tokens.refractionHeight > 0.dp && tokens.refractionAmount > 0.dp
    val drawSurface: DrawScope.() -> Unit =
        remember(surfaceColor, ambientMonetTint) {
            {
                if (surfaceColor.alpha > 0f) drawRect(surfaceColor)
                if (ambientMonetTint.alpha > 0f) drawRect(ambientMonetTint)
            }
        }
    if (isEssentialChrome && !useRefraction && exportedBackdrop == null) {
        return this.then(if (layerBlock != null) Modifier.graphicsLayer(layerBlock) else Modifier)
            .clip(shape)
            .then(essentialRim)
            .drawWithContent {
                drawSurface()
                drawContent()
            }
    }
    val useChromaticAberration =
        effectPolicy.chromaticAberration && tokens.chromaticAberration >= 0.08f
    val useHighlight = effectPolicy.highlight && style.highlightAlpha > 0f
    val useShadow = effectPolicy.shadow && style.shadowRadius > 0.dp && style.shadowAlpha > 0f
    val useInnerShadow =
        effectPolicy.innerShadow && style.innerShadowRadius > 0.dp && style.innerShadowAlpha > 0f

    val shapeProvider = remember(shape) { { shape } }
    val effects: BackdropEffectScope.() -> Unit =
        remember(
            tokens,
            style,
            effectPolicy,
            role,
            isLightSurface,
            liquidStrength,
            legibilitySpec,
            useAdaptiveLegibilityShader,
            useAdaptiveLegibilityFallback,
        ) {
            {
                val compactReference = 48.dp.toPx()
                val largeReference = 240.dp.toPx()
                val sizeProgress =
                    if (size.isSpecified) {
                        ((size.minDimension - compactReference) /
                                (largeReference - compactReference))
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                val opticalThickness =
                    when (role) {
                        LiquidGlassRole.Control,
                        LiquidGlassRole.Navigation -> 0.84f + 0.16f * sizeProgress
                        LiquidGlassRole.TopBar -> 0.92f + 0.16f * sizeProgress
                        LiquidGlassRole.Surface,
                        LiquidGlassRole.Menu,
                        LiquidGlassRole.Dialog,
                        LiquidGlassRole.Sheet -> 0.96f + 0.22f * sizeProgress
                    }

                val baseBrightness = if (isLightSurface) style.lightBrightness * liquidStrength else 0f
                colorControls(
                    brightness =
                        if (useAdaptiveLegibilityFallback) {
                            baseBrightness + legibilitySpec.fallbackBrightnessDelta
                        } else {
                            baseBrightness
                        },
                    contrast =
                        if (useAdaptiveLegibilityFallback) legibilitySpec.fallbackContrast else 1f,
                    saturation = 1f + (style.saturation - 1f) * liquidStrength,
                )
                if (useBlur) {
                    blur(tokens.blurRadius.toPx() * opticalThickness * legibilitySpec.blurMultiplier)
                }
                if (useAdaptiveLegibilityShader) {
                    adaptiveLegibility(
                        darkSurface = legibilitySpec.darkSurface,
                        threshold = legibilitySpec.threshold,
                        targetLuma = legibilitySpec.targetLuma,
                        strength = legibilitySpec.strength,
                        tintColor = legibilitySpec.tintColor,
                    )
                }
                if (useRefraction && size.isSpecified && size.minDimension > 0f) {
                    lens(
                        refractionHeight = tokens.refractionHeight.toPx() * opticalThickness,
                        refractionAmount = tokens.refractionAmount.toPx() * opticalThickness,
                        depthEffect = true,
                        chromaticAberration = useChromaticAberration,
                    )
                }
            }
        }
    val highlight: (() -> Highlight?)? =
        remember(useHighlight, style.highlightAlpha, liquidStrength) {
            if (useHighlight) {
                val value =
                    Highlight.Plain.copy(
                        alpha = style.highlightAlpha * (0.45f + 0.55f * liquidStrength)
                    )
                return@remember { value }
            } else null
        }
    val shadow: (() -> Shadow?)? =
        remember(useShadow, style.shadowRadius, style.shadowAlpha) {
            if (useShadow) {
                val value =
                    Shadow(
                        radius = style.shadowRadius,
                        color = Color.Black.copy(alpha = style.shadowAlpha),
                    )
                return@remember { value }
            } else null
        }
    val innerShadow: (() -> InnerShadow?)? =
        remember(
            useInnerShadow,
            style.innerShadowRadius,
            style.innerShadowAlpha,
            liquidStrength,
        ) {
            if (useInnerShadow) {
                val value =
                    InnerShadow(
                        radius = style.innerShadowRadius,
                        alpha = style.innerShadowAlpha * (0.55f + 0.45f * liquidStrength),
                    )
                return@remember { value }
            } else null
        }
    return then(essentialRim).drawBackdrop(
        backdrop = effectiveBackdrop,
        shape = shapeProvider,
        effects = effects,
        highlight = highlight,
        shadow = shadow,
        innerShadow = innerShadow,
        layerBlock = layerBlock,
        exportedBackdrop = exportedBackdrop,
        resolutionScale = surfacePerformance.renderResolutionScale,
        onDrawSurface = drawSurface,
    )
}

/**
 * Base surface color matching a [LiquidCard] over [LiquidBackground] so every Essential (`MINIMAL`)
 * component shares the exact same light/dark Monet glass tone as cards.
 */
internal fun essentialCardBaseColor(
    isLightSurface: Boolean,
    surface: Color,
    primary: Color,
): Color =
    if (isLightSurface) {
        blend(surface, Color.White, 0.30f)
    } else {
        blend(Color(0xFF303238), primary, 0.22f)
    }

/**
 * Higher-opacity card-matched surface for floating chrome, controls, menus and modals in the
 * Essential (`MINIMAL`) tier so underlying content does not bleed through without blur.
 * Cards (`LiquidGlassRole.Surface`) stay on the standard translucent pipeline.
 */
internal fun resolveEssentialChromeSurfaceColor(
    containerColor: Color?,
    role: LiquidGlassRole,
    isLightSurface: Boolean,
    surface: Color,
    onSurface: Color,
    primary: Color,
    sharedControl: Boolean = false,
): Color {
    if (containerColor != null && containerColor.alpha == 0f) return Color.Transparent

    val base = essentialCardBaseColor(isLightSurface, surface, primary)

    val targetAlpha =
        when (role) {
            LiquidGlassRole.Dialog,
            LiquidGlassRole.Sheet,
            LiquidGlassRole.Menu -> if (isLightSurface) 0.80f else 0.82f
            LiquidGlassRole.TopBar,
            LiquidGlassRole.Navigation -> if (isLightSurface) 0.74f else 0.76f
            LiquidGlassRole.Control ->
                if (sharedControl) {
                    if (isLightSurface) 0.56f else 0.60f
                } else {
                    if (isLightSurface) 0.66f else 0.68f
                }
            LiquidGlassRole.Surface -> return containerColor ?: Color.Transparent
        }

    if (containerColor == null) {
        if (sharedControl) {
            val lift = onSurface.copy(alpha = if (isLightSurface) 0.045f else 0.065f)
            return lift.compositeOver(base).copy(alpha = targetAlpha)
        }
        return base.copy(alpha = targetAlpha)
    }
    if (containerColor.alpha >= targetAlpha) return containerColor
    return containerColor.compositeOver(base).copy(alpha = targetAlpha)
}
