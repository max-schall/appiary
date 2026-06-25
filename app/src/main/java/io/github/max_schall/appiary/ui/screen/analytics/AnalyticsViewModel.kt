package io.github.max_schall.appiary.ui.screen.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.repository.AnalyticsRepository
import io.github.max_schall.appiary.domain.analytics.AnalyticsData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AnalyticsViewModel(repository: AnalyticsRepository) : ViewModel() {
    val state = repository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null as AnalyticsData?)
}
