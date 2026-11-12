package com.sohanbhai.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/** Night sky: twinkling stars. */
@Composable
fun StarField() {
    val t by rememberInfiniteTransition(label = "sky").animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF040A16), Color(0xFF0B1B33), Night)))
        val rnd = Random(7)
        repeat(70) {
            val x = rnd.nextFloat() * size.width
            val y = rnd.nextFloat() * size.height * 0.5f
            val phase = rnd.nextFloat()
            val a = 0.25f + 0.75f * abs(sin((t + phase) * 2f * PI.toFloat()))
            drawCircle(Color.White.copy(alpha = a), radius = 1f + rnd.nextFloat() * 1.6f, center = Offset(x, y))
        }
    }
}

/** Card border with a coloured light that keeps travelling left to right. */
@Composable
fun GlowBorder(modifier: Modifier = Modifier, radius: Dp = 22.dp, content: @Composable BoxScope.() -> Unit) {
    val phase by rememberInfiniteTransition(label = "glow").animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "phase"
    )
    Box(
        modifier
            .drawBehind {
                val o = phase * 900f
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Cyan, Violet, Color(0xFFEC4899), Cyan),
                        start = Offset(o, 0f),
                        end = Offset(o + 450f, 0f),
                        tileMode = TileMode.Repeated
                    ),
                    cornerRadius = CornerRadius(radius.toPx()),
                    style = Stroke(2.dp.toPx())
                )
            }
            .padding(2.dp)
            .clip(RoundedCornerShape(radius))
            .background(CardBg),
        content = content
    )
}

/** Text whose gradient shine keeps sweeping across it. */
@Composable
fun ShimmerText(text: String, fontSize: TextUnit, modifier: Modifier = Modifier) {
    val x by rememberInfiniteTransition(label = "shimmer").animateFloat(
        -400f, 900f,
        infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "x"
    )
    Text(
        text,
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            brush = Brush.linearGradient(
                colors = listOf(Color.White, Cyan, Violet, Color.White),
                start = Offset(x, 0f),
                end = Offset(x + 300f, 0f)
            )
        )
    )
}

/** Our own logo (replaces the ChatGPT-style knot): floating gradient sparkle with a spinning ring. */
@Composable
fun AiLogo(box: Dp) {
    val tr = rememberInfiniteTransition(label = "logo")
    val lift by tr.animateFloat(
        -5f, 5f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lift"
    )
    val spin by tr.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "spin")
    Box(Modifier.size(box).offset(y = lift.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            rotate(spin) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Cyan, Violet, Color(0xFFEC4899), Cyan)),
                    radius = size.minDimension / 2f,
                    style = Stroke(3.dp.toPx())
                )
            }
        }
        Box(
            Modifier
                .size(box * 0.7f)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Cyan, Violet, Color(0xFFEC4899)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(box * 0.3f))
        }
    }
}

@Composable
fun AiAvatar() {
    SohanPhoto(32.dp, ring = false)
}

/** সোহান ভাইয়ের প্রোফাইল ছবি (গোল)। */
@Composable
fun SohanPhoto(size: Dp, ring: Boolean = true, modifier: Modifier = Modifier) {
    val ringMod = if (ring) {
        Modifier.border(2.dp, Brush.linearGradient(listOf(Cyan, Violet, Color(0xFFEC4899))), CircleShape)
    } else {
        Modifier
    }
    Box(
        modifier.size(size).clip(CircleShape).then(ringMod),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.sohan_profile),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun IconCircle(icon: ImageVector, desc: String?, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(CardBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, desc, tint = Color.White)
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconCircle(Icons.Filled.ArrowBack, "back", onBack)
        Spacer(Modifier.width(12.dp))
        ShimmerText(title, 18.sp)
    }
}

/** Fades and slides a block in, staggered by index. */
@Composable
fun Reveal(index: Int, content: @Composable () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 90L)
        shown = true
    }
    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }
    ) {
        content()
    }
}
