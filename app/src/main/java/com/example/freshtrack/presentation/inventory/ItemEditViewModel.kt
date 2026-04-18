package com.example.freshtrack.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshtrack.data.local.model.ItemEntity
import com.example.freshtrack.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ItemEditViewModel(
    private val repository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemEditUiState())
    val uiState: StateFlow<ItemEditUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.getCategoriesStream().collectLatest { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun loadItem(itemId: Int) {
        viewModelScope.launch {
            val item = repository.getItem(itemId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                id = item.id,
                name = item.name,
                quantity = item.quantity.toString(),
                unit = item.unit,
                expirationDate = item.expirationDate,
                note = item.note.orEmpty(),
                selectedCategoryId = item.categoryId
            )
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onQuantityChange(v: String) { _uiState.value = _uiState.value.copy(quantity = v) }
    fun onUnitChange(v: String) { _uiState.value = _uiState.value.copy(unit = v) }
    fun onNoteChange(v: String) { _uiState.value = _uiState.value.copy(note = v) }
    fun onCategoryChange(id: Int?) { _uiState.value = _uiState.value.copy(selectedCategoryId = id) }
    fun onExpirationDateChange(millis: Long) { _uiState.value = _uiState.value.copy(expirationDate = millis) }

    fun save(onDone: () -> Unit) {
        val s = _uiState.value
        if (!s.isValid) return

        viewModelScope.launch {
            val entity = ItemEntity(
                id = s.id ?: 0,
                name = s.name.trim(),
                categoryId = s.selectedCategoryId,
                quantity = s.quantity.toDouble(),
                unit = s.unit.trim(),
                expirationDate = s.expirationDate!!,
                note = s.note.trim().ifBlank { null },
                isConsumed = false
            )

            if (s.id == null) repository.insertItem(entity)
            else repository.updateItem(entity)

            onDone()
        }
    }
}