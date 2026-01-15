package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.UserChallengeStatus
import com.example.habittracker.data.model.UserChallenge

/**
 * Repository for managing UserChallenge relationships
 * Handles user joining/leaving challenges and tracking progress
 */
class UserChallengeRepository {
    private val collectionName = UserChallenge.COLLECTION_NAME

    /**
     * Get all challenges joined by a user
     */
    suspend fun getUserChallenges(userId: String): List<UserChallenge> {
        return try {
            FirestoreManager.getCollection(collectionName) { doc ->
                UserChallenge.fromDocument(doc)
            }.filter { it?.userId == userId }.filterNotNull()
        } catch (e: Exception) {
            println("Error getting user challenges: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get all ongoing challenges for a user
     */
    suspend fun getOngoingChallenges(userId: String): List<UserChallenge> {
        return getUserChallenges(userId).filter { it.status == UserChallengeStatus.ONGOING }
    }

    /**
     * Get all completed challenges for a user
     */
    suspend fun getCompletedChallenges(userId: String): List<UserChallenge> {
        return getUserChallenges(userId).filter { it.status == UserChallengeStatus.COMPLETED }
    }

    /**
     * Check if user has joined a challenge
     */
    suspend fun hasUserJoinedChallenge(userId: String, challengeId: String): Boolean {
        return try {
            val id = "${userId}_${challengeId}"
            val doc = FirestoreManager.getDocument(collectionName, id)
            doc != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get UserChallenge record by ID
     */
    suspend fun getUserChallenge(userId: String, challengeId: String): UserChallenge? {
        return try {
            val id = "${userId}_${challengeId}"
            val doc = FirestoreManager.getDocument(collectionName, id)
            doc?.let { UserChallenge.fromDocument(it) }
        } catch (e: Exception) {
            println("Error getting user challenge: ${e.message}")
            null
        }
    }

    /**
     * User joins a challenge
     */
    suspend fun joinChallenge(userId: String, challengeId: String): Boolean {
        return try {
            val userChallenge = UserChallenge(
                userId = userId,
                challengeId = challengeId,
                joinedAt = System.currentTimeMillis(),
                status = UserChallengeStatus.ONGOING
            )
            val id = userChallenge.generateId()
            println("DEBUG: Attempting to join challenge - userId: $userId, challengeId: $challengeId, id: $id")
            val result = FirestoreManager.addDocumentWithId(
                collectionName,
                id,
                userChallenge.toMap()
            )
            val success = result != null
            println("DEBUG: Join result - success: $success, result: $result")
            success
        } catch (e: Exception) {
            println("Error joining challenge: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * User leaves a challenge
     */
    suspend fun leaveChallenge(userId: String, challengeId: String): Boolean {
        return try {
            val id = "${userId}_${challengeId}"
            FirestoreManager.deleteDocument(collectionName, id)
        } catch (e: Exception) {
            println("Error leaving challenge: ${e.message}")
            false
        }
    }

    /**
     * Update challenge progress for a user
     */
    suspend fun updateProgress(
        userId: String,
        challengeId: String,
        progress: Int
    ): Boolean {
        return try {
            val id = "${userId}_${challengeId}"
            FirestoreManager.updateDocument(
                collectionName,
                id,
                mapOf("progress" to progress)
            )
        } catch (e: Exception) {
            println("Error updating progress: ${e.message}")
            false
        }
    }

    /**
     * Complete a challenge
     */
    suspend fun completeChallenge(userId: String, challengeId: String): Boolean {
        return try {
            val id = "${userId}_${challengeId}"
            FirestoreManager.updateDocument(
                collectionName,
                id,
                mapOf(
                    "status" to UserChallengeStatus.COMPLETED.name,
                    "completedAt" to System.currentTimeMillis(),
                    "progress" to 100
                )
            )
        } catch (e: Exception) {
            println("Error completing challenge: ${e.message}")
            false
        }
    }

    /**
     * Abandon a challenge
     */
    suspend fun abandonChallenge(userId: String, challengeId: String): Boolean {
        return try {
            val id = "${userId}_${challengeId}"
            FirestoreManager.updateDocument(
                collectionName,
                id,
                mapOf("status" to UserChallengeStatus.ABANDONED.name)
            )
        } catch (e: Exception) {
            println("Error abandoning challenge: ${e.message}")
            false
        }
    }

    /**
     * Get total users joined a specific challenge
     */
    suspend fun getChallengeParticipantCount(challengeId: String): Int {
        return try {
            FirestoreManager.getCollection(collectionName) { doc ->
                UserChallenge.fromDocument(doc)
            }.filterNotNull().count { it.challengeId == challengeId }
        } catch (e: Exception) {
            println("Error getting participant count: ${e.message}")
            0
        }
    }
}
