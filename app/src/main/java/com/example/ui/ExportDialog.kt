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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExportFormat
import com.example.model.ExportOptions
import com.example.ui.theme.YupiBorder
import com.example.ui.theme.YupiElectricBlue
import com.example.ui.theme.YupiPurpleDark
import com.example.ui.theme.YupiPurpleLight
import com.example.ui.theme.YupiSurface
import com.example.ui.theme.YupiSurfaceElevated
import com.example.ui.theme.YupiSurfaceVariant
import com.example.ui.theme.YupiTextPrimary
import com.example.ui.theme.YupiTextSecondary

@Composable
fun ExportDialog(
    projectWidth: Int,
    projectHeight: Int,
    onDismiss: () -> Unit,
    onExport: (ExportOptions) -> Unit
) {
    var format by remember { mutableStateOf(ExportFormat.PNG) }
    var quality by remember { mutableIntStateOf(95) }
    var scaleMultiplier by remember { mutableFloatStateOf(1.0f) }
    var transparentBg by remember { mutableStateOf(false) }

    val outputWidth = (projectWidth * scaleMultiplier).toInt()
    val outputHeight = (projectHeight * scaleMultiplier).toInt()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = YupiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Exportar Imagen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Format selector (PNG, JPG, WEBP)
                Text(
                    text = "Formato de Salida",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { fmt ->
                        val isSel = format == fmt
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) YupiElectricBlue else YupiBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { format = fmt },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) YupiSurfaceElevated else YupiSurfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = fmt.name,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) YupiElectricBlue else YupiTextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (fmt) {
                                        ExportFormat.PNG -> "Sin pérdida"
                                        ExportFormat.JPG -> "Comprimido"
                                        ExportFormat.WEBP -> "Moderno"
                                    },
                                    fontSize = 10.sp,
                                    color = YupiTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quality slider (for JPG and WEBP)
                if (format != ExportFormat.PNG) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Calidad de Compresión",
                            style = MaterialTheme.typography.bodySmall,
                            color = YupiTextSecondary
                        )
                        Text(
                            text = "$quality%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = YupiElectricBlue
                        )
                    }
                    Slider(
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = YupiElectricBlue,
                            activeTrackColor = YupiPurpleLight
                        )
                    )
                }

                // Transparent background toggle (for PNG and WEBP)
                if (format != ExportFormat.JPG) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Fondo Transparente",
                                style = MaterialTheme.typography.bodyMedium,
                                color = YupiTextPrimary
                            )
                            Text(
                                text = "Ideal para stickers y logotipos",
                                style = MaterialTheme.typography.bodySmall,
                                color = YupiTextSecondary
                            )
                        }
                        Switch(
                            checked = transparentBg,
                            onCheckedChange = { transparentBg = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = YupiElectricBlue,
                                checkedTrackColor = YupiPurpleDark
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Resolution options
                Text(
                    text = "Resolución: ${outputWidth}x${outputHeight} px",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = scaleMultiplier == 1.0f,
                        onClick = { scaleMultiplier = 1.0f },
                        label = { Text("1X Original", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YupiElectricBlue,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = scaleMultiplier == 2.0f,
                        onClick = { scaleMultiplier = 2.0f },
                        label = { Text("2X Alta Definición", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YupiElectricBlue,
                            selectedLabelColor = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar", color = YupiTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onExport(
                                ExportOptions(
                                    format = format,
                                    quality = quality,
                                    scaleMultiplier = scaleMultiplier,
                                    transparentBackground = transparentBg
                                )
                            )
                        },
                        modifier = Modifier.testTag("confirm_export_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
