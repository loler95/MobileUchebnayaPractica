package com.example.collegeschedule.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collegeschedule.data.dto.GroupsDto
import com.example.collegeschedule.data.repository.FavoritesRepository
import com.example.collegeschedule.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesState(
    val favorites: List<GroupsDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedGroup: GroupsDto? = null
)

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                favoritesRepository.favorites.collect { favorites ->
                    _state.update {
                        it.copy(
                            favorites = favorites,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка загрузки избранного"
                    )
                }
            }
        }
    }

    fun toggleFavorite(group: GroupsDto) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(group)
        }
    }

    fun isFavorite(groupName: String): Boolean {
        return favoritesRepository.isFavorite(groupName)
    }

    fun selectGroup(group: GroupsDto) {
        _state.update { it.copy(selectedGroup = group) }
    }

    fun clearSelectedGroup() {
        _state.update { it.copy(selectedGroup = null) }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            favoritesRepository.clearAll()
        }
    }
}

class FavoritesViewModelFactory(
    private val favoritesRepository: FavoritesRepository,
    private val scheduleRepository: ScheduleRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(favoritesRepository, scheduleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}