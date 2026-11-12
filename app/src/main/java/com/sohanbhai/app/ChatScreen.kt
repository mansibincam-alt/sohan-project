package com.sohanbhai.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ToolInfo(val key: String, val label: String, val icon: ImageVector, val color: Color, val subtitle: String)

/** খালি চ্যাটে যে চারটি কার্ড দেখায়। */
val suggestionTools = listOf(
    ToolInfo("image", "ছবি তৈরি করুন", Icons.Filled.Photo, Color(0xFFA78BFA), "দৃষ্টিনন্দন ছবি তৈরি করুন"),
    ToolInfo("code", "কোড লিখুন", Icons.Filled.Code, Color(0xFF34D399), "প্রোগ্রামিংয়ে সাহায্য নিন"),
    ToolInfo("ideas", "নতুন ধারণা", Icons.Filled.Lightbulb, Color(0xFFFBBF24), "নতুন আইডিয়া খুঁজুন"),
    ToolInfo("summarize", "সংক্ষেপ করুন", Icons.Filled.Description, Color(0xFF60A5FA), "মূল পয়েন্ট বের করুন")
)

/** অ্যাটাচ (+) শিটে যা যা থাকবে। */
private val allTools = listOf(
    ToolInfo("image", "ছবি তৈরি করুন", Icons.Filled.Photo, Color(0xFFA78BFA), "বর্ণনা লিখে ছবি বানান"),
    ToolInfo("edit", "ছবি এডিট করুন", Icons.Filled.AutoFixHigh, Color(0xFFFBBF24), "একটি ছবি বেছে নিয়ে বদলান"),
    ToolInfo("pdf", "PDF পড়ুন", Icons.Filled.PictureAsPdf, Color(0xFFA78BFA), "PDF থেকে মূল বিষয় বের করুন"),
    ToolInfo("translate", "অনুবাদ", Icons.Filled.Translate, Color(0xFF38BDF8), "বাংলা ↔ ইংরেজি"),
    ToolInfo("code", "কোড লিখুন", Icons.Filled.Code, Color(0xFF34D399), "প্রোগ্রামিংয়ে সাহায্য নিন"),
    ToolInfo("ideas", "নতুন ধারণা", Icons.Filled.Lightbulb, Color(0xFFFBBF24), "নতুন আইডিয়া খুঁজুন"),
    ToolInfo("summarize", "সংক্ষেপ করুন", Icons.Filled.Description, Color(0xFF60A5FA), "মূল পয়েন্ট বের করুন")
)

private val toolKeys = setOf("image", "edit", "pdf", "translate")

/** প্রতিটি টুলের আগে যোগ হওয়া নির্দেশ। */
private val toolPrefix = mapOf(
    "image" to "এই বর্ণনা অনুযায়ী একটি ছবি তৈরি করো: ",
    "edit" to "এই ছবিটি এডিট করে নতুন ছবি তৈরি করো: ",
    "code" to "প্রোগ্রামিংয়ে সাহায্য করো। কোড সহ স্পষ্ট উত্তর দাও: ",
    "ideas" to "কিছু নতুন ও ভালো ধারণা দাও: ",
    "summarize" to "নিচের বিষয়টির মূল পয়েন্টগুলো সংক্ষেপে বলো: ",
    "translate" to "নিচের লেখাটি অনুবাদ করো (বাংলা ↔ ইংরেজি): ",
    "pdf" to ""
)

/** ছবি তৈরির ৩০ সেকেন্ড বিরতি: শেষ অনুরোধের সময় এখানে থাকে। */
private const val IMAGE_COOLDOWN_MS = 30_000L
private var lastRequestAt = 0L

