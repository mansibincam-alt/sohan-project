package com.sohanbhai.app

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(go: (Route) -> Unit) {
    val ctx = LocalContext.current
    var here by remember { mutableStateOf<HereWeather?>(null) }
    var all by remember { mutableStateOf<List<DistrictWeather>>(emptyList()) }
    var idx by remember { mutableIntStateOf(0) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) permLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    // নিজের অবস্থানের আবহাওয়া: প্রতি ১০ সেকেন্ডে অটো রিলোড
    LaunchedEffect(Unit) {
        while (true) {
            val fresh = withContext(Dispatchers.IO) { runCatching { WeatherApi.fetchHere(ctx) }.getOrNull() }
            if (fresh != null) here = fresh
            delay(10_000)
        }
    }

    // ৬৪ জেলার আবহাওয়া: একটি রিকোয়েস্টে সব জেলা, প্রতি ১০ মিনিটে নতুন করে আনা হয়
    LaunchedEffect(Unit) {
        while (true) {
            val fresh = withContext(Dispatchers.IO) { runCatching { WeatherApi.fetchDistricts() }.getOrNull() }
            if (fresh != null) all = fresh
            delay(600_000)
        }
    }

    // প্রতি ৪ সেকেন্ডে পরের জেলা
    LaunchedEffect(Unit) {
        while (true) {
            delay(4_000)
            idx = (idx + 1) % 64
        }
    }

    Box(Modifier.fillMaxSize()) {
        VideoBackground(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 10.dp, bottom = 24.dp)
        ) {
            Reveal(0) { WeatherSlot(all, here, idx) }

            Spacer(Modifier.height(22.dp))
            Reveal(1) { ProfileCard(onClick = { go(Route.Profile) }) }

            Spacer(Modifier.height(20.dp))
            Reveal(2) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AiOrb { go(Route.Chat()) }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "এআই-তে ক্লিক করুন",
                        color = Muted,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { go(Route.Chat()) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Reveal(3) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard("ছবি তৈরি করুন", Icons.Filled.Photo, Color(0xFF818CF8), Modifier.weight(1f)) { go(Route.Chat(mode = "image")) }
                    FeatureCard("চ্যাট করুন", Icons.Filled.Chat, Color(0xFF34D399), Modifier.weight(1f)) { go(Route.Chat()) }
                    FeatureCard("ছবি এডিট করুন", Icons.Filled.AutoFixHigh, Color(0xFFFBBF24), Modifier.weight(1f)) { go(Route.Chat(mode = "edit")) }
                }
            }
            Spacer(Modifier.height(10.dp))
            Reveal(4) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureCard("অনুবাদ করুন", Icons.Filled.Translate, Color(0xFF38BDF8), Modifier.weight(1f)) { go(Route.Chat(mode = "translate")) }
                    FeatureCard("PDF পড়ুন", Icons.Filled.PictureAsPdf, Color(0xFFA78BFA), Modifier.weight(1f)) { go(Route.Chat(mode = "pdf")) }
                    FeatureCard("আরও ফিচার", Icons.Filled.Apps, Color(0xFF4ADE80), Modifier.weight(1f)) { go(Route.Chat()) }
                }
            }
        }
    }
}

