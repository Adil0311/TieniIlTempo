package com.uniupo.tieniiltempo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uniupo.tieniiltempo.data.model.Activity
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
}