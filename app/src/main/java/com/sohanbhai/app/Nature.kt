package com.sohanbhai.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * ভিডিও ছাড়াই প্রকৃতির দৃশ্য: রাতের আকাশ, স্তরে স্তরে পাহাড়, মাঝখানে ঝর্ণা ও ভেসে যাওয়া কুয়াশা।
 * t (০→১) প্রতি ১২ সেকেন্ডে একবার ঘুরে আসে, তাই অ্যানিমেশন নিখুঁতভাবে লুপ হয়।
 */
@Composable
fun NatureScene(modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition(label = "nature").animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    val twoPi = (2 * PI).toFloat()

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // আকাশ
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0xFF060E1E), Color(0xFF0B2A3F), Color(0xFF0F5257), Color(0xFF1F7A6E))
            )
        )

        // দূরের তারা (আস্তে জ্বলে-নেভে)
        for (i in 0 until 40) {
            val x = ((i * 0.618f) % 1f) * w
            val y = ((i * 0.381f) % 1f) * h * 0.4f
            val a = 0.3f + 0.7f * abs(sin(t * twoPi + i))
            drawCircle(Color.White.copy(alpha = a * 0.8f), radius = 1.2f, center = Offset(x, y))
        }

        // পাহাড়ের স্তর: দূরে থেকে কাছে, প্রতিটি স্তর একটু আলাদা গতিতে সরে (প্যারালাক্স)
        mountainLayer(w, h, base = h * 0.52f, amp = h * 0.13f, peaks = 3f, shift = t * twoPi * 1f,
            color = Color(0xFF2B5873).copy(alpha = 0.9f))
        mountainLayer(w, h, base = h * 0.64f, amp = h * 0.12f, peaks = 4f, shift = t * twoPi * 2f,
            color = Color(0xFF1B4A4E))

        // ঝর্ণা: সামনের পাহাড়ের গা বেয়ে নামে
        val wx = w * 0.72f
        val top = h * 0.60f
        val bottom = h * 0.9f
        drawRect(
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color(0xFFBAE6FD).copy(alpha = 0.45f))),
            topLeft = Offset(wx - w * 0.014f, top),
            size = Size(w * 0.028f, bottom - top)
        )
        for (i in 0 until 12) {
            val lane = wx - w * 0.012f + i * (w * 0.024f / 11f)
            val p = (t * 2.5f + i * 0.17f) % 1f
            val y = top + p * (bottom - top)
            drawLine(
                Color.White.copy(alpha = 0.75f),
                Offset(lane, y),
                Offset(lane, y + h * 0.05f),
                strokeWidth = 2.2f
            )
        }
        // ঝর্ণার নিচে জলের ফেনা
        drawOval(
            Color.White.copy(alpha = 0.25f),
            topLeft = Offset(wx - w * 0.06f, bottom - h * 0.02f),
            size = Size(w * 0.12f, h * 0.04f)
        )

        // সামনের পাহাড়
        mountainLayer(w, h, base = h * 0.80f, amp = h * 0.09f, peaks = 2f, shift = t * twoPi * 3f,
            color = Color(0xFF0E2F33))

        // ভেসে যাওয়া কুয়াশা
        val mistX = ((t * 1.2f) % 1f) * w * 1.4f - w * 0.2f
        drawOval(
            Color.White.copy(alpha = 0.10f),
            topLeft = Offset(mistX, h * 0.56f),
            size = Size(w * 0.6f, h * 0.06f)
        )
    }
}

/** একটি পাহাড়ের স্তর; সাইন-তরঙ্গ থেকে চূড়াগুলো তৈরি হয়। */
private fun DrawScope.mountainLayer(
    w: Float,
    h: Float,
    base: Float,
    amp: Float,
    peaks: Float,
    shift: Float,
    color: Color
) {
    val steps = 80
    val path = Path().apply {
        moveTo(0f, h)
        for (i in 0..steps) {
            val x = w * i / steps
            val y = base - amp * abs(sin(x / w * peaks * PI.toFloat() + shift))
            lineTo(x, y)
        }
        lineTo(w, h)
        close()
    }
    drawPath(path, color)
}
