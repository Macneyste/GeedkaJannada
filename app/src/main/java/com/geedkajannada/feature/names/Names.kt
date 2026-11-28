package com.geedkajannada.feature.names

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geedkajannada.feature.dua.DuaSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AllahName(val id: Int, val arabic: String, val latin: String, val meaning: String)

object NamesData {
    fun parse(json: String): List<AllahName> {
        val list = JSONObject(json).getJSONArray("names")
        return List(list.length()) {
            val o = list.getJSONObject(it)
            AllahName(o.getInt("id"), o.getString("ar"), o.getString("latin"), o.getString("so"))
        }
    }

    /** Raadin: lambar, akhris, macne ama Carabi (harakaad la'aan). */
    fun filter(names: List<AllahName>, query: String): List<AllahName> {
        val q = DuaSearch.normalize(query)
        if (q.isEmpty()) return names
        return names.filter { n ->
            n.id.toString() == q || listOf(n.latin, n.meaning, n.arabic).any { DuaSearch.normalize(it).contains(q) }
        }
    }
}

class NamesRepository(private val context: Context, private val dataStore: DataStore<Preferences>) {
    @Volatile
    private var cache: List<AllahName>? = null
    private val learnedKey = stringSetPreferencesKey("names_learned")

    suspend fun names(): List<AllahName> = cache ?: withContext(Dispatchers.IO) {
        val json = context.assets.open("names.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        NamesData.parse(json).also { cache = it }
    }

    val learned: Flow<Set<Int>> = dataStore.data.map { p -> p[learnedKey].orEmpty().mapNotNull { it.toIntOrNull() }.toSet() }

    suspend fun toggleLearned(id: Int) {
        dataStore.edit {
            val set = it[learnedKey].orEmpty()
            val key = id.toString()
            it[learnedKey] = if (key in set) set - key else set + key
        }
    }
}

data class NamesUiState(
    val query: String = "",
    val names: List<AllahName> = emptyList(),
    val total: Int = 0,
    val learned: Set<Int> = emptySet(),
    /** Magaca la furay (kaarka barashada), ama null. */
    val open: AllahName? = null,
    val meaningShown: Boolean = false,
)

class NamesViewModel(private val repo: NamesRepository) : ViewModel() {
    private val all = MutableStateFlow<List<AllahName>>(emptyList())
    private val query = MutableStateFlow("")
    private val openId = MutableStateFlow<Int?>(null)
    private val shown = MutableStateFlow(false)

    init {
        viewModelScope.launch { runCatching { all.value = repo.names() } }
    }

    val uiState: StateFlow<NamesUiState> =
        combine(all, query, repo.learned, openId, shown) { names, q, learned, id, meaning ->
            NamesUiState(q, NamesData.filter(names, q), names.size, learned, names.firstOrNull { it.id == id }, meaning)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NamesUiState())

    fun onQuery(q: String) { query.value = q }

    fun open(id: Int) {
        shown.value = false
        openId.value = id
    }

    fun close() { openId.value = null }
    fun showMeaning() { shown.value = true }

    /** Hore (-1) ama xiga (+1); dhammaadka wuu ku laabtaa bilowga. */
    fun step(delta: Int) {
        val count = all.value.size
        val id = openId.value ?: return
        if (count > 0) open((id - 1 + delta + count) % count + 1)
    }

    fun toggleLearned() {
        val id = openId.value ?: return
        viewModelScope.launch { repo.toggleLearned(id) }
    }
}
