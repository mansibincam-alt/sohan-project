package com.sohanbhai.app

import android.content.Context

/** একবার দেখানো হয়েছে কিনা, সেই হিসাব রাখে (SharedPreferences)। */
object Prefs {
    const val KEY_WELCOME = "welcome_seen"
    const val KEY_CHAT_RULES = "chat_rules_seen"
    private const val FILE = "sohan_prefs"

    private fun sp(ctx: Context) = ctx.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun seen(ctx: Context, key: String): Boolean = sp(ctx).getBoolean(key, false)

    fun markSeen(ctx: Context, key: String) {
        sp(ctx).edit().putBoolean(key, true).apply()
    }
}
