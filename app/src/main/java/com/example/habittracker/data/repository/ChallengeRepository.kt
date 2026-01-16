package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.model.ChallengeStatus

class ChallengeRepository {
    private val collectionName = "challenges"
    private val userChallengeRepository = UserChallengeRepository()

    // Get all approved challenges
    suspend fun getAllChallenges(): List<Challenge> {
        return try {
            FirestoreManager.getCollection(collectionName) { doc ->
                Challenge.fromDocument(doc)
            }.filterNotNull().filter { it.status == ChallengeStatus.APPROVED }
        } catch (e: Exception) {
            println("Error getting all challenges: ${e.message}")
            emptyList()
        }
    }

    // Get all pending challenges for admin review
    suspend fun getPendingChallenges(): List<Challenge> {
        return try {
            FirestoreManager.getCollection(collectionName) { doc ->
                Challenge.fromDocument(doc)
            }.filterNotNull().filter { it.status == ChallengeStatus.PENDING }
        } catch (e: Exception) {
            println("Error getting pending challenges: ${e.message}")
            emptyList()
        }
    }

    // Approve a challenge
    suspend fun approveChallenge(challengeId: String): Boolean {
        return try {
            FirestoreManager.updateDocument(
                collectionName,
                challengeId,
                mapOf("status" to ChallengeStatus.APPROVED.name)
            )
        } catch (e: Exception) {
            println("Error approving challenge: ${e.message}")
            false
        }
    }

    // Reject a challenge
    suspend fun rejectChallenge(challengeId: String): Boolean {
        return try {
            FirestoreManager.updateDocument(
                collectionName,
                challengeId,
                mapOf("status" to ChallengeStatus.REJECTED.name)
            )
        } catch (e: Exception) {
            println("Error rejecting challenge: ${e.message}")
            false
        }
    }

    // Get all challenges with join status for a specific user
    suspend fun getAllChallengesWithUserStatus(userId: String): List<ChallengeWithStatus> {
        return try {
            val challenges = getAllChallenges()
            challenges.map { challenge ->
                val isJoined = userChallengeRepository.hasUserJoinedChallenge(userId, challenge.id)
                ChallengeWithStatus(
                    challenge = challenge,
                    isJoined = isJoined
                )
            }
        } catch (e: Exception) {
            println("Error getting challenges with user status: ${e.message}")
            emptyList()
        }
    }

    // Get challenge by ID
    suspend fun getChallengeById(id: String): Challenge? {
        return try {
            val doc = FirestoreManager.getDocument(collectionName, id)
            doc?.let { Challenge.fromDocument(it) }
        } catch (e: Exception) {
            println("Error getting challenge by ID: ${e.message}")
            null
        }
    }

    // Get challenge by ID with user join status
    suspend fun getChallengeWithStatus(id: String, userId: String): ChallengeWithStatus? {
        return try {
            val challenge = getChallengeById(id)
            if (challenge != null) {
                val isJoined = userChallengeRepository.hasUserJoinedChallenge(userId, id)
                ChallengeWithStatus(
                    challenge = challenge,
                    isJoined = isJoined
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error getting challenge with status: ${e.message}")
            null
        }
    }

    // Create new challenge
    suspend fun createChallenge(challenge: Challenge): String? {
        return try {
            val challengeMap = challenge.toMap()
            FirestoreManager.addDocument(collectionName, challengeMap)
        } catch (e: Exception) {
            println("Error creating challenge: ${e.message}")
            null
        }
    }

    // Update existing challenge
    suspend fun updateChallenge(id: String, challenge: Challenge): Boolean {
        return try {
            val challengeMap = challenge.toMap()
            FirestoreManager.updateDocument(collectionName, id, challengeMap)
        } catch (e: Exception) {
            println("Error updating challenge: ${e.message}")
            false
        }
    }

    // Delete challenge
    suspend fun deleteChallenge(id: String): Boolean {
        return try {
            FirestoreManager.deleteDocument(collectionName, id)
        } catch (e: Exception) {
            println("Error deleting challenge: ${e.message}")
            false
        }
    }

    // Update participant count when user joins/leaves
    suspend fun updateParticipantCount(challengeId: String): Boolean {
        return try {
            val count = userChallengeRepository.getChallengeParticipantCount(challengeId)
            FirestoreManager.updateDocument(
                collectionName,
                challengeId,
                mapOf("participantCount" to count)
            )
        } catch (e: Exception) {
            println("Error updating participant count: ${e.message}")
            false
        }
    }
}

/**
 * Data class to hold Challenge with user's join status
 */
data class ChallengeWithStatus(
    val challenge: Challenge,
    val isJoined: Boolean
)