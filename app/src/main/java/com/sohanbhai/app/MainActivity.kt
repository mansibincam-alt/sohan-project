package com.sohanbhai.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

sealed class Route {
    object Splash : Route()
    object Home : Route()
    data class Chat(val mode: String = "chat", val conv: Conversation? = null) : Route()
    object Profile : Route()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        readOpenRequest(intent)
        setContent {
            SohanBhaiTheme { App() }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readOpenRequest(intent)
    }

    /** ছবির নোটিফিকেশন থেকে এলে, কোন চ্যাট খুলতে হবে সেটি রাখে। */
    private fun readOpenRequest(i: Intent?) {
        i?.getStringExtra(ImageNotifier.EXTRA_CONV)?.let { ImageNotifier.openRequest.value = it }
    }
}

@Composable
fun App() {
    var route by remember { mutableStateOf<Route>(Route.Splash) }
    BackHandler(enabled = route != Route.Home && route != Route.Splash) { route = Route.Home }

    // নোটিফিকেশন থেকে এলে সংশ্লিষ্ট চ্যাট খোলে (স্প্ল্যাশ শেষ হওয়ার পরে)
    val openId by ImageNotifier.openRequest
    LaunchedEffect(openId, route) {
        val id = openId ?: return@LaunchedEffect
        if (route == Route.Splash) return@LaunchedEffect
        ImageNotifier.openRequest.value = null
        Store.conversations.firstOrNull { it.id == id }?.let { route = Route.Chat(conv = it) }
    }

    val go: (Route) -> Unit = { route = it }
    when (val r = route) {
        Route.Splash -> SplashScreen(onDone = { route = Route.Home })
        Route.Home -> HomeScreen(go)
        is Route.Chat -> ChatScreen(r.mode, r.conv, onBack = { route = Route.Home })
        Route.Profile -> ProfileScreen(onBack = { route = Route.Home })
    }
}
