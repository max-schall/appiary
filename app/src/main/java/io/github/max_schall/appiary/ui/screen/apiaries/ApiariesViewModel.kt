package io.github.max_schall.appiary.ui.screen.apiaries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.dao.ApiaryStats
import io.github.max_schall.appiary.data.repository.ApiaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApiariesViewModel(private val apiaryRepo: ApiaryRepository) : ViewModel() {
    val apiaries = apiaryRepo.observeApiaryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<ApiaryStats>())

    fun addApiary(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { apiaryRepo.createApiary(name.trim()) }
    }
}
