package com.sohanbhai.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

// ---------- Shared components ----------

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
        val moon = Offset(size.width * 0.82f, size.height * 0.07f)
        drawCircle(Color(0xFFFDE68A), 26.dp.toPx(), moon)
        drawCircle(Color(0xFF0B1B33), 22.dp.toPx(), moon + Offset(9.dp.toPx(), -5.dp.toPx()))
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
fun TopBar(title: String, onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconCircle(Icons.Filled.ArrowBack, "back", onBack)
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

@Composable
fun AiAvatar() {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Cyan, Violet))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

/** Rotating glowing orb that opens the AI chat. */
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
fun FeatureCard(label: String, icon: ImageVector, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, Cyan.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
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

// ---------- Home ----------

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

            Spacer(Modifier.height(16.dp))
            Row {
                Text("সোহান ", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("ভাই", color = Cyan, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("আপনার ব্যক্তিগত AI সহকারী", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.AutoAwesome, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(18.dp))
            ProfileCard()

            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AiOrb { go(Route.Chat()) }
            }
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Cyan, RoundedCornerShape(50))
                    .background(CardBg)
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .clickable { go(Route.Chat()) }
            ) {
                Text("👆 এআই-তে ক্লিক করুন", color = Color.White, fontSize = 14.sp)
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard("ছবি তৈরি করুন", Icons.Filled.Photo, Color(0xFF818CF8), Modifier.weight(1f)) { go(Route.Image()) }
                FeatureCard("চ্যাট করুন", Icons.Filled.Chat, Color(0xFF34D399), Modifier.weight(1f)) { go(Route.Chat()) }
                FeatureCard("ছবি এডিট করুন", Icons.Filled.AutoFixHigh, Color(0xFFFBBF24), Modifier.weight(1f)) { go(Route.Image(edit = true)) }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard("অনুবাদ করুন", Icons.Filled.Translate, Color(0xFF38BDF8), Modifier.weight(1f)) { go(Route.Chat(mode = "translate")) }
                FeatureCard("PDF পড়ুন", Icons.Filled.PictureAsPdf, Color(0xFFA78BFA), Modifier.weight(1f)) { go(Route.Chat(mode = "pdf")) }
                FeatureCard("আরও ফিচার", Icons.Filled.Apps, Color(0xFF4ADE80), Modifier.weight(1f)) { go(Route.Chat()) }
            }

            Spacer(Modifier.height(18.dp))
            WeatherCard(weather) { reload() }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardBg)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lightbulb, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("জানেন কি?", color = Color(0xFF4ADE80), fontWeight = FontWeight.SemiBold)
                    Text("নিয়মিত দোয়া ও ইবাদত আপনার জীবনে শান্তি আনে।", color = Muted, fontSize = 13.sp)
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
            NavItem("ছবি", Icons.Filled.Photo, false) { go(Route.Image()) }
        }
    }
}

