package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiProvider
import com.example.model.BlendModeType
import com.example.model.FilterType
import com.example.model.ImageAdjustments
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.ShapeData
import com.example.model.ShapeType
import com.example.model.TextData
import com.example.ui.theme.OutfitFamily
import com.example.ui.theme.YupiBackground
import com.example.ui.theme.YupiBorder
import com.example.ui.theme.YupiElectricBlue
import com.example.ui.theme.YupiNeonCyan
import com.example.ui.theme.YupiPink
import com.example.ui.theme.YupiPurple
import com.example.ui.theme.YupiPurpleDark
import com.example.ui.theme.YupiPurpleLight
import com.example.ui.theme.YupiPurpleVibrant
import com.example.ui.theme.YupiSurface
import com.example.ui.theme.YupiSurfaceElevated
import com.example.ui.theme.YupiSurfaceVariant
import com.example.ui.theme.YupiTextPrimary
import com.example.ui.theme.YupiTextSecondary
import com.example.ui.theme.YupiTextTertiary

// --- Adjustments Panel ---

@Composable
fun AdjustmentsPanel(
    adjustments: ImageAdjustments,
    onAdjustmentsChanged: (ImageAdjustments) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YupiSurface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ajustes de Imagen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = YupiTextPrimary
            )
            Row {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("reset_adjustments_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YupiTextSecondary)
                ) {
                    Text("Reiniciar", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApply,
                    modifier = Modifier.testTag("apply_adjustments_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = YupiPurpleLight)
                ) {
                    Text("Aplicar", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = YupiTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AdjustmentSlider(
            label = "Brillo",
            value = adjustments.brightness,
            range = -100f..100f,
            onValueChange = { onAdjustmentsChanged(adjustments.copy(brightness = it)) }
        )
        AdjustmentSlider(
            label = "Contraste",
            value = adjustments.contrast,
            range = -100f..100f,
            onValueChange = { onAdjustmentsChanged(adjustments.copy(contrast = it)) }
        )
        AdjustmentSlider(
            label = "Saturación",
            value = adjustments.saturation,
            range = -100f..100f,
            onValueChange = { onAdjustmentsChanged(adjustments.copy(saturation = it)) }
        )
        AdjustmentSlider(
            label = "Exposición",
            value = adjustments.exposure,
            range = -100f..100f,
            onValueChange = { onAdjustmentsChanged(adjustments.copy(exposure = it)) }
        )
        AdjustmentSlider(
            label = "Temperatura (Cálido / Frío)",
            value = adjustments.temperature,
            range = -100f..100f,
            onValueChange = { onAdjustmentsChanged(adjustments.copy(temperature = it)) }
        )
        AdjustmentSlider(
            label = "Desenfoque",
            value = adjustments.blur,
            range = 0f..100f,
            onValueChange = { onAdjustmentsChanged(adjustments.copy(blur = it)) }
        )
        AdjustmentSlider(
            label = "Nitidez",
            value = adjustments.sharpness,
            range = 0f..100f,
            onValueChange = { onAdjustmentsChanged(adjustments.copy(sharpness = it)) }
        )
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = YupiTextSecondary)
            Text(
                text = "${value.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (value != 0f) YupiElectricBlue else YupiTextTertiary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = YupiElectricBlue,
                activeTrackColor = YupiPurpleLight,
                inactiveTrackColor = YupiSurfaceVariant
            )
        )
    }
}

// --- Filters Panel ---

@Composable
fun FiltersPanel(
    activeFilter: FilterType,
    intensity: Float,
    onFilterSelected: (FilterType) -> Unit,
    onIntensityChanged: (Float) -> Unit,
    onApply: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YupiSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Filtros y Efectos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                Text(
                    text = "Filtro activo: ${activeFilter.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiElectricBlue
                )
            }
            Row {
                Button(
                    onClick = onApply,
                    enabled = activeFilter != FilterType.NONE,
                    modifier = Modifier.testTag("apply_filter_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue)
                ) {
                    Text("Aplicar", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = YupiTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Intensity slider
        if (activeFilter != FilterType.NONE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intensidad: ${(intensity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiTextSecondary
                )
            }
            Slider(
                value = intensity,
                onValueChange = onIntensityChanged,
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = YupiElectricBlue,
                    activeTrackColor = YupiElectricBlue,
                    inactiveTrackColor = YupiSurfaceVariant
                )
            )
        }

        // Horizontal filter cards carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(FilterType.values()) { filter ->
                val isSelected = activeFilter == filter
                Card(
                    modifier = Modifier
                        .size(width = 96.dp, height = 90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) YupiElectricBlue else YupiBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onFilterSelected(filter) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) YupiSurfaceElevated else YupiSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(getFilterPreviewBrush(filter)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = filter.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = if (isSelected) YupiElectricBlue else YupiTextPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun getFilterPreviewBrush(filter: FilterType): Brush {
    return when (filter) {
        FilterType.NONE -> Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF334155)))
        FilterType.CINEMATIC_TEAL_ORANGE -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF7043)))
        FilterType.CINEMATIC_CYBERPUNK -> Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF00E5FF)))
        FilterType.CINEMATIC_FILM_NOIR -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        FilterType.CINEMATIC_GOLDEN_HOUR -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444)))
        FilterType.BW_HIGH_CONTRAST -> Brush.linearGradient(listOf(Color.White, Color.Black))
        FilterType.BW_VINTAGE_MONO -> Brush.linearGradient(listOf(Color(0xFFD1D5DB), Color(0xFF4B5563)))
        FilterType.BW_SEPIA -> Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFF78350F)))
        FilterType.VINTAGE_WARM_70S -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309)))
        FilterType.VINTAGE_POLAROID -> Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF60A5FA)))
        FilterType.VINTAGE_FADE -> Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF475569)))
        FilterType.HDR_VIVID -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B)))
        FilterType.LIGHT_VIGNETTE -> Brush.radialGradient(listOf(Color.White, Color.Black))
        FilterType.LIGHT_GLOW -> Brush.radialGradient(listOf(Color(0xFFFEF08A), Color.Transparent))
        FilterType.COLOR_DUOTONE_PURPLE_BLUE -> Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF00E5FF)))
        FilterType.COLOR_INVERT -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFFEC4899)))
        FilterType.COLOR_HUE_SHIFT -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF10B981), Color(0xFF3B82F6)))
        FilterType.ARTISTIC_POSTERIZE -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF59E0B)))
        FilterType.ARTISTIC_PIXELATE -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
        FilterType.ARTISTIC_SKETCH -> Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF334155)))
        FilterType.ARTISTIC_EMBOSS -> Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF475569)))
    }
}

