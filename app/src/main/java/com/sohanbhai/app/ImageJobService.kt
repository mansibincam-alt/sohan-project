package com.sohanbhai.app

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ছবি তৈরির ফোর্গ্রাউন্ড সার্ভিস। অ্যাপ বন্ধ (সোয়াইপ) করলেও এটি চলতে থাকে,
 * ফলে ছবিগুলো একটি একটি করে নোটিফিকেশনে আসে।
 */
class ImageJobService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Store.init(applicationContext)
        val req = ImageJobs.takeRequest()
        if (req == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        ImageNotifier.ensureChannels(this)
        val note = ImageNotifier.progressNotification(this, 0, req.total, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ImageNotifier.PROGRESS_ID, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(ImageNotifier.PROGRESS_ID, note)
        }

        val app = applicationContext
        scope.launch {
            try {
                ImageJobs.execute(app, req)
            } finally {
                Handler(Looper.getMainLooper()).post {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
