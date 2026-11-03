package com.example.ui

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.model.DetectedObject
import com.example.model.DrawingPoint
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.Project
import com.example.model.ShapeType
import com.example.ui.theme.YupiElectricBlue
import com.example.ui.theme.YupiNeonCyan
import com.example.ui.theme.YupiPurpleLight
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EditorCanvas(
    project: Project,
    selectedLayerId: String?,
    activeTab: EditorToolTab,
    isEraser: Boolean,
    detectedObjects: List<DetectedObject>,
    inpaintBox: RectF?,
    onSelectLayer: (String?) -> Unit,
    onLayerTransform: (translationX: Float, translationY: Float, scale: Float, rotation: Float) -> Unit,
    onDrawingPoint: (DrawingPoint) -> Unit,
    onFinishStroke: () -> Unit,
    onInpaintBoxChanged: (RectF) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom and Pan
    var canvasScale by remember { mutableFloatStateOf(1.0f) }
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    var workspaceIsWhite by remember { mutableStateOf(true) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(if (workspaceIsWhite) Color(0xFFFFFFFF) else Color(0xFF090812)),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val availableWidthPx = constraints.maxWidth.toFloat()
        val availableHeightPx = constraints.maxHeight.toFloat()

        // Fit project within constraints
        val aspectProj = project.width.toFloat() / project.height.toFloat()
        val aspectView = availableWidthPx / availableHeightPx

        val baseCanvasW: Float
        val baseCanvasH: Float

        if (aspectProj > aspectView) {
            baseCanvasW = availableWidthPx * 0.92f
            baseCanvasH = baseCanvasW / aspectProj
        } else {
            baseCanvasH = availableHeightPx * 0.92f
            baseCanvasW = baseCanvasH * aspectProj
        }

        val scaleX = baseCanvasW / project.width
        val scaleY = baseCanvasH / project.height

        Box(
            modifier = Modifier
                .size(
                    width = with(density) { baseCanvasW.toDp() },
                    height = with(density) { baseCanvasH.toDp() }
                )
                .border(
                    width = 2.dp,
                    color = if (workspaceIsWhite) Color(0xFF1E293B) else Color(0xFF334155)
                )
                // Multi-touch gestures for pan & zoom (when not drawing)
                .pointerInput(activeTab) {
                    if (activeTab != EditorToolTab.DRAW) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            canvasScale = (canvasScale * zoom).coerceIn(0.5f, 5.0f)
                            canvasOffset += pan
                        }
                    }
                }
                // Pointer input for drawing
                .pointerInput(activeTab, isEraser) {
                    if (activeTab == EditorToolTab.DRAW) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val projX = offset.x / scaleX
                                val projY = offset.y / scaleY
                                onDrawingPoint(DrawingPoint(projX, projY))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val projX = change.position.x / scaleX
                                val projY = change.position.y / scaleY
                                onDrawingPoint(DrawingPoint(projX, projY))
                            },
                            onDragEnd = { onFinishStroke() },
                            onDragCancel = { onFinishStroke() }
                        )
                    } else if (activeTab == EditorToolTab.AI_TOOLS) {
                        // Drag to define inpaint area
                        detectDragGestures(
                            onDragStart = { start ->
                                val normX = (start.x / baseCanvasW).coerceIn(0f, 1f)
                                val normY = (start.y / baseCanvasH).coerceIn(0f, 1f)
                                onInpaintBoxChanged(RectF(normX - 0.08f, normY - 0.08f, normX + 0.08f, normY + 0.08f))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val normX = (change.position.x / baseCanvasW).coerceIn(0f, 1f)
                                val normY = (change.position.y / baseCanvasH).coerceIn(0f, 1f)
                                onInpaintBoxChanged(RectF(normX - 0.12f, normY - 0.12f, normX + 0.12f, normY + 0.12f))
                            }
                        )
                    } else {
                        // Drag active layer
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (selectedLayerId != null) {
                                onLayerTransform(dragAmount.x / scaleX, dragAmount.y / scaleY, 1f, 0f)
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                // Draw solid black canvas as requested ("el lienzo sea negro")
                drawRect(Color.Black)

                // 3. Render all layers in order
                for (layer in project.layers) {
                    if (!layer.isVisible) continue

                    val layerTx = layer.translationX * scaleX
                    val layerTy = layer.translationY * scaleY

                    translate(left = layerTx, top = layerTy) {
                        rotate(
                            degrees = layer.rotation,
                            pivot = Offset(canvasW * 0.5f, canvasH * 0.5f)
                        ) {
                            scale(
                                scale = layer.scale,
                                pivot = Offset(canvasW * 0.5f, canvasH * 0.5f)
                            ) {
                                renderLayer(layer, canvasW, canvasH, scaleX, scaleY)
                            }
                        }
                    }
                }

                // 4. Draw selection boundary if a layer is selected
                if (selectedLayerId != null && activeTab != EditorToolTab.DRAW) {
                    val selLayer = project.layers.find { it.id == selectedLayerId }
                    if (selLayer != null) {
                        val layerTx = selLayer.translationX * scaleX
                        val layerTy = selLayer.translationY * scaleY
                        translate(left = layerTx, top = layerTy) {
                            rotate(
                                degrees = selLayer.rotation,
                                pivot = Offset(canvasW * 0.5f, canvasH * 0.5f)
                            ) {
                                scale(
                                    scale = selLayer.scale,
                                    pivot = Offset(canvasW * 0.5f, canvasH * 0.5f)
                                ) {
                                    drawSelectionBox(canvasW, canvasH)
                                }
                            }
                        }
                    }
                }

                // 5. Draw detected object overlays (AI Detection)
                if (detectedObjects.isNotEmpty()) {
                    for (obj in detectedObjects) {
                        val left = obj.bounds.left * canvasW
                        val top = obj.bounds.top * canvasH
                        val right = obj.bounds.right * canvasW
                        val bottom = obj.bounds.bottom * canvasH
                        val color = Color(obj.tagColor)

                        drawRoundRect(
                            color = color.copy(alpha = 0.25f),
                            topLeft = Offset(left, top),
                            size = Size(right - left, bottom - top),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                        )
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(left, top),
                            size = Size(right - left, bottom - top),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Label badge
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                this.color = obj.tagColor.toInt()
                                textSize = 32f
                                isFakeBoldText = true
                            }
                            drawText(
                                "${obj.label} ${(obj.confidence * 100).toInt()}%",
                                left + 12f,
                                (top - 12f).coerceAtLeast(36f),
                                paint
                            )
                        }
                    }
                }

                // 6. Draw inpainting target box (AI Object Removal)
                if (inpaintBox != null) {
                    val left = inpaintBox.left * canvasW
                    val top = inpaintBox.top * canvasH
                    val right = inpaintBox.right * canvasW
                    val bottom = inpaintBox.bottom * canvasH

                    drawRect(
                        color = Color(0x66EC4899),
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top)
                    )
                    drawRect(
                        color = Color(0xFFEC4899),
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                        )
                    )
                }
            }
        }

        // Floating Workspace background mode toggle (White / Dark)
        androidx.compose.material3.Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (workspaceIsWhite) Color(0xEEFFFFFF) else Color(0xDD1E1B38),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (workspaceIsWhite) Color(0xFFCBD5E1) else Color(0xFF3B3560))
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .clickable { workspaceIsWhite = !workspaceIsWhite }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = if (workspaceIsWhite) "☀️ Fondo Blanco" else "🌙 Fondo Oscuro",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (workspaceIsWhite) Color(0xFF0F172A) else Color.White
                )
            }
        }
    }
}