// --- Transform Panel ---

@Composable
fun TransformPanel(
    onRotate90: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onCropPreset: (Float, Float, Float, Float) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YupiSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recortar y Rotar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = YupiTextPrimary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = YupiTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick rotate & flip actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onRotate90,
                modifier = Modifier.testTag("rotate_90_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YupiTextPrimary)
            ) {
                Icon(Icons.Default.Rotate90DegreesCw, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Rotar 90°", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onFlipH,
                modifier = Modifier.testTag("flip_h_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YupiTextPrimary)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Voltear H", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onFlipV,
                modifier = Modifier.testTag("flip_v_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YupiTextPrimary)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Voltear V", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Proporciones de Recorte",
            style = MaterialTheme.typography.bodySmall,
            color = YupiTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Crop presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onCropPreset(0.1f, 0.1f, 0.9f, 0.9f) },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceElevated)
            ) {
                Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp), tint = YupiElectricBlue)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cuadrado 1:1", fontSize = 12.sp, color = YupiTextPrimary)
            }

            Button(
                onClick = { onCropPreset(0.05f, 0.15f, 0.95f, 0.85f) },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceElevated)
            ) {
                Text("Retrato 4:5", fontSize = 12.sp, color = YupiTextPrimary)
            }

            Button(
                onClick = { onCropPreset(0.0f, 0.2f, 1.0f, 0.8f) },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceElevated)
            ) {
                Text("Paisaje 16:9", fontSize = 12.sp, color = YupiTextPrimary)
            }

            Button(
                onClick = { onCropPreset(0.15f, 0.05f, 0.85f, 0.95f) },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceElevated)
            ) {
                Text("Historia 9:16", fontSize = 12.sp, color = YupiTextPrimary)
            }
        }
    }
}

