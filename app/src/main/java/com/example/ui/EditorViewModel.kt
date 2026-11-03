package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiTokenService
import com.example.engine.ImageEngine
import com.example.model.AiProvider
import com.example.model.BlendModeType
import com.example.model.DetectedObject
import com.example.model.DrawingPoint
import com.example.model.DrawingStroke
import com.example.model.ExportOptions
import com.example.model.FilterType
import com.example.model.ImageAdjustments
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.Project
import com.example.model.ShapeData
import com.example.model.ShapeType
import com.example.model.StickerData
import com.example.model.TextData
import com.example.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class EditorToolTab {
    NONE,
    ADJUSTMENTS,
    FILTERS,
    TRANSFORM,
    AI_TOOLS,
    DRAW,
    TEXT,
    SHAPES,
    STICKERS
}

sealed interface EditorUiEvent {
    data class ShowMessage(val message: String) : EditorUiEvent
    data class ExportComplete(val uri: Uri) : EditorUiEvent
}

class EditorViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorToolTab.NONE)
    val activeTab: StateFlow<EditorToolTab> = _activeTab.asStateFlow()

    private val _selectedLayerId = MutableStateFlow<String?>(null)
    val selectedLayerId: StateFlow<String?> = _selectedLayerId.asStateFlow()

    private val _isLayersSheetOpen = MutableStateFlow(false)
    val isLayersSheetOpen: StateFlow<Boolean> = _isLayersSheetOpen.asStateFlow()

    private val _isExportDialogOpen = MutableStateFlow(false)
    val isExportDialogOpen: StateFlow<Boolean> = _isExportDialogOpen.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingMessage = MutableStateFlow("")
    val processingMessage: StateFlow<String> = _processingMessage.asStateFlow()

    private val _events = MutableSharedFlow<EditorUiEvent>()
    val events: SharedFlow<EditorUiEvent> = _events.asSharedFlow()

    // Undo / Redo history stacks
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Adjustments working state
    private val _currentAdjustments = MutableStateFlow(ImageAdjustments())
    val currentAdjustments: StateFlow<ImageAdjustments> = _currentAdjustments.asStateFlow()

    // Filter working state
    private val _activeFilter = MutableStateFlow(FilterType.NONE)
    val activeFilter: StateFlow<FilterType> = _activeFilter.asStateFlow()

    private val _filterIntensity = MutableStateFlow(0.85f)
    val filterIntensity: StateFlow<Float> = _filterIntensity.asStateFlow()

    // Drawing state
    private val _brushColor = MutableStateFlow(0xFF00E5FF)
    val brushColor: StateFlow<Long> = _brushColor.asStateFlow()

    private val _brushSize = MutableStateFlow(16f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    private val _brushOpacity = MutableStateFlow(1.0f)
    val brushOpacity: StateFlow<Float> = _brushOpacity.asStateFlow()

    private val _isEraser = MutableStateFlow(false)
    val isEraser: StateFlow<Boolean> = _isEraser.asStateFlow()

    private val _currentStrokePoints = MutableStateFlow<List<DrawingPoint>>(emptyList())
    val currentStrokePoints: StateFlow<List<DrawingPoint>> = _currentStrokePoints.asStateFlow()

    // AI Object Detection overlay state
    private val _detectedObjects = MutableStateFlow<List<DetectedObject>>(emptyList())
    val detectedObjects: StateFlow<List<DetectedObject>> = _detectedObjects.asStateFlow()

    // Inpainting target selection box
    private val _inpaintBox = MutableStateFlow<RectF?>(null)
    val inpaintBox: StateFlow<RectF?> = _inpaintBox.asStateFlow()

    fun loadProject(projectToLoad: Project) {
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _project.value = projectToLoad
        _selectedLayerId.value = projectToLoad.layers.firstOrNull()?.id
        _detectedObjects.value = emptyList()
        _inpaintBox.value = null
    }

    fun createNewProject(name: String, width: Int, height: Int, initialBitmap: Bitmap? = null) {
        val layers = mutableListOf<Layer>()
        if (initialBitmap != null) {
            layers.add(
                Layer(
                    id = "layer_img_${UUID.randomUUID()}",
                    name = "Imagen Base",
                    type = LayerType.IMAGE,
                    bitmap = initialBitmap
                )
            )
        }
        val newProj = Project(
            name = name,
            width = width,
            height = height,
            layers = layers
        )
        loadProject(newProj)
        saveProject()
    }

    fun setActiveTab(tab: EditorToolTab) {
        _activeTab.value = if (_activeTab.value == tab) EditorToolTab.NONE else tab
        if (tab != EditorToolTab.AI_TOOLS) {
            _detectedObjects.value = emptyList()
            _inpaintBox.value = null
        }
    }

    fun toggleLayersSheet() {
        _isLayersSheetOpen.value = !_isLayersSheetOpen.value
    }

    fun toggleExportDialog(open: Boolean) {
        _isExportDialogOpen.value = open
    }

    fun selectLayer(id: String?) {
        _selectedLayerId.value = id
    }

    private fun pushSnapshot() {
        _project.value?.let { current ->
            undoStack.add(current)
            if (undoStack.size > 25) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
            _canUndo.value = true
            _canRedo.value = false
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _project.value ?: return
        redoStack.add(current)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        _project.value = previous
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = true
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _project.value ?: return
        undoStack.add(current)
        val next = redoStack.removeAt(redoStack.lastIndex)
        _project.value = next
        _canUndo.value = true
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun saveProject() {
        val proj = _project.value ?: return
        viewModelScope.launch {
            repository.saveProject(proj)
        }
    }

    fun renameProject(newName: String) {
        val current = _project.value ?: return
        if (newName.isBlank()) return
        pushSnapshot()
        _project.value = current.copy(name = newName)
        saveProject()
    }

    // --- Layers Manipulation ---

    fun addImageLayer(bitmap: Bitmap, name: String = "Nueva Imagen") {
        val current = _project.value ?: return
        pushSnapshot()
        val newLayer = Layer(
            name = name,
            type = LayerType.IMAGE,
            bitmap = bitmap
        )
        _project.value = current.copy(layers = current.layers + newLayer)
        _selectedLayerId.value = newLayer.id
        saveProject()
    }

    fun addTextLayer(textData: TextData) {
        val current = _project.value ?: return
        pushSnapshot()
        val newLayer = Layer(
            name = "Texto: ${textData.text.take(12)}",
            type = LayerType.TEXT,
            textData = textData
        )
        _project.value = current.copy(layers = current.layers + newLayer)
        _selectedLayerId.value = newLayer.id
        saveProject()
    }

    fun addShapeLayer(shapeData: ShapeData) {
        val current = _project.value ?: return
        pushSnapshot()
        val newLayer = Layer(
            name = "Forma: ${shapeData.shapeType.name.lowercase().replace('_', ' ')}",
            type = LayerType.SHAPE,
            shapeData = shapeData
        )
        _project.value = current.copy(layers = current.layers + newLayer)
        _selectedLayerId.value = newLayer.id
        saveProject()
    }

    fun addStickerLayer(emoji: String) {
        val current = _project.value ?: return
        pushSnapshot()
        val newLayer = Layer(
            name = "Sticker $emoji",
            type = LayerType.STICKER,
            stickerData = StickerData(emoji = emoji)
        )
        _project.value = current.copy(layers = current.layers + newLayer)
        _selectedLayerId.value = newLayer.id
        saveProject()
    }

    fun updateSelectedLayer(update: (Layer) -> Layer) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        pushSnapshot()
        val updatedLayers = current.layers.map { layer ->
            if (layer.id == selId) update(layer) else layer
        }
        _project.value = current.copy(layers = updatedLayers)
        saveProject()
    }

    fun reorderLayer(layerId: String, moveUp: Boolean) {
        val current = _project.value ?: return
        val list = current.layers.toMutableList()
        val index = list.indexOfFirst { it.id == layerId }
        if (index == -1) return
        val targetIndex = if (moveUp) index + 1 else index - 1
        if (targetIndex in list.indices) {
            pushSnapshot()
            val item = list.removeAt(index)
            list.add(targetIndex, item)
            _project.value = current.copy(layers = list)
            saveProject()
        }
    }

    fun toggleLayerVisibility(layerId: String) {
        val current = _project.value ?: return
        val updated = current.layers.map {
            if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
        }
        _project.value = current.copy(layers = updated)
    }

    fun toggleLayerLock(layerId: String) {
        val current = _project.value ?: return
        val updated = current.layers.map {
            if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
        }
        _project.value = current.copy(layers = updated)
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        val current = _project.value ?: return
        val updated = current.layers.map {
            if (it.id == layerId) it.copy(opacity = opacity.coerceIn(0f, 1f)) else it
        }
        _project.value = current.copy(layers = updated)
    }

    fun setLayerBlendMode(layerId: String, blendMode: BlendModeType) {
        val current = _project.value ?: return
        pushSnapshot()
        val updated = current.layers.map {
            if (it.id == layerId) it.copy(blendMode = blendMode) else it
        }
        _project.value = current.copy(layers = updated)
        saveProject()
    }

    fun duplicateLayer(layerId: String) {
        val current = _project.value ?: return
        val layer = current.layers.find { it.id == layerId } ?: return
        pushSnapshot()
        val dup = layer.copy(
            id = UUID.randomUUID().toString(),
            name = "${layer.name} (Copia)",
            translationX = layer.translationX + 30f,
            translationY = layer.translationY + 30f,
            bitmap = layer.bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        )
        _project.value = current.copy(layers = current.layers + dup)
        _selectedLayerId.value = dup.id
        saveProject()
    }

    fun deleteLayer(layerId: String) {
        val current = _project.value ?: return
        pushSnapshot()
        val updated = current.layers.filter { it.id != layerId }
        _project.value = current.copy(layers = updated)
        if (_selectedLayerId.value == layerId) {
            _selectedLayerId.value = updated.lastOrNull()?.id
        }
        saveProject()
    }

    // --- Adjustments ---

    fun updateAdjustments(adjustments: ImageAdjustments) {
        _currentAdjustments.value = adjustments
    }

    fun applyAdjustmentsToActiveLayer() {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return
        val adj = _currentAdjustments.value
        if (adj.isDefault) return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Aplicando ajustes..."
            val updatedBitmap = withContext(Dispatchers.Default) {
                ImageEngine.applyAdjustments(layer.bitmap!!, adj)
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = updatedBitmap) else it
            }
            _project.value = current.copy(layers = layers)
            _currentAdjustments.value = ImageAdjustments()
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("Ajustes aplicados correctamente"))
        }
    }

    fun resetAdjustments() {
        _currentAdjustments.value = ImageAdjustments()
    }

    // --- Filters ---

    fun setFilter(filter: FilterType) {
        _activeFilter.value = filter
    }

    fun setFilterIntensity(intensity: Float) {
        _filterIntensity.value = intensity
    }

    fun applyFilterToActiveLayer() {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return
        val filter = _activeFilter.value
        if (filter == FilterType.NONE) return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Procesando filtro ${filter.displayName}..."
            val updatedBitmap = withContext(Dispatchers.Default) {
                ImageEngine.applyFilter(layer.bitmap!!, filter, _filterIntensity.value)
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = updatedBitmap) else it
            }
            _project.value = current.copy(layers = layers)
            _activeFilter.value = FilterType.NONE
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("Filtro ${filter.displayName} aplicado"))
        }
    }

    // --- Transform (Rotate, Flip, Crop, Resize) ---

    fun rotateActiveLayer(degrees: Float) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Rotando imagen..."
            val rotated = withContext(Dispatchers.Default) {
                ImageEngine.transformBitmap(layer.bitmap!!, rotationDegrees = degrees)
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = rotated) else it
            }
            _project.value = current.copy(layers = layers)
            _isProcessing.value = false
            saveProject()
        }
    }

    fun flipActiveLayer(horizontal: Boolean) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            val flipped = withContext(Dispatchers.Default) {
                ImageEngine.transformBitmap(
                    layer.bitmap!!,
                    flipH = horizontal,
                    flipV = !horizontal
                )
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = flipped) else it
            }
            _project.value = current.copy(layers = layers)
            saveProject()
        }
    }

    fun cropActiveLayer(cropRect: RectF) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Recortando..."
            val cropped = withContext(Dispatchers.Default) {
                ImageEngine.transformBitmap(layer.bitmap!!, cropRect = cropRect)
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = cropped) else it
            }
            _project.value = current.copy(layers = layers)
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("Recorte aplicado"))
        }
    }

    fun resizeProject(targetW: Int, targetH: Int) {
        val current = _project.value ?: return
        pushSnapshot()
        _project.value = current.copy(width = targetW, height = targetH)
        saveProject()
    }

    // --- Drawing Tool ---

    fun setBrushColor(color: Long) {
        _brushColor.value = color
    }

    fun setBrushSize(size: Float) {
        _brushSize.value = size
    }

    fun setBrushOpacity(opacity: Float) {
        _brushOpacity.value = opacity
    }

    fun toggleEraser(isEraser: Boolean) {
        _isEraser.value = isEraser
    }

    fun addDrawingPoint(point: DrawingPoint) {
        _currentStrokePoints.value = _currentStrokePoints.value + point
    }

    fun finishDrawingStroke() {
        val points = _currentStrokePoints.value
        if (points.size < 2) {
            _currentStrokePoints.value = emptyList()
            return
        }

        val stroke = DrawingStroke(
            points = points,
            color = _brushColor.value,
            strokeWidth = _brushSize.value,
            alpha = _brushOpacity.value,
            isEraser = _isEraser.value
        )
        _currentStrokePoints.value = emptyList()

        val current = _project.value ?: return
        pushSnapshot()

        // Find or create drawing layer
        val drawLayerIndex = current.layers.indexOfFirst { it.type == LayerType.DRAWING }
        val updatedLayers = current.layers.toMutableList()
        if (drawLayerIndex != -1) {
            val l = updatedLayers[drawLayerIndex]
            updatedLayers[drawLayerIndex] = l.copy(strokes = l.strokes + stroke)
        } else {
            val newDrawLayer = Layer(
                name = "Capa de Dibujo",
                type = LayerType.DRAWING,
                strokes = listOf(stroke)
            )
            updatedLayers.add(newDrawLayer)
            _selectedLayerId.value = newDrawLayer.id
        }
        _project.value = current.copy(layers = updatedLayers)
        saveProject()
    }

    fun clearDrawingStrokes() {
        val current = _project.value ?: return
        val drawLayer = current.layers.find { it.type == LayerType.DRAWING } ?: return
        pushSnapshot()
        val updated = current.layers.map {
            if (it.id == drawLayer.id) it.copy(strokes = emptyList()) else it
        }
        _project.value = current.copy(layers = updated)
        saveProject()
    }

    // --- AI Smart Tools ---

    fun runAiRemoveBackground() {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "IA: Eliminando fondo de la imagen..."
            val cutout = withContext(Dispatchers.Default) {
                ImageEngine.removeBackgroundSmart(layer.bitmap!!)
            }
            pushSnapshot()
            val newLayer = Layer(
                name = "${layer.name} (Sin Fondo)",
                type = LayerType.IMAGE,
                bitmap = cutout
            )
            _project.value = current.copy(layers = current.layers + newLayer)
            _selectedLayerId.value = newLayer.id
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("¡Fondo eliminado con éxito! Capa transparente creada"))
        }
    }

    fun setInpaintBox(box: RectF?) {
        _inpaintBox.value = box
    }

    fun runAiRemoveObject() {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return
        val box = _inpaintBox.value ?: RectF(0.35f, 0.40f, 0.65f, 0.65f)

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "IA: Eliminando objeto mediante síntesis..."
            val inpainted = withContext(Dispatchers.Default) {
                ImageEngine.removeObjectInpaint(layer.bitmap!!, box)
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = inpainted) else it
            }
            _project.value = current.copy(layers = layers)
            _inpaintBox.value = null
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("Objeto eliminado y textura sintetizada"))
        }
    }

    fun runAiAutoEnhance() {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "IA: Mejorando nitidez, contraste y color..."
            val enhanced = withContext(Dispatchers.Default) {
                ImageEngine.autoEnhanceQuality(layer.bitmap!!)
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = enhanced) else it
            }
            _project.value = current.copy(layers = layers)
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("¡Calidad mejorada con IA!"))
        }
    }

    fun runAiSuperResolution(scaleFactor: Float = 2.0f) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "IA: Aumentando resolución a ${scaleFactor.toInt()}X..."
            val upscaled = withContext(Dispatchers.Default) {
                ImageEngine.superResolutionUpscale(layer.bitmap!!, scaleFactor)
            }
            pushSnapshot()
            val layers = current.layers.map {
                if (it.id == selId) it.copy(bitmap = upscaled) else it
            }
            _project.value = current.copy(layers = layers)
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("Resolución incrementada a ${upscaled.width}x${upscaled.height} px"))
        }
    }

    fun runAiDetectObjects() {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "IA: Analizando y detectando objetos..."
            val detected = withContext(Dispatchers.Default) {
                ImageEngine.detectObjectsInImage(layer.bitmap!!)
            }
            _detectedObjects.value = detected
            _isProcessing.value = false
            _events.emit(EditorUiEvent.ShowMessage("Se detectaron ${detected.size} áreas principales"))
        }
    }

    fun clearDetectedObjects() {
        _detectedObjects.value = emptyList()
    }

    fun runAiReplaceBackground(sceneType: String) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.IMAGE && it.bitmap != null } ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "IA: Generando y sustituyendo fondo..."
            val replaced = withContext(Dispatchers.Default) {
                // First isolate subject if not already transparent
                val subject = ImageEngine.removeBackgroundSmart(layer.bitmap!!)
                ImageEngine.replaceBackgroundWithScene(subject, sceneType)
            }
            pushSnapshot()
            val newLayer = Layer(
                name = "${layer.name} ($sceneType)",
                type = LayerType.IMAGE,
                bitmap = replaced
            )
            _project.value = current.copy(layers = current.layers + newLayer)
            _selectedLayerId.value = newLayer.id
            _isProcessing.value = false
            saveProject()
            _events.emit(EditorUiEvent.ShowMessage("Nuevo fondo $sceneType aplicado"))
        }
    }

    fun runAiTextToImage(
        prompt: String,
        style: String,
        provider: AiProvider = AiProvider.LOCAL_NEURAL,
        model: String = ""
    ) {
        if (prompt.isBlank()) return
        val current = _project.value ?: return

        viewModelScope.launch {
            _isProcessing.value = true
            val effectiveModel = if (model.isNotBlank()) model else (provider.models.firstOrNull() ?: "")
            _processingMessage.value = "IA (${provider.displayName}): Generando con '$effectiveModel'..."
            try {
                val generated = withContext(Dispatchers.IO) {
                    AiTokenService.generateImage(
                        context = repository.context,
                        provider = provider,
                        model = effectiveModel,
                        prompt = prompt,
                        style = style,
                        targetWidth = current.width,
                        targetHeight = current.height
                    )
                }
                pushSnapshot()
                val providerLabel = when (provider) {
                    AiProvider.GEMINI -> "Gemini"
                    AiProvider.OPENAI -> "ChatGPT"
                    AiProvider.NVIDIA -> "NVIDIA"
                    AiProvider.LOCAL_NEURAL -> "Local"
                }
                val newLayer = Layer(
                    name = "IA ($providerLabel): $prompt",
                    type = LayerType.IMAGE,
                    bitmap = generated
                )
                _project.value = current.copy(layers = current.layers + newLayer)
                _selectedLayerId.value = newLayer.id
                _events.emit(EditorUiEvent.ShowMessage("¡Imagen generada con éxito con ${provider.displayName}!"))
                saveProject()
            } catch (e: Exception) {
                _events.emit(EditorUiEvent.ShowMessage("Error IA: ${e.message ?: "Fallo al generar imagen"}"))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun saveAiToken(provider: AiProvider, token: String) {
        AiTokenService.saveToken(repository.context, provider, token)
    }

    fun getAiToken(provider: AiProvider): String {
        return AiTokenService.getToken(repository.context, provider)
    }

    fun updateActiveShapeDimensions(width: Float, height: Float, cornerRadius: Float) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.SHAPE } ?: return

        pushSnapshot()
        val updated = current.layers.map {
            if (it.id == selId) {
                it.copy(shapeData = it.shapeData.copy(width = width, height = height, cornerRadius = cornerRadius))
            } else it
        }
        _project.value = current.copy(layers = updated)
        saveProject()
    }

    fun updateActiveShapeColors(fillColor: Long, strokeColor: Long, strokeWidth: Float) {
        val current = _project.value ?: return
        val selId = _selectedLayerId.value ?: return
        val layer = current.layers.find { it.id == selId && it.type == LayerType.SHAPE } ?: return

        pushSnapshot()
        val updated = current.layers.map {
            if (it.id == selId) {
                it.copy(shapeData = it.shapeData.copy(fillColor = fillColor, strokeColor = strokeColor, strokeWidth = strokeWidth))
            } else it
        }
        _project.value = current.copy(layers = updated)
        saveProject()
    }

    // --- Export ---

    fun exportFinalImage(options: ExportOptions) {
        val current = _project.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            _processingMessage.value = "Exportando en ${options.format.name} (${options.quality}%)..."

            val composite = withContext(Dispatchers.Default) {
                compositeProject(current, options.transparentBackground)
            }

            val uri = repository.exportProjectImage(
                bitmap = composite,
                title = current.name,
                options = options
            )

            _isProcessing.value = false
            _isExportDialogOpen.value = false

            if (uri != null) {
                _events.emit(EditorUiEvent.ExportComplete(uri))
                _events.emit(EditorUiEvent.ShowMessage("¡Imagen guardada en la galería!"))
            } else {
                _events.emit(EditorUiEvent.ShowMessage("Error al exportar la imagen"))
            }
        }
    }

    /**
     * Composites all visible layers into a single high-resolution Bitmap
     */
    private fun compositeProject(project: Project, transparentBg: Boolean): Bitmap {
        val result = Bitmap.createBitmap(project.width, project.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        if (!transparentBg) {
            canvas.drawColor(project.backgroundColor.toInt())
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        for (layer in project.layers) {
            if (!layer.isVisible) continue

            canvas.save()
            canvas.translate(layer.translationX, layer.translationY)
            canvas.rotate(layer.rotation, project.width * 0.5f, project.height * 0.5f)
            canvas.scale(layer.scale, layer.scale, project.width * 0.5f, project.height * 0.5f)

            paint.alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()

            paint.xfermode = when (layer.blendMode) {
                BlendModeType.MULTIPLY -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                BlendModeType.SCREEN -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                BlendModeType.OVERLAY -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
                BlendModeType.DARKEN -> PorterDuffXfermode(PorterDuff.Mode.DARKEN)
                BlendModeType.LIGHTEN -> PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
                BlendModeType.NORMAL -> null
            }

            when (layer.type) {
                LayerType.IMAGE -> {
                    layer.bitmap?.let { bmp ->
                        val dst = Rect(0, 0, project.width, project.height)
                        canvas.drawBitmap(bmp, null, dst, paint)
                    }
                }
                LayerType.DRAWING -> {
                    for (stroke in layer.strokes) {
                        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            strokeWidth = stroke.strokeWidth
                            color = stroke.color.toInt()
                            alpha = ((stroke.alpha * layer.opacity).coerceIn(0f, 1f) * 255).toInt()
                            if (stroke.isEraser) {
                                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                            }
                        }
                        for (i in 0 until stroke.points.size - 1) {
                            val p1 = stroke.points[i]
                            val p2 = stroke.points[i + 1]
                            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, strokePaint)
                        }
                    }
                }
                LayerType.TEXT -> {
                    val tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = layer.textData.textColor.toInt()
                        textSize = layer.textData.textSize * 2.2f
                        isFakeBoldText = layer.textData.isBold
                        textAlign = Paint.Align.CENTER
                        if (layer.textData.shadowColor != 0L) {
                            setShadowLayer(8f, 2f, 2f, layer.textData.shadowColor.toInt())
                        }
                    }

                    val textX = project.width * 0.5f
                    val textY = project.height * 0.5f

                    if (layer.textData.hasBackground) {
                        val textBounds = Rect()
                        tPaint.getTextBounds(layer.textData.text, 0, layer.textData.text.length, textBounds)
                        val pad = 36f
                        val bgRect = RectF(
                            textX - textBounds.width() * 0.5f - pad,
                            textY + textBounds.top - pad * 0.6f,
                            textX + textBounds.width() * 0.5f + pad,
                            textY + textBounds.bottom + pad * 0.6f
                        )
                        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = layer.textData.backgroundColor.toInt()
                        }
                        canvas.drawRoundRect(bgRect, 24f, 24f, bgPaint)
                    }

                    canvas.drawText(layer.textData.text, textX, textY, tPaint)
                }
                LayerType.SHAPE -> {
                    val sPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = layer.shapeData.fillColor.toInt()
                        style = Paint.Style.FILL
                    }
                    val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = layer.shapeData.strokeColor.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = layer.shapeData.strokeWidth * 2f
                    }
                    val cx = project.width * 0.5f
                    val cy = project.height * 0.5f
                    val r = 160f

                    when (layer.shapeData.shapeType) {
                        ShapeType.CIRCLE -> {
                            canvas.drawCircle(cx, cy, r, sPaint)
                            if (layer.shapeData.strokeWidth > 0) canvas.drawCircle(cx, cy, r, strokeP)
                        }
                        ShapeType.ROUNDED_RECTANGLE -> {
                            val hw = (if (layer.shapeData.width > 0f) layer.shapeData.width else r * 2.8f) * 0.5f
                            val hh = (if (layer.shapeData.height > 0f) layer.shapeData.height else r * 2.0f) * 0.5f
                            val cr = if (layer.shapeData.cornerRadius >= 0f) layer.shapeData.cornerRadius else 32f
                            val rect = RectF(cx - hw, cy - hh, cx + hw, cy + hh)
                            canvas.drawRoundRect(rect, cr, cr, sPaint)
                            if (layer.shapeData.strokeWidth > 0) canvas.drawRoundRect(rect, cr, cr, strokeP)
                        }
                        ShapeType.RECTANGLE -> {
                            val hw = (if (layer.shapeData.width > 0f) layer.shapeData.width else r * 2.8f) * 0.5f
                            val hh = (if (layer.shapeData.height > 0f) layer.shapeData.height else r * 2.0f) * 0.5f
                            val rect = RectF(cx - hw, cy - hh, cx + hw, cy + hh)
                            canvas.drawRect(rect, sPaint)
                            if (layer.shapeData.strokeWidth > 0) canvas.drawRect(rect, strokeP)
                        }
                        ShapeType.STAR, ShapeType.BADGE -> {
                            val path = android.graphics.Path()
                            val numPoints = 5
                            val outerRadius = r * 1.3f
                            val innerRadius = r * 0.6f
                            for (i in 0 until numPoints * 2) {
                                val radius = if (i % 2 == 0) outerRadius else innerRadius
                                val angle = i * Math.PI / numPoints - Math.PI / 2
                                val x = (cx + radius * Math.cos(angle)).toFloat()
                                val y = (cy + radius * Math.sin(angle)).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            path.close()
                            canvas.drawPath(path, sPaint)
                            if (layer.shapeData.strokeWidth > 0) canvas.drawPath(path, strokeP)
                        }
                        ShapeType.HEART -> {
                            val path = android.graphics.Path()
                            path.moveTo(cx, cy + r * 0.9f)
                            path.cubicTo(cx - r * 1.2f, cy - r * 0.2f, cx - r * 1.2f, cy - r * 0.9f, cx, cy - r * 0.4f)
                            path.cubicTo(cx + r * 1.2f, cy - r * 0.9f, cx + r * 1.2f, cy - r * 0.2f, cx, cy + r * 0.9f)
                            path.close()
                            canvas.drawPath(path, sPaint)
                            if (layer.shapeData.strokeWidth > 0) canvas.drawPath(path, strokeP)
                        }
                        ShapeType.ARROW -> {
                            val path = android.graphics.Path()
                            path.moveTo(cx + r * 1.2f, cy)
                            path.lineTo(cx, cy - r * 0.7f)
                            path.lineTo(cx, cy - r * 0.3f)
                            path.lineTo(cx - r * 1.2f, cy - r * 0.3f)
                            path.lineTo(cx - r * 1.2f, cy + r * 0.3f)
                            path.lineTo(cx, cy + r * 0.3f)
                            path.lineTo(cx, cy + r * 0.7f)
                            path.close()
                            canvas.drawPath(path, sPaint)
                            if (layer.shapeData.strokeWidth > 0) canvas.drawPath(path, strokeP)
                        }
                    }
                }
                LayerType.STICKER -> {
                    val sPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = layer.stickerData.size * 2.2f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(layer.stickerData.emoji, project.width * 0.5f, project.height * 0.5f + sPaint.textSize * 0.35f, sPaint)
                }
            }

            canvas.restore()
        }

        return result
    }
}
