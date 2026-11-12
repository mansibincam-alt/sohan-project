package com.sohanbhai.app

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.sqrt

/** Records 16 kHz mono audio and publishes live loudness levels for the waveform. */
class VoiceRecorder {
    val levels = MutableStateFlow(List(30) { 0.05f })

    private val pcm = ByteArrayOutputStream()
    @Volatile private var running = false
    private var record: AudioRecord? = null
    private var thread: Thread? = null

    @SuppressLint("MissingPermission")
    fun start() {
        val rate = 16000
        val min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val r = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            rate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            max(min, 6400)
        )
        record = r
        pcm.reset()
        levels.value = List(30) { 0.05f }
        running = true
        r.startRecording()
        thread = Thread {
            val buf = ShortArray(800) // ~50 ms per read
            while (running) {
                val n = r.read(buf, 0, buf.size)
                if (n <= 0) continue
                var sum = 0.0
                for (i in 0 until n) {
                    val s = buf[i].toInt()
                    pcm.write(s and 0xff)
                    pcm.write((s shr 8) and 0xff)
                    sum += s.toDouble() * s
                }
                val level = (sqrt(sum / n) / 5000.0).coerceIn(0.05, 1.0).toFloat()
                levels.value = levels.value.drop(1) + level
            }
        }.also { it.start() }
    }

    /** Stops recording and returns the audio as WAV bytes. */
    fun stop(): ByteArray {
        running = false
        thread?.join(1000)
        record?.let {
            try {
                it.stop()
            } catch (_: Exception) {
            }
            it.release()
        }
        record = null
        return wavOf(pcm.toByteArray(), 16000)
    }

    private fun wavOf(data: ByteArray, rate: Int): ByteArray {
        val out = ByteArrayOutputStream()
        fun i32(v: Int) {
            out.write(v and 0xff); out.write((v shr 8) and 0xff)
            out.write((v shr 16) and 0xff); out.write((v shr 24) and 0xff)
        }
        fun i16(v: Int) {
            out.write(v and 0xff); out.write((v shr 8) and 0xff)
        }
        out.write("RIFF".toByteArray()); i32(36 + data.size); out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray()); i32(16); i16(1); i16(1)
        i32(rate); i32(rate * 2); i16(2); i16(16)
        out.write("data".toByteArray()); i32(data.size)
        out.write(data)
        return out.toByteArray()
    }
}

/** Bars that rise and fall with the voice. */
@Composable
fun Waveform(levels: List<Float>, modifier: Modifier = Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        levels.forEach { l ->
            Box(
                Modifier
                    .width(3.dp)
                    .height((4 + 36 * l).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(Cyan, Violet)))
            )
        }
    }
}
