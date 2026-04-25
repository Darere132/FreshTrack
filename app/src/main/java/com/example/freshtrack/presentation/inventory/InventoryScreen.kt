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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.freshtrack.R
import com.example.freshtrack.presentation.ItemStatus

enum class InventoryFilterStatus { ALL, FRESH, EXPIRING_SOON, EXPIRED }
enum class InventorySort { EXPIRY_ASC, EXPIRY_DESC, NAME_ASC }

@Composable
private fun statusLabel(status: ItemStatus) = when (status) {
    ItemStatus.FRESH -> stringResource(R.string.status_fresh)
    ItemStatus.EXPIRING_SOON -> stringResource(R.string.status_expiring_soon)
    ItemStatus.EXPIRED -> stringResource(R.string.status_expired)
    ItemStatus.CONSUMED -> stringResource(R.string.status_consumed)
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

    val colors = MaterialTheme.colorScheme
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = colors.primary,
        selectedTextColor = colors.primary,
        indicatorColor = colors.primary.copy(alpha = 0.15f),
        unselectedIconColor = colors.onSurfaceVariant,
        unselectedTextColor = colors.onSurfaceVariant
    )

    // Build filter/sort label maps inside composable so stringResource works
    val filterOptions = listOf(
        InventoryFilterStatus.ALL to stringResource(R.string.filter_all),
        InventoryFilterStatus.FRESH to stringResource(R.string.filter_fresh),
        InventoryFilterStatus.EXPIRING_SOON to stringResource(R.string.filter_expiring_soon),
        InventoryFilterStatus.EXPIRED to stringResource(R.string.filter_expired)
    )
    val sortOptions = listOf(
        InventorySort.EXPIRY_ASC to stringResource(R.string.sort_expiry_asc),
        InventorySort.EXPIRY_DESC to stringResource(R.string.sort_expiry_desc),
        InventorySort.NAME_ASC to stringResource(R.string.sort_name_asc)
    )

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
        containerColor = colors.background,
        bottomBar = {
            NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = stringResource(R.string.nav_inventory)) },
                    label = { Text(stringResource(R.string.nav_inventory)) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onRecipesClick,
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.nav_recipes)) },
                    label = { Text(stringResource(R.string.nav_recipes)) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSettingsClick,
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    colors = navItemColors
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Surface(color = colors.surface, tonalElevation = 0.dp, shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_item))
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant)

            Surface(color = colors.surface, tonalElevation = 0.dp, shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.inventory_search_label)) }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Filter dropdown
                        ExposedDropdownMenuBox(
                            expanded = filterExpanded,
                            onExpandedChange = { filterExpanded = !filterExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            val filterLabel = filterOptions.first { it.first == selectedFilter }.second
                            OutlinedTextField(
                                value = filterLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.filter_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                                filterOptions.forEach { (status, label) ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { selectedFilter = status; filterExpanded = false })
                                }
                            }
                        }
                        // Sort dropdown
                        ExposedDropdownMenuBox(
                            expanded = sortExpanded,
                            onExpandedChange = { sortExpanded = !sortExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            val sortLabel = sortOptions.first { it.first == selectedSort }.second
                            OutlinedTextField(
                                value = sortLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.sort_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                                sortOptions.forEach { (sort, label) ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { selectedSort = sort; sortExpanded = false })
                                }
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
                if (filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.inventory_empty))
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredItems, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                modifier = Modifier.fillMaxWidth().clickable { onEditClick(item.id) }
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        Surface(color = statusColor(item.status), shape = RoundedCornerShape(12.dp)) {
                                            Text(
                                                text = statusLabel(item.status),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    val categoryAndQty = buildString {
                                        if (!item.categoryName.isNullOrBlank()) { append(item.categoryName); append(" • ") }
                                        append(item.quantityText)
                                    }
                                    Text(text = categoryAndQty, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                    Text(text = stringResource(R.string.item_expires, item.formattedExpiryDate), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}