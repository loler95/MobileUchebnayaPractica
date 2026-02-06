package com.example.collegeschedule.ui.groups

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collegeschedule.data.dto.GroupsDto
import com.example.collegeschedule.data.repository.ScheduleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.collegeschedule.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.StateFlow
data class HomeState(
    val searchQuery: String = "",
    val selectedCourse: Int? = null,
    val selectedSpeciality: String? = null,
    val groups: List<GroupsDto> = emptyList(),
    val favoriteGroups: List<GroupsDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedGroup: GroupsDto? = null
)

class GroupsSearchViewModel(
    private val repository: ScheduleRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAllGroups()
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesRepository.favorites.collect { favorites ->
                _state.update { currentState ->
                    currentState.copy(favoriteGroups = favorites)
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

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            searchGroups()
        }
    }

    fun searchGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val groups = repository.getGroups(
                    course = _state.value.selectedCourse,
                    speciality = _state.value.selectedSpeciality
                )

                val filteredGroups = if (_state.value.searchQuery.isNotEmpty()) {
                    groups.filter { group ->
                        group.groupName.contains(
                            _state.value.searchQuery,
                            ignoreCase = true
                        ) || group.speciality.contains(
                            _state.value.searchQuery,
                            ignoreCase = true
                        )
                    }
                } else {
                    groups
                }

                _state.update {
                    it.copy(
                        groups = filteredGroups,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка загрузки групп"
                    )
                }
            }
        }
    }

    fun loadAllGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val groups = repository.getGroups()
                _state.update {
                    it.copy(
                        groups = groups,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка загрузки групп"
                    )
                }
            }
        }
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                searchQuery = "",
                selectedCourse = null,
                selectedSpeciality = null
            )
        }
        loadAllGroups()
    }

    fun selectGroup(group: GroupsDto) {
        _state.update { it.copy(selectedGroup = group) }
    }

    fun clearSelectedGroup() {
        _state.update { it.copy(selectedGroup = null) }
    }
}

class GroupsSearchViewModelFactory(
    private val repository: ScheduleRepository,
    private val favoritesRepository: FavoritesRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupsSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupsSearchViewModel(repository, favoritesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}