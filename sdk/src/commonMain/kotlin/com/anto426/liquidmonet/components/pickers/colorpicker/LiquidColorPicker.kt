package com.anto426.liquidmonet.components.pickers.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.pickers.colorpicker.internal.colorToHex
import com.anto426.liquidmonet.components.pickers.colorpicker.internal.colorToHsv
import com.anto426.liquidmonet.components.pickers.colorpicker.internal.hsvToColor
import com.anto426.liquidmonet.components.pickers.colorpicker.internal.parseHexToColor
import com.anto426.liquidmonet.components.pickers.colorpicker.motion.LiquidColorPickerMotion
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.liquidmonet.theme.monet.contentColorFor
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * LiquidColorPicker - Selettore cromatico avanzato rifatto da zero.
 *
 * Architettura:
 * 1. Header con anteprima cromatica ad alta fedeltà, codice HEX interattivo, valori RGB e
 *    ripristino rapido.
 * 2. Selettore modalità a segmenti (Palette, Spettro 2D, Slider).
 * 3. Palette tematiche con pillole di categoria a scorrimento orizzontale privo di wrap spezzati.
 * 4. Spettro continuo con manipolazione fluida 2D di saturazione e luminosità.
 * 5. Cursori di precisione individuali con barre cromatiche dinamiche in tempo reale.
 * 6. Input HEX diretto con validazione istantanea e supporto clipboard.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
    showAlpha: Boolean = false,
) {
    val performance = LocalLiquidGlassPerformance.current
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val initialColor = remember { selectedColor }
    var hue by remember { mutableFloatStateOf(210f) }
    var saturation by remember { mutableFloatStateOf(0.85f) }
    var value by remember { mutableFloatStateOf(0.90f) }
    var alpha by remember { mutableFloatStateOf(selectedColor.alpha) }

    var selectedMode by remember { mutableIntStateOf(0) } // 0: Palette, 1: Spettro, 2: Slider
    var selectedCategoryIndex by remember {
        mutableIntStateOf(0)
    } // 0: Tutti, 1: Accademici, 2: Vibranti, 3: Pastello, 4: Material
    var hexInputText by remember { mutableStateOf("") }
    var copiedFeedback by remember { mutableStateOf(false) }

    val recentColors = remember { mutableStateListOf<Color>() }

    // Sincronizza HSV con selectedColor se cambia dall'esterno
    LaunchedEffect(selectedColor) {
        val (h, s, v) = colorToHsv(selectedColor)
        hue = h
        saturation = s
        value = v
        alpha = selectedColor.alpha
        if (!recentColors.any { it.toArgb() == selectedColor.toArgb() }) {
            recentColors.add(0, selectedColor)
            if (recentColors.size > 8) {
                recentColors.removeLast()
            }
        }
    }

    val currentColor =
        remember(hue, saturation, value, alpha) {
            hsvToColor(hue, saturation, value, alpha)
        }

    val hexString =
        remember(currentColor, showAlpha) {
            colorToHex(currentColor, showAlpha)
        }

    val rInt = (currentColor.red * 255).toInt().coerceIn(0, 255)
    val gInt = (currentColor.green * 255).toInt().coerceIn(0, 255)
    val bInt = (currentColor.blue * 255).toInt().coerceIn(0, 255)

    // Palette tematiche predefinite
    val academicPresets = remember {
        listOf(
            Color(0xFF0B57D0), // UniMol Royal
            Color(0xFFE91E63), // Amaranto Vivo
            Color(0xFF00897B), // Smeraldo Luminoso
            Color(0xFFE53935), // Rosso Rubino
            Color(0xFF8E24AA), // Viola Dottorato
            Color(0xFFFB8C00), // Ambra Calda
            Color(0xFF0288D1), // Cerulean Blue
            Color(0xFF43A047), // Verde Campus
        )
    }

    val vibrantPresets = remember {
        listOf(
            Color(0xFF2979FF), // Electric Blue
            Color(0xFF00E676), // Neon Mint
            Color(0xFFFF9100), // Vivid Amber
            Color(0xFFFF1744), // Laser Ruby
            Color(0xFFD500F9), // Neon Magenta
            Color(0xFF00E5FF), // Cyan Aqua
            Color(0xFFFFD600), // Sunny Gold
            Color(0xFFFF6D00), // Cyber Orange
        )
    }

    val pastelPresets = remember {
        listOf(
            Color(0xFFB39DDB), // Lavender
            Color(0xFFA5D6A7), // Sage
            Color(0xFFFFCC80), // Peach
            Color(0xFFF48FB1), // Soft Pink
            Color(0xFF90CAF9), // Sky Blue
            Color(0xFFFFAB91), // Soft Coral
            Color(0xFF80CBC4), // Pastel Mint
            Color(0xFFFFF59D), // Buttercream
        )
    }

    val materialPresets = remember {
        listOf(
            Color(0xFF009688), // Teal
            Color(0xFF3F51B5), // Indigo
            Color(0xFF7E57C2), // Deep Purple Luminoso
            Color(0xFF8BC34A), // Light Green
            Color(0xFFFFB300), // Amber
            Color(0xFFFF5722), // Deep Orange
            Color(0xFF26C6DA), // Cyan Luminoso
            Color(0xFFFFFFFF), // White
            Color(0xFFAB47BC), // Bright Orchid
        )
    }

    val currentPalette =
        remember(selectedCategoryIndex) {
            when (selectedCategoryIndex) {
                1 -> academicPresets
                2 -> vibrantPresets
                3 -> pastelPresets
                4 -> materialPresets
                else -> academicPresets + vibrantPresets + pastelPresets + materialPresets
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = RoundedRectangle(24.dp),
                    role = LiquidGlassRole.Surface,
                )
                .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // =========================================================
        // 1. HERO HEADER: ANTEPRIMA CROMATICA + HEX BADGE + VALORI
        // =========================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Riquadro colore attivo con bordo e riflesso
                Box(
                    modifier =
                        Modifier.size(52.dp)
                            .clip(RoundedRectangle(16.dp))
                            .background(currentColor)
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.40f),
                                shape = RoundedRectangle(16.dp),
                            )
                )

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    // Badge HEX cliccabile per copiare
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier =
                            Modifier.clip(Capsule())
                                .background(colorScheme.onSurface.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    Capsule(),
                                )
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(hexString))
                                    copiedFeedback = true
                                    coroutineScope.launch {
                                        delay(1400)
                                        copiedFeedback = false
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = hexString,
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp,
                                ),
                            color = colorScheme.onSurface,
                        )

                        Icon(
                            imageVector =
                                if (copiedFeedback) LiquidIcons.Check else LiquidIcons.Copy,
                            contentDescription = "Copia Hex",
                            tint =
                                if (copiedFeedback) colorScheme.primary
                                else glassColors.secondaryContent,
                            modifier = Modifier.size(14.dp),
                        )
                    }

                    Text(
                        text = "RGB: $rInt, $gInt, $bInt",
                        style = MaterialTheme.typography.labelSmall,
                        color = glassColors.secondaryContent,
                        fontSize = 11.sp,
                    )
                }
            }

            // Pulsante Ripristina al colore originario (se modificato)
            if (currentColor.toArgb() != initialColor.toArgb()) {
                Box(
                    modifier =
                        Modifier.clip(Capsule())
                            .background(colorScheme.primary.copy(alpha = 0.12f))
                            .border(1.dp, colorScheme.primary.copy(alpha = 0.3f), Capsule())
                            .clickable {
                                val (h, s, v) = colorToHsv(initialColor)
                                hue = h
                                saturation = s
                                value = v
                                alpha = initialColor.alpha
                                onColorSelected(initialColor)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.Refresh,
                            contentDescription = "Ripristina",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "Ripristina",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                            color = colorScheme.primary,
                        )
                    }
                }
            }
        }

        // =========================================================
        // 2. SELETTORE MODALITÀ (Palette / Spettro / Slider)
        // =========================================================
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedRectangle(14.dp))
                    .background(colorScheme.onSurface.copy(alpha = 0.06f))
                    .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val modes =
                listOf(
                    Triple("Palette", LiquidIcons.Palette, 0),
                    Triple("Spettro", LiquidIcons.Star, 1),
                    Triple("Slider", LiquidIcons.Settings, 2),
                )

            modes.forEach { (label, icon, index) ->
                val isSelected = selectedMode == index
                Box(
                    modifier =
                        Modifier.weight(1f)
                            .clip(RoundedRectangle(11.dp))
                            .background(if (isSelected) colorScheme.surface else Color.Transparent)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedRectangle(11.dp),
                                    )
                                } else Modifier
                            )
                            .clickable { selectedMode = index }
                            .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint =
                                if (isSelected) colorScheme.primary
                                else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = label,
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontWeight =
                                        if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                            color =
                                if (isSelected) colorScheme.primary
                                else colorScheme.onSurfaceVariant,
                            fontSize = 12.5.sp,
                        )
                    }
                }
            }
        }

        // =========================================================
        // 3. CONTENUTO SPECIFICO DELLA MODALITÀ
        // =========================================================
        when (selectedMode) {
            // -----------------------------------------------------
            // MODALITÀ 0: PALETTE TEMATICHE
            // -----------------------------------------------------
            0 -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Pillole categoria con scorrimento orizzontale
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val categories =
                            listOf("Tutti", "Accademici", "Vibranti", "Pastello", "Material")
                        categories.forEachIndexed { catIndex, catName ->
                            val isCatSelected = selectedCategoryIndex == catIndex
                            Box(
                                modifier =
                                    Modifier.clip(Capsule())
                                        .background(
                                            if (isCatSelected)
                                                colorScheme.primary.copy(alpha = 0.16f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = if (isCatSelected) 1.5.dp else 1.dp,
                                            color =
                                                if (isCatSelected) colorScheme.primary
                                                else colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = Capsule(),
                                        )
                                        .clickable { selectedCategoryIndex = catIndex }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = catName,
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            fontWeight =
                                                if (isCatSelected) FontWeight.Bold
                                                else FontWeight.Medium
                                        ),
                                    color =
                                        if (isCatSelected) colorScheme.primary
                                        else colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }

                    // Griglia dei campioni cromatici
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = 7,
                    ) {
                        currentPalette.forEach { preset ->
                            val isPresetSelected =
                                (selectedColor.toArgb() == preset.toArgb()) ||
                                    (currentColor.toArgb() == preset.toArgb())
                            val isLightPreset = preset == Color.White

                            val dropletScale by
                                LiquidColorPickerMotion.animateFloat(
                                    targetValue = if (isPresetSelected) 1.12f else 1.0f,
                                    performance = performance,
                                    label = "dropletScale",
                                )

                            val checkTint = contentColorFor(preset)

                            Box(
                                modifier =
                                    Modifier.size(38.dp)
                                        .graphicsLayer {
                                            scaleX = dropletScale
                                            scaleY = dropletScale
                                        }
                                        .clip(CircleShape)
                                        .background(if (isLightPreset) Color.White else preset)
                                        .border(
                                            width = if (isPresetSelected) 2.5.dp else 1.dp,
                                            color =
                                                if (isPresetSelected) colorScheme.onSurface
                                                else glassColors.outline.copy(alpha = 0.35f),
                                            shape = CircleShape,
                                        )
                                        .clickable(
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                            indication = null,
                                        ) {
                                            val (h, s, v) = colorToHsv(preset)
                                            hue = h
                                            saturation = s
                                            value = v
                                            alpha = preset.alpha
                                            onColorSelected(preset)
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isPresetSelected) {
                                    Icon(
                                        imageVector = LiquidIcons.Check,
                                        contentDescription = "Selezionato",
                                        tint = checkTint,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Fila colori recenti
                    if (recentColors.isNotEmpty()) {
                        LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Colori Recenti",
                                style = MaterialTheme.typography.labelSmall,
                                color = glassColors.secondaryContent,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                recentColors.take(6).forEach { recentColor ->
                                    val isCurrent = currentColor.toArgb() == recentColor.toArgb()
                                    Box(
                                        modifier =
                                            Modifier.size(26.dp)
                                                .clip(CircleShape)
                                                .background(recentColor)
                                                .border(
                                                    width = if (isCurrent) 2.dp else 1.dp,
                                                    color =
                                                        if (isCurrent) colorScheme.primary
                                                        else
                                                            glassColors.outline.copy(alpha = 0.35f),
                                                    shape = CircleShape,
                                                )
                                                .clickable {
                                                    val (h, s, v) = colorToHsv(recentColor)
                                                    hue = h
                                                    saturation = s
                                                    value = v
                                                    alpha = recentColor.alpha
                                                    onColorSelected(recentColor)
                                                }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------
            // MODALITÀ 1: SPETTRO 2D CONTINUO
            // -----------------------------------------------------
            1 -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Canvas 2D Saturazione / Luminosità
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedRectangle(16.dp))
                                .border(
                                    1.dp,
                                    glassColors.outline.copy(alpha = 0.35f),
                                    RoundedRectangle(16.dp),
                                )
                    ) {
                        val pureHueColor =
                            remember(hue) {
                                hsvToColor(hue, 1f, 1f, 1f)
                            }

                        Canvas(
                            modifier =
                                Modifier.fillMaxSize()
                                    .pointerInput(hue) {
                                        detectTapGestures { offset ->
                                            val sat = (offset.x / size.width).coerceIn(0f, 1f)
                                            val v = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                                            saturation = sat
                                            value = v
                                            val updated = hsvToColor(hue, saturation, value, alpha)
                                            onColorSelected(updated)
                                        }
                                    }
                                    .pointerInput(hue) {
                                        detectDragGestures { change, _ ->
                                            change.consume()
                                            val sat =
                                                (change.position.x / size.width).coerceIn(0f, 1f)
                                            val v =
                                                (1f - (change.position.y / size.height)).coerceIn(
                                                    0f,
                                                    1f,
                                                )
                                            saturation = sat
                                            value = v
                                            val updated = hsvToColor(hue, saturation, value, alpha)
                                            onColorSelected(updated)
                                        }
                                    }
                        ) {
                            // 1. Tonalità pura
                            drawRect(color = pureHueColor)

                            // 2. Gradiente saturazione (bianco a trasparente)
                            drawRect(
                                brush =
                                    Brush.horizontalGradient(listOf(Color.White, Color.Transparent))
                            )

                            // 3. Gradiente luminosità (trasparente a nero)
                            drawRect(
                                brush =
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
                            )

                            // 4. Reticolo mirino
                            val reticleX = saturation * size.width
                            val reticleY = (1f - value) * size.height

                            drawCircle(
                                color = Color.Black.copy(alpha = 0.6f),
                                radius = 12.dp.toPx(),
                                center = Offset(reticleX, reticleY),
                                style = Stroke(width = 3.5.dp.toPx()),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 10.dp.toPx(),
                                center = Offset(reticleX, reticleY),
                                style = Stroke(width = 2.5.dp.toPx()),
                            )
                            drawCircle(
                                color = currentColor,
                                radius = 7.5.dp.toPx(),
                                center = Offset(reticleX, reticleY),
                            )
                        }
                    }

                    // Slider continuo Tonalità (Hue)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Tonalità spettro",
                                style = MaterialTheme.typography.labelSmall,
                                color = glassColors.secondaryContent,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${hue.toInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        val hueGradient = remember {
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red,
                                )
                            )
                        }
                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(12.dp)
                                    .clip(Capsule())
                                    .background(hueGradient)
                                    .border(1.dp, glassColors.outline.copy(alpha = 0.3f), Capsule())
                        )
                        LiquidSlider(
                            value = hue,
                            onValueChange = {
                                hue = it
                                val updated = hsvToColor(hue, saturation, value, alpha)
                                onColorSelected(updated)
                            },
                            valueRange = 0f..360f,
                            backdropState = effectiveBackdrop,
                        )
                    }
                }
            }

            // -----------------------------------------------------
            // MODALITÀ 2: CURSORI DI PRECISIONE
            // -----------------------------------------------------
            2 -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // 1. Tonalità
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Tonalità (Hue)",
                                style = MaterialTheme.typography.labelSmall,
                                color = glassColors.secondaryContent,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${hue.toInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        val hueGradient = remember {
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red,
                                )
                            )
                        }
                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(10.dp)
                                    .clip(Capsule())
                                    .background(hueGradient)
                                    .border(1.dp, glassColors.outline.copy(alpha = 0.3f), Capsule())
                        )
                        LiquidSlider(
                            value = hue,
                            onValueChange = {
                                hue = it
                                val updated = hsvToColor(hue, saturation, value, alpha)
                                onColorSelected(updated)
                            },
                            valueRange = 0f..360f,
                            backdropState = effectiveBackdrop,
                        )
                    }

                    // 2. Saturazione
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Saturazione",
                                style = MaterialTheme.typography.labelSmall,
                                color = glassColors.secondaryContent,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${(saturation * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        val satGradient =
                            remember(hue, value) {
                                Brush.horizontalGradient(
                                    listOf(
                                        hsvToColor(hue, 0f, value, 1f),
                                        hsvToColor(hue, 1f, value, 1f),
                                    )
                                )
                            }
                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(10.dp)
                                    .clip(Capsule())
                                    .background(satGradient)
                                    .border(1.dp, glassColors.outline.copy(alpha = 0.3f), Capsule())
                        )
                        LiquidSlider(
                            value = saturation,
                            onValueChange = {
                                saturation = it
                                val updated = hsvToColor(hue, saturation, value, alpha)
                                onColorSelected(updated)
                            },
                            valueRange = 0f..1f,
                            backdropState = effectiveBackdrop,
                        )
                    }

                    // 3. Luminosità
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Luminosità",
                                style = MaterialTheme.typography.labelSmall,
                                color = glassColors.secondaryContent,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${(value * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        val valGradient =
                            remember(hue, saturation) {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black,
                                        hsvToColor(hue, saturation, 1f, 1f),
                                    )
                                )
                            }
                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .height(10.dp)
                                    .clip(Capsule())
                                    .background(valGradient)
                                    .border(1.dp, glassColors.outline.copy(alpha = 0.3f), Capsule())
                        )
                        LiquidSlider(
                            value = value,
                            onValueChange = {
                                value = it
                                val updated = hsvToColor(hue, saturation, value, alpha)
                                onColorSelected(updated)
                            },
                            valueRange = 0f..1f,
                            backdropState = effectiveBackdrop,
                        )
                    }

                    // 4. Alpha opzionale
                    if (showAlpha) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Opacità (Alpha)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glassColors.secondaryContent,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "${(alpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            val alphaGradient =
                                remember(currentColor) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            currentColor.copy(alpha = 0f),
                                            currentColor.copy(alpha = 1f),
                                        )
                                    )
                                }
                            Box(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .height(10.dp)
                                        .clip(Capsule())
                                        .background(alphaGradient)
                                        .border(
                                            1.dp,
                                            glassColors.outline.copy(alpha = 0.3f),
                                            Capsule(),
                                        )
                            )
                            LiquidSlider(
                                value = alpha,
                                onValueChange = {
                                    alpha = it
                                    val updated = hsvToColor(hue, saturation, value, alpha)
                                    onColorSelected(updated)
                                },
                                valueRange = 0f..1f,
                                backdropState = effectiveBackdrop,
                            )
                        }
                    }
                }
            }
        }

        // =========================================================
        // 4. INSERIMENTO CODICE ESADECIMALE MANUALE / INCOLLA
        // =========================================================
        LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(RoundedRectangle(14.dp))
                    .background(colorScheme.onSurface.copy(alpha = 0.05f))
                    .border(
                        1.dp,
                        colorScheme.outlineVariant.copy(alpha = 0.25f),
                        RoundedRectangle(14.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "#",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.primary,
                    ),
            )

            BasicTextField(
                value = hexInputText,
                onValueChange = { input ->
                    val clean = input.filter { it.isLetterOrDigit() }.take(8).uppercase()
                    hexInputText = clean
                    val parsed = parseHexToColor(clean)
                    if (parsed != null) {
                        val (h, s, v) = colorToHsv(parsed)
                        hue = h
                        saturation = s
                        value = v
                        alpha = parsed.alpha
                        onColorSelected(parsed)
                    }
                },
                textStyle =
                    TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colorScheme.onSurface,
                    ),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                modifier = Modifier.weight(1f),
                cursorBrush = SolidColor(colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (hexInputText.isEmpty()) {
                        Text(
                            text = hexString.removePrefix("#"),
                            style =
                                TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                ),
                        )
                    }
                    innerTextField()
                },
            )

            // Pulsante Incolla da appunti
            Box(
                modifier =
                    Modifier.clip(Capsule())
                        .background(colorScheme.primary.copy(alpha = 0.12f))
                        .clickable {
                            val clipText = clipboardManager.getText()?.text?.trim().orEmpty()
                            val clean =
                                clipText
                                    .removePrefix("#")
                                    .filter { it.isLetterOrDigit() }
                                    .take(8)
                                    .uppercase()
                            if (clean.isNotEmpty()) {
                                hexInputText = clean
                                val parsed = parseHexToColor(clean)
                                if (parsed != null) {
                                    val (h, s, v) = colorToHsv(parsed)
                                    hue = h
                                    saturation = s
                                    value = v
                                    alpha = parsed.alpha
                                    onColorSelected(parsed)
                                }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Incolla",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary,
                )
            }
        }
    }
}
