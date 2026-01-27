package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.model.ChallengeStatus
import com.example.habittracker.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class ChallengeRepository {
    private val collectionName = "challenges"
    private val userChallengeRepository = UserChallengeRepository()

    // Get all approved challenges
    suspend fun getAllChallenges(): List<Challenge> {
        return try {
            val list = FirestoreManager.getCollection(collectionName) { doc ->
                Challenge.fromDocument(doc)
            }
            list.filter { it.status == ChallengeStatus.APPROVED }
        } catch (e: Exception) {
            println("Error getting all challenges: ${e.message}")
            emptyList()
        }
    }

    // Get all challenges including PENDING if the user is the creator
    suspend fun getAllVisibleChallenges(userId: String): List<Challenge> {
        return try {
            val list = FirestoreManager.getCollection(collectionName) { doc ->
                Challenge.fromDocument(doc)
            }
            list.filter {
                it.status == ChallengeStatus.APPROVED || (it.status == ChallengeStatus.PENDING && it.creatorId == userId)
            }
        } catch (e: Exception) {
            println("Error getting visible challenges: ${e.message}")
            emptyList()
        }
    }

    // Vote for a challenge through a post
    suspend fun voteForChallenge(challengeId: String, postId: String, userId: String): Boolean {
        return try {
            val challenge = getChallengeById(challengeId) ?: return false

            // Check if user already voted for this challenge (Global check)
            if (challenge.votedBy.contains(userId)) return false

            val postRepository = PostRepository.getInstance()
            val post = postRepository.getPostById(postId) ?: return false

            val newPostVotes = post.voteCount + 1
            val newPostVotedBy = post.votedBy + userId

            // Update Post (the one clicked)
            val postUpdated = FirestoreManager.updateDocument(
                "posts",
                postId,
                mapOf(
                    "voteCount" to newPostVotes,
                    "votedBy" to newPostVotedBy
                )
            )

            if (postUpdated) {
                // Also update the user's votedChallengeIds (Global state for UI sync)
                FirestoreManager.updateDocument(
                    "users",
                    userId,
                    mapOf("votedChallengeIds" to FieldValue.arrayUnion(challengeId))
                )

                // Update ALL posts with this challengeId to ensure everything stays in sync
                try {
                    val db = FirebaseFirestore.getInstance()
                    val querySnapshot = db.collection("posts")
                        .whereEqualTo("challengeId", challengeId)
                        .get()
                        .await()

                    val batch = db.batch()
                    for (doc in querySnapshot.documents) {
                        val p = Post.fromDocument(doc)
                        if (p != null && !p.votedBy.contains(userId)) {
                            batch.update(doc.reference, mapOf(
                                "voteCount" to (p.voteCount + 1),
                                "votedBy" to FieldValue.arrayUnion(userId)
                            ))
                        }
                    }
                    batch.commit().await()
                } catch (e: Exception) {
                    println("Error syncing all challenge posts: ${e.message}")
                }

                val newChallengeVotes = challenge.votes + 1
                val newChallengeVotedBy = challenge.votedBy + userId
                val updates = mutableMapOf<String, Any>(
                    "votes" to newChallengeVotes,
                    "votedBy" to newChallengeVotedBy
                )

                // Check if reached 1000 votes
                if (newChallengeVotes >= 1000 && challenge.status == ChallengeStatus.PENDING) {
                    updates["status"] = ChallengeStatus.APPROVED.name
                }

                FirestoreManager.updateDocument(collectionName, challengeId, updates)
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error voting for challenge: ${e.message}")
            false
        }
    }

    // Get all challenges with join status for a specific user
    suspend fun getAllChallengesWithUserStatus(userId: String): List<ChallengeWithStatus> {
        return try {
            val challenges = getAllVisibleChallenges(userId)
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