@Composable
fun ProfileCard() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, Cyan.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Replace this with a drawable of the person's photo when available.
        Box(
            Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(2.dp, Cyan, CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF0E7490)))),
            contentAlignment = Alignment.Center
        ) {
            Text("স", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("সোহান ভাই", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
            }
            Text("মাদ্রাসার ছাত্র | বাংলাদেশ খেলাফত মজলিস", color = Muted, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = Muted, modifier = Modifier.size(14.dp))
                Text("বাংলাদেশ", color = Muted, fontSize = 12.sp)
            }
            Text(
                "“সত্যের পথে, জ্ঞানের আলোয়…”",
                color = Cyan.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun WeatherCard(w: Weather?, onRefresh: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, Cyan.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

// ---------- Chat ----------

@Composable
fun ChatScreen(mode: String, initial: Conversation?, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var conv by remember { mutableStateOf(initial ?: Conversation()) }
    var input by remember {
        mutableStateOf(if (mode == "translate") "এই লেখাটি অনুবাদ করো: " else "")
    }
    var attachment by remember { mutableStateOf<Attachment?>(null) }
    var busy by remember { mutableStateOf(false) }
    var animId by remember { mutableStateOf<String?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@rememberLauncherForActivityResult
        val mime = ctx.contentResolver.getType(uri) ?: "application/octet-stream"
        attachment = Attachment(mime, bytes, uri.lastPathSegment ?: "file")
        if (input.isBlank()) {
            input = if (mime == "application/pdf") "এই PDF টি পড়ে মূল বিষয়গুলো বাংলায় সংক্ষেপে বলো: "
            else "এই ফাইলটি নিয়ে: "
        }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val spoken = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) input = if (input.isBlank()) spoken else "$input $spoken"
    }

    LaunchedEffect(mode) {
        if (mode == "pdf") filePicker.launch("application/pdf")
    }

    LaunchedEffect(conv.messages.size, busy) {
        val target = if (busy) conv.messages.size else conv.messages.size - 1
        if (target >= 0) listState.animateScrollToItem(target)
    }

    fun send() {
        val text = input.trim()
        val att = attachment
        if ((text.isEmpty() && att == null) || busy) return
        val history = conv.messages.toList()
        val shown = if (att != null) "📎 ${att.name}\n$text" else text
        conv.messages.add(ChatMessage(isUser = true, text = shown))
        if (conv.title == "নতুন চ্যাট") conv.title = (text.ifEmpty { att?.name ?: "ফাইল" }).take(30)
        input = ""
        attachment = null
        busy = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    Gemini.generate(ctx, history, text.ifEmpty { "এই ফাইলটি বিশ্লেষণ করো" }, att, false)
                }
            }
            busy = false
            val aiMsg = result.fold(
                onSuccess = { (t, img) ->
                    ChatMessage(
                        isUser = false,
                        text = t.ifEmpty { "দুঃখিত, উত্তর পাওয়া যায়নি।" },
                        imagePath = img?.let { Store.saveImage(it) }
                    )
                },
                onFailure = { ChatMessage(isUser = false, text = it.message ?: "কিছু একটা সমস্যা হয়েছে") }
            )
            conv.messages.add(aiMsg)
            animId = aiMsg.id
            Store.touch(conv)
        }
    }

    Box(Modifier.fillMaxSize()) {
        StarField()
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            TopBar(
                title = "সোহান ভাই · এআই",
                onBack = onBack,
                trailing = {
                    Box {
                        IconCircle(Icons.Filled.MoreVert, "menu") { menuOpen = true }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("নতুন চ্যাট") },
                                onClick = {
                                    conv = Conversation()
                                    menuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("ইতিহাস") },
                                onClick = {
                                    showHistory = true
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (conv.messages.isEmpty()) {
                    item { Greeting { input = it } }
                }
                items(conv.messages, key = { it.id }) { m ->
                    MessageBubble(m, animate = m.id == animId)
                }
                if (busy) {
                    item { TypingDots() }
                }
            }

            attachment?.let { att ->
                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📎 ${att.name}", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { attachment = null }) { Text("মুছুন", color = Cyan) }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { filePicker.launch("*/*") }) {
                    Icon(Icons.Filled.AttachFile, "attach", tint = Muted)
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("এখানে লিখুন…", color = Muted) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )
                IconButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    runCatching { micLauncher.launch(intent) }
                }) {
                    Icon(Icons.Filled.Mic, "voice", tint = Cyan)
                }
                IconButton(onClick = { send() }, enabled = !busy) {
                    Icon(Icons.Filled.Send, "send", tint = if (busy) Muted else Cyan)
                }
            }
        }
    }

    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            confirmButton = { TextButton(onClick = { showHistory = false }) { Text("বন্ধ করুন") } },
            title = { Text("চ্যাটের ইতিহাস") },
            text = {
                if (Store.conversations.isEmpty()) {
                    Text("এখনো কোনো ইতিহাস নেই", color = Muted)
                } else {
                    LazyColumn(Modifier.heightIn(max = 380.dp)) {
                        items(Store.conversations, key = { it.id }) { c ->
                            Text(
                                c.title,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        conv = c
                                        showHistory = false
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun Greeting(onPick: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("আসসালামু আলাইকুম", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "ইসলামী জ্ঞান বা যেকোনো বিষয়ে আমাকে জিজ্ঞেস করো",
            color = Muted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        val picks = listOf(
            "কুরআন" to "কুরআনের একটি ছোট সূরার বাংলা তর্জমা দাও: ",
            "সালাত" to "নামাজের ফরজ কয়টি?",
            "রোজা" to "রোজার নিয়মগুলো সংক্ষেপে বলো",
            "বিবাহ" to "ইসলামে বিবাহের গুরুত্ব সংক্ষেপে বলো"
        )
        picks.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, prompt) ->
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardBg)
                            .border(1.dp, Cyan.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                            .clickable { onPick(prompt) }
                            .padding(16.dp)
                    ) {
                        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(m: ChatMessage, animate: Boolean) {
    val isUser = m.isUser
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            AiAvatar()
            Spacer(Modifier.width(8.dp))
        }
        Column(
            Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isUser) Color(0xFF0E7490) else CardBg)
                .padding(12.dp)
        ) {
            m.imagePath?.let { path ->
                val bmp = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
                if (bmp != null) {
                    Image(
                        bmp, null,
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                    if (m.text.isNotEmpty()) Spacer(Modifier.height(8.dp))
                }
            }
            if (m.text.isNotEmpty()) {
                if (isUser || !animate) {
                    Text(m.text, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
                } else {
                    StreamingText(m.text)
                }
            }
        }
    }
}

/** Reveals text progressively; purple while writing, turns white when finished. */
@Composable
fun StreamingText(full: String) {
    var shown by remember(full) { mutableIntStateOf(0) }
    LaunchedEffect(full) {
        while (shown < full.length) {
            shown = minOf(full.length, shown + 3)
            delay(14)
        }
    }
    val done = shown >= full.length
    val color by animateColorAsState(
        if (done) Color.White else Color(0xFFC4B5FD),
        tween(900),
        label = "textColor"
    )
    Text(full.take(shown), color = color, fontSize = 15.sp, lineHeight = 22.sp)
}

@Composable
fun TypingDots() {
    val inf = rememberInfiniteTransition(label = "dots")
    Row(verticalAlignment = Alignment.CenterVertically) {
        AiAvatar()
        Spacer(Modifier.width(8.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(CardBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { i ->
                val y by inf.animateFloat(
                    0f, -6f,
                    infiniteRepeatable(tween(450, delayMillis = i * 150), RepeatMode.Reverse),
                    label = "dot$i"
                )
                Box(
                    Modifier
                        .size(8.dp)
                        .offset(y = y.dp)
                        .clip(CircleShape)
                        .background(Cyan)
                )
            }
        }
    }
}

// ---------- Image create / edit ----------

@Composable
fun ImageScreen(edit: Boolean, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("বর্গাকার (1:1)") }
    var style by remember { mutableStateOf("সাধারণ") }
    var source by remember { mutableStateOf<Attachment?>(null) }
    var result by remember { mutableStateOf<ChatMessage?>(null) }
    var busy by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@rememberLauncherForActivityResult
        val mime = ctx.contentResolver.getType(uri) ?: "image/png"
        source = Attachment(mime, bytes, uri.lastPathSegment ?: "image")
    }

    LaunchedEffect(edit) { if (edit) picker.launch("image/*") }

    val sizes = listOf("বর্গাকার (1:1)", "ল্যান্ডস্কেপ (16:9)", "পোর্ট্রেট (9:16)")
    val styles = listOf("সাধারণ", "বাস্তবধর্মী", "অ্যানিমে", "3D")

    fun generate() {
        val p = prompt.trim()
        if (p.isEmpty() || busy) return
        val full = "$p। সাইজ: $size, স্টাইল: $style"
        busy = true
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                runCatching { Gemini.generate(ctx, emptyList(), full, source, true) }
            }
            busy = false
            val msg = r.fold(
                onSuccess = { (t, img) ->
                    if (img != null) ChatMessage(isUser = false, text = t, imagePath = Store.saveImage(img))
                    else ChatMessage(isUser = false, text = t.ifEmpty { "ছবি তৈরি হয়নি, আবার চেষ্টা করো।" })
                },
                onFailure = { ChatMessage(isUser = false, text = it.message ?: "সমস্যা হয়েছে") }
            )
            result = msg
            val c = Conversation(title = p.take(30))
            c.messages.add(ChatMessage(isUser = true, text = p))
            c.messages.add(msg)
            Store.touch(c)
        }
    }

    Box(Modifier.fillMaxSize()) {
        StarField()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            TopBar(title = if (edit) "ছবি এডিট" else "Create Image", onBack = onBack)

            source?.let {
                Text("নির্বাচিত ছবি: ${it.name}", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
            }
            if (edit) {
                TextButton(onClick = { picker.launch("image/*") }) { Text("অন্য ছবি বেছে নাও", color = Cyan) }
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("কী ধরনের ছবি চান? বর্ণনা লিখুন…", color = Muted) }
            )

            Spacer(Modifier.height(18.dp))
            Text("Select a Size", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sizes.forEach { s ->
                    FilterChip(selected = size == s, onClick = { size = s }, label = { Text(s, fontSize = 12.sp) })
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Select a Style", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                styles.forEach { s ->
                    FilterChip(selected = style == s, onClick = { style = s }, label = { Text(s, fontSize = 12.sp) })
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { generate() },
                enabled = !busy && prompt.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (busy) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Filled.AutoFixHigh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("ছবি তৈরি করুন")
            }

            Spacer(Modifier.height(20.dp))
            result?.let { r ->
                val bmp = r.imagePath?.let { p -> BitmapFactory.decodeFile(p)?.asImageBitmap() }
                if (bmp != null) {
                    Image(
                        bmp, null,
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                } else if (r.text.isNotEmpty()) {
                    Text(r.text, color = Color.White)
                }
            }
        }
    }
}
