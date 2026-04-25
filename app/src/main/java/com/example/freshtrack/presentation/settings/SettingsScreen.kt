package com.example.freshtrack.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onInventoryClick: () -> Unit = {},
    onRecipesClick: () -> Unit = {},
    onNotificationsToggle: (Boolean) -> Unit,
    onDaysInputChange: (String) -> Unit,
    onDaysSave: (Int) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    daysInputState: String,
    daysInputError: String?
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = MaterialTheme.colorScheme
    var themeExpanded by remember { mutableStateOf(false) }

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = colors.primary,
        selectedTextColor = colors.primary,
        indicatorColor = colors.primary.copy(alpha = 0.15f),
        unselectedIconColor = colors.onSurfaceVariant,
        unselectedTextColor = colors.onSurfaceVariant
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Surface(
                color = colors.surface,
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
                    Text("Settings", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onInventoryClick,
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onRecipesClick,
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Recipes") },
                    label = { Text("Recipes") },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = navItemColors
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant)

            // ── Notifications ──────────────────────────────────────────────
            SectionHeader("Notifications")

            SettingsRow(
                label = "Enable notifications",
                description = "Get alerted before items expire"
            ) {
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = onNotificationsToggle
                )
            }

            if (uiState.notificationsEnabled) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Days before expiry", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "How many days ahead to send the notification",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = daysInputState,
                            onValueChange = onDaysInputChange,
                            label = { Text("Days") },
                            isError = daysInputError != null,
                            supportingText = { if (daysInputError != null) Text(daysInputError) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val num = daysInputState.trim().toIntOrNull()
                                    if (num != null && daysInputError == null) {
                                        onDaysSave(num)
                                        keyboard?.hide()
                                    }
                                }
                            ),
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                        Button(
                            onClick = {
                                val num = daysInputState.trim().toIntOrNull()
                                if (num != null && daysInputError == null) {
                                    onDaysSave(num)
                                    keyboard?.hide()
                                }
                            },
                            enabled = daysInputError == null && daysInputState.isNotBlank(),
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Save") }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = colors.outlineVariant
            )

            // ── Appearance ─────────────────────────────────────────────────
            SectionHeader("Appearance")

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Theme", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Choose the app colour scheme",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = themeExpanded,
                    onExpandedChange = { themeExpanded = !themeExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.theme.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Theme") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false }
                    ) {
                        AppTheme.entries.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.label) },
                                onClick = {
                                    onThemeChange(theme)
                                    themeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    label: String,
    description: String,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        control()
    }
}