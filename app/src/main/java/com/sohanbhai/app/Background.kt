package com.sohanbhai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * Looping, muted background video from res/raw/background_video.mp4.
 * Put a waterfall / mountain clip there (keep it ~1080p, ~5-10 MB, so the APK stays small).
 * If the file is missing, the animated starfield is shown instead.
 */
@Composable
fun VideoBackground(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val resId = remember { ctx.resources.getIdentifier("background_video", "raw", ctx.packageName) }

    Box(modifier) {
        if (resId == 0) {
            StarField()
        } else {
            val player = remember {
                ExoPlayer.Builder(ctx).build().apply {
                    setMediaItem(MediaItem.fromUri("android.resource://${ctx.packageName}/$resId"))
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    prepare()
                    playWhenReady = true
                }
            }
            DisposableEffect(player) {
                onDispose { player.release() }
            }
            AndroidView(
                factory = { c ->
                    PlayerView(c).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        this.player = player
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        // darkening so text stays readable
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Night.copy(alpha = 0.45f), Night.copy(alpha = 0.8f), Night)
                    )
                )
        )
    }
}
