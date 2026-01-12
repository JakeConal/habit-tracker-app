package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing User data in Firestore
 */
class FirestoreUserRepository private constructor() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: FirestoreUserRepository? = null

        fun getInstance(): FirestoreUserRepository {
            return instance ?: synchronized(this) {
                instance ?: FirestoreUserRepository().also { instance = it }
            }
        }
    }

    /**
     * Create or update user in Firestore
     */
    suspend fun createOrUpdateUser(user: User): Boolean {
        return try {
            val success = if (userExists(user.id)) {
                FirestoreManager.updateDocument(
                    User.COLLECTION_NAME,
                    user.id,
                    user.toMap()
                )
            } else {
                FirestoreManager.addDocumentWithId(
                    User.COLLECTION_NAME,
                    user.id,
                    user.toMap()
                ) != null
            }
            if (success) {
                _currentUser.value = user
            }
            success
        } catch (e: Exception) {
            println("Error creating/updating user: ${e.message}")
            false
        }
    }

    /**
     * Get user by ID from Firestore
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val document = FirestoreManager.getDocument(User.COLLECTION_NAME, userId)
            document?.let { User.fromDocument(it) }
        } catch (e: Exception) {
            println("Error getting user: ${e.message}")
            null
        }
    }

    /**
     * Check if user exists in Firestore
     */
    private suspend fun userExists(userId: String): Boolean {
        return try {
            val document = FirestoreManager.getDocument(User.COLLECTION_NAME, userId)
            document?.exists() == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update user points
     */
    suspend fun updateUserPoints(userId: String, points: Int): Boolean {
        return try {
            FirestoreManager.updateDocument(
                User.COLLECTION_NAME,
                userId,
                mapOf("points" to points)
            )
        } catch (e: Exception) {
            println("Error updating user points: ${e.message}")
            false
        }
    }

    /**
     * Update user rank
     */
    suspend fun updateUserRank(userId: String, rank: Int): Boolean {
        return try {
            FirestoreManager.updateDocument(
                User.COLLECTION_NAME,
                userId,
                mapOf("rank" to rank)
            )
        } catch (e: Exception) {
            println("Error updating user rank: ${e.message}")
            false
        }
    }

    /**
     * Update user avatar
     */
    suspend fun updateUserAvatar(userId: String, avatarUrl: String): Boolean {
        return try {
            FirestoreManager.updateDocument(
                User.COLLECTION_NAME,
                userId,
                mapOf("avatarUrl" to avatarUrl)
            )
        } catch (e: Exception) {
            println("Error updating user avatar: ${e.message}")
            false
        }
    }

    /**
     * Update last login time
     */
    suspend fun updateLastLogin(userId: String): Boolean {
        return try {
            FirestoreManager.updateDocument(
                User.COLLECTION_NAME,
                userId,
                mapOf("lastLoginAt" to System.currentTimeMillis())
            )
        } catch (e: Exception) {
            println("Error updating last login: ${e.message}")
            false
        }
    }

    /**
     * Get current authenticated user from Firestore
     */
    suspend fun getCurrentUser(): User? {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                val user = getUserById(currentUserId)
                _currentUser.value = user
                user
            } else {
                _currentUser.value = null
                null
            }
        } catch (e: Exception) {
            println("Error getting current user: ${e.message}")
            _currentUser.value = null
            null
        }
    }

    /**
     * Delete user from Firestore
     */
    suspend fun deleteUser(userId: String): Boolean {
        return try {
            val success = FirestoreManager.deleteDocument(User.COLLECTION_NAME, userId)
            if (success && _currentUser.value?.id == userId) {
                _currentUser.value = null
            }
            success
        } catch (e: Exception) {
            println("Error deleting user: ${e.message}")
            false
        }
    }

    /**
     * Search users by name
     */
    suspend fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return emptyList()
        
        // Search by name (prefix)
        val nameResults = FirestoreManager.searchCollection(
            User.COLLECTION_NAME,
            "name",
            query
        ) { User.fromDocument(it) }

        // Search by email (prefix)
        val emailResults = FirestoreManager.searchCollection(
             User.COLLECTION_NAME,
             "email",
             query
        ) { User.fromDocument(it) }

        // Combine and remove duplicates
        return (nameResults + emailResults).distinctBy { it.id }
    }
}