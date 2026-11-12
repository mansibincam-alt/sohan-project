package com.sohanbhai.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(go: (Route) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var weather by remember { mutableStateOf<Weather?>(null) }

    fun reload() {
        scope.launch {
            weather = withContext(Dispatchers.IO) { runCatching { WeatherApi.fetch(ctx) }.getOrNull() }
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { reload() }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) reload() else permLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    Box(Modifier.fillMaxSize()) {
        StarField()

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 96.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconCircle(Icons.Filled.Menu, "menu") {}
                Spacer(Modifier.weight(1f))
                Box {
                    IconCircle(Icons.Filled.Notifications, "notifications") {}
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Reveal(0) {
                Column {
                    ShimmerText("সোহান ভাই", 30.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("আপনার ব্যক্তিগত AI সহকারী", color = Muted, fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.AutoAwesome, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Reveal(1) { ProfileCard(onClick = { go(Route.Profile) }) }

            Spacer(Modifier.height(18.dp))
            Reveal(2) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AiOrb { go(Route.Chat()) }
                }
            }
            Reveal(3) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    GlowBorder(Modifier.clickable { go(Route.Chat()) }, radius = 50.dp) {
                        Row(
                            Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("এআই-তে ক্লিক করুন", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Reveal(4) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard("ছবি তৈরি করুন", Icons.Filled.Photo, Color(0xFF818CF8), Modifier.weight(1f)) { go(Route.Chat(mode = "image")) }
                    FeatureCard("চ্যাট করুন", Icons.Filled.Chat, Color(0xFF34D399), Modifier.weight(1f)) { go(Route.Chat()) }
                    FeatureCard("ছবি এডিট করুন", Icons.Filled.AutoFixHigh, Color(0xFFFBBF24), Modifier.weight(1f)) { go(Route.Chat(mode = "edit")) }
                }
            }
            Spacer(Modifier.height(10.dp))
            Reveal(5) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard("অনুবাদ করুন", Icons.Filled.Translate, Color(0xFF38BDF8), Modifier.weight(1f)) { go(Route.Chat(mode = "translate")) }
                    FeatureCard("PDF পড়ুন", Icons.Filled.PictureAsPdf, Color(0xFFA78BFA), Modifier.weight(1f)) { go(Route.Chat(mode = "pdf")) }
                    FeatureCard("আরও ফিচার", Icons.Filled.Apps, Color(0xFF4ADE80), Modifier.weight(1f)) { go(Route.Chat()) }
                }
            }

            Spacer(Modifier.height(18.dp))
            Reveal(6) { WeatherCard(weather) { reload() } }

            Spacer(Modifier.height(12.dp))
            Reveal(7) {
                GlowBorder(Modifier.fillMaxWidth(), radius = 18.dp) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lightbulb, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("জানেন কি?", color = Color(0xFF4ADE80), fontWeight = FontWeight.SemiBold)
                            Text("নিয়মিত দোয়া ও ইবাদত আপনার জীবনে শান্তি আনে।", color = Muted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardBg)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavItem("হোম", Icons.Filled.Home, true) {}
            NavItem("এআই", Icons.Filled.AutoAwesome, false) { go(Route.Chat()) }
            NavItem("প্রোফাইল", Icons.Filled.Person, false) { go(Route.Profile) }
        }
    }
}

@Composable
fun AiOrb(onClick: () -> Unit) {
    val rot by rememberInfiniteTransition(label = "orb").animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "rot"
    )
    Box(Modifier.size(200.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            rotate(rot) {
                drawOval(
                    Brush.linearGradient(listOf(Violet, Cyan)),
                    topLeft = Offset(w * 0.05f, h * 0.3f),
                    size = Size(w * 0.9f, h * 0.4f),
                    style = Stroke(3.dp.toPx())
                )
            }
            rotate(-rot * 0.6f) {
                drawOval(
                    Brush.linearGradient(listOf(Cyan, Violet)),
                    topLeft = Offset(w * 0.2f, h * 0.1f),
                    size = Size(w * 0.6f, h * 0.8f),
                    style = Stroke(2.dp.toPx())
                )
            }
        }
        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Cyan, Violet))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(56.dp))
        }
    }
}

@Composable
fun ProfileCard(onClick: () -> Unit) {
    GlowBorder(Modifier.fillMaxWidth().clickable(onClick = onClick), radius = 22.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF0E7490)))),
                contentAlignment = Alignment.Center
            ) {
                Text("স", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                ShimmerText(SohanProfile.name, 18.sp)
                Text(SohanProfile.title + " | " + SohanProfile.org, color = Muted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = Muted, modifier = Modifier.size(14.dp))
                    Text(SohanProfile.location, color = Muted, fontSize = 12.sp)
                }
                Text(
                    SohanProfile.quote,
                    color = Cyan.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
                Text("বিস্তারিত দেখতে ট্যাপ করুন", color = Color(0xFFFBBF24), fontSize = 11.sp)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Cyan)
        }
    }
}

@Composable
fun FeatureCard(label: String, icon: ImageVector, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "fc").animateFloat(
        1f, 1.15f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    GlowBorder(modifier.clickable(onClick = onClick), radius = 18.dp) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, null, tint = tint,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
            )
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun NavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val c = if (selected) Cyan else Muted
        Icon(icon, label, tint = c)
        Text(label, color = c, fontSize = 11.sp)
    }
}

@Composable
fun WeatherCard(w: Weather?, onRefresh: () -> Unit) {
    GlowBorder(Modifier.fillMaxWidth(), radius = 20.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Cloud, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(12.dp))
            if (w == null) {
                Text("আবহাওয়া লোড হচ্ছে…", color = Muted, modifier = Modifier.weight(1f))
            } else {
                Column(Modifier.weight(1f)) {
                    Text("${w.temp}°C", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text(w.label, color = Muted, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(w.place, color = Color.White, fontSize = 12.sp)
                    Text("আজকের সর্বোচ্চ: ${w.max}°C", color = Muted, fontSize = 12.sp)
                    Text("সর্বনিম্ন: ${w.min}°C", color = Muted, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, "refresh", tint = Cyan)
            }
        }
    }
}
