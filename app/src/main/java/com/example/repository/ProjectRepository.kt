package com.example.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.R
import com.example.engine.ImageEngine
import com.example.model.CanvasPreset
import com.example.model.DrawingPoint
import com.example.model.DrawingStroke
import com.example.model.ExportFormat
import com.example.model.ExportOptions
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.Project
import com.example.model.ShapeData
import com.example.model.ShapeType
import com.example.model.StickerData
import com.example.model.TextData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ExportedItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val filePath: String,
    val format: String,
    val timestamp: Long = System.currentTimeMillis(),
    val width: Int,
    val height: Int
)

class ProjectRepository(val context: Context) {

    private val projectsDir = File(context.filesDir, "yupiforge_projects").apply { mkdirs() }
    private val exportDir = File(context.cacheDir, "images").apply { mkdirs() }

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _exportedGallery = MutableStateFlow<List<ExportedItem>>(emptyList())
    val exportedGallery: StateFlow<List<ExportedItem>> = _exportedGallery.asStateFlow()

    val canvasPresets = listOf(
        CanvasPreset("Cuadrado 1:1", "Instagram, Perfil", 1080, 1080, "1:1"),
        CanvasPreset("Historia 9:16", "Instagram, TikTok, Reels", 1080, 1920, "9:16"),
        CanvasPreset("Retrato 4:5", "Publicación Vertical", 1080, 1350, "4:5"),
        CanvasPreset("Paisaje 16:9", "YouTube, Banner, TV", 1920, 1080, "16:9"),
        CanvasPreset("Foto 3:2", "Cámara Estándar", 1500, 1000, "3:2"),
        CanvasPreset("Alta Res 4K", "Ultra Definición", 3840, 2160, "16:9")
    )

    init {
        initializeSampleProjectsIfNeeded()
        refreshProjects()
        refreshExportedGallery()
    }

