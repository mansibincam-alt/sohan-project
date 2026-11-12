package com.sohanbhai.app

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.graphics.Color
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
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) permLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    // নিজের অবস্থানের আবহাওয়া: প্রতি ১০ সেকেন্ডে রিলোড
    LaunchedEffect(Unit) {
        while (true) {
            val fresh = withContext(Dispatchers.IO) { runCatching { WeatherApi.fetchHere(ctx) }.getOrNull() }
            if (fresh != null) here = fresh
            delay(10_000)
        }
    }

    // ৬৪ জেলা: একটি রিকোয়েস্টে সব, প্রতি ১০ মিনিটে নতুন করে
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
        StarField()

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            // ১. একদম উপরে ছোট আবহাওয়ার বক্স
            Reveal(0) { WeatherBox(all, here, idx) }

            Spacer(Modifier.height(12.dp))

            // ২. সোহান ভাইয়ের বক্স: ভিডিও ব্যাকগ্রাউন্ড, মাঝখানে গোল ছবি, নিচে বাণী ও অ্যাবাউট
            Reveal(1) { ProfileBox(onAbout = { go(Route.Profile) }) }

            Spacer(Modifier.height(14.dp))

            // ৩. লম্বা চ্যাট বাটন
            Reveal(2) { ChatBox(onClick = { go(Route.Chat()) }) }

            Spacer(Modifier.height(14.dp))

            // ৪. ফিচার গ্রিড
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

/** উপরের ছোট আবহাওয়ার বক্স: ৬৪ জেলা ইন-আউট অ্যানিমেশনে ঘোরে, নিচে আপনার অবস্থান। */
@Composable
fun WeatherBox(all: List<DistrictWeather>, here: HereWeather?, idx: Int) {
    GlowBorder(Modifier.fillMaxWidth(), radius = 14.dp) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Cloud, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                if (all.isEmpty()) {
                    Text("জেলার আবহাওয়া লোড হচ্ছে…", color = Muted, fontSize = 12.sp)
                } else {
                    val i = idx % all.size
                    AnimatedContent(
                        targetState = i,
                        transitionSpec = {
                            (slideInHorizontally(tween(400)) { it / 3 } + fadeIn(tween(400))) togetherWith
                                    (slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(250)))
                        },
                        label = "district"
                    ) { k ->
                        val dw = all[k]
                        Text(
                            "${dw.district.name} ${dw.weather.temp.bn()}°C · ${dw.weather.label} · " +
                                    "সর্বোচ্চ ${dw.weather.max.bn()}° সর্বনিম্ন ${dw.weather.min.bn()}°",
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
                val h = here
                Text(
                    if (h == null) "আপনার অবস্থান: লোড হচ্ছে…"
                    else "আপনার অবস্থান: ${h.nearest.name}${if (h.hasGps) "" else " (ডিফল্ট)"} · ${h.weather.temp.bn()}°C",
                    color = Cyan,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * সোহান ভাইয়ের বক্স: পিছনে ভিডিও (চললে ঝর্ণার দৃশ্য), মাঝখানে গোল ছবি,
 * নিচে বাণী (প্রতি ২ সেকেন্ডে বদলায়) এবং "অ্যাবাউটে যান" বাটন।
 */
@Composable
fun ProfileBox(onAbout: () -> Unit) {
    val quotes = SohanProfile.quotes
    var q by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000)
            q = (q + 1) % quotes.size
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Cyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
    ) {
        VideoBackground(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // মাঝখানে গোল ছবি
            SohanPhoto(132.dp)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ShimmerText(SohanProfile.name, 22.sp)
                Spacer(Modifier.height(2.dp))
                Text(SohanProfile.headline, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            // বাণী: প্রতি ২ সেকেন্ডে এসে চলে যায়
            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
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
                        color = Color(0xFFE9D5FF),
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // অ্যাবাউট বাটন
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Cyan, RoundedCornerShape(50))
                    .background(Night.copy(alpha = 0.55f))
                    .clickable(onClick = onAbout)
                    .padding(horizontal = 22.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("অ্যাবাউটে যান", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ChevronRight, null, tint = Cyan, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/** লম্বা চ্যাট বাটন: "এআই এর সাথে চ্যাট করতে এখানে ক্লিক করুন"। */
@Composable
fun ChatBox(onClick: () -> Unit) {
    GlowBorder(Modifier.fillMaxWidth().clickable(onClick = onClick), radius = 20.dp) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "এআই এর সাথে চ্যাট করতে এখানে ক্লিক করুন",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
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
