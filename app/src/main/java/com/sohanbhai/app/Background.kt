package com.sohanbhai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * পিছনে লুপ হওয়া ভিডিও: res/raw/background_video.mp4 (নিঃশব্দ)।
 * ফিল্টার হালকা: ভিডিও স্পষ্ট দেখা যায়, সামান্য ব্লার ও হালকা ছায়া থাকে।
 * ফাইল না থাকলে তারার আকাশ দেখায়।
 */
@Composable
fun VideoBackground(modifier: Modifier = Modifier, resName: String = "background_video", starsIfMissing: Boolean = true) {
    val ctx = LocalContext.current
    val resId = remember { ctx.resources.getIdentifier(resName, "raw", ctx.packageName) }

    Box(modifier) {
        if (resId == 0) {
            if (starsIfMissing) StarField()
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
                modifier = Modifier
                    .fillMaxSize()
                    .blur(3.dp)
            )
        }
        // হালকা ছায়া: উপরে-নিচে সামান্য গাঢ়, মাঝখানে স্পষ্ট
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Night.copy(alpha = 0.25f),
                            Night.copy(alpha = 0.05f),
                            Night.copy(alpha = 0.35f)
                        )
                    )
                )
        )
    }
}
