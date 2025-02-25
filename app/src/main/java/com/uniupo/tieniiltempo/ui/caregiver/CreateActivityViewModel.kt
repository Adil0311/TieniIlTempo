package com.uniupo.tieniiltempo.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniupo.tieniiltempo.data.model.Activity
import com.uniupo.tieniiltempo.data.model.SubActivity
import com.uniupo.tieniiltempo.data.model.User
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// CreateActivityViewModel.kt
class CreateActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _subActivities = MutableStateFlow<MutableList<SubActivity>>(mutableListOf())
    val subActivities: StateFlow<List<SubActivity>> = _subActivities

    private val _createActivityState = MutableStateFlow<CreateActivityState>(CreateActivityState.Idle)
    val createActivityState: StateFlow<CreateActivityState> = _createActivityState

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val usersList = userRepository.getUsersByRole("user")
                _users.value = usersList
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addSubActivity(subActivity: SubActivity) {
        val currentList = _subActivities.value.toMutableList()
        // Assign order based on current position
        val subActivityWithOrder = subActivity.copy(order = currentList.size)
        currentList.add(subActivityWithOrder)
        _subActivities.value = currentList
    }

    fun removeSubActivity(position: Int) {
        val currentList = _subActivities.value.toMutableList()
        if (position >= 0 && position < currentList.size) {
            currentList.removeAt(position)
            // Update orders after removal
            currentList.forEachIndexed { index, subActivity ->
                currentList[index] = subActivity.copy(order = index)
            }
            _subActivities.value = currentList
        }
    }

    fun createActivity(title: String, description: String, userId: String) {
        if (title.isBlank() || description.isBlank() || userId.isBlank()) {
            _createActivityState.value = CreateActivityState.Error("Tutti i campi sono obbligatori")
            return
        }

        if (_subActivities.value.isEmpty()) {
            _createActivityState.value = CreateActivityState.Error("Aggiungi almeno una sotto-attività")
            return
        }

        _createActivityState.value = CreateActivityState.Loading

        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUser()?.uid
                    ?: throw Exception("Utente non autenticato")

                val activity = Activity(
                    title = title,
                    description = description,
                    caregiverId = currentUserId,
                    userId = userId,
                    createdAt = Date(),
                    status = "pending"
                )

                val activityId = activityRepository.createActivity(activity)
                    ?: throw Exception("Errore durante la creazione dell'attività")

                // Add all sub-activities
                _subActivities.value.forEach { subActivity ->
                    activityRepository.createSubActivity(
                        subActivity.copy(activityId = activityId)
                    )
                }

                _createActivityState.value = CreateActivityState.Success
            } catch (e: Exception) {
                _createActivityState.value = CreateActivityState.Error(e.message ?: "Errore sconosciuto")
            }
        }
    }
}

sealed class CreateActivityState {
    object Idle : CreateActivityState()
    object Loading : CreateActivityState()
    object Success : CreateActivityState()
    data class Error(val message: String) : CreateActivityState()
}