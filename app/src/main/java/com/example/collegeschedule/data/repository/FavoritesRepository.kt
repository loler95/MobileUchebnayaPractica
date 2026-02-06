package com.example.collegeschedule.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.collegeschedule.data.dto.GroupsDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesRepository(context: Context) {
    companion object {
        private const val PREFS_NAME = "college_schedule_favorites"
        private const val FAVORITES_KEY = "favorite_groups_data"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _favorites = MutableStateFlow<List<GroupsDto>>(loadFavorites())
    val favorites: StateFlow<List<GroupsDto>> = _favorites.asStateFlow()

    private fun loadFavorites(): List<GroupsDto> {
        val json = prefs.getString(FAVORITES_KEY, "[]") ?: "[]"
        val type = object : TypeToken<List<GroupsDto>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveFavorites(favorites: List<GroupsDto>) {
        val json = gson.toJson(favorites)
        prefs.edit {
            putString(FAVORITES_KEY, json)
        }
        _favorites.value = favorites
    }

    suspend fun addToFavorites(group: GroupsDto) {
        val currentFavorites = loadFavorites().toMutableList()
        if (currentFavorites.none { it.groupName == group.groupName }) {
            currentFavorites.add(group)
            saveFavorites(currentFavorites)
        }
    }

    suspend fun removeFromFavorites(groupName: String) {
        val currentFavorites = loadFavorites().toMutableList()
        currentFavorites.removeAll { it.groupName == groupName }
        saveFavorites(currentFavorites)
    }

    suspend fun toggleFavorite(group: GroupsDto): Boolean {
        val currentFavorites = loadFavorites().toMutableList()
        val existingIndex = currentFavorites.indexOfFirst { it.groupName == group.groupName }

        return if (existingIndex != -1) {
            // Удаляем если уже в избранном
            currentFavorites.removeAt(existingIndex)
            saveFavorites(currentFavorites)
            false
        } else {
            // Добавляем если не в избранном
            currentFavorites.add(group)
            saveFavorites(currentFavorites)
            true
        }
    }

    fun isFavorite(groupName: String): Boolean {
        return _favorites.value.any { it.groupName == groupName }
    }

    fun getFavoriteGroups(): List<GroupsDto> {
        return _favorites.value
    }

    suspend fun clearAll() {
        saveFavorites(emptyList())
    }
}