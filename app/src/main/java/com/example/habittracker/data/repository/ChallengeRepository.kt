package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.Challenge

class ChallengeRepository {
    private val collectionName = "challenges"

    // Get all challenges
    suspend fun getAllChallenges(): List<Challenge> {
        return FirestoreManager.getCollection(collectionName) { doc ->
            doc.toObject(Challenge::class.java)
        }
    }

    // Get challenge by ID
    suspend fun getChallengeById(id: String): Challenge? {
        val doc = FirestoreManager.getDocument(collectionName, id)
        return doc?.toObject(Challenge::class.java)
    }
}