private fun readAttachment(ctx: Context, uri: Uri): Attachment? {
    val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val mime = ctx.contentResolver.getType(uri) ?: "application/octet-stream"
    return Attachment(mime, bytes, uri.lastPathSegment ?: "file")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(mode: String, initial: Conversation?, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var conv by remember { mutableStateOf(initial ?: Conversation()) }
    var tool by remember { mutableStateOf(if (mode in toolKeys) mode else "") }
    var input by remember { mutableStateOf("") }
    var attachment by remember { mutableStateOf<Attachment?>(null) }
    var busy by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var animId by remember { mutableStateOf<String?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var sheetOpen by remember { mutableStateOf(false) }
    var cooldownLeft by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    // ছবি তৈরির ৩০ সেকেন্ড কাউন্টডাউন
    LaunchedEffect(busy) {
        while (true) {
            val left = ((lastRequestAt + IMAGE_COOLDOWN_MS - System.currentTimeMillis()) / 1000).toInt()
            cooldownLeft = if (left > 0) left else 0
            delay(500)
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) readAttachment(ctx, uri)?.let { attachment = it }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) readAttachment(ctx, uri)?.let { attachment = it }
    }

    // মাইক্রোফোন: আগের মতো Google-এর ভয়েস রিকগনিশন
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val spoken = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) input = if (input.isBlank()) spoken else "$input $spoken"
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            runCatching { micLauncher.launch(intent) }
        } else {
            notice = "মাইক্রোফোনের অনুমতি দিন"
        }
    }

    // হোম থেকে নির্দিষ্ট ফিচারে ঢুকলে সংশ্লিষ্ট পিকার নিজে থেকে খুলবে
    LaunchedEffect(mode) {
        when (mode) {
            "pdf" -> filePicker.launch("application/pdf")
            "edit" -> imagePicker.launch("image/*")
        }
    }

    LaunchedEffect(conv.messages.size, busy) {
        val target = if (busy) conv.messages.size else conv.messages.size - 1
        if (target >= 0) listState.animateScrollToItem(target)
    }

    fun send() {
        val text = input.trim()
        val att = attachment
        if ((text.isEmpty() && att == null) || busy) return

        val t = tool
        val isImage = t == "image" || t == "edit"

        if (isImage) {
            val left = ((lastRequestAt + IMAGE_COOLDOWN_MS - System.currentTimeMillis()) / 1000).toInt()
            if (left > 0) {
                notice = "ছবি তৈরির আগে ${left} সেকেন্ড অপেক্ষা করুন"
                return
            }
        }
        if (t == "edit" && att?.mime?.startsWith("image/") != true) {
            notice = "আগে একটি ছবি বেছে নিন"
            imagePicker.launch("image/*")
            return
        }

        val promptText = (toolPrefix[t].orEmpty() + text).ifEmpty {
            if (isImage) "ছবিটি এডিট করো" else "এই ফাইলটি বিশ্লেষণ করো"
        }
        val history = conv.messages.toList()

        val shown = (if (att != null) "📎 ${att.name}\n" else "") + text
        conv.messages.add(ChatMessage(isUser = true, text = shown.trim()))
        if (conv.title == "নতুন চ্যাট") conv.title = (text.ifEmpty { att?.name ?: "ফাইল" }).take(30)
        input = ""
        attachment = null
        notice = null
        busy = true
        lastRequestAt = System.currentTimeMillis()

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { Gemini.generate(ctx, history, promptText, att, isImage) }
            }
            busy = false
            val aiMsg = result.fold(
                onSuccess = { (txt, img) ->
                    ChatMessage(
                        isUser = false,
                        text = txt.ifEmpty { if (img == null) "দুঃখিত, উত্তর পাওয়া যায়নি।" else "" },
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                HistoryPanel(
                    selectedId = conv.id,
                    onNew = {
                        conv = Conversation()
                        scope.launch { drawerState.close() }
                    },
                    onPick = { c ->
                        conv = c
                        scope.launch { drawerState.close() }
                    },
                    onDelete = { c ->
                        Store.delete(c)
                        if (conv.id == c.id) conv = Conversation()
                    }
                )
            }
        }
    ) {
        // চ্যাটের ব্যাকগ্রাউন্ড: সাদামাটা গাঢ় রঙ
        Box(Modifier.fillMaxSize().background(Night)) {
            Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {

                // উপরে: ইতিহাস (বাঁয়ে), শিরোনাম (মাঝে), ৩-ডট মেনু (ডানে)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconCircle(Icons.Filled.Menu, "history") { scope.launch { drawerState.open() } }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ShimmerText("সোহান ভাই", 18.sp)
                    }
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
                                text = { Text("এই চ্যাট মুছুন") },
                                onClick = {
                                    Store.delete(conv)
                                    conv = Conversation()
                                    menuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("সব ইতিহাস মুছুন") },
                                onClick = {
                                    confirmClear = true
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (conv.messages.isEmpty()) {
                        item { Welcome(onPick = { info -> tool = info.key }) }
                    }
                    items(conv.messages, key = { it.id }) { m ->
                        MessageBubble(m, animate = m.id == animId)
                    }
                    if (busy) {
                        item { TypingDots() }
                    }
                }

                // নির্বাচিত টুল চিপ
                val activeTool = allTools.find { it.key == tool }
                if (activeTool != null) {
                    Row(
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(CardBg)
                            .border(1.dp, activeTool.color.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(activeTool.icon, null, tint = activeTool.color, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(activeTool.label, color = Color.White, fontSize = 13.sp)
                        IconButton(onClick = { tool = "" }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, "remove tool", tint = Muted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // সংযুক্ত ফাইল
                attachment?.let { att ->
                    Row(
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📎 ${att.name}",
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { attachment = null }) { Text("মুছুন", color = Cyan) }
                    }
                }

                notice?.let {
                    Text(
                        it,
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                    )
                }

                // ইনপুট বক্স: লেখা উপরে; নিচে + (বাঁয়ে), মাইক ও পাঠানো (ডানে) — সব বক্সের ভেতরে
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF111827))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("সোহান ভাইকে লিখুন…", color = Muted) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Cyan
                        ),
                        maxLines = 6
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { sheetOpen = true }) {
                            Icon(Icons.Filled.Add, "features", tint = Muted)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { micPermission.launch(Manifest.permission.RECORD_AUDIO) }) {
                            Icon(Icons.Filled.Mic, "voice", tint = Cyan)
                        }
                        SendButton(
                            active = (input.isNotBlank() || attachment != null) && !busy,
                            busy = busy
                        ) { send() }
                    }
                }
            }
        }
    }

    // অ্যাটাচ (+) শিট: ফিচারগুলো এখানে; ক্লিক করলে চ্যাটে সেই টুল সিলেক্ট হয়
    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { sheetOpen = false },
            containerColor = CardBg
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("ফিচার", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                allTools.forEach { info ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                tool = info.key
                                sheetOpen = false
                                when (info.key) {
                                    "pdf" -> filePicker.launch("application/pdf")
                                    "edit" -> imagePicker.launch("image/*")
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(info.color.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(info.icon, null, tint = info.color, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(info.label, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(info.subtitle, color = Muted, fontSize = 12.sp)
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            sheetOpen = false
                            filePicker.launch("*/*")
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AttachFile, null, tint = Muted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("ফাইল সংযুক্ত করুন", color = Color.White)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("সব ইতিহাস মুছবেন?") },
            text = { Text("এটি আর ফেরত আনা যাবে না।") },
            confirmButton = {
                TextButton(onClick = {
                    Store.clearAll()
                    conv = Conversation()
                    confirmClear = false
                }) { Text("মুছুন") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
fun Welcome(onPick: (ToolInfo) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Reveal(0) { SohanPhoto(84.dp) }
        Spacer(Modifier.height(16.dp))
        Reveal(1) { ShimmerText("যা খুশি জিজ্ঞেস করুন…", 22.sp) }
        Spacer(Modifier.height(22.dp))
        suggestionTools.forEachIndexed { i, info ->
            Reveal(i + 2) { ToolCard(info) { onPick(info) } }
        }
    }
}

@Composable
fun ToolCard(info: ToolInfo, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF111827))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(info.color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(info.icon, null, tint = info.color)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(info.label, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(info.subtitle, color = Muted, fontSize = 12.sp)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Muted)
    }
}

@Composable
fun SendButton(active: Boolean, busy: Boolean, onClick: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "send").animateFloat(
        1f, 1.05f,
        infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        Modifier
            .size(40.dp)
            .graphicsLayer {
                val s = if (active) pulse else 1f
                scaleX = s
                scaleY = s
            }
            .clip(CircleShape)
            .background(
                if (active) Brush.linearGradient(listOf(Cyan, Violet))
                else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF334155)))
            )
            .clickable(enabled = active, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Icon(Icons.Filled.ArrowUpward, "send", tint = Color.White)
        }
    }
}

