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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.freshtrack.R

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
    onNotificationTimeSave: (hour: Int, minute: Int) -> Unit,
    daysInputState: String,
    daysInputError: Int?   // @StringRes Int? — resolved here in the Composable
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = MaterialTheme.colorScheme
    var themeExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = uiState.notificationHour,
        initialMinute = uiState.notificationMinute,
        is24Hour = true
    )

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = colors.primary,
        selectedTextColor = colors.primary,
        indicatorColor = colors.primary.copy(alpha = 0.15f),
        unselectedIconColor = colors.onSurfaceVariant,
        unselectedTextColor = colors.onSurfaceVariant
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.settings_time_picker_title)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                Button(onClick = {
                    onNotificationTimeSave(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.btn_save)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Surface(color = colors.surface, tonalElevation = 0.dp, shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = false, onClick = onInventoryClick,
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = stringResource(R.string.nav_inventory)) },
                    label = { Text(stringResource(R.string.nav_inventory)) }, colors = navItemColors
                )
                NavigationBarItem(
                    selected = false, onClick = onRecipesClick,
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.nav_recipes)) },
                    label = { Text(stringResource(R.string.nav_recipes)) }, colors = navItemColors
                )
                NavigationBarItem(
                    selected = true, onClick = {},
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings)) }, colors = navItemColors
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant)

            // ── Notifications ──────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_notifications))

            SettingsRow(
                label = stringResource(R.string.settings_notifications_enable),
                description = stringResource(R.string.settings_notifications_enable_desc)
            ) {
                Switch(checked = uiState.notificationsEnabled, onCheckedChange = onNotificationsToggle)
            }

            if (uiState.notificationsEnabled) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(stringResource(R.string.settings_days_before_expiry), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = stringResource(R.string.settings_days_before_expiry_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = daysInputState,
                            onValueChange = onDaysInputChange,
                            label = { Text(stringResource(R.string.settings_days_label)) },
                            isError = daysInputError != null,
                            supportingText = { if (daysInputError != null) Text(stringResource(daysInputError)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                val num = daysInputState.trim().toIntOrNull()
                                if (num != null && daysInputError == null) { onDaysSave(num); keyboard?.hide() }
                            }),
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                        Button(
                            onClick = {
                                val num = daysInputState.trim().toIntOrNull()
                                if (num != null && daysInputError == null) { onDaysSave(num); keyboard?.hide() }
                            },
                            enabled = daysInputError == null && daysInputState.isNotBlank(),
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text(stringResource(R.string.btn_save)) }
                    }
                }

                SettingsRow(
                    label = stringResource(R.string.settings_notification_time),
                    description = stringResource(R.string.settings_notification_time_desc, uiState.notificationHour, uiState.notificationMinute)
                ) {
                    OutlinedButton(onClick = { showTimePicker = true }) {
                        Text(stringResource(R.string.settings_notification_time_desc, uiState.notificationHour, uiState.notificationMinute))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = colors.outlineVariant)

            // ── Appearance ─────────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_appearance))

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyLarge)
                Text(text = stringResource(R.string.settings_theme_desc), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = !themeExpanded }) {
                    OutlinedTextField(
                        value = stringResource(uiState.theme.labelRes),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_theme)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                        AppTheme.entries.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(stringResource(theme.labelRes)) },
                                onClick = { onThemeChange(theme); themeExpanded = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp))
}

@Composable
private fun SettingsRow(label: String, description: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        control()
    }
}