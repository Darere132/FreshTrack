package com.example.freshtrack.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshtrack.data.settings.SettingsDataStore
import com.example.freshtrack.data.settings.SettingsDataStore.Companion.THEME_DARK
import com.example.freshtrack.data.settings.SettingsDataStore.Companion.THEME_LIGHT
import com.example.freshtrack.data.settings.SettingsDataStore.Companion.THEME_SYSTEM
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// All three settings combined into one data class for the UI
data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val daysBeforeExpiry: Int = 3,
    // Raw text in the input field — can be invalid while the user types
    val daysInput: String = "3",
    val daysInputError: String? = null,
    val theme: AppTheme = AppTheme.SYSTEM,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0
)

enum class AppTheme(val key: String, val label: String) {
    SYSTEM(THEME_SYSTEM, "System default"),
    LIGHT(THEME_LIGHT, "Light"),
    DARK(THEME_DARK, "Dark");

    companion object {
        fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

class SettingsViewModel(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        dataStore.notificationsEnabled,
        dataStore.daysBeforeExpiry,
        dataStore.theme
    ) { enabled, days, themeKey ->
        SettingsUiState(
            notificationsEnabled = enabled,
            daysBeforeExpiry = days,
            daysInput = days.toString(),
            daysInputError = null,
            theme = AppTheme.fromKey(themeKey)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.setNotificationsEnabled(enabled) }
    }

    // Called on every keystroke — validates but doesn't persist yet
    fun onDaysInputChange(input: String): SettingsUiState {
        val trimmed = input.trim()
        val number = trimmed.toIntOrNull()
        val error = when {
            trimmed.isBlank() -> "Required"
            number == null -> "Enter a valid number"
            number < 1 -> "Minimum is 1 day"
            number > 365 -> "Maximum is 365 days"
            else -> null
        }
        return uiState.value.copy(daysInput = input, daysInputError = error)
    }

    // Persists only when the value is valid
    fun saveDaysBeforeExpiry(days: Int) {
        viewModelScope.launch { dataStore.setDaysBeforeExpiry(days) }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { dataStore.setTheme(theme.key) }
    }

    fun saveNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch { dataStore.setNotificationTime(hour, minute) }
    }

    class Factory(private val dataStore: SettingsDataStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(dataStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}