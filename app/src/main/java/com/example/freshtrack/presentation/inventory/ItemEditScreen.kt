package com.example.freshtrack.presentation.inventory

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

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
    onDelete: () -> Unit,
    onMarkConsumed: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Build DatePickerDialog. Recreates when date changes so the dialog opens at the right month.
    val calendar = Calendar.getInstance().apply {
        uiState.expirationDate?.let { timeInMillis = it }
    }
    val datePickerDialog = remember(uiState.expirationDate) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onExpirationDateChange(cal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Prevent selecting dates in the past
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    val formattedDate = uiState.expirationDate?.let {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Not set"

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete item") },
            text = { Text("Are you sure you want to delete \"${uiState.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id == null) "Add item" else "Edit item") }
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
            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Quantity + Unit on one row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.unit,
                    onValueChange = onUnitChange,
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                val selectedName = uiState.categories
                    .firstOrNull { it.id == uiState.selectedCategoryId }?.name
                    ?: "No category"
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("No category") },
                        onClick = {
                            onCategoryChange(null)
                            categoryExpanded = false
                        }
                    )
                    uiState.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                onCategoryChange(cat.id)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Expiry date picker button
            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Expiry date: $formattedDate  ▾")
            }

            // Note
            OutlinedTextField(
                value = uiState.note,
                onValueChange = onNoteChange,
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(4.dp))

            // Save / Cancel
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSave,
                    enabled = uiState.isValid,
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
            }

            // Delete / Mark consumed — only visible when editing an existing item
            if (uiState.id != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onMarkConsumed,
                        modifier = Modifier.weight(1f)
                    ) { Text("Consumed") }
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Delete") }
                }
            }
        }
    }
}
