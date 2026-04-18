package com.example.freshtrack.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshtrack.data.repository.ItemRepository
import com.example.freshtrack.presentation.InventoryItemUi
import com.example.freshtrack.presentation.ItemStatus
import com.example.freshtrack.presentation.toUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class InventoryUiState(
    val items: List<InventoryItemUi> = emptyList()
)

class InventoryViewModel(
    itemRepository: ItemRepository
) : ViewModel() {

    val uiState: StateFlow<InventoryUiState> =
        itemRepository.getAllItemsStream()
            .map { items ->
                InventoryUiState(
                    items = items
                        .map { it.toUi() }
                        .filter { it.status != ItemStatus.CONSUMED } // podľa tvojej analýzy
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InventoryUiState()
            )
}