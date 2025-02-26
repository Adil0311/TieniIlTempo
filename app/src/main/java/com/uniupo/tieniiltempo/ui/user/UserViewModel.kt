package com.uniupo.tieniiltempo.ui.user

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.uniupo.tieniiltempo.data.model.Activity
import com.uniupo.tieniiltempo.data.model.SubActivity
import com.uniupo.tieniiltempo.data.model.User
import com.uniupo.tieniiltempo.data.model.UserAchievement
import com.uniupo.tieniiltempo.data.repository.AchievementRepository
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository,
    private val storage: FirebaseStorage
) : ViewModel() {

    @Inject
    lateinit var achievementRepository: AchievementRepository

    private val _userAchievement = MutableStateFlow<UserAchievement?>(null)
    val userAchievement: StateFlow<UserAchievement?> = _userAchievement

    private val _userActivities = MutableStateFlow<List<Activity>>(emptyList())
    val userActivities: StateFlow<List<Activity>> = _userActivities

    private val _currentActivity = MutableStateFlow<Activity?>(null)
    val currentActivity: StateFlow<Activity?> = _currentActivity

    private val _subActivities = MutableStateFlow<List<SubActivity>>(emptyList())
    val subActivities: StateFlow<List<SubActivity>> = _subActivities

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    private val _weeklyProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val weeklyProgress: StateFlow<Map<String, Int>> = _weeklyProgress

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun loadUserActivities() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid ?: return@launch
            val activities = activityRepository.getActivitiesForUser(userId, "user")
            _userActivities.value = activities
        }
    }

    fun loadUserAchievements() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid ?: return@launch
            achievementRepository.getUserAchievement(userId).collect { achievement ->
                _userAchievement.value = achievement
            }
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
                val actualTime = if (subActivity.actualTime == null) {
                    // Se il tempo non è già stato tracciato, ottieni il tempo trascorso
                    val now = System.currentTimeMillis()
                    val activity = _currentActivity.value
                    if (activity != null) {
                        (now - activity.createdAt.time) / 1000 // Converti in secondi
                    } else {
                        null
                    }
                } else {
                    // Mantieni il tempo già tracciato
                    subActivity.actualTime
                }
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
                    userImage = imageUrl,
                    actualTime = actualTime
                )

                val success = activityRepository.updateSubActivity(updatedSubActivity)

                if (success) {
                    // Ricarica le sotto-attività
                    val refreshedSubActivities =
                        activityRepository.getSubActivitiesForActivity(subActivity.activityId)
                    _subActivities.value = refreshedSubActivities

                    val userId = userRepository.getCurrentUser()?.uid ?: return@launch
                    achievementRepository.updateUserPoints(
                        userId,
                        10
                    ) // 10 punti per ogni sotto-attività


                    // Controlla se tutte le sotto-attività sono completate
                    val allCompleted = refreshedSubActivities.all { it.status == "completed" }
                    if (allCompleted) {
                        achievementRepository.updateUserPoints(
                            userId,
                            50
                        ) // 50 punti bonus per l'attività completa

                        // Verifica se l'attività è stata completata in tempo
                        val activity = _currentActivity.value
                        val isOnTime = activity?.maxTime == null ||
                                (activity.maxTime > 0 && System.currentTimeMillis() < activity.createdAt.time + (activity.maxTime * 1000))

                        achievementRepository.incrementCompletedActivities(userId, isOnTime)
                        activityRepository.updateActivityStatus(subActivity.activityId, "completed")
                        _currentActivity.value = _currentActivity.value?.copy(status = "completed")
                    }

                    _actionState.value = ActionState.Success
                } else {
                    _actionState.value =
                        ActionState.Error("Errore nell'aggiornamento della sotto-attività")
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
    // Aggiungi in UserViewModel.kt:


    fun loadUserWeeklyProgress() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.uid ?: return@launch

            // In un'app reale, recupereremmo questi dati dal repository
            // Per ora, simulo alcuni dati di esempio
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            val days = mutableListOf<String>()
            val values = mutableListOf<Int>()

            // Genera dati per i giorni precedenti fino ad oggi
            for (i in Calendar.SUNDAY until dayOfWeek + 1) {
                val day = when (i) {
                    Calendar.MONDAY -> "Lun"
                    Calendar.TUESDAY -> "Mar"
                    Calendar.WEDNESDAY -> "Mer"
                    Calendar.THURSDAY -> "Gio"
                    Calendar.FRIDAY -> "Ven"
                    Calendar.SATURDAY -> "Sab"
                    Calendar.SUNDAY -> "Dom"
                    else -> "?"
                }
                days.add(day)

                // Simula valori crescenti
                val randomValue = (10..30).random() * (i - Calendar.SUNDAY + 1)
                values.add(randomValue)
            }

            // Crea la mappa day -> value
            val progressMap = days.zip(values).toMap()
            _weeklyProgress.value = progressMap
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUser()?.uid ?: return@launch
                val user = userRepository.getUserById(currentUserId)
                _currentUser.value = user
            } catch (e: Exception) {
                // Gestione errori
                Log.e("UserViewModel", "Error loading current user: ${e.message}")
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