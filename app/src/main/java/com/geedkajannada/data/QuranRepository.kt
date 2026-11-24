package com.geedkajannada.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class Surah(
    val id: Int,
    val nameSo: String,
    val nameAr: String,
    val verseCount: Int,
    val isMakki: Boolean,
)

data class SurahText(val surah: Surah, val verses: List<String>)

/** Akhriska JSON-ka assets/quran.json (offline). Qoraalka ka timid dataset furan: eeg LICENSES. */
object QuranParser {
    fun parse(json: String): List<SurahText> {
        val arr = JSONArray(json)
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val v = o.getJSONArray("v")
            val verses = List(v.length()) { v.getString(it) }
            SurahText(
                Surah(
                    id = o.getInt("id"),
                    nameSo = o.getString("so"),
                    nameAr = o.getString("ar"),
                    verseCount = o.getInt("n"),
                    isMakki = o.getString("type") == "makki",
                ),
                verses,
            )
        }
    }

    fun filter(list: List<Surah>, query: String): List<Surah> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return list
        return list.filter {
            it.id.toString() == q ||
                it.nameSo.lowercase().contains(q) ||
                it.nameSo.lowercase().replace("-", "").replace("'", "").contains(q.replace("-", "").replace("'", "")) ||
                it.nameAr.contains(q)
        }
    }
}

class QuranRepository(private val context: Context) {
    @Volatile
    private var cache: List<SurahText>? = null

    private suspend fun load(): List<SurahText> {
        cache?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = context.assets.open("quran.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            QuranParser.parse(text).also { cache = it }
        }
    }

    suspend fun surahs(): List<Surah> = load().map { it.surah }

    suspend fun surah(id: Int): SurahText? = load().firstOrNull { it.surah.id == id }
}
