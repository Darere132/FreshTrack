package com.example.freshtrack.presentation.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class InventoryFilterStatus {
    ALL, FRESH, EXPIRING_SOON, EXPIRED
}

enum class InventorySort {
    EXPIRY_ASC, EXPIRY_DESC, NAME_ASC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    uiState: InventoryUiState,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onRecipesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(InventoryFilterStatus.ALL) }
    var selectedSort by rememberSaveable { mutableStateOf(InventorySort.EXPIRY_ASC) }

    var filterExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    val filteredItems = remember(uiState.items, searchQuery, selectedFilter, selectedSort) {
        uiState.items
            .filter { item ->
                val byText = item.name.contains(searchQuery.trim(), ignoreCase = true)
                val byStatus = when (selectedFilter) {
                    InventoryFilterStatus.ALL -> true
                    InventoryFilterStatus.FRESH -> item.status.name == "FRESH"
                    InventoryFilterStatus.EXPIRING_SOON -> item.status.name == "EXPIRING_SOON"
                    InventoryFilterStatus.EXPIRED -> item.status.name == "EXPIRED"
                }
                byText && byStatus
            }
            .sortedWith(
                when (selectedSort) {
                    InventorySort.EXPIRY_ASC -> compareBy { it.expirationDate }
                    InventorySort.EXPIRY_DESC -> compareByDescending { it.expirationDate }
                    InventorySort.NAME_ASC -> compareBy { it.name.lowercase() }
                }
            )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* current screen */ },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Zásoby") },
                    label = { Text("Zásoby") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onRecipesClick,
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Recepty") },
                    label = { Text("Recepty") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSettingsClick,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Nastavenia") },
                    label = { Text("Nastavenia") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1) Header box
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FreshTrack",
                        style = MaterialTheme.typography.titleLarge
                    )
                    FilledIconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Pridať položku")
                    }
                }
            }

            // 2) Search + filter/sort box
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Vyhľadať položku") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = filterExpanded,
                            onExpandedChange = { filterExpanded = !filterExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = when (selectedFilter) {
                                    InventoryFilterStatus.ALL -> "Všetky stavy"
                                    InventoryFilterStatus.FRESH -> "Fresh"
                                    InventoryFilterStatus.EXPIRING_SOON -> "Expiring soon"
                                    InventoryFilterStatus.EXPIRED -> "Expired"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter stavu") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Všetky stavy") },
                                    onClick = {
                                        selectedFilter = InventoryFilterStatus.ALL
                                        filterExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Fresh") },
                                    onClick = {
                                        selectedFilter = InventoryFilterStatus.FRESH
                                        filterExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Expiring soon") },
                                    onClick = {
                                        selectedFilter = InventoryFilterStatus.EXPIRING_SOON
                                        filterExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Expired") },
                                    onClick = {
                                        selectedFilter = InventoryFilterStatus.EXPIRED
                                        filterExpanded = false
                                    }
                                )
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = sortExpanded,
                            onExpandedChange = { sortExpanded = !sortExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = when (selectedSort) {
                                    InventorySort.EXPIRY_ASC -> "Expirácia ↑"
                                    InventorySort.EXPIRY_DESC -> "Expirácia ↓"
                                    InventorySort.NAME_ASC -> "Názov A-Z"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Zoradenie") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Expirácia ↑") },
                                    onClick = {
                                        selectedSort = InventorySort.EXPIRY_ASC
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Expirácia ↓") },
                                    onClick = {
                                        selectedSort = InventorySort.EXPIRY_DESC
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Názov A-Z") },
                                    onClick = {
                                        selectedSort = InventorySort.NAME_ASC
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3) Scroll list
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("Žiadne položky pre zadaný filter.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditClick(item.id) }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                Text(item.quantityText)
                                Text("Status: ${item.status.name}")
                            }
                        }
                    }
                }
            }
        }
    }
}