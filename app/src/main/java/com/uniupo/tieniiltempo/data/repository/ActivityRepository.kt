package com.uniupo.tieniiltempo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uniupo.tieniiltempo.data.model.Activity
import com.uniupo.tieniiltempo.data.model.SubActivity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ActivityRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createActivity(activity: Activity): String? {
        return try {
            val docRef = firestore.collection("activities").document()
            val activityWithId = activity.copy(id = docRef.id)
            docRef.set(activityWithId).await()

            // Invia notifica
            notificationRepository.sendActivityNotification(activityWithId.id)

            activityWithId.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getActivitiesForUser(userId: String, role: String): List<Activity> {
        return try {
            val field = if (role == "caregiver") "caregiverId" else "userId"
            firestore.collection("activities")
                .whereEqualTo(field, userId)
                .get().await().toObjects(Activity::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getActivityById(activityId: String): Activity? {
        return try {
            firestore.collection("activities").document(activityId)
                .get().await().toObject(Activity::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Aggiungi al ActivityRepository.kt
    suspend fun getSubActivitiesForActivity(activityId: String): List<SubActivity> {
        return try {
            firestore.collection("subActivities")
                .whereEqualTo("activityId", activityId)
                .orderBy("order")
                .get().await().toObjects(SubActivity::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createSubActivity(subActivity: SubActivity): String? {
        return try {
            val docRef = firestore.collection("subActivities").document()
            val subActivityWithId = subActivity.copy(id = docRef.id)
            docRef.set(subActivityWithId).await()
            subActivityWithId.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateSubActivity(subActivity: SubActivity): Boolean {
        return try {
            firestore.collection("subActivities").document(subActivity.id)
                .set(subActivity).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateActivityStatus(activityId: String, status: String): Boolean {
        return try {
            firestore.collection("activities").document(activityId)
                .update("status", status).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSubActivityById(subActivityId: String): SubActivity? {
        return try {
            firestore.collection("subActivities").document(subActivityId)
                .get().await().toObject(SubActivity::class.java)
        } catch (e: Exception) {
            null
        }
    }


}