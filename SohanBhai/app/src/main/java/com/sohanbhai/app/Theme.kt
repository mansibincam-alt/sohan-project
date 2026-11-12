package com.sohanbhai.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF22D3EE)
val Violet = Color(0xFF8B5CF6)
val CardBg = Color(0xFF111C33)
val Muted = Color(0xFF94A3B8)
val Night = Color(0xFF060E1E)

@Composable
fun SohanBhaiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Cyan,
            secondary = Violet,
            background = Night,
            surface = CardBg,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
