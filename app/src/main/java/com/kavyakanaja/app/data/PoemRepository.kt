package com.kavyakanaja.app.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.Calendar

class PoemRepository(private val context: Context) {

    private var cachedPoems: List<Poem>? = null

    fun getPoems(): List<Poem> {
        cachedPoems?.let { return it }
        return try {
            val inputStream = context.assets.open("poems.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Poem>>() {}.type
            val poems: List<Poem> = Gson().fromJson(reader, type) ?: emptyList()
            reader.close()
            cachedPoems = poems
            Log.d("PoemRepository", "Loaded ${poems.size} poems")
            poems
        } catch (e: Exception) {
            Log.e("PoemRepository", "Failed to load poems: ${e.message}", e)
            emptyList()
        }
    }

    fun getPoemOfTheDay(): Poem? {
        val poems = getPoems()
        if (poems.isEmpty()) return null
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return poems[dayOfYear % poems.size]
    }
}
