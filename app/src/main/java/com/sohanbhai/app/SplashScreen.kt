package com.sohanbhai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * অ্যাপ খোলার ইন্ট্রো। res/raw/intro_video.mp4 (২ সেকেন্ড, শব্দসহ) চলে।
 * ভিডিও শেষ হলে বা সর্বোচ্চ ২.৫ সেকেন্ড পর হোমে যায়।
 * ভিডিও না থাকলে ৩০০ মিলি সেকেন্ডে সরে যায়।
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val resId = remember { ctx.resources.getIdentifier("intro_video", "raw", ctx.packageName) }
    val done by rememberUpdatedState(onDone)
    val finished = remember { booleanArrayOf(false) }

    fun finish() {
        if (!finished[0]) {
            finished[0] = true
            done()
        }
    }

    if (resId == 0) {
        LaunchedEffect(Unit) {
            delay(300)
            finish()
        }
        Box(Modifier.fillMaxSize().background(Color.Black))
        return
    }

    val player = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri("android.resource://${ctx.packageName}/$resId"))
            volume = 1f // শব্দ চালু
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) finish()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // সর্বোচ্চ ২.৫ সেকেন্ড পরে অবশ্যই হোমে
    LaunchedEffect(Unit) {
        delay(2_500)
        finish()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
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
}