@Composable
fun HistoryPanel(
    selectedId: String,
    onNew: () -> Unit,
    onPick: (Conversation) -> Unit,
    onDelete: (Conversation) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Night)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("ইতিহাস", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardBg)
                .clickable(onClick = onNew)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, null, tint = Cyan)
            Spacer(Modifier.width(10.dp))
            Text("নতুন চ্যাট", color = Color.White)
        }
        Spacer(Modifier.height(14.dp))
        if (Store.conversations.isEmpty()) {
            Text("এখনো কোনো চ্যাট নেই", color = Muted, fontSize = 13.sp)
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(Store.conversations, key = { it.id }) { c ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (c.id == selectedId) CardBg else Color.Transparent)
                        .clickable { onPick(c) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(c.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(formatDate(c.updated), color = Muted, fontSize = 11.sp)
                    }
                    IconButton(onClick = { onDelete(c) }) {
                        Icon(Icons.Filled.Delete, "delete", tint = Muted)
                    }
                }
            }
        }
    }
}

private fun formatDate(ts: Long): String =
    SimpleDateFormat("dd MMM, HH:mm", Locale.ENGLISH).format(Date(ts))

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
                .background(
                    if (isUser) Brush.linearGradient(listOf(Color(0xFF0E7490), Color(0xFF4338CA)))
                    else Brush.linearGradient(listOf(Color(0xFF111827), Color(0xFF111827)))
                )
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            m.imagePath?.let { path ->
                val bmp = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = null,
                        modifier = Modifier
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

/** লেখা ধীরে ধীরে আসে; লেখা চলাকালীন বেগুনি, শেষ হলে সাদা। */
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
                .background(Color(0xFF111827))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { i ->
                val y by inf.animateFloat(
                    0f, -5f,
                    infiniteRepeatable(tween(450, delayMillis = i * 150), RepeatMode.Reverse),
                    label = "dot$i"
                )
                Box(
                    Modifier
                        .size(7.dp)
                        .graphicsLayer { translationY = y.dp.toPx() }
                        .clip(CircleShape)
                        .background(Cyan)
                )
            }
        }
    }
}