private fun DrawScope.drawCheckerboard(width: Float, height: Float) {
    val tileSize = 20.dp.toPx()
    val cols = (width / tileSize).toInt() + 1
    val rows = (height / tileSize).toInt() + 1
    val c1 = Color(0xFF181628)
    val c2 = Color(0xFF100E1D)

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val color = if ((r + c) % 2 == 0) c1 else c2
            drawRect(
                color = color,
                topLeft = Offset(c * tileSize, r * tileSize),
                size = Size(tileSize, tileSize)
            )
        }
    }
}

private fun DrawScope.renderLayer(
    layer: Layer,
    canvasW: Float,
    canvasH: Float,
    scaleX: Float,
    scaleY: Float
) {
    val opacity = layer.opacity.coerceIn(0f, 1f)

    when (layer.type) {
        LayerType.IMAGE -> {
            layer.bitmap?.let { bmp ->
                drawImage(
                    image = bmp.asImageBitmap(),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(canvasW.toInt(), canvasH.toInt()),
                    alpha = opacity
                )
            }
        }
        LayerType.DRAWING -> {
            for (stroke in layer.strokes) {
                if (stroke.points.size < 2) continue
                val strokeColor = if (stroke.isEraser) Color(0xFF090812) else Color(stroke.color)
                val p = Path()
                p.moveTo(stroke.points[0].x * scaleX, stroke.points[0].y * scaleY)
                for (i in 1 until stroke.points.size) {
                    p.lineTo(stroke.points[i].x * scaleX, stroke.points[i].y * scaleY)
                }
                drawPath(
                    path = p,
                    color = strokeColor.copy(alpha = stroke.alpha * opacity),
                    style = Stroke(
                        width = stroke.strokeWidth * scaleX,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
        LayerType.TEXT -> {
            val textPaint = android.graphics.Paint().apply {
                color = layer.textData.textColor.toInt()
                textSize = layer.textData.textSize * scaleX * 2.2f
                isFakeBoldText = layer.textData.isBold
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = (opacity * 255).toInt()
                if (layer.textData.shadowColor != 0L) {
                    setShadowLayer(6f, 2f, 2f, layer.textData.shadowColor.toInt())
                }
            }

            val cx = canvasW * 0.5f
            val cy = canvasH * 0.5f

            if (layer.textData.hasBackground) {
                val bounds = android.graphics.Rect()
                textPaint.getTextBounds(layer.textData.text, 0, layer.textData.text.length, bounds)
                val padX = 24.dp.toPx()
                val padY = 12.dp.toPx()
                val bgRect = androidx.compose.ui.geometry.Rect(
                    cx - bounds.width() * 0.5f - padX,
                    cy + bounds.top - padY,
                    cx + bounds.width() * 0.5f + padX,
                    cy + bounds.bottom + padY
                )
                drawRoundRect(
                    color = Color(layer.textData.backgroundColor.toInt()).copy(alpha = opacity),
                    topLeft = bgRect.topLeft,
                    size = bgRect.size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                )
            }

            drawContext.canvas.nativeCanvas.drawText(
                layer.textData.text,
                cx,
                cy,
                textPaint
            )
        }
        LayerType.SHAPE -> {
            val cx = canvasW * 0.5f
            val cy = canvasH * 0.5f
            val r = 80.dp.toPx()
            val fillC = Color(layer.shapeData.fillColor).copy(alpha = opacity)
            val strokeC = Color(layer.shapeData.strokeColor).copy(alpha = opacity)
            val strokeW = layer.shapeData.strokeWidth * scaleX * 2f

            when (layer.shapeData.shapeType) {
                ShapeType.CIRCLE -> {
                    drawCircle(color = fillC, radius = r, center = Offset(cx, cy))
                    if (strokeW > 0) drawCircle(color = strokeC, radius = r, center = Offset(cx, cy), style = Stroke(strokeW))
                }
                ShapeType.ROUNDED_RECTANGLE -> {
                    val halfW = layer.shapeData.width * scaleX * 0.5f
                    val halfH = layer.shapeData.height * scaleY * 0.5f
                    val rad = layer.shapeData.cornerRadius * scaleX
                    val rect = androidx.compose.ui.geometry.Rect(cx - halfW, cy - halfH, cx + halfW, cy + halfH)
                    drawRoundRect(color = fillC, topLeft = rect.topLeft, size = rect.size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(rad, rad))
                    if (strokeW > 0) drawRoundRect(color = strokeC, topLeft = rect.topLeft, size = rect.size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(rad, rad), style = Stroke(strokeW))
                }
                ShapeType.RECTANGLE -> {
                    val halfW = layer.shapeData.width * scaleX * 0.5f
                    val halfH = layer.shapeData.height * scaleY * 0.5f
                    val rect = androidx.compose.ui.geometry.Rect(cx - halfW, cy - halfH, cx + halfW, cy + halfH)
                    drawRect(color = fillC, topLeft = rect.topLeft, size = rect.size)
                    if (strokeW > 0) drawRect(color = strokeC, topLeft = rect.topLeft, size = rect.size, style = Stroke(strokeW))
                }
                ShapeType.STAR, ShapeType.BADGE -> {
                    val path = Path()
                    val numPoints = 5
                    val outer = r * 1.3f
                    val inner = r * 0.6f
                    for (i in 0 until numPoints * 2) {
                        val radius = if (i % 2 == 0) outer else inner
                        val angle = i * Math.PI / numPoints - Math.PI / 2
                        val x = (cx + radius * cos(angle)).toFloat()
                        val y = (cy + radius * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, fillC)
                    if (strokeW > 0) drawPath(path, strokeC, style = Stroke(strokeW))
                }
                ShapeType.HEART -> {
                    val path = Path()
                    path.moveTo(cx, cy + r * 0.9f)
                    path.cubicTo(cx - r * 1.2f, cy - r * 0.2f, cx - r * 1.2f, cy - r * 0.9f, cx, cy - r * 0.4f)
                    path.cubicTo(cx + r * 1.2f, cy - r * 0.9f, cx + r * 1.2f, cy - r * 0.2f, cx, cy + r * 0.9f)
                    path.close()
                    drawPath(path, fillC)
                    if (strokeW > 0) drawPath(path, strokeC, style = Stroke(strokeW))
                }
                ShapeType.ARROW -> {
                    val path = Path()
                    path.moveTo(cx + r * 1.2f, cy)
                    path.lineTo(cx, cy - r * 0.7f)
                    path.lineTo(cx, cy - r * 0.3f)
                    path.lineTo(cx - r * 1.2f, cy - r * 0.3f)
                    path.lineTo(cx - r * 1.2f, cy + r * 0.3f)
                    path.lineTo(cx, cy + r * 0.3f)
                    path.lineTo(cx, cy + r * 0.7f)
                    path.close()
                    drawPath(path, fillC)
                    if (strokeW > 0) drawPath(path, strokeC, style = Stroke(strokeW))
                }
            }
        }
        LayerType.STICKER -> {
            val paint = android.graphics.Paint().apply {
                textSize = layer.stickerData.size * scaleX * 2.2f
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = (opacity * 255).toInt()
            }
            drawContext.canvas.nativeCanvas.drawText(
                layer.stickerData.emoji,
                canvasW * 0.5f,
                canvasH * 0.5f + paint.textSize * 0.35f,
                paint
            )
        }
    }
}

private fun DrawScope.drawSelectionBox(width: Float, height: Float) {
    val pad = 12.dp.toPx()
    val boxRect = androidx.compose.ui.geometry.Rect(
        -pad,
        -pad,
        width + pad,
        height + pad
    )

    drawRoundRect(
        color = YupiElectricBlue,
        topLeft = boxRect.topLeft,
        size = boxRect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
        )
    )

    // Corner handle circles
    val handleR = 6.dp.toPx()
    val corners = listOf(
        boxRect.topLeft,
        boxRect.topRight,
        boxRect.bottomLeft,
        boxRect.bottomRight
    )
    for (c in corners) {
        drawCircle(color = YupiElectricBlue, radius = handleR, center = c)
        drawCircle(color = Color.White, radius = handleR * 0.5f, center = c)
    }
}
