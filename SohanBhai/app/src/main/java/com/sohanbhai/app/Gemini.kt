package com.sohanbhai.app

import android.content.Context
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object Gemini {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private const val TEXT_MODEL = "gemini-2.5-flash"
    private const val IMAGE_MODEL = "gemini-2.5-flash-image"

    /** Reads the key from assets/API_KEY (the user pastes it there). */
    fun apiKey(ctx: Context): String =
        ctx.assets.open("API_KEY").bufferedReader().use { it.readText() }.trim()

    /** Returns (text, imageBytes?). */
    fun generate(
        ctx: Context,
        history: List<ChatMessage>,
        prompt: String,
        attachment: Attachment?,
        wantImage: Boolean
    ): Pair<String, ByteArray?> {
        val key = apiKey(ctx)
        if (key.isEmpty()) throw IllegalStateException("API_KEY ফাইলে এখনো কী বসানো হয়নি।")

        val contents = JSONArray()
        if (!wantImage) {
            history.filter { it.text.isNotBlank() }.takeLast(16).forEach { m ->
                contents.put(
                    JSONObject()
                        .put("role", if (m.isUser) "user" else "model")
                        .put("parts", JSONArray().put(JSONObject().put("text", m.text)))
                )
            }
        }

        val parts = JSONArray().put(JSONObject().put("text", prompt))
        if (attachment != null) {
            parts.put(
                JSONObject().put(
                    "inline_data",
                    JSONObject()
                        .put("mime_type", attachment.mime)
                        .put("data", Base64.encodeToString(attachment.bytes, Base64.NO_WRAP))
                )
            )
        }
        contents.put(JSONObject().put("role", "user").put("parts", parts))

        val body = JSONObject().put("contents", contents)
        if (wantImage) {
            body.put(
                "generationConfig",
                JSONObject().put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
            )
        } else {
            body.put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", Persona.SYSTEM)))
            )
        }

        val model = if (wantImage) IMAGE_MODEL else TEXT_MODEL
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
            .header("x-goog-api-key", key)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("সমস্যা হয়েছে (${resp.code})। API কী ও ইন্টারনেট চেক করো।")
            }
            val cands = JSONObject(raw).optJSONArray("candidates")
            if (cands == null || cands.length() == 0) {
                throw IllegalStateException("উত্তর পাওয়া যায়নি। অন্যভাবে প্রশ্ন করে দেখো।")
            }
            val outParts = cands.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts") ?: JSONArray()
            val text = StringBuilder()
            var image: ByteArray? = null
            for (i in 0 until outParts.length()) {
                val p = outParts.getJSONObject(i)
                val t = p.optString("text")
                if (t.isNotEmpty()) text.append(t)
                val inline = p.optJSONObject("inlineData") ?: p.optJSONObject("inline_data")
                if (inline != null) image = Base64.decode(inline.getString("data"), Base64.DEFAULT)
            }
            return text.toString().trim() to image
        }
    }
}
