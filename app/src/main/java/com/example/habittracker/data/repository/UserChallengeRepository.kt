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
