package com.uniupo.tieniiltempo.ui.caregiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniupo.tieniiltempo.data.model.Activity
import com.uniupo.tieniiltempo.data.model.SubActivity
import com.uniupo.tieniiltempo.data.model.User
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.NotificationRepository
import com.uniupo.tieniiltempo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// CaregiverViewModel.kt
class CaregiverViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity

    private val _subActivities = MutableStateFlow<List<SubActivity>>(emptyList())
    val subActivities: StateFlow<List<SubActivity>> = _subActivities


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

    fun loadActivityDetails(activityId: String) {
        viewModelScope.launch {
            val activity = activityRepository.getActivityById(activityId)
            _currentActivity.value = activity

            if (activity != null) {
                val subActivities = activityRepository.getSubActivitiesForActivity(activityId)
                _subActivities.value = subActivities
            }
        }
    }

    fun startActivity(activityId: String) {
        viewModelScope.launch {
            val success = activityRepository.updateActivityStatus(activityId, "in_progress")
            if (success) {
                _currentActivity.value = _currentActivity.value?.copy(status = "in_progress")

                // Invia notifica all'utente
                val activity = activityRepository.getActivityById(activityId)
                if (activity != null) {
                    notificationRepository.sendActivityNotification(activityId)
                }
            }
        }
    }
}