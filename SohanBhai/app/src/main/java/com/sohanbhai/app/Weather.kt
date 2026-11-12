package com.sohanbhai.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.roundToInt

data class Weather(val temp: Int, val max: Int, val min: Int, val label: String, val place: String)

/** Free, key-less API: https://open-meteo.com */
object WeatherApi {
    private val http = OkHttpClient()

    fun fetch(ctx: Context): Weather {
        val loc = lastLocation(ctx)
        val lat = loc?.first ?: 23.8103   // Dhaka fallback
        val lon = loc?.second ?: 90.4125
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
        return http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            val j = JSONObject(resp.body?.string().orEmpty())
            val cur = j.getJSONObject("current")
            val daily = j.getJSONObject("daily")
            Weather(
                temp = cur.getDouble("temperature_2m").roundToInt(),
                max = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
                min = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
                label = describe(cur.getInt("weather_code")),
                place = if (loc == null) "ঢাকা, বাংলাদেশ" else "আপনার অবস্থান"
            )
        }
    }

    private fun describe(code: Int): String = when (code) {
        0 -> "পরিষ্কার আকাশ"
        1, 2, 3 -> "আংশিক মেঘলা"
        45, 48 -> "কুয়াশা"
        in 51..67, in 80..82 -> "বৃষ্টি"
        in 71..77, 85, 86 -> "তুষারপাত"
        in 95..99 -> "ঝড়ো বৃষ্টি"
        else -> "মেঘলা"
    }

    @SuppressLint("MissingPermission")
    private fun lastLocation(ctx: Context): Pair<Double, Double>? {
        val granted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .firstOrNull()
        return loc?.let { it.latitude to it.longitude }
    }
}
