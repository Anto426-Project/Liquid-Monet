package com.anto426.liquidmonet.components.display.emptystate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive illustrative hero frame on a fully transparent canvas.
 *
 * Draws an accent ring with an overlapping bottom-right badge ring (using offscreen compositing +
 * [BlendMode.Clear] so the ring intersection is cleanly erased without painting an opaque circle
 * over the Liquid Glass scene), framed by subtle organic Expressive shapes.
 */
@Composable
fun LiquidExpressiveHeroFrame(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    mainContent: @Composable BoxScope.() -> Unit,
    badgeContent: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val ringColor = accentColor.copy(alpha = 0.85f)
    val cloverTint = accentColor.copy(alpha = 0.24f)
    val pebbleTint = colorScheme.onSurfaceVariant.copy(alpha = 0.16f)
    val starburstTint = colorScheme.onSurfaceVariant.copy(alpha = 0.14f)

    val ringSize = 122.dp
    val badgeSize = 54.dp
    val badgeOffsetX = 43.dp
    val badgeOffsetY = 40.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Offscreen compositing so BlendMode.Clear cleanly erases the ring intersection behind the badge
        // without drawing any opaque circle background over the Liquid Glass background scene.
        Box(
            modifier = Modifier
                .width(236.dp)
                .height(164.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    val centerPoint = Offset(w * 0.50f, h * 0.48f)
                    val mainRadius = (ringSize / 2f).toPx()
                    val badgeRadius = (badgeSize / 2f).toPx()
                    val strokePx = 5.dp.toPx()
                    val ringStroke = Stroke(width = strokePx)
                    val cloverPath = buildScallopedPath(
                        center = Offset(w * 0.15f, h * 0.19f),
                        radius = 18.dp.toPx(),
                        lobes = 4,
                        amplitude = 0.13f,
                        phaseRadians = 0.25f,
                    )
                    val starburstPath = buildScallopedPath(
                        center = Offset(w * 0.15f, h * 0.78f),
                        radius = 17.dp.toPx(),
                        lobes = 12,
                        amplitude = 0.16f,
                        phaseRadians = 0f,
                    )
                    val pebbleCenter = Offset(w * 0.86f, h * 0.28f)
                    val pebbleTopLeft = Offset(pebbleCenter.x - 15.dp.toPx(), pebbleCenter.y - 11.dp.toPx())
                    val pebbleSize = Size(30.dp.toPx(), 22.dp.toPx())
                    val badgeCenter = Offset(
                        x = centerPoint.x + badgeOffsetX.toPx(),
                        y = centerPoint.y + badgeOffsetY.toPx(),
                    )
                    val clearRadius = badgeRadius + strokePx * 0.45f

                    onDrawBehind {
                        drawPath(path = cloverPath, color = cloverTint)
                        drawPath(path = starburstPath, color = starburstTint)
                        rotate(degrees = 24f, pivot = pebbleCenter) {
                            drawOval(color = pebbleTint, topLeft = pebbleTopLeft, size = pebbleSize)
                        }
                        drawCircle(
                            color = ringColor,
                            radius = mainRadius,
                            center = centerPoint,
                            style = ringStroke,
                        )
                        drawCircle(
                            color = Color.Transparent,
                            radius = clearRadius,
                            center = badgeCenter,
                            blendMode = BlendMode.Clear,
                        )
                        drawCircle(
                            color = ringColor,
                            radius = badgeRadius,
                            center = badgeCenter,
                            style = ringStroke,
                        )
                    }
                },
        )

        Box(
            modifier = Modifier
                .offset(y = (-3).dp)
                .size(ringSize - 10.dp),
            contentAlignment = Alignment.Center,
            content = mainContent,
        )

        Box(
            modifier = Modifier
                .offset(x = badgeOffsetX, y = badgeOffsetY - 3.dp)
                .size(badgeSize - 10.dp),
            contentAlignment = Alignment.Center,
            content = badgeContent,
        )
    }
}

private fun buildScallopedPath(
    center: Offset,
    radius: Float,
    lobes: Int,
    amplitude: Float,
    phaseRadians: Float,
): Path {
    val steps = 96
    return Path().apply {
        for (i in 0 until steps) {
            val theta = (2.0 * PI * i / steps).toFloat()
            val r = radius * (1f - amplitude + amplitude * cos(lobes * theta + phaseRadians))
            val x = center.x + r * cos(theta)
            val y = center.y + r * sin(theta)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}
