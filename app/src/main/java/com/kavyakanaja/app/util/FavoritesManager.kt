package com.kavyakanaja.app.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FavoritesManager {

    private const val PREFS_NAME = "kavya_kanaja_prefs"
    private const val KEY_FAVORITES = "favorite_poem_ids"
    private const val KEY_STREAK_DATE = "streak_last_date"
    private const val KEY_STREAK_COUNT = "streak_count"
    private const val KEY_XP = "user_xp"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --------------- XP and Leveling ---------------
    fun getXP(context: Context): Int {
        return prefs(context).getInt(KEY_XP, 0)
    }

    fun addXP(context: Context, amount: Int) {
        val current = getXP(context)
        prefs(context).edit().putInt(KEY_XP, current + amount).apply()
    }

    fun getLevel(xp: Int): Pair<Int, Float> {
        // Level 1: 0-100, Level 2: 100-250, Level 3: 250-450, etc.
        var level = 1
        var xpNeeded = 100
        var currentXp = xp
        while (currentXp >= xpNeeded) {
            currentXp -= xpNeeded
            level++
            xpNeeded = (xpNeeded * 1.5).toInt()
        }
        return Pair(level, currentXp.toFloat() / xpNeeded)
    }

    // --------------- Favorites ---------------
    fun getFavoriteIds(context: Context): Set<Int> {
        val json = prefs(context).getString(KEY_FAVORITES, "[]") ?: "[]"
        val type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson<List<Int>>(json, type).toSet()
    }

    fun toggleFavorite(context: Context, poemId: Int): Boolean {
        val current = getFavoriteIds(context).toMutableSet()
        val added = if (current.contains(poemId)) {
            current.remove(poemId)
            false
        } else {
            current.add(poemId)
            true
        }
        prefs(context).edit().putString(KEY_FAVORITES, Gson().toJson(current.toList())).apply()
        return added
    }

    fun isFavorite(context: Context, poemId: Int): Boolean = getFavoriteIds(context).contains(poemId)

    // --------------- Streak ---------------
    private const val KEY_ACTIVE_DATES = "active_dates"

    fun getActiveDates(context: Context): Set<String> {
        val json = prefs(context).getString(KEY_ACTIVE_DATES, "[]") ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(json, type).toSet()
    }

    fun updateStreak(context: Context): Int {
        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val lastDate = prefs(context).getString(KEY_STREAK_DATE, "") ?: ""
        val currentStreak = prefs(context).getInt(KEY_STREAK_COUNT, 0)
        
        // Add to active dates history
        val activeDates = getActiveDates(context).toMutableSet()
        var updatedDates = false
        if (!activeDates.contains(today)) {
            activeDates.add(today)
            prefs(context).edit().putString(KEY_ACTIVE_DATES, Gson().toJson(activeDates.toList())).apply()
            updatedDates = true
        }

        if (lastDate == today) return currentStreak

        val yesterday = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            .format(java.util.Date(System.currentTimeMillis() - 86_400_000))

        val newStreak = if (lastDate == yesterday) currentStreak + 1 else 1
        prefs(context).edit()
            .putString(KEY_STREAK_DATE, today)
            .putInt(KEY_STREAK_COUNT, newStreak)
            .apply()
        return newStreak
    }

    fun getStreak(context: Context): Int = prefs(context).getInt(KEY_STREAK_COUNT, 0)
}
