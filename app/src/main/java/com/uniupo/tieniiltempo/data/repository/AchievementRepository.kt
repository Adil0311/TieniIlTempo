// app/src/main/java/com/uniupo/tieniiltempo/data/repository/AchievementRepository.kt
package com.uniupo.tieniiltempo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.uniupo.tieniiltempo.data.model.UserAchievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AchievementRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getUserAchievement(userId: String): Flow<UserAchievement> {
        return firestore.collection("achievements")
            .document(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(UserAchievement::class.java) ?: UserAchievement(userId = userId)
            }
    }

    suspend fun updateUserPoints(userId: String, pointsToAdd: Int): Boolean {
        return try {
            val docRef = firestore.collection("achievements").document(userId)
            val achievement = docRef.get().await().toObject(UserAchievement::class.java)
                ?: UserAchievement(userId = userId)

            val updatedAchievement = achievement.copy(points = achievement.points + pointsToAdd)
            docRef.set(updatedAchievement).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addBadge(userId: String, badgeName: String): Boolean {
        return try {
            val docRef = firestore.collection("achievements").document(userId)
            val achievement = docRef.get().await().toObject(UserAchievement::class.java)
                ?: UserAchievement(userId = userId)

            if (!achievement.badges.contains(badgeName)) {
                val updatedBadges = achievement.badges + badgeName
                docRef.update("badges", updatedBadges).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun incrementCompletedActivities(userId: String, onTime: Boolean = false): Boolean {
        return try {
            val docRef = firestore.collection("achievements").document(userId)
            val achievement = docRef.get().await().toObject(UserAchievement::class.java)
                ?: UserAchievement(userId = userId)

            val updatedAchievement = achievement.copy(
                completedActivities = achievement.completedActivities + 1,
                completedOnTime = if (onTime) achievement.completedOnTime + 1 else achievement.completedOnTime
            )

            docRef.set(updatedAchievement).await()

            // Verifica e assegna badge in base ai risultati
            checkAndAssignBadges(userId, updatedAchievement)

            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun checkAndAssignBadges(userId: String, achievement: UserAchievement) {
        // Assegna badge per il primo completamento
        if (achievement.completedActivities == 1) {
            addBadge(userId, "primo_completamento")
        }

        // Assegna badge per 5 completamenti
        if (achievement.completedActivities == 5) {
            addBadge(userId, "cinque_completamenti")
        }

        // Assegna badge per 10 completamenti in tempo
        if (achievement.completedOnTime == 10) {
            addBadge(userId, "puntuale")
        }
    }
}