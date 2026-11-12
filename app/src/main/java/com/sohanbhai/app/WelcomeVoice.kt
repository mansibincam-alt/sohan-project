package com.sohanbhai.app

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * অ্যাপ খুললে স্বাগত ভয়েস। res/raw/welcome_voice.mp3 থাকলে সেটি বাজে (সুন্দর ছেলের কণ্ঠ),
 * না থাকলে ডিভাইসের বাংলা কণ্ঠে (TTS) একই লাইনটি বলে।
 */
object WelcomeVoice {
    private const val LINE =
        "সোহান ভাইয়ের অ্যাপ্লিকেশনে আপনাকে স্বাগতম। এখানে আপনি পাবেন সোহান ভাইয়ের বিস্তারিত পরিচয় এবং এআই চ্যাটবট।"

    /** প্রক্রিয়া (process) চালু থাকাকালে একবারই বাজে; প্রতিবার অ্যাপ নতুন করে খুললে আবার বাজে। */
    private var greeted = false
    private var tts: TextToSpeech? = null

    fun playOncePerLaunch(ctx: Context) {
        if (greeted) return
        greeted = true
        val app = ctx.applicationContext

        val resId = app.resources.getIdentifier("welcome_voice", "raw", app.packageName)
        if (resId != 0) {
            runCatching {
                MediaPlayer.create(app, resId)?.apply {
                    setOnCompletionListener { it.release() }
                    start()
                }
            }
            return
        }

        runCatching {
            tts = TextToSpeech(app) { status ->
                val engine = tts ?: return@TextToSpeech
                if (status == TextToSpeech.SUCCESS) {
                    engine.setLanguage(Locale("bn", "BD"))
                    // ডিভাইসে বাংলা পুরুষ কণ্ঠ থাকলে সেটি বেছে নেওয়া হয়
                    engine.voices
                        ?.filter { it.locale.language == "bn" }
                        ?.firstOrNull { it.name.contains("male", ignoreCase = true) && !it.name.contains("female", ignoreCase = true) }
                        ?.let { engine.voice = it }
                    engine.setPitch(0.95f)
                    engine.speak(LINE, TextToSpeech.QUEUE_FLUSH, null, "welcome")
                }
            }
        }
    }
}
