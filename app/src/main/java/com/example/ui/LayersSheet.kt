package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlendModeType
import com.example.model.Layer
import com.example.model.LayerType
import com.example.ui.theme.YupiBorder
import com.example.ui.theme.YupiElectricBlue
import com.example.ui.theme.YupiPink
import com.example.ui.theme.YupiPurpleDark
import com.example.ui.theme.YupiPurpleLight
import com.example.ui.theme.YupiSurface
import com.example.ui.theme.YupiSurfaceElevated
import com.example.ui.theme.YupiSurfaceVariant
import com.example.ui.theme.YupiTextPrimary
import com.example.ui.theme.YupiTextSecondary
import com.example.ui.theme.YupiTextTertiary

@Composable
fun LayersSheet(
    layers: List<Layer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onReorder: (String, Boolean) -> Unit,
    onSetOpacity: (String, Float) -> Unit,
    onSetBlendMode: (String, BlendModeType) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAddImageClick: () -> Unit = {},
    onClose: () -> Unit
) {
    val activeLayer = layers.find { it.id == selectedLayerId }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = YupiSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Capas del Proyecto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = YupiTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(YupiPurpleDark)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${layers.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = YupiElectricBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onAddImageClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+ Imagen", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = YupiTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active layer controls: Opacity, Reorder, Duplicate, Delete
            if (activeLayer != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = YupiSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Capa: ${activeLayer.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = YupiElectricBlue,
                                maxLines = 1
                            )
                            Row {
                                IconButton(
                                    onClick = { onReorder(activeLayer.id, true) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Subir capa", tint = YupiTextPrimary)
                                }
                                IconButton(
                                    onClick = { onReorder(activeLayer.id, false) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar capa", tint = YupiTextPrimary)
                                }
                                IconButton(
                                    onClick = { onDuplicate(activeLayer.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = YupiTextPrimary)
                                }
                                IconButton(
                                    onClick = { onDelete(activeLayer.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = YupiPink)
                                }
                            }
                        }

                        // Opacity slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Opacidad: ${(activeLayer.opacity * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = YupiTextSecondary
                            )
                        }
                        Slider(
                            value = activeLayer.opacity,
                            onValueChange = { onSetOpacity(activeLayer.id, it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = YupiElectricBlue,
                                activeTrackColor = YupiPurpleLight
                            )
                        )

                        // Blend modes
                        Text(
                            text = "Modo de Fusión",
                            style = MaterialTheme.typography.bodySmall,
                            color = YupiTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                BlendModeType.NORMAL to "Normal",
                                BlendModeType.MULTIPLY to "Multiplicar",
                                BlendModeType.SCREEN to "Pantalla",
                                BlendModeType.OVERLAY to "Superponer"
                            ).forEach { (mode, label) ->
                                FilterChip(
                                    selected = activeLayer.blendMode == mode,
                                    onClick = { onSetBlendMode(activeLayer.id, mode) },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = YupiPurpleLight,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Layer items list (Top to bottom)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Render from top layer to bottom layer
                itemsIndexed(layers.reversed()) { _, layer ->
                    val isSelected = layer.id == selectedLayerId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) YupiElectricBlue else YupiBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectLayer(layer.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) YupiSurfaceElevated else YupiSurfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Layer icon
                                Text(
                                    text = when (layer.type) {
                                        LayerType.IMAGE -> "🖼️"
                                        LayerType.DRAWING -> "✏️"
                                        LayerType.TEXT -> "🔤"
                                        LayerType.SHAPE -> "🔷"
                                        LayerType.STICKER -> "⭐"
                                    },
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = layer.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) YupiElectricBlue else YupiTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${layer.type.name.lowercase()} • ${(layer.opacity * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = YupiTextTertiary
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { onToggleVisibility(layer.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Visibilidad",
                                        tint = if (layer.isVisible) YupiElectricBlue else YupiTextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleLock(layer.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Bloqueo",
                                        tint = if (layer.isLocked) YupiPink else YupiTextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
