package com.stunext.vault

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {
    private val SELECTED_APPS_KEY = stringSetPreferencesKey("selected_apps")

    val selectedApps: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_APPS_KEY] ?: emptySet()
        }

    suspend fun saveSelectedApps(apps: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_APPS_KEY] = apps
        }
    }
}