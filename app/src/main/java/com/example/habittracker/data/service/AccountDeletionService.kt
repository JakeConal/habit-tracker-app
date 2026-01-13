package com.example.habittracker.data.service

import com.example.habittracker.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service to coordinate the hard deletion of a user account and all associated data.
 */
class AccountDeletionService private constructor() {

    private val authRepository = AuthRepository.getInstance()
    private val userRepository = FirestoreUserRepository.getInstance()
    private val habitRepository = HabitRepository.getInstance()
    private val categoryRepository = CategoryRepository.getInstance()
    private val postRepository = PostRepository.getInstance()
    private val friendRepository = FriendRepository.getInstance()

    companion object {
        @Volatile
        private var instance: AccountDeletionService? = null

        fun getInstance(): AccountDeletionService {
            return instance ?: synchronized(this) {
                instance ?: AccountDeletionService().also { instance = it }
            }
        }
    }

    /**
     * Delete the current user's account and all associated data.
     */
    suspend fun deleteCurrentUserAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = authRepository.getCurrentUser() ?: return@withContext Result.failure(Exception("No user logged in"))
            val userId = user.uid

            // 1. Delete Habits
            habitRepository.deleteHabitsForUser(userId)

            // 2. Delete Categories
            categoryRepository.deleteCategoriesForUser(userId)

            // 3. Delete Posts and User Content
            postRepository.deleteUserContent(userId)

            // 4. Delete Friend Data (ships and requests)
            friendRepository.deleteUserData(userId)

            // 5. Delete User Profile from Firestore
            userRepository.deleteUser(userId)

            // 6. Delete Firebase Auth Account
            val authResult = authRepository.deleteAccount()
            
            if (authResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(authResult.exceptionOrNull() ?: Exception("Failed to delete auth account"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
