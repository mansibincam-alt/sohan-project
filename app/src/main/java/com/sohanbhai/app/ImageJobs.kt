package com.sohanbhai.app

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/** UI-তে দেখানোর জন্য চলমান ছবি-ব্যাচের অবস্থা। */
data class BatchState(val total: Int, val finished: Int, val percent: Int)

/** সার্ভিসে যাওয়ার একটি ছবি-ব্যাচের কাজ। */
class ImageRequest(val prompt: String, val total: Int, val conv: Conversation)

/** সংখ্যাকে বাংলা অঙ্কে বদলায়। */
fun bnDigits(n: Int): String =
    n.toString().map { c -> if (c in '0'..'9') '\u09E6' + (c - '0') else c }.joinToString("")

/**
 * ছবি তৈরির ব্যাচ। একবারে সর্বোচ্চ ৫টি অর্ডার, প্রতি ১০ সেকেন্ড পর পর একটি করে
 * (কোনো অর্ডার ব্যর্থ হলে পরের গ্যাপ ১৫ সেকেন্ড)। অ্যাপ বন্ধ থাকলেও ফোর্গ্রাউন্ড
 * সার্ভিস (ImageJobService) কাজ চালিয়ে যায় এবং প্রতিটি ছবি নোটিফিকেশনে আসে।
 *
 * মেইন থ্রেডের অবস্থা (total, finished, inflight ইত্যাদি) শুধু মেইন থ্রেড থেকে বদলায়;
 * নেটওয়ার্কের কাজ ব্যাকগ্রাউন্ডে হয় এবং ফল মেইন থ্রেডে ফেরত আসে।
 */
object ImageJobs {
    const val MAX_COUNT = 5

    /** অর্ডার পাঠানোর ব্যবধান। */
    const val GAP_MS = 10_000L
    const val SLOW_GAP_MS = 15_000L

    /** একটি ছবি আনুমানিক কত মিলিসেকেন্ডে হয় (শতাংশ হিসাবের জন্য)। */
    private const val ESTIMATE_MS = 40_000.0

    /** UI এই অবস্থা পড়ে। null মানে কোনো ছবি-ব্যাচ চলছে না। */
    val batch = mutableStateOf<BatchState?>(null)

    val isActive: Boolean get() = batch.value != null

    private val main = Handler(Looper.getMainLooper())
    private var appCtx: Context? = null
    private var pending: ImageRequest? = null
    private var total = 0
    private var finished = 0
    private var succeeded = 0
    private val inflight = HashMap<Int, Long>()

    @Volatile
    private var gapMs = GAP_MS

    private val tick = object : Runnable {
        override fun run() {
            val c = appCtx ?: return
            if (isActive) {
                publish(c)
                main.postDelayed(this, 1_000)
            }
        }
    }

    /** চ্যাট স্ক্রিন থেকে (মেইন থ্রেডে) একটি ছবি-ব্যাচ শুরু করে। */
    fun start(ctx: Context, conv: Conversation, prompt: String, count: Int) {
        if (isActive) return
        val app = ctx.applicationContext
        val n = count.coerceIn(1, MAX_COUNT)

        appCtx = app
        total = n
        finished = 0
        succeeded = 0
        inflight.clear()
        gapMs = GAP_MS
        pending = ImageRequest(prompt, n, conv)
        batch.value = BatchState(n, 0, 0)

        conv.messages.add(
            ChatMessage(
                isUser = false,
                text = "${bnDigits(n)}টি ছবির অর্ডার নেওয়া হয়েছে। প্রতি ${bnDigits((GAP_MS / 1000).toInt())} সেকেন্ড পর পর একটি করে তৈরি হবে। এর মধ্যে এআই-এর সাথে চ্যাট করা যাবে।"
            )
        )
        Store.touch(conv)

        ImageNotifier.ensureChannels(app)
        main.removeCallbacks(tick)
        main.postDelayed(tick, 1_000)
        ContextCompat.startForegroundService(app, Intent(app, ImageJobService::class.java))
    }

    /** সার্ভিস শুরু হলে কাজটি একবারই নেয় (মেইন থ্রেড)। */
    fun takeRequest(): ImageRequest? {
        val r = pending
        pending = null
        return r
    }

    /** সার্ভিসের ভেতরে চলে: অর্ডারগুলো ব্যবধান রেখে পাঠায়, সবগুলো শেষ হলে ফেরে। */
    suspend fun execute(ctx: Context, req: ImageRequest) {
        coroutineScope {
            for (i in 1..req.total) {
                if (i > 1) delay(gapMs)
                main.post { inflight[i] = System.currentTimeMillis() }
                launch { runOne(ctx, req, i) }
            }
        }
    }

    private suspend fun runOne(ctx: Context, req: ImageRequest, idx: Int) {
        val result: Result<String> = withContext(Dispatchers.IO) {
            runCatching {
                val bytes = Gemini.generate(
                    ctx, emptyList(), req.prompt, null, true, Random.nextInt(1, 999_999)
                ).second ?: error("ছবি পাওয়া যায়নি।")
                Store.saveImage(bytes)
            }
        }
        main.post { finish(ctx, req, idx, result) }
    }

    /** একটি অর্ডারের ফল (মেইন থ্রেড): চ্যাটে বার্তা, নোটিফিকেশন, অগ্রগতি। */
    private fun finish(ctx: Context, req: ImageRequest, idx: Int, result: Result<String>) {
        inflight.remove(idx)
        finished++
        result.onSuccess { path ->
            succeeded++
            req.conv.messages.add(
                ChatMessage(
                    isUser = false,
                    text = "ছবি ${bnDigits(idx)}/${bnDigits(req.total)} তৈরি হয়েছে।",
                    imagePath = path
                )
            )
            ImageNotifier.image(ctx, req.conv.id, idx, req.total, path)
        }
        result.onFailure { e ->
            gapMs = SLOW_GAP_MS
            req.conv.messages.add(
                ChatMessage(
                    isUser = false,
                    text = "ছবি ${bnDigits(idx)} তৈরি করা যায়নি। ${e.message ?: "কিছু একটা সমস্যা হয়েছে।"}"
                )
            )
        }
        Store.touch(req.conv)

        if (finished >= req.total) end(req) else publish(ctx)
    }

    private fun end(req: ImageRequest) {
        main.removeCallbacks(tick)
        batch.value = null
        req.conv.messages.add(
            ChatMessage(isUser = false, text = "ছবির কাজ শেষ। ${bnDigits(succeeded)}টি ছবি তৈরি হয়েছে।")
        )
        Store.touch(req.conv)
    }

    /** অগ্রগতি হিসাব: শেষ হওয়া অর্ডার + চলমান অর্ডারের আনুমানিক অংশ (সর্বোচ্চ ৯০%)। */
    private fun publish(ctx: Context) {
        val now = System.currentTimeMillis()
        val partial = inflight.values.sumOf { started ->
            ((now - started) / ESTIMATE_MS).coerceIn(0.0, 0.9)
        }
        val pct = ((finished + partial) * 100.0 / total).toInt().coerceIn(0, 99)
        batch.value = BatchState(total, finished, pct)
        ImageNotifier.progress(ctx, finished, total, pct)
    }
}
