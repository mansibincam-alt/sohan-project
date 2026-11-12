package com.sohanbhai.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        StarField()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp)
        ) {
            TopBar("প্রোফাইল", onBack)
            Spacer(Modifier.height(8.dp))

            Reveal(0) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SpinningAvatar()
                }
            }
            Spacer(Modifier.height(16.dp))

            Reveal(1) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    ShimmerText(SohanProfile.name, 28.sp)
                    Text(SohanProfile.title, color = Cyan, fontSize = 15.sp)
                    Text(SohanProfile.org, color = Muted, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = Muted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(SohanProfile.location, color = Muted, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Reveal(2) { InfoCard("পরিচিতি", SohanProfile.bio) }
            Spacer(Modifier.height(12.dp))
            Reveal(3) { InfoCard("সংগঠন", SohanProfile.orgNote) }
            Spacer(Modifier.height(12.dp))
            Reveal(4) { InfoCard("এই অ্যাপ", SohanProfile.appNote) }
            Spacer(Modifier.height(12.dp))

            Reveal(5) {
                Column(Modifier.fillMaxWidth()) {
                    Text("আগ্রহ", color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    SohanProfile.interests.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { item ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(CardBg)
                                        .border(1.dp, Violet.copy(alpha = 0.4f), RoundedCornerShape(50))
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Reveal(6) {
                GlowBorder(Modifier.fillMaxWidth(), radius = 18.dp) {
                    Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            SohanProfile.quote,
                            color = Cyan,
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    GlowBorder(Modifier.fillMaxWidth(), radius = 18.dp) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text(body, color = Color.White, fontSize = 14.sp, lineHeight = 22.sp)
        }
    }
}

/** Avatar with a ring that keeps rotating. Replace the letter with a photo drawable later. */
@Composable
private fun SpinningAvatar() {
    val spin by rememberInfiniteTransition(label = "avatar").animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "spin"
    )
    Box(Modifier.size(128.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            rotate(spin) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Cyan, Violet, Color(0xFFEC4899), Cyan)),
                    radius = size.minDimension / 2f - 3.dp.toPx(),
                    style = Stroke(4.dp.toPx())
                )
            }
        }
        Box(
            Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF0E7490)))),
            contentAlignment = Alignment.Center
        ) {
            Text("স", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        }
    }
}
