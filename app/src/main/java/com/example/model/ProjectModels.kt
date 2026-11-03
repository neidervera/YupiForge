package com.example.model

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import java.util.UUID

enum class LayerType {
    IMAGE,
    DRAWING,
    TEXT,
    SHAPE,
    STICKER
}

enum class BlendModeType {
    NORMAL,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN
}

data class DrawingPoint(val x: Float, val y: Float)

data class DrawingStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<DrawingPoint>,
    val color: Long,
    val strokeWidth: Float,
    val alpha: Float = 1.0f,
    val isEraser: Boolean = false
)

data class TextData(
    val text: String = "Texto",
    val fontName: String = "Montserrat",
    val textColor: Long = 0xFFFFFFFF,
    val textSize: Float = 36f,
    val hasBackground: Boolean = false,
    val backgroundColor: Long = 0x889333EA,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val shadowColor: Long = 0xFF000000
)

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    STAR,
    HEART,
    ARROW,
    BADGE
}

data class ShapeData(
    val shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE,
    val fillColor: Long = 0xFFA855F7,
    val strokeColor: Long = 0xFF00E5FF,
    val strokeWidth: Float = 4f,
    val isFilled: Boolean = true,
    val width: Float = 340f,
    val height: Float = 200f,
    val cornerRadius: Float = 36f
)

enum class AiProvider(
    val id: String,
    val displayName: String,
    val icon: String,
    val tokenHint: String,
    val description: String,
    val models: List<String>
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        icon = "✨",
        tokenHint = "AIzaSy...",
        description = "Modelos Gemini e Imagen de Google",
        models = listOf("imagen-3.0-generate-002", "gemini-3.8-flash", "gemini-2.5-flash-image")
    ),
    OPENAI(
        id = "openai",
        displayName = "ChatGPT / DALL-E (OpenAI)",
        icon = "🤖",
        tokenHint = "sk-...",
        description = "Modelos DALL-E 3 y DALL-E 2",
        models = listOf("dall-e-3", "dall-e-2")
    ),
    NVIDIA(
        id = "nvidia",
        displayName = "NVIDIA AI (NIM)",
        icon = "⚡",
        tokenHint = "nvapi-...",
        description = "NVIDIA NIM: Stable Diffusion XL, Edify, Flux",
        models = listOf("stabilityai/sdxl-turbo", "stabilityai/stable-diffusion-xl-base-1.0", "nvidia/edify-image")
    ),
    LOCAL_NEURAL(
        id = "local",
        displayName = "Motor YupiForge Local",
        icon = "🎨",
        tokenHint = "No requiere token",
        description = "Síntesis procedural neuronal integrada de alta definición",
        models = listOf("yupiforge-neural-v1", "yupiforge-cyber-ultra", "yupiforge-landscape-hdr")
    )
}

data class StickerData(
    val emoji: String = "✨",
    val size: Float = 64f
)

data class Layer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: LayerType,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f,
    val blendMode: BlendModeType = BlendModeType.NORMAL,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    // Content payload
    val bitmap: Bitmap? = null,
    val strokes: List<DrawingStroke> = emptyList(),
    val textData: TextData = TextData(),
    val shapeData: ShapeData = ShapeData(),
    val stickerData: StickerData = StickerData()
)

data class ImageAdjustments(
    val brightness: Float = 0f,    // -100 to 100
    val contrast: Float = 0f,      // -100 to 100
    val saturation: Float = 0f,    // -100 to 100
    val exposure: Float = 0f,      // -100 to 100
    val temperature: Float = 0f,   // -100 to 100
    val blur: Float = 0f,          // 0 to 100
    val sharpness: Float = 0f      // 0 to 100
) {
    val isDefault: Boolean
        get() = brightness == 0f && contrast == 0f && saturation == 0f &&
                exposure == 0f && temperature == 0f && blur == 0f && sharpness == 0f
}

enum class FilterType(val displayName: String, val category: String) {
    NONE("Original", "Básico"),
    CINEMATIC_TEAL_ORANGE("Teal & Orange", "Cinematográfico"),
    CINEMATIC_CYBERPUNK("Cyberpunk", "Cinematográfico"),
    CINEMATIC_FILM_NOIR("Film Noir", "Cinematográfico"),
    CINEMATIC_GOLDEN_HOUR("Golden Hour", "Cinematográfico"),
    BW_HIGH_CONTRAST("B&W Contraste", "Blanco y Negro"),
    BW_VINTAGE_MONO("B&W Clásico", "Blanco y Negro"),
    BW_SEPIA("Sepia Cálido", "Blanco y Negro"),
    VINTAGE_WARM_70S("Retro 70s", "Vintage"),
    VINTAGE_POLAROID("Polaroid", "Vintage"),
    VINTAGE_FADE("Fade Nostalgia", "Vintage"),
    HDR_VIVID("HDR Vívido", "Color"),
    LIGHT_VIGNETTE("Viñeta", "Efectos de Luz"),
    LIGHT_GLOW("Resplandor", "Efectos de Luz"),
    COLOR_DUOTONE_PURPLE_BLUE("Duotono Yupi", "Efectos de Color"),
    COLOR_INVERT("Invertir", "Efectos de Color"),
    COLOR_HUE_SHIFT("Giro de Color", "Efectos de Color"),
    ARTISTIC_POSTERIZE("Posterizado", "Artístico"),
    ARTISTIC_PIXELATE("Pixel Art", "Artístico"),
    ARTISTIC_SKETCH("Dibujo a Lápiz", "Artístico"),
    ARTISTIC_EMBOSS("Relieve Metálico", "Artístico")
}

data class DetectedObject(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val bounds: RectF,
    val confidence: Float,
    val tagColor: Long
)

enum class ExportFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    WEBP("webp", "image/webp")
}

data class ExportOptions(
    val format: ExportFormat = ExportFormat.PNG,
    val quality: Int = 95,
    val scaleMultiplier: Float = 1.0f,
    val transparentBackground: Boolean = false,
    val customWidth: Int? = null,
    val customHeight: Int? = null
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Proyecto sin título",
    val width: Int = 1080,
    val height: Int = 1080,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val backgroundColor: Long = 0xFF000000,
    val layers: List<Layer> = emptyList(),
    val thumbnailFileName: String? = null
)

data class CanvasPreset(
    val name: String,
    val subtitle: String,
    val width: Int,
    val height: Int,
    val aspectLabel: String
)
