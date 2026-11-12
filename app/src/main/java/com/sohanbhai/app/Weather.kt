package com.sohanbhai.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class District(val name: String, val lat: Double, val lon: Double)
data class Weather(val temp: Int, val max: Int, val min: Int, val label: String)
data class DistrictWeather(val district: District, val weather: Weather)
data class HereWeather(val weather: Weather, val nearest: District, val hasGps: Boolean)

/** বাংলাদেশের ৬৪ জেলা (আনুমানিক কেন্দ্রীয় স্থানাঙ্ক)। */
val bangladeshDistricts: List<District> = listOf(
    District("ঢাকা", 23.8103, 90.4125),
    District("ফরিদপুর", 23.6070, 89.8429),
    District("গাজীপুর", 23.9999, 90.4203),
    District("গোপালগঞ্জ", 23.0050, 89.8266),
    District("কিশোরগঞ্জ", 24.4449, 90.7766),
    District("মাদারীপুর", 23.1641, 90.1896),
    District("মানিকগঞ্জ", 23.8644, 90.0047),
    District("মুন্সীগঞ্জ", 23.5422, 90.5305),
    District("নারায়ণগঞ্জ", 23.6238, 90.5000),
    District("নরসিংদী", 23.9322, 90.7150),
    District("রাজবাড়ী", 23.7574, 89.6444),
    District("শরীয়তপুর", 23.2423, 90.4348),
    District("টাঙ্গাইল", 24.2513, 89.9167),
    District("বান্দরবান", 22.1953, 92.2184),
    District("ব্রাহ্মণবাড়িয়া", 23.9571, 91.1119),
    District("চাঁদপুর", 23.2333, 90.6712),
    District("চট্টগ্রাম", 22.3569, 91.7832),
    District("কক্সবাজার", 21.4272, 92.0058),
    District("কুমিল্লা", 23.4607, 91.1809),
    District("ফেনী", 23.0159, 91.3976),
    District("খাগড়াছড়ি", 23.1193, 91.9847),
    District("লক্ষ্মীপুর", 22.9447, 90.8282),
    District("নোয়াখালী", 22.8696, 91.0995),
    District("রাঙামাটি", 22.6370, 92.1997),
    District("বগুড়া", 24.8465, 89.3773),
    District("জয়পুরহাট", 25.0968, 89.0227),
    District("নওগাঁ", 24.8066, 88.9318),
    District("নাটোর", 24.4206, 88.9818),
    District("নবাবগঞ্জ", 24.5965, 88.2775),
    District("পাবনা", 24.0064, 89.2372),
    District("রাজশাহী", 24.3745, 88.6042),
    District("সিরাজগঞ্জ", 24.4534, 89.7007),
    District("বাগেরহাট", 22.6516, 89.7860),
    District("চুয়াডাঙ্গা", 23.6402, 88.8419),
    District("যশোর", 23.1634, 89.2182),
    District("ঝিনাইদহ", 23.5448, 89.1539),
    District("খুলনা", 22.8456, 89.5403),
    District("কুষ্টিয়া", 23.9013, 89.1205),
    District("মাগুরা", 23.4874, 89.4198),
    District("মেহেরপুর", 23.7622, 88.6318),
    District("নড়াইল", 23.1725, 89.5126),
    District("সাতক্ষীরা", 22.7185, 89.0705),
    District("বরগুনা", 22.1591, 90.1260),
    District("বরিশাল", 22.7010, 90.3535),
    District("ভোলা", 22.6859, 90.6482),
    District("ঝালকাঠি", 22.6406, 90.1987),
    District("পটুয়াখালী", 22.3596, 90.3298),
    District("পিরোজপুর", 22.5841, 89.9720),
    District("হবিগঞ্জ", 24.3749, 91.4156),
    District("মৌলভীবাজার", 24.4829, 91.7774),
    District("সুনামগঞ্জ", 25.0658, 91.4087),
    District("সিলেট", 24.8949, 91.8687),
    District("দিনাজপুর", 25.6279, 88.6332),
    District("গাইবান্ধা", 25.3288, 89.5430),
    District("কুড়িগ্রাম", 25.8054, 89.6361),
    District("লালমনিরহাট", 25.9923, 89.2847),
    District("নীলফামারী", 25.9318, 88.8560),
    District("পঞ্চগড়", 26.3411, 88.5541),
    District("রংপুর", 25.7439, 89.2752),
    District("ঠাকুরগাঁও", 26.0337, 88.4616),
    District("জামালপুর", 24.9375, 89.9378),
    District("ময়মনসিংহ", 24.7471, 90.4203),
    District("নেত্রকোণা", 24.8707, 90.7279),
    District("শেরপুর", 25.0205, 90.0152)
)

/** Bengali digits, e.g. 26 -> ২৬ */
fun Int.bn(): String =
    toString().map { c -> if (c in '0'..'9') "০১২৩৪৫৬৭৮৯"[c - '0'] else c }.joinToString("")

object WeatherApi {
    private val http = OkHttpClient()

    /** One request for all 64 districts (multi-location Open-Meteo call). */
    fun fetchDistricts(): List<DistrictWeather> {
        val lats = bangladeshDistricts.joinToString(",") { it.lat.toString() }
        val lons = bangladeshDistricts.joinToString(",") { it.lon.toString() }
        val arr = JSONArray(getJson(lats, lons))
        return bangladeshDistricts.mapIndexed { i, d ->
            DistrictWeather(d, parse(arr.getJSONObject(i)))
        }
    }

    /** Weather for the phone's location (Dhaka if no location). */
    fun fetchHere(ctx: Context): HereWeather {
        val loc = lastLocation(ctx)
        val lat = loc?.first ?: 23.8103
        val lon = loc?.second ?: 90.4125
        val w = parse(JSONObject(getJson(lat.toString(), lon.toString())))
        return HereWeather(w, nearest(lat, lon), loc != null)
    }

    fun nearest(lat: Double, lon: Double): District =
        bangladeshDistricts.minByOrNull { (it.lat - lat) * (it.lat - lat) + (it.lon - lon) * (it.lon - lon) }!!

    private fun getJson(lats: String, lons: String): String {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lats&longitude=$lons" +
                "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
        return http.newCall(Request.Builder().url(url).build()).execute().use { it.body?.string().orEmpty() }
    }

    private fun parse(o: JSONObject): Weather {
        val cur = o.getJSONObject("current")
        val daily = o.getJSONObject("daily")
        return Weather(
            temp = cur.getDouble("temperature_2m").roundToInt(),
            max = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
            min = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
            label = describe(cur.getInt("weather_code"))
        )
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
