package com.anto426.liquidmonet.glass

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPreset
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.effectPolicy
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

/** Applies the canonical stable glass pipeline and clips content with the same [shape]. */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop = emptyBackdrop(),
    shape: Shape,
    role: LiquidGlassRole = LiquidGlassRole.Surface,
    containerColor: Color? = null,
    preset: LiquidGlassPreset? = null,
    performance: LiquidGlassPerformanceState = LocalLiquidGlassPerformance.current,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
    exportedBackdrop: LayerBackdrop? = null,
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    interactive: Boolean = layerBlock != null,
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
    )

/** Only LiquidCard selects this treatment. The flag stays local to its optical background. */
@Composable
internal fun Modifier.liquidGlass(
    backdrop: Backdrop = emptyBackdrop(),
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
): Modifier {
    // A shared container already paid for backdrop sampling. Descendants retain their exact
    // shape, semantic tint and press deformation without multiplying blur/refraction passes.
    if (
        LocalLiquidGlassContainerMode.current == LiquidGlassContainerMode.Shared &&
            backdropPolicy == LiquidGlassBackdropPolicy.SceneFirst
    ) {
        val groupedSurfaceColor = containerColor ?: Color.Transparent
        val groupedSurface: ContentDrawScope.() -> Unit =
            remember(groupedSurfaceColor) {
                {
                    if (groupedSurfaceColor.alpha > 0f) drawRect(groupedSurfaceColor)
                    drawContent()
                }
            }
        return this.then(if (layerBlock != null) Modifier.graphicsLayer(layerBlock) else Modifier)
            .clip(shape)
            .drawWithContent(groupedSurface)
    }

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val colorScheme = MaterialTheme.colorScheme
    val isLightSurface = remember(colorScheme.surface) { colorScheme.surface.luminance() > 0.5f }
    val style = remember(role) { LiquidGlassStyleManager.resolve(role) }
    val tokens =
        remember(preset, style.preset, performance) {
            (preset ?: style.preset).resolve(performance)
        }
    val liquidStrength = performance.liquidIntensity.coerceIn(0f, 1f)
    val ambientMonetTint =
        remember(colorScheme.primary, isLightSurface, liquidStrength) {
            colorScheme.primary.copy(
                alpha =
                    LiquidGlassStyleManager.ambientTintAlpha(isLightSurface) *
                        (0.35f + 0.65f * liquidStrength)
            )
        }
    val surfaceColor = containerColor ?: LiquidGlassStyleManager.surfaceColor(role, isLightSurface)

    val effectPolicy =
        remember(performance, role, interactive, isCardSurface) {
            performance.effectPolicy(role, interactive, isCardSurface)
        }
    val useBlur = effectPolicy.blur && tokens.blurRadius > 0.dp
    val useRefraction =
        effectPolicy.refraction && tokens.refractionHeight > 0.dp && tokens.refractionAmount > 0.dp
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

                colorControls(
                    brightness = if (isLightSurface) style.lightBrightness * liquidStrength else 0f,
                    saturation = 1f + (style.saturation - 1f) * liquidStrength,
                )
                if (useBlur) {
                    blur(tokens.blurRadius.toPx() * opticalThickness)
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
    val drawSurface: DrawScope.() -> Unit =
        remember(surfaceColor, ambientMonetTint) {
            {
                if (surfaceColor.alpha > 0f) drawRect(surfaceColor)
                if (ambientMonetTint.alpha > 0f) drawRect(ambientMonetTint)
            }
        }

    return drawBackdrop(
        backdrop = effectiveBackdrop,
        shape = shapeProvider,
        effects = effects,
        highlight = highlight,
        shadow = shadow,
        innerShadow = innerShadow,
        layerBlock = layerBlock,
        exportedBackdrop = exportedBackdrop,
        resolutionScale = performance.renderResolutionScale,
        onDrawSurface = drawSurface,
    )
}
