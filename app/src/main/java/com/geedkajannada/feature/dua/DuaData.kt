package com.geedkajannada.feature.dua

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.geedkajannada.data.QuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class DuaCategory(val id: String, val so: String, val en: String, val order: Int)

data class Dua(
    val id: String,
    val categoryId: String,
    val surah: Int,
    val from: Int,
    val to: Int,
    val repeat: Int,
    val so: String,
    val en: String,
    val needsReview: Boolean,
    /** Qoraalka Carabiga ee dataset-ka Quraanka (lama qoro gacanta). */
    val arabic: String = "",
) {
    val reference: String get() = if (to > from) "$surah:$from-$to" else "$surah:$from"
}

data class DuaLibrary(val categories: List<DuaCategory>, val duas: List<Dua>)

object DuaParser {
    fun parse(json: String): DuaLibrary {
        val root = JSONObject(json)
        val cats = root.getJSONArray("categories")
        val list = root.getJSONArray("duas")
        return DuaLibrary(
            categories = List(cats.length()) {
                val c = cats.getJSONObject(it)
                DuaCategory(c.getString("id"), c.getString("so"), c.getString("en"), c.getInt("order"))
            }.sortedBy { it.order },
            duas = List(list.length()) {
                val d = list.getJSONObject(it)
                Dua(
                    id = d.getString("id"),
                    categoryId = d.getString("category"),
                    surah = d.getInt("surah"),
                    from = d.getInt("from"),
                    to = d.getInt("to"),
                    repeat = d.getInt("repeat"),
                    so = d.getString("so"),
                    en = d.getString("en"),
                    needsReview = d.optBoolean("needsReview", true),
                )
            },
        )
    }
}

/** Raadin aan harakaad iyo qaabka alif ka xiisayn (Carabi, Soomaali, Ingiriisi). */
object DuaSearch {
    fun normalize(text: String): String = buildString {
        for (c in text.lowercase()) {
            when (c) {
                // harakaad, calaamadaha mushaf-ka iyo tatweel waa la tuuraa
                in 'ً'..'ٟ', in 'ۖ'..'ۭ', 'ٰ', 'ـ' -> Unit
                'أ', 'إ', 'آ', 'ٱ' -> append('ا')
                'ى' -> append('ي')
                '\'', '’', '-' -> Unit
                else -> append(c)
            }
        }
    }.trim()

    fun filter(duas: List<Dua>, query: String): List<Dua> {
        val q = normalize(query)
        if (q.isEmpty()) return duas
        return duas.filter { d ->
            d.reference == q || listOf(d.so, d.en, d.arabic).any { normalize(it).contains(q) }
        }
    }
}

/** Maktabadda (assets/dua.json + quran.json) iyo xogta isticmaalaha (favorites, dhawaan, tirada). */
class DuaRepository(
    private val context: Context,
    private val quran: QuranRepository,
    private val dataStore: DataStore<Preferences>,
) {
    @Volatile
    private var cache: DuaLibrary? = null

    suspend fun library(): DuaLibrary = cache ?: withContext(Dispatchers.IO) {
        val json = context.assets.open("dua.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val parsed = DuaParser.parse(json)
        val withText = parsed.duas.map { d ->
            val verses = quran.surah(d.surah)?.verses ?: error("surah ${d.surah}")
            d.copy(arabic = verses.subList(d.from - 1, d.to).joinToString(" "))
        }
        parsed.copy(duas = withText).also { cache = it }
    }

    private val favoritesKey = stringSetPreferencesKey("dua_favorites")
    private val recentKey = stringPreferencesKey("dua_recent")
    private val sizeKey = intPreferencesKey("dua_text_size")
    private fun countKey(date: String, id: String) = intPreferencesKey("duac_${date}_$id")

    val favorites: Flow<Set<String>> = dataStore.data.map { it[favoritesKey] ?: emptySet() }
    val recent: Flow<List<String>> = dataStore.data.map { p -> p[recentKey]?.split(',')?.filter { it.isNotEmpty() }.orEmpty() }
    val textSize: Flow<Int> = dataStore.data.map { it[sizeKey] ?: 1 }

    fun count(date: String, id: String): Flow<Int> = dataStore.data.map { it[countKey(date, id)] ?: 0 }

    suspend fun toggleFavorite(id: String) {
        dataStore.edit {
            val set = it[favoritesKey] ?: emptySet()
            it[favoritesKey] = if (id in set) set - id else set + id
        }
    }

    suspend fun markRecent(id: String) {
        dataStore.edit { p ->
            val list = p[recentKey]?.split(',')?.filter { it.isNotEmpty() && it != id }.orEmpty()
            p[recentKey] = (listOf(id) + list).take(MAX_RECENT).joinToString(",")
        }
    }

    suspend fun setTextSize(step: Int) {
        dataStore.edit { it[sizeKey] = step.coerceIn(0, 3) }
    }

    /** Kordhi tirada maanta; kama gudbo tirada soo noqnoqda. */
    suspend fun increment(date: String, id: String, repeat: Int) {
        dataStore.edit { it[countKey(date, id)] = minOf(repeat, (it[countKey(date, id)] ?: 0) + 1) }
    }

    suspend fun resetCount(date: String, id: String) {
        dataStore.edit { it.remove(countKey(date, id)) }
    }

    companion object {
        const val MAX_RECENT = 20
    }
}
