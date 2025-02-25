package com.uniupo.tieniiltempo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.uniupo.tieniiltempo.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    fun getMessagesForActivity(activityId: String): Flow<List<Message>> {
        return firestore.collection("messages")
            .whereEqualTo("activityId", activityId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
            }
    }


    suspend fun sendMessage(message: Message): Boolean {
        return try {
            val docRef = firestore.collection("messages").document()
            val messageWithId = message.copy(id = docRef.id)
            docRef.set(messageWithId).await()

            // Invia notifica
            notificationRepository.sendMessageNotification(messageWithId.activityId, messageWithId.id)

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendMessageWithImage(message: Message, imageStream: InputStream): Boolean {
        return try {
            // Upload image to Firebase Storage
            val imageName = "chat_images/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(imageName)
            storageRef.putStream(imageStream).await()

            // Get download URL for the image
            val imageUrl = storageRef.downloadUrl.await().toString()

            // Create and send message with image URL
            val messageWithImage = message.copy(imageUrl = imageUrl)
            sendMessage(messageWithImage)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markMessagesAsRead(activityId: String, userId: String) {
        try {
            val messages = firestore.collection("messages")
                .whereEqualTo("activityId", activityId)
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("isRead", false)
                .get().await()

            for (document in messages.documents) {
                firestore.collection("messages").document(document.id)
                    .update("isRead", true).await()
            }
        } catch (e: Exception) {
            // Handle exception
        }
    }
}