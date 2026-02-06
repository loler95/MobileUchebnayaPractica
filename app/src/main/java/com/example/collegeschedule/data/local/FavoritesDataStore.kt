package com.example.collegeschedule.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites_datastore")

class FavoritesDataStore(private val context: Context) {
    companion object {
        private val FAVORITE_GROUPS_KEY = stringSetPreferencesKey("favorite_groups")
    }

    suspend fun addGroup(groupName: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
            preferences[FAVORITE_GROUPS_KEY] = currentFavorites + groupName
        }
    }

    suspend fun removeGroup(groupName: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
            preferences[FAVORITE_GROUPS_KEY] = currentFavorites - groupName
        }
    }

    fun getFavoriteGroups(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
        }
    }

    suspend fun toggleGroup(groupName: String): Boolean {
        var isAdded = false
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
            isAdded = groupName !in currentFavorites
            preferences[FAVORITE_GROUPS_KEY] = if (isAdded) {
                currentFavorites + groupName
            } else {
                currentFavorites - groupName
            }
        }
        return isAdded
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.remove(FAVORITE_GROUPS_KEY)
        }
    }
}