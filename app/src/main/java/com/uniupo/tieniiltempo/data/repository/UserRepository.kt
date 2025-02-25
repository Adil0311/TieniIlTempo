package com.uniupo.tieniiltempo.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniupo.tieniiltempo.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun getCurrentUser() = auth.currentUser

    suspend fun getUserById(userId: String): User? {
        return try {
            firestore.collection("users").document(userId)
                .get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUserProfile(user: User): Boolean {
        return try {
            firestore.collection("users").document(user.id)
                .set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun updateUserFcmToken(userId: String, token: String): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .update("fcmToken", token).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUsersByRole(role: String): List<User> {
        return try {
            firestore.collection("users")
                .whereEqualTo("role", role)
                .get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}