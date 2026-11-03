package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.ai.AiTokenService
import com.example.model.AiProvider
import com.example.model.CanvasPreset
import com.example.model.ExportFormat
import com.example.model.Project
import com.example.repository.ExportedItem
import com.example.repository.ProjectRepository
import com.example.ui.theme.MontserratFamily
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
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    repository: ProjectRepository,
    onOpenProject: (Project) -> Unit,
    onCreateNewProject: (name: String, width: Int, height: Int, initialBitmap: Bitmap?) -> Unit
) {
    val projects by repository.projects.collectAsState()
    val exportedItems by repository.exportedGallery.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPresetDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedPreviewItem by remember { mutableStateOf<ExportedItem?>(null) }
    var currentMainTab by remember { mutableIntStateOf(0) } // 0 = Proyectos, 1 = Galería Exportada

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        onCreateNewProject(
                            "Edición ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}",
                            bitmap.width,
                            bitmap.height,
                            bitmap
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = YupiBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Brand & Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.yupiforge_icon_1793746264620),
                        contentDescription = "Logo YupiForge",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, YupiElectricBlue, RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "YupiForge",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = MontserratFamily,
                            fontWeight = FontWeight.Bold,
                            color = YupiTextPrimary
                        )
                        Text(
                            text = "Editor Profesional de Imágenes",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = OutfitFamily,
                            color = YupiElectricBlue
                        )
                    }
                }

                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(YupiSurfaceVariant)
                        .testTag("settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = YupiTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Creation Buttons (Nueva Edición & Abrir Imagen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nueva Edición
                Button(
                    onClick = { showPresetDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("new_edition_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YupiPurpleLight
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nueva edición",
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                // Abrir Imagen
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("open_image_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YupiElectricBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Abrir imagen",
                        fontFamily = MontserratFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick AI Tools Highlights Row
            Text(
                text = "Herramientas Inteligentes",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.Bold,
                color = YupiTextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    AiFeatureChip(
                        icon = "🪄",
                        title = "Quitar Fondo",
                        gradient = listOf(YupiPurpleVibrant, YupiPurpleDark),
                        onClick = {
                            val sample = projects.firstOrNull()
                            if (sample != null) onOpenProject(sample) else showPresetDialog = true
                        }
                    )
                }
                item {
                    AiFeatureChip(
                        icon = "✨",
                        title = "Mejorar Calidad",
                        gradient = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                        onClick = {
                            val sample = projects.firstOrNull()
                            if (sample != null) onOpenProject(sample) else showPresetDialog = true
                        }
                    )
                }
                item {
                    AiFeatureChip(
                        icon = "💡",
                        title = "Generador IA",
                        gradient = listOf(Color(0xFF7C3AED), Color(0xFF4C1D95)),
                        onClick = {
                            val sample = projects.firstOrNull()
                            if (sample != null) onOpenProject(sample) else showPresetDialog = true
                        }
                    )
                }
                item {
                    AiFeatureChip(
                        icon = "🎨",
                        title = "Filtros Pro",
                        gradient = listOf(Color(0xFFDB2777), Color(0xFF831843)),
                        onClick = {
                            val sample = projects.firstOrNull()
                            if (sample != null) onOpenProject(sample) else showPresetDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab bar: Proyectos Recientes vs Galería de Imágenes Editadas
            TabRow(
                selectedTabIndex = currentMainTab,
                containerColor = Color.Transparent,
                contentColor = YupiElectricBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentMainTab]),
                        color = YupiElectricBlue
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = currentMainTab == 0,
                    onClick = { currentMainTab = 0 },
                    text = {
                        Text(
                            text = "Proyectos Recientes (${projects.size})",
                            fontFamily = MontserratFamily,
                            fontWeight = if (currentMainTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentMainTab == 0) YupiElectricBlue else YupiTextSecondary
                        )
                    }
                )
                Tab(
                    selected = currentMainTab == 1,
                    onClick = { currentMainTab = 1 },
                    text = {
                        Text(
                            text = "Galería Editada (${exportedItems.size})",
                            fontFamily = MontserratFamily,
                            fontWeight = if (currentMainTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentMainTab == 1) YupiElectricBlue else YupiTextSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab 0: Proyectos Recientes
            if (currentMainTab == 0) {
                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🎨", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin proyectos aún",
                                style = MaterialTheme.typography.titleMedium,
                                color = YupiTextPrimary
                            )
                            Text(
                                text = "Comienza creando una nueva edición o abriendo una foto",
                                style = MaterialTheme.typography.bodySmall,
                                color = YupiTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        projects.forEach { project ->
                            ProjectCardItem(
                                project = project,
                                onOpen = { onOpenProject(project) },
                                onDuplicate = {
                                    scope.launch { repository.duplicateProject(project) }
                                },
                                onDelete = {
                                    scope.launch { repository.deleteProject(project.id) }
                                }
                            )
                        }
                    }
                }
            } else {
                // Tab 1: Galería Exportada
                if (exportedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🖼️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aún no has exportado imágenes",
                                style = MaterialTheme.typography.titleMedium,
                                color = YupiTextPrimary
                            )
                            Text(
                                text = "Exporta tus creaciones en PNG, JPG o WEBP para verlas aquí y en tu galería",
                                style = MaterialTheme.typography.bodySmall,
                                color = YupiTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            exportedItems.chunked(2).forEach { columnItems ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    columnItems.forEach { item ->
                                        ExportedItemCard(
                                            item = item,
                                            onPreview = { selectedPreviewItem = item },
                                            onShare = {
                                                val uri = repository.getShareableUri(File(item.filePath))
                                                repository.shareImage(context, uri)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Canvas Preset Dialog
        if (showPresetDialog) {
            CanvasPresetsDialog(
                presets = repository.canvasPresets,
                onDismiss = { showPresetDialog = false },
                onSelectPreset = { preset ->
                    showPresetDialog = false
                    onCreateNewProject(preset.name, preset.width, preset.height, null)
                }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                onDismiss = { showSettingsDialog = false }
            )
        }

        // Fullscreen Image Preview Dialog
        if (selectedPreviewItem != null) {
            FullscreenPreviewDialog(
                item = selectedPreviewItem!!,
                onDismiss = { selectedPreviewItem = null },
                onShare = {
                    val uri = repository.getShareableUri(File(selectedPreviewItem!!.filePath))
                    repository.shareImage(context, uri)
                }
            )
        }
    }
}

@Composable
private fun AiFeatureChip(
    icon: String,
    title: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(gradient))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontFamily = MontserratFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ProjectCardItem(
    project: Project,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(project.modifiedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, YupiBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .testTag("project_item_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = YupiSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail preview
            val firstImgLayer = project.layers.find { it.type == com.example.model.LayerType.IMAGE && it.bitmap != null }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(YupiSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (firstImgLayer?.bitmap != null) {
                    Image(
                        bitmap = firstImgLayer.bitmap.asImageBitmap(),
                        contentDescription = project.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "🎨", fontSize = 26.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.width} × ${project.height} px • ${project.layers.size} capas",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiElectricBlue
                )
                Text(
                    text = "Editado $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = YupiTextTertiary
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = YupiTextSecondary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(YupiSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicar", color = YupiTextPrimary) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = YupiElectricBlue) },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = YupiPink) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = YupiPink) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportedItemCard(
    item: ExportedItem,
    onPreview: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, YupiBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onPreview),
        colors = CardDefaults.cardColors(containerColor = YupiSurface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(YupiSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(item.filePath),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Format badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.format,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = YupiElectricBlue
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = YupiTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${item.width}×${item.height}",
                        style = MaterialTheme.typography.labelSmall,
                        color = YupiTextTertiary
                    )
                }
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = YupiElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CanvasPresetsDialog(
    presets: List<CanvasPreset>,
    onDismiss: () -> Unit,
    onSelectPreset: (CanvasPreset) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = YupiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Seleccionar Tamaño del Lienzo",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectPreset(preset) },
                            colors = CardDefaults.cardColors(containerColor = YupiSurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = YupiTextPrimary
                                    )
                                    Text(
                                        text = preset.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = YupiTextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(YupiPurpleDark)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${preset.width}×${preset.height}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = YupiElectricBlue
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

@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var defaultFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var defaultQuality by remember { mutableIntStateOf(95) }

    var selectedProviderForToken by remember { mutableStateOf(AiProvider.GEMINI) }
    var tokenInput by remember { mutableStateOf(AiTokenService.getToken(context, AiProvider.GEMINI)) }
    var tokenSavedMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = YupiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Configuración de YupiForge",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MontserratFamily,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Section: AI Tokens / API Keys
                Text(
                    text = "🔑 Tokens de Inteligencia Artificial",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = YupiElectricBlue
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Configura tus claves para generar con ChatGPT, Gemini o NVIDIA:",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AiProvider.values().filter { it != AiProvider.LOCAL_NEURAL }.forEach { provider ->
                        val isSel = selectedProviderForToken == provider
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedProviderForToken = provider
                                tokenInput = AiTokenService.getToken(context, provider)
                                tokenSavedMessage = null
                            },
                            label = { Text("${provider.icon} ${provider.displayName.substringBefore(' ')}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YupiElectricBlue,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Token de ${selectedProviderForToken.displayName}") },
                    placeholder = { Text(selectedProviderForToken.tokenHint, color = YupiTextTertiary) },
                    modifier = Modifier.fillMaxWidth().testTag("settings_token_input"),
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
                    if (tokenSavedMessage != null) {
                        Text(tokenSavedMessage!!, fontSize = 11.sp, color = Color(0xFF10B981))
                    } else {
                        val currentToken = AiTokenService.getToken(context, selectedProviderForToken)
                        Text(
                            text = if (currentToken.isNotBlank()) "Guardado: ${currentToken.take(4)}...${currentToken.takeLast(4)}" else "Sin token guardado",
                            fontSize = 11.sp,
                            color = if (currentToken.isNotBlank()) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Button(
                        onClick = {
                            AiTokenService.saveToken(context, selectedProviderForToken, tokenInput)
                            tokenSavedMessage = "¡Guardado!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YupiPurpleLight)
                    ) {
                        Text("Guardar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Formato de exportación predeterminado",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.values().forEach { fmt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = defaultFormat == fmt,
                                onClick = { defaultFormat = fmt },
                                colors = RadioButtonDefaults.colors(selectedColor = YupiElectricBlue)
                            )
                            Text(text = fmt.name, color = YupiTextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Calidad predeterminada: $defaultQuality%",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiTextSecondary
                )
                Slider(
                    value = defaultQuality.toFloat(),
                    onValueChange = { defaultQuality = it.toInt() },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(thumbColor = YupiElectricBlue, activeTrackColor = YupiPurpleLight)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = YupiSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "YupiForge Studio Pro v1.0",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = YupiElectricBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Editor nativo de imágenes con edición por capas, ajustes fotográficos de alta precisión, formas estirables y generadores de IA (ChatGPT, Gemini, NVIDIA, Local).",
                            style = MaterialTheme.typography.bodySmall,
                            color = YupiTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_settings_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue)
                ) {
                    Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FullscreenPreviewDialog(
    item: ExportedItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = YupiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = YupiTextPrimary
                        )
                        Text(
                            text = "${item.width} × ${item.height} px • ${item.format}",
                            style = MaterialTheme.typography.bodySmall,
                            color = YupiElectricBlue
                        )
                    }
                    Button(
                        onClick = onShare,
                        colors = ButtonDefaults.buttonColors(containerColor = YupiElectricBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(item.filePath),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver", color = YupiTextSecondary)
                }
            }
        }
    }
}