    private fun initializeSampleProjectsIfNeeded() {
        val flagFile = File(projectsDir, ".initialized")
        if (!flagFile.exists()) {
            try {
                // 1. Cyberpunk Sample Project
                val cyberResId = R.drawable.cyberpunk_portrait_1793746376820
                val cyberBitmap = BitmapFactory.decodeResource(context.resources, cyberResId)
                if (cyberBitmap != null) {
                    val p1 = Project(
                        id = "sample_cyberpunk",
                        name = "Retrato Neón Cyberpunk",
                        width = cyberBitmap.width,
                        height = cyberBitmap.height,
                        createdAt = System.currentTimeMillis() - 86400000L,
                        modifiedAt = System.currentTimeMillis() - 3600000L,
                        layers = listOf(
                            Layer(
                                id = "layer_bg_img",
                                name = "Fotografía Base",
                                type = LayerType.IMAGE,
                                bitmap = cyberBitmap
                            ),
                            Layer(
                                id = "layer_text_title",
                                name = "Título Neón",
                                type = LayerType.TEXT,
                                translationX = 0f,
                                translationY = -350f,
                                textData = TextData(
                                    text = "YupiForge Cyber",
                                    fontName = "Montserrat",
                                    textColor = 0xFF00E5FF,
                                    textSize = 48f,
                                    isBold = true,
                                    hasBackground = true,
                                    backgroundColor = 0xAA581C87
                                )
                            ),
                            Layer(
                                id = "layer_badge",
                                name = "Insignia",
                                type = LayerType.SHAPE,
                                translationX = 280f,
                                translationY = -480f,
                                shapeData = ShapeData(
                                    shapeType = ShapeType.STAR,
                                    fillColor = 0xFFEC4899,
                                    strokeColor = 0xFFFFFFFF,
                                    strokeWidth = 3f
                                )
                            )
                        )
                    )
                    saveProjectInternal(p1)
                }

                // 2. Sneaker Product Project
                val sneakerResId = R.drawable.sneaker_product_1793746388279
                val sneakerBitmap = BitmapFactory.decodeResource(context.resources, sneakerResId)
                if (sneakerBitmap != null) {
                    val p2 = Project(
                        id = "sample_sneaker",
                        name = "Campaña Sneaker Pro",
                        width = sneakerBitmap.width,
                        height = sneakerBitmap.height,
                        createdAt = System.currentTimeMillis() - 172800000L,
                        modifiedAt = System.currentTimeMillis() - 7200000L,
                        layers = listOf(
                            Layer(
                                id = "layer_sneaker_img",
                                name = "Producto Zapatilla",
                                type = LayerType.IMAGE,
                                bitmap = sneakerBitmap
                            ),
                            Layer(
                                id = "layer_sneaker_text",
                                name = "Texto Promo",
                                type = LayerType.TEXT,
                                translationX = 0f,
                                translationY = 320f,
                                textData = TextData(
                                    text = "EDICIÓN LIMITADA 2026",
                                    fontName = "Montserrat",
                                    textColor = 0xFFFFFFFF,
                                    textSize = 34f,
                                    isBold = true,
                                    hasBackground = true,
                                    backgroundColor = 0xDD00E5FF
                                )
                            ),
                            Layer(
                                id = "layer_sticker",
                                name = "Sticker Fuego",
                                type = LayerType.STICKER,
                                translationX = -260f,
                                translationY = -280f,
                                stickerData = StickerData(emoji = "🔥", size = 80f)
                            )
                        )
                    )
                    saveProjectInternal(p2)
                }

                flagFile.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshProjects() {
        val files = projectsDir.listFiles { f -> f.isDirectory } ?: return
        val list = mutableListOf<Project>()
        for (folder in files) {
            val metaFile = File(folder, "project.json")
            if (metaFile.exists()) {
                try {
                    val jsonStr = metaFile.readText()
                    val project = parseProjectJson(JSONObject(jsonStr), folder)
                    list.add(project)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        _projects.value = list.sortedByDescending { it.modifiedAt }
    }

    fun refreshExportedGallery() {
        val files = exportDir.listFiles { f ->
            f.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp")
        } ?: return

        val items = files.map { file ->
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, opts)
            ExportedItem(
                id = file.name,
                title = file.nameWithoutExtension,
                filePath = file.absolutePath,
                format = file.extension.uppercase(),
                timestamp = file.lastModified(),
                width = opts.outWidth.coerceAtLeast(1),
                height = opts.outHeight.coerceAtLeast(1)
            )
        }.sortedByDescending { it.timestamp }
        _exportedGallery.value = items
    }

    suspend fun saveProject(project: Project): Boolean = withContext(Dispatchers.IO) {
        try {
            saveProjectInternal(project)
            refreshProjects()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveProjectInternal(project: Project) {
        val folder = File(projectsDir, project.id).apply { mkdirs() }
        val metaJson = JSONObject()
        metaJson.put("id", project.id)
        metaJson.put("name", project.name)
        metaJson.put("width", project.width)
        metaJson.put("height", project.height)
        metaJson.put("createdAt", project.createdAt)
        metaJson.put("modifiedAt", System.currentTimeMillis())
        metaJson.put("backgroundColor", project.backgroundColor)

        val layersArray = JSONArray()
        for ((idx, layer) in project.layers.withIndex()) {
            val lObj = JSONObject()
            lObj.put("id", layer.id)
            lObj.put("name", layer.name)
            lObj.put("type", layer.type.name)
            lObj.put("isVisible", layer.isVisible)
            lObj.put("isLocked", layer.isLocked)
            lObj.put("opacity", layer.opacity.toDouble())
            lObj.put("blendMode", layer.blendMode.name)
            lObj.put("translationX", layer.translationX.toDouble())
            lObj.put("translationY", layer.translationY.toDouble())
            lObj.put("scale", layer.scale.toDouble())
            lObj.put("rotation", layer.rotation.toDouble())

            // If layer has bitmap, save PNG
            if (layer.bitmap != null) {
                val bmpFileName = "layer_${layer.id}.png"
                val bmpFile = File(folder, bmpFileName)
                FileOutputStream(bmpFile).use { out ->
                    layer.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                lObj.put("bitmapFile", bmpFileName)
            }

            // Text data
            val tObj = JSONObject()
            tObj.put("text", layer.textData.text)
            tObj.put("fontName", layer.textData.fontName)
            tObj.put("textColor", layer.textData.textColor)
            tObj.put("textSize", layer.textData.textSize.toDouble())
            tObj.put("hasBackground", layer.textData.hasBackground)
            tObj.put("backgroundColor", layer.textData.backgroundColor)
            tObj.put("isBold", layer.textData.isBold)
            tObj.put("isItalic", layer.textData.isItalic)
            lObj.put("textData", tObj)

            // Shape data
            val sObj = JSONObject()
            sObj.put("shapeType", layer.shapeData.shapeType.name)
            sObj.put("fillColor", layer.shapeData.fillColor)
            sObj.put("strokeColor", layer.shapeData.strokeColor)
            sObj.put("strokeWidth", layer.shapeData.strokeWidth.toDouble())
            sObj.put("width", layer.shapeData.width.toDouble())
            sObj.put("height", layer.shapeData.height.toDouble())
            sObj.put("cornerRadius", layer.shapeData.cornerRadius.toDouble())
            lObj.put("shapeData", sObj)

            // Sticker data
            val stObj = JSONObject()
            stObj.put("emoji", layer.stickerData.emoji)
            stObj.put("size", layer.stickerData.size.toDouble())
            lObj.put("stickerData", stObj)

            layersArray.put(lObj)
        }
        metaJson.put("layers", layersArray)

        // Save project.json
        File(folder, "project.json").writeText(metaJson.toString())
    }

    private fun parseProjectJson(json: JSONObject, folder: File): Project {
        val id = json.getString("id")
        val name = json.optString("name", "Proyecto")
        val width = json.optInt("width", 1080)
        val height = json.optInt("height", 1080)
        val createdAt = json.optLong("createdAt", System.currentTimeMillis())
        val modifiedAt = json.optLong("modifiedAt", System.currentTimeMillis())
        val bgColor = json.optLong("backgroundColor", 0xFF000000)

        val layersArray = json.optJSONArray("layers") ?: JSONArray()
        val layers = mutableListOf<Layer>()

        for (i in 0 until layersArray.length()) {
            val lObj = layersArray.getJSONObject(i)
            val lId = lObj.getString("id")
            val lName = lObj.getString("name")
            val lType = LayerType.valueOf(lObj.getString("type"))
            val isVis = lObj.optBoolean("isVisible", true)
            val isLock = lObj.optBoolean("isLocked", false)
            val opacity = lObj.optDouble("opacity", 1.0).toFloat()
            val blendMode = com.example.model.BlendModeType.valueOf(lObj.optString("blendMode", "NORMAL"))
            val tx = lObj.optDouble("translationX", 0.0).toFloat()
            val ty = lObj.optDouble("translationY", 0.0).toFloat()
            val scale = lObj.optDouble("scale", 1.0).toFloat()
            val rot = lObj.optDouble("rotation", 0.0).toFloat()

            var bmp: Bitmap? = null
            if (lObj.has("bitmapFile")) {
                val f = File(folder, lObj.getString("bitmapFile"))
                if (f.exists()) {
                    bmp = BitmapFactory.decodeFile(f.absolutePath)
                }
            }

            var textData = TextData()
            if (lObj.has("textData")) {
                val t = lObj.getJSONObject("textData")
                textData = TextData(
                    text = t.optString("text", "Texto"),
                    fontName = t.optString("fontName", "Montserrat"),
                    textColor = t.optLong("textColor", 0xFFFFFFFF),
                    textSize = t.optDouble("textSize", 36.0).toFloat(),
                    hasBackground = t.optBoolean("hasBackground", false),
                    backgroundColor = t.optLong("backgroundColor", 0x889333EA),
                    isBold = t.optBoolean("isBold", false),
                    isItalic = t.optBoolean("isItalic", false)
                )
            }

            var shapeData = ShapeData()
            if (lObj.has("shapeData")) {
                val s = lObj.getJSONObject("shapeData")
                shapeData = ShapeData(
                    shapeType = ShapeType.valueOf(s.optString("shapeType", "ROUNDED_RECTANGLE")),
                    fillColor = s.optLong("fillColor", 0xFFA855F7),
                    strokeColor = s.optLong("strokeColor", 0xFF00E5FF),
                    strokeWidth = s.optDouble("strokeWidth", 4.0).toFloat(),
                    width = s.optDouble("width", 340.0).toFloat(),
                    height = s.optDouble("height", 200.0).toFloat(),
                    cornerRadius = s.optDouble("cornerRadius", 36.0).toFloat()
                )
            }

            var stickerData = StickerData()
            if (lObj.has("stickerData")) {
                val st = lObj.getJSONObject("stickerData")
                stickerData = StickerData(
                    emoji = st.optString("emoji", "✨"),
                    size = st.optDouble("size", 64.0).toFloat()
                )
            }

            layers.add(
                Layer(
                    id = lId,
                    name = lName,
                    type = lType,
                    isVisible = isVis,
                    isLocked = isLock,
                    opacity = opacity,
                    blendMode = blendMode,
                    translationX = tx,
                    translationY = ty,
                    scale = scale,
                    rotation = rot,
                    bitmap = bmp,
                    textData = textData,
                    shapeData = shapeData,
                    stickerData = stickerData
                )
            )
        }

        return Project(
            id = id,
            name = name,
            width = width,
            height = height,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            backgroundColor = bgColor,
            layers = layers
        )
    }

    suspend fun duplicateProject(project: Project): Project? = withContext(Dispatchers.IO) {
        try {
            val newId = UUID.randomUUID().toString()
            val duplicated = project.copy(
                id = newId,
                name = "${project.name} (Copia)",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis()
            )
            saveProjectInternal(duplicated)
            refreshProjects()
            duplicated
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteProject(projectId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val folder = File(projectsDir, projectId)
            if (folder.exists()) {
                folder.deleteRecursively()
            }
            refreshProjects()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportProjectImage(
        bitmap: Bitmap,
        title: String,
        options: ExportOptions
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // Apply scale multiplier or custom resolution
            val scaledBitmap = if (options.customWidth != null && options.customHeight != null) {
                Bitmap.createScaledBitmap(bitmap, options.customWidth, options.customHeight, true)
            } else if (options.scaleMultiplier != 1.0f) {
                val w = (bitmap.width * options.scaleMultiplier).toInt()
                val h = (bitmap.height * options.scaleMultiplier).toInt()
                Bitmap.createScaledBitmap(bitmap, w, h, true)
            } else {
                bitmap
            }

            val bytes = ImageEngine.exportBitmap(
                bitmap = scaledBitmap,
                format = options.format,
                quality = options.quality,
                transparent = options.transparentBackground
            )

            val safeTitle = title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val fileName = "YupiForge_${safeTitle}_${System.currentTimeMillis()}.${options.format.extension}"

            // 1. Save to internal cache for quick sharing & app gallery
            val cacheFile = File(exportDir, fileName)
            FileOutputStream(cacheFile).use { it.write(bytes) }

            // 2. Save directly to Android MediaStore Pictures/YupiForge folder
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, options.format.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/YupiForge")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(bytes)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
            }

            refreshExportedGallery()
            uri ?: getShareableUri(cacheFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getShareableUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun shareImage(context: Context, fileUri: Uri, mimeType: String = "image/*") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir con YupiForge"))
    }
}
