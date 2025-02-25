package com.uniupo.tieniiltempo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {

    suspend fun sendActivityNotification(activityId: String): Boolean {
        return try {
            val data = hashMapOf(
                "type" to "activity",
                "activityId" to activityId
            )

            functions.getHttpsCallable("sendNotification")
                .call(data).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendMessageNotification(activityId: String, messageId: String): Boolean {
        return try {
            val data = hashMapOf(
                "type" to "message",
                "activityId" to activityId,
                "messageId" to messageId
            )

            functions.getHttpsCallable("sendNotification")
                .call(data).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendTimeoutNotification(activityId: String, subActivityId: String): Boolean {
        return try {
            val data = hashMapOf(
                "type" to "timeout",
                "activityId" to activityId,
                "subActivityId" to subActivityId
            )

            functions.getHttpsCallable("sendNotification")
                .call(data).await()

            true
        } catch (e: Exception) {
            false
        }
    }
}