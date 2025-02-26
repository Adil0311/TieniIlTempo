package com.uniupo.tieniiltempo.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniupo.tieniiltempo.data.model.ChatPreview
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.ChatRepository
import com.uniupo.tieniiltempo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats: StateFlow<List<ChatPreview>> = _chats

    fun loadChats() {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUser()?.uid ?: return@launch

            // Ottieni tutte le attività relative all'utente corrente (come caregiver o user)
            val caregiverActivities = activityRepository.getActivitiesForUser(currentUserId, "caregiver")
            val userActivities = activityRepository.getActivitiesForUser(currentUserId, "user")

            val allActivities = caregiverActivities + userActivities

            val chatPreviews = mutableListOf<ChatPreview>()

            for (activity in allActivities) {
                // Determina l'ID dell'altro utente nella chat
                val otherUserId = if (activity.caregiverId == currentUserId) {
                    activity.userId
                } else {
                    activity.caregiverId
                }

                // Ottieni le informazioni sull'altro utente
                val otherUser = userRepository.getUserById(otherUserId) ?: continue

                // Ottieni l'ultimo messaggio della chat
                val lastMessage = chatRepository.getLastMessageForActivity(activity.id)

                val chatPreview = ChatPreview(
                    activityId = activity.id,
                    activityTitle = activity.title,
                    userId = otherUserId,
                    userName = otherUser.name,
                    userImage = otherUser.profileImage,
                    lastMessage = lastMessage?.text ?: "Nessun messaggio",
                    lastMessageTime = lastMessage?.timestamp,
                    hasUnreadMessages = chatRepository.hasUnreadMessages(activity.id, currentUserId)
                )

                chatPreviews.add(chatPreview)
            }

            // Ordina per data dell'ultimo messaggio (più recente in alto)
            _chats.value = chatPreviews.sortedByDescending { it.lastMessageTime }
        }
    }
}