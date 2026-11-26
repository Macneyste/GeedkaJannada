package com.geedkajannada.feature.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DuaTab { CATEGORIES, FAVORITES, RECENT }

/** Qaybta "Duca Quraanka" waa dhammaan duada Quraanka. */
const val ALL_QURAN = "quraan"

private fun loadInto(vm: ViewModel, repo: DuaRepository): MutableStateFlow<DuaLibrary?> {
    val flow = MutableStateFlow<DuaLibrary?>(null)
    vm.viewModelScope.launch { runCatching { flow.value = repo.library() } }
    return flow
}

data class DuaHomeUiState(
    val loading: Boolean = true,
    val query: String = "",
    val tab: DuaTab = DuaTab.CATEGORIES,
    val categories: List<DuaCategory> = emptyList(),
    /** Natiijada raadinta, ama liiska tab-ka Favorites / Dhawaan. */
    val duas: List<Dua> = emptyList(),
)

class DuaHomeViewModel(repo: DuaRepository) : ViewModel() {
    private val library = loadInto(this, repo)
    private val query = MutableStateFlow("")
    private val tab = MutableStateFlow(DuaTab.CATEGORIES)

    val uiState: StateFlow<DuaHomeUiState> =
        combine(library, query, tab, repo.favorites, repo.recent) { lib, q, t, fav, recent ->
            val all = lib?.duas.orEmpty()
            val list = when {
                q.isNotBlank() -> DuaSearch.filter(all, q)
                t == DuaTab.FAVORITES -> all.filter { it.id in fav }
                t == DuaTab.RECENT -> recent.mapNotNull { id -> all.firstOrNull { it.id == id } }
                else -> emptyList()
            }
            DuaHomeUiState(lib == null, q, t, lib?.categories.orEmpty(), list)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DuaHomeUiState())

    fun onQuery(q: String) { query.value = q }
    fun onTab(t: DuaTab) { tab.value = t }
}

data class DuaListUiState(val title: String? = null, val duas: List<Dua> = emptyList())

class DuaListViewModel(categoryId: String, repo: DuaRepository) : ViewModel() {
    private val library = loadInto(this, repo)

    val uiState: StateFlow<DuaListUiState> = combine(library, MutableStateFlow(categoryId)) { lib, id ->
        if (lib == null) DuaListUiState()
        else DuaListUiState(
            title = lib.categories.firstOrNull { it.id == id }?.so,
            duas = lib.duas.filter { id == ALL_QURAN || it.categoryId == id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DuaListUiState())
}

data class DuaDetailUiState(
    val dua: Dua? = null,
    val favorite: Boolean = false,
    val count: Int = 0,
    /** 0..3 = S, M, L, XL */
    val textSize: Int = 1,
    val showEnglish: Boolean = false,
)

class DuaDetailViewModel(
    private val duaId: String,
    private val repo: DuaRepository,
    private val dateKey: () -> String,
) : ViewModel() {
    private val library = loadInto(this, repo)
    private val english = MutableStateFlow(false)

    init {
        viewModelScope.launch { repo.markRecent(duaId) }
    }

    val uiState: StateFlow<DuaDetailUiState> =
        combine(library, repo.favorites, repo.count(dateKey(), duaId), repo.textSize, english) { lib, fav, count, size, en ->
            DuaDetailUiState(lib?.duas?.firstOrNull { it.id == duaId }, duaId in fav, count, size, en)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DuaDetailUiState())

    fun toggleFavorite() { viewModelScope.launch { repo.toggleFavorite(duaId) } }
    fun toggleEnglish() { english.value = !english.value }
    fun setTextSize(step: Int) { viewModelScope.launch { repo.setTextSize(step) } }
    fun count() {
        val dua = uiState.value.dua ?: return
        viewModelScope.launch { repo.increment(dateKey(), duaId, dua.repeat) }
    }
    fun resetCount() { viewModelScope.launch { repo.resetCount(dateKey(), duaId) } }
}