// --- AI Tools Panel ---

@Composable
fun AiToolsPanel(
    onRemoveBackground: () -> Unit,
    onRemoveObject: () -> Unit,
    onAutoEnhance: () -> Unit,
    onSuperResolution: (Float) -> Unit,
    onDetectObjects: () -> Unit,
    onReplaceBackground: (String) -> Unit,
    onTextToImage: (prompt: String, style: String, provider: AiProvider, model: String) -> Unit,
    onSaveToken: (AiProvider, String) -> Unit,
    onGetToken: (AiProvider) -> String,
    onClose: () -> Unit
) {
    var promptText by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk") }
    var selectedProvider by remember { mutableStateOf(AiProvider.GEMINI) }
    var selectedModel by remember { mutableStateOf(AiProvider.GEMINI.models.first()) }
    var showPromptInput by remember { mutableStateOf(true) }
    var showTokenConfig by remember { mutableStateOf(false) }
    var tokenInput by remember { mutableStateOf(onGetToken(AiProvider.GEMINI)) }
    var isTokenVisible by remember { mutableStateOf(false) }
    var tokenStatusMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YupiSurface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = YupiElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Inteligencia Artificial YupiForge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = YupiTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick AI processing tools
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiActionCard(
                title = "Quitar Fondo",
                subtitle = "Recorte automático",
                icon = "🪄",
                color = YupiPurpleLight,
                modifier = Modifier.weight(1f),
                onClick = onRemoveBackground
            )
            AiActionCard(
                title = "Borrar Objeto",
                subtitle = "Síntesis y relleno",
                icon = "🩹",
                color = YupiElectricBlue,
                modifier = Modifier.weight(1f),
                onClick = onRemoveObject
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiActionCard(
                title = "Mejorar Calidad",
                subtitle = "Nitidez & Contraste",
                icon = "✨",
                color = YupiPink,
                modifier = Modifier.weight(1f),
                onClick = onAutoEnhance
            )
            AiActionCard(
                title = "Super Resolución",
                subtitle = "Aumento 2X / 4X",
                icon = "🔍",
                color = YupiNeonCyan,
                modifier = Modifier.weight(1f),
                onClick = { onSuperResolution(2.0f) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiActionCard(
                title = "Detectar Objetos",
                subtitle = "Segmentación visual",
                icon = "🎯",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = onDetectObjects
            )
            AiActionCard(
                title = "Crear con Texto",
                subtitle = "ChatGPT, Gemini, NVIDIA",
                icon = "💡",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = { showPromptInput = !showPromptInput }
            )
        }

        // Section: Text-to-Image with multi-provider and token management
        AnimatedVisibility(visible = showPromptInput) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(YupiSurfaceElevated)
                    .border(1.dp, YupiBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generador IA Multimodelo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = YupiElectricBlue
                    )
                    OutlinedButton(
                        onClick = {
                            showTokenConfig = !showTokenConfig
                            tokenInput = onGetToken(selectedProvider)
                            tokenStatusMsg = null
                        },
                        modifier = Modifier.testTag("toggle_token_config_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = YupiPurpleLight)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tokens / API Keys", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Provider selection chips
                Text("Proveedor de IA:", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiProvider.values().forEach { provider ->
                        val isSel = selectedProvider == provider
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedProvider = provider
                                selectedModel = provider.models.firstOrNull() ?: ""
                                tokenInput = onGetToken(provider)
                                tokenStatusMsg = null
                            },
                            label = {
                                Text("${provider.icon} ${provider.displayName}", fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YupiElectricBlue,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("provider_chip_${provider.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model Selection Row according to chosen provider
                Text("Modelo de ${selectedProvider.displayName}:", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedProvider.models.forEach { modelName ->
                        val isModelSel = selectedModel == modelName
                        FilterChip(
                            selected = isModelSel,
                            onClick = { selectedModel = modelName },
                            label = { Text(modelName, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YupiPurpleLight,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("model_chip_${modelName.replace('/', '_')}")
                        )
                    }
                }

                // Token Input / Config Block
                val currentSavedToken = onGetToken(selectedProvider)
                val hasToken = currentSavedToken.isNotBlank() || selectedProvider == AiProvider.LOCAL_NEURAL

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedProvider == AiProvider.LOCAL_NEURAL) {
                        Text("✅ Motor local autónomo listo", fontSize = 11.sp, color = Color(0xFF10B981))
                    } else if (hasToken) {
                        Text("✅ Token configurado (${currentSavedToken.take(4)}...${currentSavedToken.takeLast(4)})", fontSize = 11.sp, color = Color(0xFF10B981))
                    } else {
                        Text("⚠️ Requiere token (${selectedProvider.tokenHint})", fontSize = 11.sp, color = Color(0xFFF59E0B))
                    }

                    if (selectedProvider != AiProvider.LOCAL_NEURAL) {
                        Text(
                            text = if (showTokenConfig) "Ocultar" else "Editar Token",
                            fontSize = 11.sp,
                            color = YupiElectricBlue,
                            modifier = Modifier
                                .clickable {
                                    showTokenConfig = !showTokenConfig
                                    tokenInput = onGetToken(selectedProvider)
                                    tokenStatusMsg = null
                                }
                                .padding(4.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = showTokenConfig && selectedProvider != AiProvider.LOCAL_NEURAL) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(YupiSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Token / API Key para ${selectedProvider.displayName}:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = YupiTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            placeholder = { Text("Ej: ${selectedProvider.tokenHint}", color = YupiTextTertiary) },
                            modifier = Modifier.fillMaxWidth().testTag("token_input_field"),
                            trailingIcon = {
                                IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                    Icon(
                                        if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Alternar visibilidad",
                                        tint = YupiTextSecondary
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YupiElectricBlue,
                                unfocusedBorderColor = YupiBorder,
                                focusedTextColor = YupiTextPrimary,
                                unfocusedTextColor = YupiTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tokenStatusMsg != null) {
                                Text(tokenStatusMsg!!, fontSize = 11.sp, color = Color(0xFF10B981))
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                            Button(
                                onClick = {
                                    onSaveToken(selectedProvider, tokenInput)
                                    tokenStatusMsg = "¡Token guardado exitosamente!"
                                },
                                modifier = Modifier.testTag("save_token_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue)
                            ) {
                                Text("Guardar Token", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Prompt Input
                Text("Prompt / Descripción:", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("Ej: Un astronauta en un bosque bioluminiscente con destellos púrpuras", color = YupiTextTertiary) },
                    modifier = Modifier.fillMaxWidth().testTag("ai_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YupiElectricBlue,
                        unfocusedBorderColor = YupiBorder,
                        focusedTextColor = YupiTextPrimary,
                        unfocusedTextColor = YupiTextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Styles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cyberpunk", "Fotorrealista", "Anime", "3D Render", "Óleo", "Minimalista", "Neón").forEach { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            label = { Text(style, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YupiPurpleLight,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (promptText.isNotBlank()) {
                            onTextToImage(promptText, selectedStyle, selectedProvider, selectedModel)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("generate_image_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = YupiPurpleLight)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Generar con ${selectedProvider.displayName} (${selectedModel.substringAfterLast('/')})",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Background Replacement section
        Text(
            text = "Sustitución de Fondos Inteligente",
            style = MaterialTheme.typography.bodySmall,
            color = YupiTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onReplaceBackground("NEON_CYBERPUNK") },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
            ) {
                Text("🌌 Neón Cyber", fontSize = 11.sp, color = YupiTextPrimary)
            }
            Button(
                onClick = { onReplaceBackground("STUDIO_DARK") },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
            ) {
                Text("🎬 Estudio Oscuro", fontSize = 11.sp, color = YupiTextPrimary)
            }
            Button(
                onClick = { onReplaceBackground("SUNSET_GOLDEN") },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
            ) {
                Text("🌅 Atardecer", fontSize = 11.sp, color = YupiTextPrimary)
            }
            Button(
                onClick = { onReplaceBackground("MINIMAL_LIGHT") },
                colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
            ) {
                Text("⚪ Minimalista", fontSize = 11.sp, color = YupiTextPrimary)
            }
        }
    }
}

@Composable
private fun AiActionCard(
    title: String,
    subtitle: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(1.dp, YupiBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = YupiSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = YupiTextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = YupiTextTertiary, maxLines = 1)
            }
        }
    }
}

// --- Drawing Panel ---

@Composable
fun DrawingPanel(
    brushColor: Long,
    brushSize: Float,
    brushOpacity: Float,
    isEraser: Boolean,
    onColorChanged: (Long) -> Unit,
    onSizeChanged: (Float) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onToggleEraser: (Boolean) -> Unit,
    onClearStrokes: () -> Unit,
    onClose: () -> Unit
) {
    val presetColors = listOf(
        0xFF00E5FF, 0xFFA855F7, 0xFFEC4899, 0xFFFFFFFF,
        0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFF3B82F6,
        0xFF000000, 0xFFFFEE58
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YupiSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Herramienta de Dibujo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = YupiTextPrimary
            )
            Row {
                OutlinedButton(
                    onClick = onClearStrokes,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Borrar Trazos", fontSize = 11.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = YupiTextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tool Mode: Brush vs Eraser
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onToggleEraser(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isEraser) YupiElectricBlue else YupiSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Brush, contentDescription = null, tint = if (!isEraser) Color.Black else YupiTextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pincel", color = if (!isEraser) Color.Black else YupiTextSecondary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onToggleEraser(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEraser) YupiPink else YupiSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("🧽 Borrador", color = if (isEraser) Color.Black else YupiTextSecondary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Color Palette
        if (!isEraser) {
            Text("Color del Trazo", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetColors.forEach { c ->
                    val isSel = brushColor == c
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(
                                width = if (isSel) 3.dp else 1.dp,
                                color = if (isSel) YupiElectricBlue else Color(0x44FFFFFF),
                                shape = CircleShape
                            )
                            .clickable { onColorChanged(c) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSel) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = if (c == 0xFFFFFFFF) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Brush Size slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Grosor: ${brushSize.toInt()} px", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
        }
        Slider(
            value = brushSize,
            onValueChange = onSizeChanged,
            valueRange = 2f..80f,
            colors = SliderDefaults.colors(thumbColor = YupiElectricBlue, activeTrackColor = YupiPurpleLight)
        )
    }
}

// --- Text Creation Dialog / Panel ---

@Composable
fun AddTextDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (TextData) -> Unit
) {
    var text by remember { mutableStateOf(initialText.ifEmpty { "Tu Texto Aquí" }) }
    var selectedColor by remember { mutableStateOf(0xFFFFFFFF) }
    var textSize by remember { mutableFloatStateOf(42f) }
    var hasBackground by remember { mutableStateOf(true) }
    var bgColor by remember { mutableStateOf(0xAA9333EA) }
    var isBold by remember { mutableStateOf(true) }

    val presetColors = listOf(0xFFFFFFFF, 0xFF00E5FF, 0xFFA855F7, 0xFFEC4899, 0xFFF59E0B, 0xFF10B981, 0xFF000000)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = YupiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Agregar Texto Personalizado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().testTag("text_input_field"),
                    label = { Text("Texto") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YupiElectricBlue,
                        focusedTextColor = YupiTextPrimary,
                        unfocusedTextColor = YupiTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color selector
                Text("Color de Texto", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(if (selectedColor == c) 2.dp else 1.dp, if (selectedColor == c) YupiElectricBlue else Color.Gray, CircleShape)
                                .clickable { selectedColor = c }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Size slider
                Text("Tamaño: ${textSize.toInt()} pt", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Slider(
                    value = textSize,
                    onValueChange = { textSize = it },
                    valueRange = 16f..90f,
                    colors = SliderDefaults.colors(thumbColor = YupiElectricBlue, activeTrackColor = YupiPurpleLight)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fondo Estilo Insignia", style = MaterialTheme.typography.bodyMedium, color = YupiTextPrimary)
                    Switch(
                        checked = hasBackground,
                        onCheckedChange = { hasBackground = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = YupiElectricBlue, checkedTrackColor = YupiPurpleDark)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar", color = YupiTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                TextData(
                                    text = text,
                                    textColor = selectedColor,
                                    textSize = textSize,
                                    hasBackground = hasBackground,
                                    backgroundColor = bgColor,
                                    isBold = isBold
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue)
                    ) {
                        Text("Agregar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Shapes & Stickers Panel ---

@Composable
fun ShapesAndStickersPanel(
    activeShapeData: ShapeData? = null,
    onAddShape: (ShapeData) -> Unit,
    onUpdateActiveShapeDimensions: (width: Float, height: Float, cornerRadius: Float) -> Unit = { _, _, _ -> },
    onUpdateActiveShapeColors: (fillColor: Long, strokeColor: Long, strokeWidth: Float) -> Unit = { _, _, _ -> },
    onAddSticker: (String) -> Unit,
    onClose: () -> Unit
) {
    val stickers = listOf("🔥", "✨", "💎", "🚀", "⚡", "🎨", "📸", "👾", "👑", "🌟", "💜", "💯", "🛸", "🌈", "🕶️", "💥")
    val presetColors = listOf(0xFFA855F7, 0xFF00E5FF, 0xFFEC4899, 0xFFF59E0B, 0xFF10B981, 0xFFFFFFFF, 0xFF000000)

    var width by remember(activeShapeData) { mutableFloatStateOf(activeShapeData?.width ?: 360f) }
    var height by remember(activeShapeData) { mutableFloatStateOf(activeShapeData?.height ?: 220f) }
    var cornerRadius by remember(activeShapeData) { mutableFloatStateOf(activeShapeData?.cornerRadius ?: 36f) }
    var fillColor by remember(activeShapeData) { mutableStateOf(activeShapeData?.fillColor ?: 0xFFA855F7) }
    var strokeColor by remember(activeShapeData) { mutableStateOf(activeShapeData?.strokeColor ?: 0xFF00E5FF) }
    var strokeWidth by remember(activeShapeData) { mutableFloatStateOf(activeShapeData?.strokeWidth ?: 4f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YupiSurface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Formas y Stickers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                if (activeShapeData != null) {
                    Text(
                        text = "Editando capa de forma activa",
                        style = MaterialTheme.typography.bodySmall,
                        color = YupiElectricBlue
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = YupiTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section: Rectángulo con Esquinas Redondeadas Estirable
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(YupiSurfaceElevated)
                .border(1.dp, YupiBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔲 Rectángulo con Esquinas Redondeadas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = YupiElectricBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick stretch presets
            Text("Estilos Rápidos:", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        width = 540f; height = 160f; cornerRadius = 24f
                        if (activeShapeData != null) onUpdateActiveShapeDimensions(540f, 160f, 24f)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
                ) {
                    Text("Banner Ancho", fontSize = 11.sp, color = YupiTextPrimary)
                }
                Button(
                    onClick = {
                        width = 380f; height = 240f; cornerRadius = 36f
                        if (activeShapeData != null) onUpdateActiveShapeDimensions(380f, 240f, 36f)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
                ) {
                    Text("Tarjeta / Tarjetón", fontSize = 11.sp, color = YupiTextPrimary)
                }
                Button(
                    onClick = {
                        width = 360f; height = 100f; cornerRadius = 50f
                        if (activeShapeData != null) onUpdateActiveShapeDimensions(360f, 100f, 50f)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
                ) {
                    Text("Cápsula / Botón", fontSize = 11.sp, color = YupiTextPrimary)
                }
                Button(
                    onClick = {
                        width = 280f; height = 280f; cornerRadius = 48f
                        if (activeShapeData != null) onUpdateActiveShapeDimensions(280f, 280f, 48f)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
                ) {
                    Text("Cuadro Redondeado", fontSize = 11.sp, color = YupiTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stretch Width Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ancho (Estirar Horizontal)", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Text("${width.toInt()} px", style = MaterialTheme.typography.bodySmall, color = YupiElectricBlue, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = width,
                onValueChange = {
                    width = it
                    if (activeShapeData != null) onUpdateActiveShapeDimensions(width, height, cornerRadius)
                },
                valueRange = 60f..800f,
                colors = SliderDefaults.colors(thumbColor = YupiElectricBlue, activeTrackColor = YupiPurpleLight),
                modifier = Modifier.testTag("shape_width_slider")
            )

            // Stretch Height Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Alto (Estirar Vertical)", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Text("${height.toInt()} px", style = MaterialTheme.typography.bodySmall, color = YupiElectricBlue, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = height,
                onValueChange = {
                    height = it
                    if (activeShapeData != null) onUpdateActiveShapeDimensions(width, height, cornerRadius)
                },
                valueRange = 40f..700f,
                colors = SliderDefaults.colors(thumbColor = YupiElectricBlue, activeTrackColor = YupiPurpleLight),
                modifier = Modifier.testTag("shape_height_slider")
            )

            // Corner Radius Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Radio de Esquinas Redondeadas", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
                Text("${cornerRadius.toInt()} px", style = MaterialTheme.typography.bodySmall, color = YupiPurpleLight, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = cornerRadius,
                onValueChange = {
                    cornerRadius = it
                    if (activeShapeData != null) onUpdateActiveShapeDimensions(width, height, cornerRadius)
                },
                valueRange = 0f..140f,
                colors = SliderDefaults.colors(thumbColor = YupiPurpleLight, activeTrackColor = YupiElectricBlue),
                modifier = Modifier.testTag("shape_corner_radius_slider")
            )

            // Colors for Shape
            Spacer(modifier = Modifier.height(4.dp))
            Text("Color de Relleno:", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presetColors.forEach { c ->
                    val isSel = fillColor == c
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(if (isSel) 2.dp else 1.dp, if (isSel) YupiElectricBlue else Color.Gray, CircleShape)
                            .clickable {
                                fillColor = c
                                if (activeShapeData != null) onUpdateActiveShapeColors(fillColor, strokeColor, strokeWidth)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onAddShape(
                        ShapeData(
                            shapeType = ShapeType.ROUNDED_RECTANGLE,
                            fillColor = fillColor,
                            strokeColor = strokeColor,
                            strokeWidth = strokeWidth,
                            width = width,
                            height = height,
                            cornerRadius = cornerRadius
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("add_rounded_rect_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue)
            ) {
                Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("+ Añadir Rectángulo Redondeado", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Otras Formas Vectoriales", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShapeType.values().filter { it != ShapeType.ROUNDED_RECTANGLE }.forEach { st ->
                Button(
                    onClick = {
                        onAddShape(
                            ShapeData(
                                shapeType = st,
                                fillColor = fillColor,
                                strokeColor = strokeColor,
                                strokeWidth = strokeWidth,
                                width = width,
                                height = height,
                                cornerRadius = cornerRadius
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YupiSurfaceVariant)
                ) {
                    Text(
                        text = when (st) {
                            ShapeType.RECTANGLE -> "⬛ Rectángulo Recto"
                            ShapeType.CIRCLE -> "⚪ Círculo"
                            ShapeType.STAR -> "⭐ Estrella"
                            ShapeType.HEART -> "❤️ Corazón"
                            ShapeType.ARROW -> "➡️ Flecha"
                            ShapeType.BADGE -> "🛡️ Insignia"
                            else -> st.name
                        },
                        fontSize = 11.sp,
                        color = YupiTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Stickers", style = MaterialTheme.typography.bodySmall, color = YupiTextSecondary)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(stickers) { emoji ->
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(YupiSurfaceVariant)
                        .border(1.dp, YupiBorder, RoundedCornerShape(10.dp))
                        .clickable { onAddSticker(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
    }
}

