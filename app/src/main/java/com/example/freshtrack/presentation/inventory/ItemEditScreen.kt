package com.example.freshtrack.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditScreen(
    uiState: ItemEditUiState,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategoryChange: (Int?) -> Unit,
    onExpirationDateChange: (Long) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id == null) "Pridať položku" else "Upraviť položku") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Názov") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.quantity,
                onValueChange = onQuantityChange,
                label = { Text("Množstvo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.unit,
                onValueChange = onUnitChange,
                label = { Text("Jednotka") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.expirationDate?.toString().orEmpty(),
                onValueChange = { it.toLongOrNull()?.let(onExpirationDateChange) },
                label = { Text("Expirácia (epoch millis)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = { Text("Poznámka") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave, enabled = uiState.isValid) { Text("Uložiť") }
                OutlinedButton(onClick = onBack) { Text("Zrušiť") }
            }
        }
    }
}