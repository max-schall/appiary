package io.github.max_schall.appiary.ui.screen.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.max_schall.appiary.data.entity.InventoryItemEntity
import io.github.max_schall.appiary.data.repository.InventoryRepository
import io.github.max_schall.appiary.domain.model.InventoryCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    /** Items grouped by category, preserving the enum's declaration order. */
    val groups: List<Pair<InventoryCategory, List<InventoryItemEntity>>> = emptyList(),
    val lowStockCount: Int = 0,
)

class InventoryViewModel(
    private val repo: InventoryRepository,
) : ViewModel() {

    val uiState = repo.observeAll()
        .map { items ->
            InventoryUiState(
                groups = items
                    .groupBy { it.category }
                    .toList()
                    .sortedBy { it.first.ordinal },
                lowStockCount = items.count { it.isLow },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventoryUiState())

    fun add(
        name: String,
        category: InventoryCategory,
        quantity: Double,
        unit: String?,
        lowStockThreshold: Double?,
        notes: String?,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.add(name, category, quantity, unit, lowStockThreshold, notes)
        }
    }

    fun save(item: InventoryItemEntity) = viewModelScope.launch { repo.save(item) }
    fun adjust(item: InventoryItemEntity, delta: Double) = viewModelScope.launch { repo.adjustQuantity(item, delta) }
    fun delete(item: InventoryItemEntity) = viewModelScope.launch { repo.delete(item) }
}
