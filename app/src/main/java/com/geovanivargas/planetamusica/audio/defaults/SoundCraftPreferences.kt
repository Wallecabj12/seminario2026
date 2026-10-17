package com.geovanivargas.planetamusica.audio.defaults

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "soundcraft_settings")

class SoundCraftPreferences(private val context: Context) {

    companion object {
        private val KEY_DEFAULT_SOUNDS_GENERATED = booleanPreferencesKey("default_sounds_generated")
    }

    suspend fun areDefaultSoundsGenerated(): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_DEFAULT_SOUNDS_GENERATED] ?: false
        }.first()
    }

    suspend fun setDefaultSoundsGenerated() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_SOUNDS_GENERATED] = true
        }
    }
}
