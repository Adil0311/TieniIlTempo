package com.uniupo.tieniiltempo.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniupo.tieniiltempo.data.model.Activity
import com.uniupo.tieniiltempo.data.model.User
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// CaregiverViewModel.kt
class CaregiverViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    fun loadActivities() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid ?: return@launch
            val userActivities = activityRepository.getActivitiesForUser(userId, "caregiver")
            _activities.value = userActivities
        }
    }

    fun loadAssignableUsers() {
        viewModelScope.launch {
            // In a real app, implement logic to get users that this caregiver can assign tasks to
            // For now, we'll just load all users with "user" role
            _users.value = userRepository.getUsersByRole("user")
        }
    }
}