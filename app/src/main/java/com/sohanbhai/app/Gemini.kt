package com.sohanbhai.app

import android.content.Context
import android.util.Base64
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object Gemini {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    /** চ্যাটের মডেল: সেরাটা আগে, না চললে পরেরটি। */
    private val TEXT_MODELS = listOf(
        "gemini-3.6-flash",
        "gemini-3.5-flash",
        "gemini-3-flash-preview",
        "gemini-2.5-flash"
    )

    /** ছবি তৈরি ও এডিটের মডেল (Nano Banana 2 → Nano Banana Pro → Nano Banana)। */
    private val IMAGE_MODELS = listOf(
        "gemini-3.1-flash-image-preview",
        "gemini-3-pro-image-preview",
        "gemini-2.5-flash-image"
    )

    /**
     * assets/API_KEY ফাইলে প্রতি লাইনে একটি কী, [ ] ব্র্যাকেটের ভেতরে লেখা।
     * ব্র্যাকেটের বাইরের লেখা (যেমন API 1) ও খালি ব্র্যাকেট বাদ যায়।
     * ব্র্যাকেট না থাকলে পুরো লাইনটাই কী ধরা হয়।
     */
    fun apiKeys(ctx: Context): List<String> =
        runCatching {
            ctx.assets.open("API_KEY").bufferedReader().use { it.readLines() }
                .mapNotNull { line ->
                    val s = line.trim()
                    val key = if (s.contains('[') && s.contains(']'))
                        s.substringAfter('[').substringBefore(']').trim()
                    else s
                    if (key.isEmpty()) null else key
                }
        }.getOrDefault(emptyList())

    /** HTTP এরর কোড সহ। 404 হলে মডেল নেই (পরের মডেল), বাকি সব হলে পরের কী। */
    private class HttpFail(val code: Int, msg: String) : IllegalStateException(msg)

    private fun part(text: String) = JSONObject().put("text", text)

    private fun inline(mime: String, bytes: ByteArray) = JSONObject().put(
        "inline_data",
        JSONObject()
            .put("mime_type", mime)
            .put("data", Base64.encodeToString(bytes, Base64.NO_WRAP))
    )

    /** ব্যবহারকারীকে দেখানোর ছোট বার্তা। বিস্তারিত তথ্য শুধু Logcat-এ যায়। */
    private fun friendly(code: Int): String = when (code) {
        429 -> "এখন ব্যবহারের সীমা শেষ হয়েছে। কিছুক্ষণ পরে আবার চেষ্টা করুন।"
        400, 401, 403 -> "API key ঠিক নেই বা অনুমতি নেই।"
        404 -> "এই মডেলটি এখন পাওয়া যাচ্ছে না।"
        else -> "এখন উত্তর দেওয়া যাচ্ছে না, আবার চেষ্টা করুন।"
    }

    private fun post(key: String, model: String, body: JSONObject): JSONObject {
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
            .header("x-goog-api-key", key)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val detail = runCatching { JSONObject(raw).getJSONObject("error").getString("message") }
                    .getOrDefault(raw.take(200))
                Log.w("Gemini", "[$model] ${resp.code}: $detail")
                throw HttpFail(resp.code, friendly(resp.code))
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

    /** প্রথম candidate থেকে (লেখা, ছবি) বের করে। ছবি বা লেখা কিছুই না থাকলে ব্যর্থ ধরা হয়। */
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

    /**
     * কী ধরে ধরে চেষ্টা: প্রথমে কী-১ দিয়ে একটি কল। এরর হলে কোনো বার্তা না দেখিয়ে কী-২,
     * তারপর কী-৩। একসাথে একটির বেশি কল যায় না; প্রথম সফল উত্তরেই থামে।
     * মডেল না পাওয়া গেলে (404) একই কী দিয়ে পরের মডেল। সব ব্যর্থ হলে শেষ এরর ফেরত আসে।
     */
    private fun runWithFailover(
        ctx: Context,
        models: List<String>,
        body: JSONObject,
        needImage: Boolean
    ): Pair<String, ByteArray?> {
        val keys = apiKeys(ctx)
        if (keys.isEmpty()) throw IllegalStateException("API_KEY ফাইলে এখনো কী বসানো হয়নি।")

        var lastError: Exception = IllegalStateException("উত্তর পাওয়া যায়নি।")
        for (key in keys) {
            for (model in models) {
                try {
                    val result = extract(post(key, model, body))
                    if (!needImage || result.second != null) return result
                    lastError = IllegalStateException(result.first.ifEmpty { "মডেল ছবি পাঠায়নি।" })
                } catch (e: HttpFail) {
                    lastError = e
                    if (e.code != 404) break // কী-সংক্রান্ত/কোটার এরর: পরের কী
                } catch (e: Exception) {
                    lastError = e
                    break // অন্য এরর: পরের কী
                }
            }
        }
        throw lastError
    }

    /** Pollinations থেকে ফ্রি ছবি (কোনো key লাগে না)। */
    private fun pollinationsImage(prompt: String, seed: Int?): ByteArray {
        val encoded = URLEncoder.encode(prompt, "UTF-8").replace("+", "%20")
        val seedPart = if (seed != null) "&seed=$seed" else ""
        val req = Request.Builder()
            .url("https://image.pollinations.ai/prompt/$encoded?width=1024&height=1024$seedPart")
            .get()
            .build()
        http.newCall(req).execute().use { resp ->
            val bytes = resp.body?.bytes()
            if (!resp.isSuccessful || bytes == null || bytes.isEmpty()) {
                throw IllegalStateException("ছবি এখন তৈরি করা যাচ্ছে না। কিছুক্ষণ পরে আবার চেষ্টা করুন।")
            }
            return bytes
        }
    }

    /** চ্যাট (wantImage = false) বা ছবি তৈরি/এডিট (wantImage = true)। */
    fun generate(
        ctx: Context,
        history: List<ChatMessage>,
        prompt: String,
        attachment: Attachment?,
        wantImage: Boolean,
        seed: Int? = null
    ): Pair<String, ByteArray?> {
        // নতুন ছবি তৈরি সবসময় Pollinations দিয়ে (কোনো key লাগে না)।
        if (wantImage && attachment == null) {
            return "" to pollinationsImage(prompt, seed)
        }
        val body = buildBody(history, prompt, attachment, wantImage)
        return if (wantImage) {
            // ছবি এডিট (ছবি আপলোড করা) শুধু Gemini পারে।
            runWithFailover(ctx, IMAGE_MODELS, body, needImage = true)
        } else {
            runWithFailover(ctx, TEXT_MODELS, body, needImage = false)
        }
    }
}
