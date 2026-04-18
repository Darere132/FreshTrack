package com.example.freshtrack.presentation.inventory

import com.example.freshtrack.presentation.InventoryItemUi

data class InventoryUiState(
    val items: List<InventoryItemUi> = emptyList()
)
