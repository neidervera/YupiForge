package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.engine.ImageEngine
import com.example.model.FilterType
import com.example.model.ImageAdjustments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("YupiForge", appName)
    }

    @Test
    fun `test image engine adjustments and filter`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        assertNotNull(bitmap)

        val adjusted = ImageEngine.applyAdjustments(
            bitmap,
            ImageAdjustments(brightness = 20f, contrast = 10f, saturation = 15f)
        )
        assertNotNull(adjusted)
        assertEquals(100, adjusted.width)
        assertEquals(100, adjusted.height)

        val filtered = ImageEngine.applyFilter(
            bitmap,
            FilterType.CINEMATIC_CYBERPUNK,
            intensity = 0.8f
        )
        assertNotNull(filtered)
        assertEquals(100, filtered.width)
    }

    @Test
    fun `test ai token service save and retrieve`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.ai.AiTokenService.saveToken(context, com.example.model.AiProvider.OPENAI, "sk-test-token-123")
        com.example.ai.AiTokenService.saveToken(context, com.example.model.AiProvider.NVIDIA, "nvapi-test-token-456")
        com.example.ai.AiTokenService.saveToken(context, com.example.model.AiProvider.GEMINI, "AIzaSyTest789")

        assertEquals("sk-test-token-123", com.example.ai.AiTokenService.getToken(context, com.example.model.AiProvider.OPENAI))
        assertEquals("nvapi-test-token-456", com.example.ai.AiTokenService.getToken(context, com.example.model.AiProvider.NVIDIA))
        assertEquals("AIzaSyTest789", com.example.ai.AiTokenService.getToken(context, com.example.model.AiProvider.GEMINI))
    }

    @Test
    fun `test stretched rounded rectangle shape dimensions`() {
        val shape = com.example.model.ShapeData(
            shapeType = com.example.model.ShapeType.ROUNDED_RECTANGLE,
            width = 540f,
            height = 180f,
            cornerRadius = 45f
        )
        assertEquals(com.example.model.ShapeType.ROUNDED_RECTANGLE, shape.shapeType)
        assertEquals(540f, shape.width)
        assertEquals(180f, shape.height)
        assertEquals(45f, shape.cornerRadius)
    }
}
