package com.anto426.liquidmonet.components.display.sectionheader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Size scale options for [LiquidSectionHeader]. */
enum class LiquidSectionHeaderSize {
    /** Compact header size (ideal for preference groups and dense sections). */
    Small,
    /** Standard medium header size for compact sections. */
    Medium,
    /** Default prominent large header size for main screen sections. */
    Large;

    companion object {
        val Default: LiquidSectionHeaderSize
            get() = Medium
    }
}

/**
 * Lightweight heading for a related group of Liquid Monet components.
 *
 * [size] configures typography and paddings ([LiquidSectionHeaderSize.Medium] by default, with
 * [LiquidSectionHeaderSize.Small] optimized for settings preference groups).
 *
 * [trailingContent] renders optional actions (e.g. "Vedi tutti" or edit buttons) aligned on the
 * right.
 *
 * The optional [subtitle] explains the group without wrapping it in another surface, keeping the
 * hierarchy used by Android settings screens.
 */
@Composable
fun LiquidSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    size: LiquidSectionHeaderSize = LiquidSectionHeaderSize.Default,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.primary,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val titleStyle: TextStyle
    val subtitleStyle: TextStyle
    val spacing: Dp
    val paddingValues: PaddingValues

    when (size) {
        LiquidSectionHeaderSize.Small -> {
            titleStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            subtitleStyle = MaterialTheme.typography.labelSmall
            spacing = 2.dp
            paddingValues = PaddingValues(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
        }
        LiquidSectionHeaderSize.Medium -> {
            titleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            subtitleStyle = MaterialTheme.typography.bodySmall
            spacing = 3.dp
            paddingValues = PaddingValues(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
        }
        LiquidSectionHeaderSize.Large -> {
            titleStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            subtitleStyle = MaterialTheme.typography.bodyMedium
            spacing = 4.dp
            paddingValues = PaddingValues(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
        }
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { heading() }
                .padding(paddingValues),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            Text(
                text = title,
                color = titleColor,
                style = titleStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    style = subtitleStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}
