package com.anto426.liquidmonet.components.cards.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

internal object LiquidControlCenterDefaults {
    val shape = RoundedRectangle(24.dp)
    val iconShape = Capsule()
    val iconSize = 44.dp
    val spacing = 14.dp
    val minimumHeight = 76.dp
}

/** Shared icon presentation; the containing control owns all interaction and motion. */
@Composable
internal fun LiquidControlCenterIcon(
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
) {
    Box(
        Modifier.size(LiquidControlCenterDefaults.iconSize)
            .background(containerColor, LiquidControlCenterDefaults.iconShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
    }
}
