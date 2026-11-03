package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import com.example.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AiProvider
import com.example.model.LayerType
import com.example.model.Project
import com.example.ui.theme.YupiBackground
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val selectedLayerId by viewModel.selectedLayerId.collectAsState()
    val isLayersSheetOpen by viewModel.isLayersSheetOpen.collectAsState()
    val isExportDialogOpen by viewModel.isExportDialogOpen.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingMessage by viewModel.processingMessage.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    val adjustments by viewModel.currentAdjustments.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val filterIntensity by viewModel.filterIntensity.collectAsState()

    val brushColor by viewModel.brushColor.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()
    val brushOpacity by viewModel.brushOpacity.collectAsState()
    val isEraser by viewModel.isEraser.collectAsState()

    val detectedObjects by viewModel.detectedObjects.collectAsState()
    val inpaintBox by viewModel.inpaintBox.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showRenameDialog by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showAddImageDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        val count = (project?.layers?.count { it.type == LayerType.IMAGE } ?: 0) + 1
                        viewModel.addImageLayer(bitmap, "Imagen $count")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Intercept back button: close panel/sheet first, or return to home
    BackHandler {
        if (isLayersSheetOpen) {
            viewModel.toggleLayersSheet()
        } else if (activeTab != EditorToolTab.NONE) {
            viewModel.setActiveTab(EditorToolTab.NONE)
        } else {
            viewModel.saveProject()
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EditorUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditorUiEvent.ExportComplete -> {
                    // Export finished
                }
            }
        }
    }

    if (project == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(YupiBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = YupiElectricBlue)
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showRenameDialog = true }
                    ) {
                        Text(
                            text = project!!.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = YupiTextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✏️",
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveProject()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("back_to_home_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Inicio",
                            tint = YupiTextPrimary
                        )
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Deshacer",
                            tint = if (canUndo) YupiElectricBlue else YupiTextTertiary
                        )
                    }
                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Rehacer",
                            tint = if (canRedo) YupiElectricBlue else YupiTextTertiary
                        )
                    }
                    // Add Image
                    IconButton(
                        onClick = { showAddImageDialog = true },
                        modifier = Modifier.testTag("add_image_top_btn")
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Añadir Imagen",
                            tint = YupiElectricBlue
                        )
                    }
                    // Layers sheet toggle
                    IconButton(
                        onClick = { viewModel.toggleLayersSheet() },
                        modifier = Modifier.testTag("toggle_layers_btn")
                    ) {
                        Box {
                            Icon(
                                Icons.Default.Layers,
                                contentDescription = "Capas",
                                tint = if (isLayersSheetOpen) YupiElectricBlue else YupiTextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(YupiPurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${project!!.layers.size}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                    // Export
                    IconButton(
                        onClick = { viewModel.toggleExportDialog(true) },
                        modifier = Modifier.testTag("export_menu_btn")
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Exportar",
                            tint = YupiElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YupiSurface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YupiSurface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Active tool sub-panel
                AnimatedVisibility(
                    visible = activeTab != EditorToolTab.NONE,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    when (activeTab) {
                        EditorToolTab.ADJUSTMENTS -> {
                            AdjustmentsPanel(
                                adjustments = adjustments,
                                onAdjustmentsChanged = { viewModel.updateAdjustments(it) },
                                onApply = { viewModel.applyAdjustmentsToActiveLayer() },
                                onReset = { viewModel.resetAdjustments() },
                                onClose = { viewModel.setActiveTab(EditorToolTab.NONE) }
                            )
                        }
                        EditorToolTab.FILTERS -> {
                            FiltersPanel(
                                activeFilter = activeFilter,
                                intensity = filterIntensity,
                                onFilterSelected = { viewModel.setFilter(it) },
                                onIntensityChanged = { viewModel.setFilterIntensity(it) },
                                onApply = { viewModel.applyFilterToActiveLayer() },
                                onClose = { viewModel.setActiveTab(EditorToolTab.NONE) }
                            )
                        }
                        EditorToolTab.TRANSFORM -> {
                            TransformPanel(
                                onRotate90 = { viewModel.rotateActiveLayer(90f) },
                                onFlipH = { viewModel.flipActiveLayer(true) },
                                onFlipV = { viewModel.flipActiveLayer(false) },
                                onCropPreset = { l, t, r, b ->
                                    viewModel.cropActiveLayer(android.graphics.RectF(l, t, r, b))
                                },
                                onClose = { viewModel.setActiveTab(EditorToolTab.NONE) }
                            )
                        }
                        EditorToolTab.AI_TOOLS -> {
                            AiToolsPanel(
                                onRemoveBackground = { viewModel.runAiRemoveBackground() },
                                onRemoveObject = { viewModel.runAiRemoveObject() },
                                onAutoEnhance = { viewModel.runAiAutoEnhance() },
                                onSuperResolution = { viewModel.runAiSuperResolution(it) },
                                onDetectObjects = { viewModel.runAiDetectObjects() },
                                onReplaceBackground = { viewModel.runAiReplaceBackground(it) },
                                onTextToImage = { prompt, style, provider, model ->
                                    viewModel.runAiTextToImage(prompt, style, provider, model)
                                },
                                onSaveToken = { provider, token ->
                                    viewModel.saveAiToken(provider, token)
                                },
                                onGetToken = { provider ->
                                    viewModel.getAiToken(provider)
                                },
                                onClose = { viewModel.setActiveTab(EditorToolTab.NONE) }
                            )
                        }
                        EditorToolTab.DRAW -> {
                            DrawingPanel(
                                brushColor = brushColor,
                                brushSize = brushSize,
                                brushOpacity = brushOpacity,
                                isEraser = isEraser,
                                onColorChanged = { viewModel.setBrushColor(it) },
                                onSizeChanged = { viewModel.setBrushSize(it) },
                                onOpacityChanged = { viewModel.setBrushOpacity(it) },
                                onToggleEraser = { viewModel.toggleEraser(it) },
                                onClearStrokes = { viewModel.clearDrawingStrokes() },
                                onClose = { viewModel.setActiveTab(EditorToolTab.NONE) }
                            )
                        }
                        EditorToolTab.TEXT -> {
                            // Handled by dialog trigger below
                        }
                        EditorToolTab.SHAPES -> {
                            val activeLayer = project?.layers?.find { it.id == selectedLayerId }
                            val activeShapeData = if (activeLayer?.type == LayerType.SHAPE) activeLayer.shapeData else null
                            ShapesAndStickersPanel(
                                activeShapeData = activeShapeData,
                                onAddShape = { viewModel.addShapeLayer(it) },
                                onUpdateActiveShapeDimensions = { w, h, cr ->
                                    viewModel.updateActiveShapeDimensions(w, h, cr)
                                },
                                onUpdateActiveShapeColors = { fill, stroke, strokeW ->
                                    viewModel.updateActiveShapeColors(fill, stroke, strokeW)
                                },
                                onAddSticker = { viewModel.addStickerLayer(it) },
                                onClose = { viewModel.setActiveTab(EditorToolTab.NONE) }
                            )
                        }
                        else -> {}
                    }
                }

                // Primary Bottom Navigation Bar with Tools Categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(YupiSurface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorToolItem(
                        icon = Icons.Default.Tune,
                        label = "Ajustes",
                        isSelected = activeTab == EditorToolTab.ADJUSTMENTS,
                        onClick = { viewModel.setActiveTab(EditorToolTab.ADJUSTMENTS) }
                    )
                    EditorToolItem(
                        icon = Icons.Default.ColorLens,
                        label = "Filtros",
                        isSelected = activeTab == EditorToolTab.FILTERS,
                        onClick = { viewModel.setActiveTab(EditorToolTab.FILTERS) }
                    )
                    EditorToolItem(
                        icon = Icons.Default.AddPhotoAlternate,
                        label = "+ Imagen",
                        isSelected = false,
                        onClick = { showAddImageDialog = true }
                    )
                    EditorToolItem(
                        icon = Icons.Default.Crop,
                        label = "Recortar",
                        isSelected = activeTab == EditorToolTab.TRANSFORM,
                        onClick = { viewModel.setActiveTab(EditorToolTab.TRANSFORM) }
                    )
                    EditorToolItem(
                        icon = Icons.Default.AutoAwesome,
                        label = "IA Smart",
                        isSelected = activeTab == EditorToolTab.AI_TOOLS,
                        badgeText = "PRO",
                        onClick = { viewModel.setActiveTab(EditorToolTab.AI_TOOLS) }
                    )
                    EditorToolItem(
                        icon = Icons.Default.Brush,
                        label = "Dibujar",
                        isSelected = activeTab == EditorToolTab.DRAW,
                        onClick = { viewModel.setActiveTab(EditorToolTab.DRAW) }
                    )
                    EditorToolItem(
                        icon = Icons.Default.TextFields,
                        label = "Texto",
                        isSelected = false,
                        onClick = { showAddTextDialog = true }
                    )
                    EditorToolItem(
                        icon = Icons.Default.Category,
                        label = "Formas",
                        isSelected = activeTab == EditorToolTab.SHAPES,
                        onClick = { viewModel.setActiveTab(EditorToolTab.SHAPES) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(YupiBackground)
        ) {
            // Main Canvas
            EditorCanvas(
                project = project!!,
                selectedLayerId = selectedLayerId,
                activeTab = activeTab,
                isEraser = isEraser,
                detectedObjects = detectedObjects,
                inpaintBox = inpaintBox,
                onSelectLayer = { viewModel.selectLayer(it) },
                onLayerTransform = { tx, ty, s, r ->
                    viewModel.updateSelectedLayer {
                        it.copy(
                            translationX = it.translationX + tx,
                            translationY = it.translationY + ty
                        )
                    }
                },
                onDrawingPoint = { viewModel.addDrawingPoint(it) },
                onFinishStroke = { viewModel.finishDrawingStroke() },
                onInpaintBoxChanged = { viewModel.setInpaintBox(it) }
            )

            // Canvas size and layer pill badge at top center
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = YupiSurfaceVariant.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${project!!.width} × ${project!!.height} px",
                        style = MaterialTheme.typography.labelSmall,
                        color = YupiTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        color = YupiTextTertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${project!!.layers.size} capas",
                        style = MaterialTheme.typography.labelSmall,
                        color = YupiElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Processing Overlay (for AI and heavy filters)
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xBB000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = YupiSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = YupiElectricBlue,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = processingMessage.ifEmpty { "Procesando..." },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = YupiTextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Layers Sheet Modal
            if (isLayersSheetOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000))
                        .clickable { viewModel.toggleLayersSheet() }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .clickable(enabled = false) {}
                    ) {
                        LayersSheet(
                            layers = project!!.layers,
                            selectedLayerId = selectedLayerId,
                            onSelectLayer = { viewModel.selectLayer(it) },
                            onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                            onToggleLock = { viewModel.toggleLayerLock(it) },
                            onReorder = { id, up -> viewModel.reorderLayer(id, up) },
                            onSetOpacity = { id, op -> viewModel.setLayerOpacity(id, op) },
                            onSetBlendMode = { id, bm -> viewModel.setLayerBlendMode(id, bm) },
                            onDuplicate = { viewModel.duplicateLayer(it) },
                            onDelete = { viewModel.deleteLayer(it) },
                            onAddImageClick = {
                                viewModel.toggleLayersSheet()
                                showAddImageDialog = true
                            },
                            onClose = { viewModel.toggleLayersSheet() }
                        )
                    }
                }
            }

            // Export Dialog
            if (isExportDialogOpen) {
                ExportDialog(
                    projectWidth = project!!.width,
                    projectHeight = project!!.height,
                    onDismiss = { viewModel.toggleExportDialog(false) },
                    onExport = { options -> viewModel.exportFinalImage(options) }
                )
            }

            // Add Text Dialog
            if (showAddTextDialog) {
                AddTextDialog(
                    onDismiss = { showAddTextDialog = false },
                    onConfirm = { textData ->
                        viewModel.addTextLayer(textData)
                        showAddTextDialog = false
                    }
                )
            }

            // Rename Project Dialog
            if (showRenameDialog) {
                RenameDialog(
                    currentName = project!!.name,
                    onDismiss = { showRenameDialog = false },
                    onRename = { newName ->
                        viewModel.renameProject(newName)
                        showRenameDialog = false
                    }
                )
            }

            // Add Image Dialog
            if (showAddImageDialog) {
                AddImageDialog(
                    onDismiss = { showAddImageDialog = false },
                    onPickFromGallery = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddSampleCyberpunk = {
                        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.cyberpunk_portrait_1793746376820)
                        if (bmp != null) {
                            val count = (project?.layers?.count { it.type == LayerType.IMAGE } ?: 0) + 1
                            viewModel.addImageLayer(bmp, "Cyberpunk $count")
                        }
                    },
                    onAddSampleSneaker = {
                        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.sneaker_product_1793746388279)
                        if (bmp != null) {
                            val count = (project?.layers?.count { it.type == LayerType.IMAGE } ?: 0) + 1
                            viewModel.addImageLayer(bmp, "Sneaker $count")
                        }
                    },
                    onOpenAiGenerator = {
                        viewModel.setActiveTab(EditorToolTab.AI_TOOLS)
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorToolItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) YupiSurfaceElevated else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) YupiElectricBlue else YupiTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(YupiPink)
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) YupiElectricBlue else YupiTextSecondary
        )
    }
}

