package com.example.freshtrack.presentation.inventory

import com.example.freshtrack.data.local.model.CategoryEntity

data class ItemEditUiState(
    val id: Int? = null,
    val name: String = "",
    val quantity: String = "",
    val unit: String = "pcs",
    val expirationDate: Long? = null,
    val note: String = "",
    val selectedCategoryId: Int? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val isConsumed: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                expirationDate != null &&
                (quantity.isBlank() || (quantity.toDoubleOrNull()?.let { it > 0 } == true))
}