package com.example.freshtrack.presentation.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.freshtrack.R
import com.example.freshtrack.data.settings.SettingsDataStore
import com.example.freshtrack.data.settings.SettingsDataStore.Companion.THEME_DARK
import com.example.freshtrack.data.settings.SettingsDataStore.Companion.THEME_LIGHT
import com.example.freshtrack.data.settings.SettingsDataStore.Companion.THEME_SYSTEM
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val daysBeforeExpiry: Int = 3,
    val daysInput: String = "3",
    @StringRes val daysInputError: Int? = null,
    val theme: AppTheme = AppTheme.SYSTEM,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0
)

enum class AppTheme(val key: String, @StringRes val labelRes: Int) {
    SYSTEM(THEME_SYSTEM, R.string.theme_system),
    LIGHT(THEME_LIGHT, R.string.theme_light),
    DARK(THEME_DARK, R.string.theme_dark);

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
        dataStore.theme,
        dataStore.notificationHour,
        dataStore.notificationMinute
    ) { enabled, days, themeKey, hour, minute ->
        SettingsUiState(
            notificationsEnabled = enabled,
            daysBeforeExpiry = days,
            daysInput = days.toString(),
            daysInputError = null,
            theme = AppTheme.fromKey(themeKey),
            notificationHour = hour,
            notificationMinute = minute
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.setNotificationsEnabled(enabled) }
    }

    fun onDaysInputChange(input: String): SettingsUiState {
        val trimmed = input.trim()
        val number = trimmed.toIntOrNull()
        val errorRes: Int? = when {
            trimmed.isBlank() -> R.string.error_required
            number == null -> R.string.error_invalid_number
            number < 1 -> R.string.error_min_days
            number > 365 -> R.string.error_max_days
            else -> null
        }
        return uiState.value.copy(daysInput = input, daysInputError = errorRes)
    }

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