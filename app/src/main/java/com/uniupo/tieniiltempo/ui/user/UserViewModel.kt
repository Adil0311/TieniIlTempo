package com.uniupo.tieniiltempo.ui.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.uniupo.tieniiltempo.data.model.Activity
import com.uniupo.tieniiltempo.data.model.SubActivity
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _userActivities = MutableStateFlow<List<Activity>>(emptyList())
    val userActivities: StateFlow<List<Activity>> = _userActivities

    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity

    private val _subActivities = MutableStateFlow<List<SubActivity>>(emptyList())
    val subActivities: StateFlow<List<SubActivity>> = _subActivities

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    fun loadUserActivities() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid ?: return@launch
            val activities = activityRepository.getActivitiesForUser(userId, "user")
            _userActivities.value = activities
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

    fun completeSubActivity(subActivity: SubActivity, comment: String?, imageStream: InputStream?) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            try {
                // Se c'è un'immagine, caricarla su Firebase Storage
                var imageUrl: String? = null

                if (imageStream != null) {
                    val imageName = "sub_activities/${UUID.randomUUID()}.jpg"
                    val storageRef = storage.reference.child(imageName)
                    storageRef.putStream(imageStream).await()
                    imageUrl = storageRef.downloadUrl.await().toString()
                }

                // Aggiorna la sotto-attività
                val updatedSubActivity = subActivity.copy(
                    status = "completed",
                    userComment = comment,
                    userImage = imageUrl
                )

                val success = activityRepository.updateSubActivity(updatedSubActivity)

                if (success) {
                    // Ricarica le sotto-attività
                    val refreshedSubActivities = activityRepository.getSubActivitiesForActivity(subActivity.activityId)
                    _subActivities.value = refreshedSubActivities

                    // Controlla se tutte le sotto-attività sono completate
                    val allCompleted = refreshedSubActivities.all { it.status == "completed" }
                    if (allCompleted) {
                        activityRepository.updateActivityStatus(subActivity.activityId, "completed")
                        _currentActivity.value = _currentActivity.value?.copy(status = "completed")
                    }

                    _actionState.value = ActionState.Success
                } else {
                    _actionState.value = ActionState.Error("Errore nell'aggiornamento della sotto-attività")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Errore sconosciuto")
            }
        }
    }

    fun startActivity(activityId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading

            try {
                val success = activityRepository.updateActivityStatus(activityId, "in_progress")

                if (success) {
                    _currentActivity.value = _currentActivity.value?.copy(status = "in_progress")
                    _actionState.value = ActionState.Success
                } else {
                    _actionState.value = ActionState.Error("Errore nell'avvio dell'attività")
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Errore sconosciuto")
            }
        }
    }
}

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    object Success : ActionState()
    data class Error(val message: String) : ActionState()
}