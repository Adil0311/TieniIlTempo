package com.uniupo.tieniiltempo.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniupo.tieniiltempo.data.model.Activity
import com.uniupo.tieniiltempo.data.model.Message
import com.uniupo.tieniiltempo.data.model.User
import com.uniupo.tieniiltempo.data.repository.ActivityRepository
import com.uniupo.tieniiltempo.data.repository.ChatRepository
import com.uniupo.tieniiltempo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.Date
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    val userRepository: UserRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity

    private val _chatPartner = MutableStateFlow<User?>(null)
    val chatPartner: StateFlow<User?> = _chatPartner

    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState

    fun loadActivity(activityId: String) {
        viewModelScope.launch {
            activityRepository.getActivityById(activityId)?.let { activity ->
                _activity.value = activity

                // Determine if the current user is caregiver or regular user
                val currentUserId = userRepository.getCurrentUser()?.uid ?: return@launch
                val partnerId = if (activity.caregiverId == currentUserId) {
                    activity.userId
                } else {
                    activity.caregiverId
                }

                // Load chat partner details
                _chatPartner.value = userRepository.getUserById(partnerId)

                // Mark messages as read
                chatRepository.markMessagesAsRead(activityId, currentUserId)
            }
        }
    }

    fun getMessages(activityId: String): StateFlow<List<Message>> {
        return chatRepository.getMessagesForActivity(activityId)
            .catch { emit(emptyList()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun sendTextMessage(activityId: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading

            val currentUserId = userRepository.getCurrentUser()?.uid ?: return@launch

            val message = Message(
                activityId = activityId,
                senderId = currentUserId,
                text = text,
                timestamp = Date()
            )

            val success = chatRepository.sendMessage(message)
            _sendMessageState.value = if (success) {
                SendMessageState.Success
            } else {
                SendMessageState.Error("Errore nell'invio del messaggio")
            }
        }
    }

    fun sendImageMessage(activityId: String, text: String, imageStream: InputStream) {
        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading

            val currentUserId = userRepository.getCurrentUser()?.uid ?: return@launch

            val message = Message(
                activityId = activityId,
                senderId = currentUserId,
                text = text,
                timestamp = Date()
            )

            val success = chatRepository.sendMessageWithImage(message, imageStream)
            _sendMessageState.value = if (success) {
                SendMessageState.Success
            } else {
                SendMessageState.Error("Errore nell'invio dell'immagine")
            }
        }
    }
}

sealed class SendMessageState {
    object Idle : SendMessageState()
    object Loading : SendMessageState()
    object Success : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}