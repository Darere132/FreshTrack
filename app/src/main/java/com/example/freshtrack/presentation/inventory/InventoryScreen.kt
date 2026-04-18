package com.example.freshtrack.presentation.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.freshtrack.R
import com.example.freshtrack.presentation.ItemStatus

enum class InventoryFilterStatus {
    ALL, FRESH, EXPIRING_SOON, EXPIRED
}

enum class InventorySort {
    EXPIRY_ASC, EXPIRY_DESC, NAME_ASC
}

private fun statusLabel(status: ItemStatus) = when (status) {
    ItemStatus.FRESH -> "Fresh"
    ItemStatus.EXPIRING_SOON -> "Expiring soon"
    ItemStatus.EXPIRED -> "Expired"
    ItemStatus.CONSUMED -> "Consumed"
}

private fun statusColor(status: ItemStatus) = when (status) {
    ItemStatus.FRESH -> Color(0xFF388E3C)
    ItemStatus.EXPIRING_SOON -> Color(0xFFF57C00)
    ItemStatus.EXPIRED -> Color(0xFFD32F2F)
    ItemStatus.CONSUMED -> Color(0xFF757575)
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
                    InventoryFilterStatus.FRESH -> item.status == ItemStatus.FRESH
                    InventoryFilterStatus.EXPIRING_SOON -> item.status == ItemStatus.EXPIRING_SOON
                    InventoryFilterStatus.EXPIRED -> item.status == ItemStatus.EXPIRED
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
        containerColor = Color(0xFFF4F4F4), // celkové jemne sivé pozadie
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.light_blue),
                        selectedTextColor = colorResource(id = R.color.light_blue),
                        indicatorColor = colorResource(id = R.color.light_blue).copy(alpha = 0.15f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onRecipesClick,
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Recipes") },
                    label = { Text("Recipes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.light_blue),
                        selectedTextColor = colorResource(id = R.color.light_blue),
                        indicatorColor = colorResource(id = R.color.light_blue).copy(alpha = 0.15f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSettingsClick,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.light_blue),
                        selectedTextColor = colorResource(id = R.color.light_blue),
                        indicatorColor = colorResource(id = R.color.light_blue).copy(alpha = 0.15f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1) Header - white
            Surface(
                color = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FreshTrack", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add item")
                    }
                }
            }

            // 2) Gray divider line
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFE0E0E0)
            )

            // 3) Filters area - white
            Surface(
                color = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
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
                        label = { Text("Search items") }
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
                                    InventoryFilterStatus.ALL -> "All"
                                    InventoryFilterStatus.FRESH -> "Fresh"
                                    InventoryFilterStatus.EXPIRING_SOON -> "Expiring soon"
                                    InventoryFilterStatus.EXPIRED -> "Expired"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Status") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded)
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false }
                            ) {
                                listOf(
                                    InventoryFilterStatus.ALL to "All",
                                    InventoryFilterStatus.FRESH to "Fresh",
                                    InventoryFilterStatus.EXPIRING_SOON to "Expiring soon",
                                    InventoryFilterStatus.EXPIRED to "Expired"
                                ).forEach { (status, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedFilter = status
                                            filterExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = sortExpanded,
                            onExpandedChange = { sortExpanded = !sortExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = when (selectedSort) {
                                    InventorySort.EXPIRY_ASC -> "Expiry ↑"
                                    InventorySort.EXPIRY_DESC -> "Expiry ↓"
                                    InventorySort.NAME_ASC -> "Name A-Z"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sort") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                listOf(
                                    InventorySort.EXPIRY_ASC to "Expiry ↑",
                                    InventorySort.EXPIRY_DESC to "Expiry ↓",
                                    InventorySort.NAME_ASC to "Name A-Z"
                                ).forEach { (sort, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedSort = sort
                                            sortExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4) List area - gray background, white cards
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF4F4F4))
            ) {
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No items match the current filter.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditClick(item.id) }
                            ) {
                                Column(
                                    Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = statusColor(item.status),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = statusLabel(item.status),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    val categoryAndQty = buildString {
                                        if (!item.categoryName.isNullOrBlank()) {
                                            append(item.categoryName)
                                            append(" • ")
                                        }
                                        append(item.quantityText)
                                    }

                                    Text(
                                        text = categoryAndQty,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Expires: ${item.formattedExpiryDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}