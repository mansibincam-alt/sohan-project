package com.sohanbhai.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/** ছবি তৈরির নোটিফিকেশন: অগ্রগতি (চলমান) ও প্রতিটি তৈরি ছবি (ট্যাপ করলে চ্যাট খোলে)। */
object ImageNotifier {
    const val PROGRESS_ID = 4001
    private const val DONE_BASE = 5000
    const val EXTRA_CONV = "conv_id"
    private const val CH_PROGRESS = "image_progress"
    private const val CH_DONE = "image_done"

    /** নোটিফিকেশন থেকে যে চ্যাট খুলতে হবে তার আইডি (MainActivity ও App পড়ে)। */
    val openRequest = mutableStateOf<String?>(null)

    fun ensureChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(CH_PROGRESS, "ছবি তৈরির অগ্রগতি", NotificationManager.IMPORTANCE_LOW)
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_DONE, "তৈরি হওয়া ছবি", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    private fun openIntent(ctx: Context, convId: String?): PendingIntent {
        val i = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (convId != null) putExtra(EXTRA_CONV, convId)
        }
        return PendingIntent.getActivity(
            ctx,
            convId?.hashCode() ?: 0,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun progressNotification(ctx: Context, finished: Int, total: Int, pct: Int): Notification =
        NotificationCompat.Builder(ctx, CH_PROGRESS)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle("সোহান ভাই ছবি তৈরি করছে")
            .setContentText("${bnDigits(finished)}/${bnDigits(total)} সম্পন্ন · ${bnDigits(pct)}%")
            .setProgress(100, pct, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent(ctx, null))
            .build()

    fun progress(ctx: Context, finished: Int, total: Int, pct: Int) {
        runCatching {
            NotificationManagerCompat.from(ctx)
                .notify(PROGRESS_ID, progressNotification(ctx, finished, total, pct))
        }
    }

    /** একটি ছবি তৈরি হলে আলাদা নোটিফিকেশন, ছবিসহ। */
    fun image(ctx: Context, convId: String, idx: Int, total: Int, path: String) {
        runCatching {
            val bmp = BitmapFactory.decodeFile(path)
            val b = NotificationCompat.Builder(ctx, CH_DONE)
                .setSmallIcon(android.R.drawable.ic_menu_gallery)
                .setContentTitle("ছবি ${bnDigits(idx)}/${bnDigits(total)} তৈরি হয়েছে")
                .setContentText("দেখতে ট্যাপ করুন")
                .setAutoCancel(true)
                .setContentIntent(openIntent(ctx, convId))
            if (bmp != null) {
                b.setLargeIcon(bmp)
                b.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bmp))
            }
            NotificationManagerCompat.from(ctx).notify(DONE_BASE + idx, b.build())
        }
    }
}