@Composable
private fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = YupiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Renombrar Proyecto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YupiElectricBlue,
                        focusedTextColor = YupiTextPrimary,
                        unfocusedTextColor = YupiTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = YupiTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onRename(name) }) {
                        Text("Guardar", color = YupiElectricBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddImageDialog(
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onAddSampleCyberpunk: () -> Unit,
    onAddSampleSneaker: () -> Unit,
    onOpenAiGenerator: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = YupiSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, YupiBorder),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Añadir Imagen al Proyecto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = YupiTextPrimary
                )
                Text(
                    text = "Selecciona una imagen desde tu dispositivo o agrega recursos de muestra",
                    style = MaterialTheme.typography.bodySmall,
                    color = YupiTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Gallery
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onPickFromGallery()
                        },
                    colors = CardDefaults.cardColors(containerColor = YupiSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(YupiElectricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Elegir de la Galería",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = YupiTextPrimary
                            )
                            Text(
                                text = "Abre tus fotos y añade cualquier imagen como capa",
                                style = MaterialTheme.typography.bodySmall,
                                color = YupiTextTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Sample Cyberpunk
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onAddSampleCyberpunk()
                        },
                    colors = CardDefaults.cardColors(containerColor = YupiSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Muestra: Retrato Cyberpunk", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = YupiTextPrimary)
                            Text("Fotografía de estudio con neón", style = MaterialTheme.typography.bodySmall, color = YupiTextTertiary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Sample Sneaker
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onAddSampleSneaker()
                        },
                    colors = CardDefaults.cardColors(containerColor = YupiSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👟", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Muestra: Zapatilla Deportiva", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = YupiTextPrimary)
                            Text("Fotografía de producto con iluminación pro", style = MaterialTheme.typography.bodySmall, color = YupiTextTertiary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 4: AI Generation
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onOpenAiGenerator()
                        },
                    colors = CardDefaults.cardColors(containerColor = YupiSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Generar Imagen con IA", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = YupiPurpleLight)
                            Text("Crea una imagen nueva a partir de un texto", style = MaterialTheme.typography.bodySmall, color = YupiTextTertiary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = YupiTextSecondary)
                    }
                }
            }
        }
    }
}