/** উপরের আবহাওয়া কার্ড: ৬৪ জেলা ইন-আউট অ্যানিমেশনে ঘুরবে, নিচে আপনার অবস্থান। */
@Composable
fun WeatherSlot(all: List<DistrictWeather>, here: HereWeather?, idx: Int) {
    GlowBorder(Modifier.fillMaxWidth(), radius = 22.dp) {
        Column(Modifier.padding(16.dp)) {
            if (all.isEmpty()) {
                Text("জেলার আবহাওয়া লোড হচ্ছে…", color = Muted, fontSize = 14.sp)
            } else {
                val i = idx % all.size
                AnimatedContent(
                    targetState = i,
                    transitionSpec = {
                        (slideInHorizontally(tween(450)) { it / 3 } + fadeIn(tween(450))) togetherWith
                                (slideOutHorizontally(tween(350)) { -it / 3 } + fadeOut(tween(300)))
                    },
                    label = "district"
                ) { k ->
                    val dw = all[k]
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(dw.district.name + " জেলা", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Spacer(Modifier.weight(1f))
                            Text("${(k + 1).bn()}/${all.size.bn()}", color = Muted, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${dw.weather.temp.bn()}°C",
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(dw.weather.label, color = Muted, fontSize = 13.sp)
                                Text(
                                    "সর্বোচ্চ ${dw.weather.max.bn()}° · সর্বনিম্ন ${dw.weather.min.bn()}°",
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Cyan.copy(alpha = 0.2f)))
            Spacer(Modifier.height(8.dp))
            val h = here
            Text(
                if (h == null) "আপনার অবস্থান: লোড হচ্ছে…"
                else "আপনার অবস্থান: ${h.nearest.name}${if (h.hasGps) "" else " (ডিফল্ট)"} · " +
                        "${h.weather.temp.bn()}°C ${h.weather.label}",
                color = Cyan,
                fontSize = 12.sp
            )
        }
    }
}

/** প্রোফাইল কার্ড: ছবি, নাম, স্ট্যাটাস, আর প্রতি সেকেন্ডে বদলানো জ্ঞানের বাণী। */
@Composable
fun ProfileCard(onClick: () -> Unit) {
    val quotes = SohanProfile.quotes
    var q by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000)
            q = (q + 1) % quotes.size
        }
    }

    GlowBorder(Modifier.fillMaxWidth().clickable(onClick = onClick), radius = 22.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SohanPhoto(64.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    ShimmerText(SohanProfile.name, 18.sp)
                    Text(SohanProfile.headline, color = Muted, fontSize = 13.sp)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = Cyan)
            }
            Spacer(Modifier.height(12.dp))
            Text("জ্ঞানের বাণী", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            AnimatedContent(
                targetState = q,
                transitionSpec = {
                    (fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }) togetherWith
                            (fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 3 })
                },
                label = "quote"
            ) { k ->
                Text(
                    quotes[k],
                    color = Color(0xFFC4B5FD),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("বিস্তারিত জানতে ট্যাপ করুন", color = Cyan, fontSize = 11.sp)
        }
    }
}

/**
 * এআই আইকন (Claude-এর মতো): একটি স্পার্কল যা আস্তে নিঃশ্বাস নেয় ও ধীরে ঘোরে।
 * ছোট করা হয়েছে (১২০ dp)।
 */
@Composable
fun AiOrb(onClick: () -> Unit) {
    val tr = rememberInfiniteTransition(label = "orb")
    val breathe by tr.animateFloat(
        0.9f, 1.06f,
        infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )
    val spin by tr.animateFloat(
        -10f, 10f,
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "spin"
    )
    Box(Modifier.size(120.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f * breathe
            drawCircle(
                Brush.radialGradient(listOf(Color(0x55F97316), Color.Transparent), center = c, radius = r),
                radius = r,
                center = c
            )
            rotate(spin, pivot = c) {
                val s = size.minDimension * 0.36f * breathe
                val k = s * 0.14f
                val p = Path().apply {
                    moveTo(c.x, c.y - s)
                    quadraticTo(c.x + k, c.y - k, c.x + s, c.y)
                    quadraticTo(c.x + k, c.y + k, c.x, c.y + s)
                    quadraticTo(c.x - k, c.y + k, c.x - s, c.y)
                    quadraticTo(c.x - k, c.y - k, c.x, c.y - s)
                    close()
                }
                drawPath(
                    p,
                    Brush.linearGradient(
                        listOf(Color(0xFFFB923C), Color(0xFFF472B6), Color(0xFF8B5CF6)),
                        start = Offset(c.x - s, c.y - s),
                        end = Offset(c.x + s, c.y + s)
                    )
                )
            }
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
