package com.sohanbhai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

sealed class Route {
    object Home : Route()
    data class Chat(val mode: String = "chat", val conv: Conversation? = null) : Route()
    object Profile : Route()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        setContent {
            SohanBhaiTheme { App() }
        }
    }
}

@Composable
fun App() {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    BackHandler(enabled = route != Route.Home) { route = Route.Home }

    val go: (Route) -> Unit = { route = it }
    when (val r = route) {
        Route.Home -> HomeScreen(go)
        is Route.Chat -> ChatScreen(r.mode, r.conv, onBack = { route = Route.Home })
        Route.Profile -> ProfileScreen(onBack = { route = Route.Home })
    }
}
