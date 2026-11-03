package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.engine.ImageEngine
import com.example.model.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiTokenService {
    private const val PREFS_NAME = "yupiforge_ai_tokens"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    fun saveToken(context: Context, provider: AiProvider, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("token_${provider.id}", token.trim()).apply()
    }

    fun getToken(context: Context, provider: AiProvider): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("token_${provider.id}", "") ?: ""
    }

    suspend fun generateImage(
        context: Context,
        provider: AiProvider,
        model: String,
        prompt: String,
        style: String,
        targetWidth: Int = 1024,
        targetHeight: Int = 1024
    ): Bitmap = withContext(Dispatchers.IO) {
        val token = getToken(context, provider)

        when (provider) {
            AiProvider.LOCAL_NEURAL -> {
                ImageEngine.generateArtisticFromPrompt(prompt, style, targetWidth, targetHeight)
            }
            AiProvider.OPENAI -> {
                if (token.isBlank()) {
                    throw IllegalArgumentException("Por favor ingresa tu API Key / Token de OpenAI (sk-...) en Ajustes de IA.")
                }
                generateWithOpenAi(token, model, prompt, style)
            }
            AiProvider.NVIDIA -> {
                if (token.isBlank()) {
                    throw IllegalArgumentException("Por favor ingresa tu Token de NVIDIA NIM (nvapi-...) en Ajustes de IA.")
                }
                generateWithNvidia(token, model, prompt, style)
            }
            AiProvider.GEMINI -> {
                if (token.isBlank()) {
                    throw IllegalArgumentException("Por favor ingresa tu API Key de Google Gemini (AIza...) en Ajustes de IA.")
                }
                generateWithGemini(token, model, prompt, style)
            }
        }
    }

    private fun generateWithOpenAi(token: String, model: String, prompt: String, style: String): Bitmap {
        val cleanModel = if (model.isNotBlank()) model else "dall-e-3"
        val fullPrompt = "$prompt, in $style aesthetic, 8k resolution, highly detailed digital masterpiece"
        val payload = JSONObject().apply {
            put("prompt", fullPrompt)
            put("model", cleanModel)
            put("n", 1)
            put("size", if (cleanModel == "dall-e-3") "1024x1024" else "512x512")
            put("response_format", "b64_json")
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/generations")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val json = JSONObject(responseBody)
                json.optJSONObject("error")?.optString("message") ?: response.message
            } catch (e: Exception) {
                response.message
            }
            throw RuntimeException("OpenAI Error (${response.code}): $errorMsg")
        }

        val json = JSONObject(responseBody)
        val dataArray = json.optJSONArray("data")
            ?: throw RuntimeException("Respuesta de OpenAI sin datos de imagen")
        val item = dataArray.optJSONObject(0)
            ?: throw RuntimeException("Elemento de imagen vacío en OpenAI")

        val b64 = item.optString("b64_json")
        if (b64.isNotEmpty()) {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) return bmp
        }

        val url = item.optString("url")
        if (url.isNotEmpty()) {
            val imgReq = Request.Builder().url(url).build()
            val imgResp = httpClient.newCall(imgReq).execute()
            val bytes = imgResp.body?.bytes()
            if (bytes != null) {
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) return bmp
            }
        }

        throw RuntimeException("No se pudo decodificar la imagen devuelta por OpenAI")
    }

    private fun generateWithNvidia(token: String, model: String, prompt: String, style: String): Bitmap {
        val cleanModel = if (model.isNotBlank()) model else "stabilityai/sdxl-turbo"
        val endpoint = "https://ai.api.nvidia.com/v1/genai/$cleanModel"
        val fullPrompt = "$prompt, in $style style, trending on artstation, cinematic lighting, sharp focus, 8k"

        val payload = JSONObject().apply {
            val textPrompts = JSONArray().apply {
                put(JSONObject().apply {
                    put("text", fullPrompt)
                    put("weight", 1.0)
                })
            }
            put("text_prompts", textPrompts)
            put("seed", (System.currentTimeMillis() % 100000).toInt())
            put("steps", if (cleanModel.contains("turbo")) 2 else 20)
            put("cfg_scale", 7.0)
        }

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val json = JSONObject(responseBody)
                json.optString("detail", json.optString("message", response.message))
            } catch (e: Exception) {
                response.message
            }
            throw RuntimeException("NVIDIA NIM Error (${response.code}): $errorMsg")
        }

        val json = JSONObject(responseBody)
        // Check artifacts array (common in Stability / SDXL NIMs)
        val artifacts = json.optJSONArray("artifacts")
        if (artifacts != null && artifacts.length() > 0) {
            val b64 = artifacts.getJSONObject(0).optString("base64")
            if (b64.isNotEmpty()) {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) return bmp
            }
        }

        // Check image field or choices
        val directImage = json.optString("image")
        if (directImage.isNotEmpty()) {
            val bytes = Base64.decode(directImage, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) return bmp
        }

        throw RuntimeException("No se encontró imagen válida en la respuesta de NVIDIA NIM")
    }

    private fun generateWithGemini(token: String, model: String, prompt: String, style: String): Bitmap {
        val cleanModel = if (model.isNotBlank()) model else "imagen-3.0-generate-002"
        val fullPrompt = "$prompt, styled as $style, high aesthetic quality, vivid colors, masterwork"

        // For Imagen 3
        if (cleanModel.startsWith("imagen-")) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:predict?key=$token"
            val payload = JSONObject().apply {
                val instances = JSONArray().apply {
                    put(JSONObject().apply {
                        put("prompt", fullPrompt)
                    })
                }
                put("instances", instances)
                put("parameters", JSONObject().apply {
                    put("sampleCount", 1)
                    put("aspectRatio", "1:1")
                })
            }

            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optJSONObject("error")?.optString("message") ?: response.message
                } catch (e: Exception) {
                    response.message
                }
                throw RuntimeException("Gemini Imagen Error (${response.code}): $errorMsg")
            }

            val json = JSONObject(responseBody)
            val predictions = json.optJSONArray("predictions")
                ?: throw RuntimeException("Respuesta de Imagen sin predicciones")
            val first = predictions.optJSONObject(0)
                ?: throw RuntimeException("Predicción vacía")
            val b64 = first.optString("bytesBase64Encoded")
            if (b64.isNotEmpty()) {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) return bmp
            }
            throw RuntimeException("No se pudo decodificar la imagen de Imagen 3")
        }

        // For Gemini chat models (e.g. gemini-2.0-flash / gemini-3.8-flash)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$token"
        val payload = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Generate high quality imagery description: $fullPrompt")
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val json = JSONObject(responseBody)
                json.optJSONObject("error")?.optString("message") ?: response.message
            } catch (e: Exception) {
                response.message
            }
            throw RuntimeException("Gemini Error (${response.code}): $errorMsg")
        }

        // If inlineData image exists:
        try {
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val parts = candidates?.getJSONObject(0)?.getJSONObject("content")?.getJSONArray("parts")
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    val inlineData = p.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val b64 = inlineData.optString("data")
                        if (b64.isNotEmpty()) {
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bmp != null) return bmp
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback: If model returned text instead of inlineData image, synthesize neural canvas
        return ImageEngine.generateArtisticFromPrompt(fullPrompt, style, 1024, 1024)
    }
}
