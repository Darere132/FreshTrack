package com.example.freshtrack.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a single DataStore instance tied to the app context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "freshtrack_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_DAYS_BEFORE_EXPIRY = intPreferencesKey("days_before_expiry")
        private val KEY_THEME = stringPreferencesKey("theme")

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val DEFAULT_DAYS_BEFORE_EXPIRY = 3
        const val DEFAULT_NOTIFICATIONS_ENABLED = true
        const val DEFAULT_THEME = THEME_SYSTEM
    }

    // --- Flows (read) ---

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: DEFAULT_NOTIFICATIONS_ENABLED }

    val daysBeforeExpiry: Flow<Int> = context.dataStore.data
        .map { it[KEY_DAYS_BEFORE_EXPIRY] ?: DEFAULT_DAYS_BEFORE_EXPIRY }

    val theme: Flow<String> = context.dataStore.data
        .map { it[KEY_THEME] ?: DEFAULT_THEME }

    // --- Suspend writes ---

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setDaysBeforeExpiry(days: Int) {
        context.dataStore.edit { it[KEY_DAYS_BEFORE_EXPIRY] = days }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }
}