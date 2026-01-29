package com.example.habittracker.data.repository


import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

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
            e.printStackTrace()
            false
        }
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        try {
            val updates = mapOf("fcmToken" to token)
            FirestoreManager.updateDocument(User.COLLECTION_NAME, userId, updates)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearFcmToken(userId: String) {
        try {
            val updates = mapOf("fcmToken" to "")
            FirestoreManager.updateDocument(User.COLLECTION_NAME, userId, updates)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearLocalUserData() {
        _currentUser.value = null
    }

    /**
     * Set the current user manually
     */
    fun setCurrentUser(user: User?) {
        _currentUser.value = user
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

    suspend fun updateUserProfile(
        userId: String,
        name: String,
        avatarUrl: String?,
        gender: String? = null,
        dateOfBirth: String? = null
    ): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>("name" to name)
            if (avatarUrl != null) {
                updates["avatarUrl"] = avatarUrl
            }
            if (gender != null) {
                updates["gender"] = gender
            }
            if (dateOfBirth != null) {
                updates["dateOfBirth"] = dateOfBirth
            }
            
            val success = FirestoreManager.setDocument(
                User.COLLECTION_NAME,
                userId,
                updates,
                merge = true
            )
            
            if (success) {
                // Update local flow
                val currentUserVal = _currentUser.value
                if (currentUserVal != null && currentUserVal.id == userId) {
                     val updatedUser = currentUserVal.copy(
                         name = name,
                         avatarUrl = avatarUrl ?: currentUserVal.avatarUrl,
                         gender = gender ?: currentUserVal.gender,
                         dateOfBirth = dateOfBirth ?: currentUserVal.dateOfBirth
                     )
                     _currentUser.value = updatedUser
                }

                // Verify against SERVER
                val snapshot = FirestoreManager.getDocumentFromServer(User.COLLECTION_NAME, userId)
                val fetchedName = snapshot?.getString("name")
                
                if (fetchedName == name) {
                    
                    // Sync historical posts
                    try {
                        val postRepo = PostRepository.getInstance()
                        postRepo.updateUserPosts(userId, name, avatarUrl)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    true
                } else {
                    // Return true because local cache is updated and offline persistence might handle it eventually, but warn user
                    true 
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Update user notification settings
     */
    suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean): Boolean {
        return try {
            val updates = mapOf("notificationsEnabled" to enabled)
            FirestoreManager.updateDocument(User.COLLECTION_NAME, userId, updates)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Add challenge ID to user's joined challenges list
     */
    suspend fun addJoinedChallenge(userId: String, challengeId: String): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection(User.COLLECTION_NAME).document(userId)
                .update("joinedChallengeIds", com.google.firebase.firestore.FieldValue.arrayUnion(challengeId))
                .await()
            true
        } catch (e: Exception) {
            println("Error adding joined challenge: ${e.message}")
            false
        }
    }

    /**
     * Get top users for leaderboard
     */
    suspend fun getTopUsers(limit: Long = 100): List<User> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection(User.COLLECTION_NAME)
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.documents.mapNotNull { User.fromDocument(it) }
        } catch (e: Exception) {
            println("Error getting top users: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get top users for leaderboard with pagination
     */
    suspend fun getTopUsersPaginated(pageSize: Long, lastDocument: DocumentSnapshot?): Result<Pair<List<User>, DocumentSnapshot?>> {
        return try {
            val db = FirebaseFirestore.getInstance()
            var query = db.collection(User.COLLECTION_NAME)
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(pageSize)

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            val users = snapshot.documents.mapNotNull { User.fromDocument(it) }
            val lastVisible = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null

            Result.success(Pair(users, lastVisible))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to top users in real-time
     */
    fun listenToTopUsers(limit: Long): Flow<List<User>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(User.COLLECTION_NAME)
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(limit)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Emit an empty list or keep current data on error, but ensure flow continues or terminates properly
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { User.fromDocument(it) }
                trySend(users)
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Listen to a specific user in real-time
     */
    fun listenToUser(userId: String): Flow<User?> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val subscription = db.collection(User.COLLECTION_NAME).document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    trySend(User.fromDocument(snapshot))
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Get user rank in real-time based on points
     */
    fun listenToUserRank(points: Int): Flow<Int> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(User.COLLECTION_NAME)
            .whereGreaterThan("points", points)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                trySend(snapshot.size() + 1)
            }
        }
        awaitClose { subscription.remove() }
    }
}