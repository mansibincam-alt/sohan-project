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

class GeminiException(val code: Int, message: String) : Exception(message)

object Gemini {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private const val TEXT_MODEL = "gemini-2.5-flash"

    /** ছবি তৈরির মডেল: একটি না চললে পরের নামটি চেষ্টা করা হয়। */
    private val IMAGE_MODELS = listOf(
        "gemini-3.1-flash-image",   // Nano Banana 2 (stable)
        "gemini-3-pro-image",       // Nano Banana Pro
        "gemini-2.5-flash-image"    // Nano Banana (stable, older)
    )

    fun apiKey(ctx: Context): String =
        runCatching { ctx.assets.open("API_KEY").bufferedReader().use { it.readText() }.trim() }
            .getOrDefault("")

    private fun part(text: String) = JSONObject().put("text", text)

    private fun inline(mime: String, bytes: ByteArray) = JSONObject().put(
        "inline_data",
        JSONObject()
            .put("mime_type", mime)
            .put("data", Base64.encodeToString(bytes, Base64.NO_WRAP))
    )

    private fun post(key: String, model: String, body: JSONObject): JSONObject {
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
            .header("x-goog-api-key", key)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val msg = runCatching { JSONObject(raw).getJSONObject("error").getString("message") }
                    .getOrDefault(raw.take(200))
                throw GeminiException(resp.code, "[$model] $msg")
            }
            return JSONObject(raw)
        }
    }

    private fun buildBody(
        history: List<ChatMessage>,
        prompt: String,
        att: Attachment?,
        wantImage: Boolean
    ): JSONObject {
        val contents = JSONArray()
        if (!wantImage) {
            history.filter { it.text.isNotBlank() }.takeLast(16).forEach { m ->
                contents.put(
                    JSONObject()
                        .put("role", if (m.isUser) "user" else "model")
                        .put("parts", JSONArray().put(part(m.text)))
                )
            }
        }
        val parts = JSONArray().put(part(prompt))
        if (att != null) parts.put(inline(att.mime, att.bytes))
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
                JSONObject().put("parts", JSONArray().put(part(Persona.SYSTEM)))
            )
        }
        return body
    }

    /** Returns (text, imageBytes?) from the first candidate. */
    private fun extract(json: JSONObject): Pair<String, ByteArray?> {
        val cands = json.optJSONArray("candidates")
        if (cands == null || cands.length() == 0) {
            val reason = json.optJSONObject("promptFeedback")?.optString("blockReason").orEmpty()
            throw IllegalStateException(
                if (reason.isNotEmpty()) "নিরাপত্তা নীতির কারণে উত্তর আটকানো হয়েছে ($reason)।"
                else "উত্তর পাওয়া যায়নি।"
            )
        }
        val out = cands.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts") ?: JSONArray()
        val text = StringBuilder()
        var image: ByteArray? = null
        for (i in 0 until out.length()) {
            val p = out.getJSONObject(i)
            val t = p.optString("text")
            if (t.isNotEmpty()) text.append(t)
            val inl = p.optJSONObject("inlineData") ?: p.optJSONObject("inline_data")
            if (inl != null) image = Base64.decode(inl.getString("data"), Base64.DEFAULT)
        }
        return text.toString().trim() to image
    }

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

        if (!wantImage) {
            return extract(post(key, TEXT_MODEL, buildBody(history, prompt, attachment, false)))
        }

        val body = buildBody(emptyList(), prompt, attachment, true)
        var lastError: Exception = IllegalStateException("ছবি তৈরি করা যায়নি")
        for (model in IMAGE_MODELS) {
            try {
                val (text, img) = extract(post(key, model, body))
                if (img != null) return text to img
                lastError = IllegalStateException(text.ifEmpty { "মডেল ছবি পাঠায়নি, অন্যভাবে বর্ণনা দিয়ে দেখো।" })
            } catch (e: GeminiException) {
                lastError = e
                if (e.code != 404) break // 404 = এই মডেল নামটি এই কী-তে নেই, তাই পরেরটি চেষ্টা করো
            } catch (e: Exception) {
                lastError = e
                break
            }
        }
        throw lastError
    }

    /** Own-recorded WAV audio -> text, using Gemini as the speech recogniser. */
    fun transcribe(ctx: Context, wav: ByteArray): String {
        val key = apiKey(ctx)
        if (key.isEmpty()) throw IllegalStateException("API_KEY ফাইলে এখনো কী বসানো হয়নি।")
        val parts = JSONArray()
            .put(part("এই অডিওতে কেউ যা বলেছে তা হুবহু লিখে দাও (যে ভাষায় বলা হয়েছে সেই ভাষায়)। শুধু লেখাটুকু দাও, কোনো ব্যাখ্যা নয়।"))
            .put(inline("audio/wav", wav))
        val body = JSONObject().put(
            "contents",
            JSONArray().put(JSONObject().put("role", "user").put("parts", parts))
        )
        return extract(post(key, TEXT_MODEL, body)).first
    }
}
