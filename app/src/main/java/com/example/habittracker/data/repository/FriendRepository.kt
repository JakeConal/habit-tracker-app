package com.example.habittracker.data.repository

import com.example.habittracker.data.firebase.FirestoreManager
import com.example.habittracker.data.model.Friend
import com.example.habittracker.data.model.FriendRequest
import com.example.habittracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userRepository = FirestoreUserRepository.getInstance()

    companion object {
        @Volatile
        private var instance: FriendRepository? = null

        fun getInstance(): FriendRepository {
            return instance ?: synchronized(this) {
                instance ?: FriendRepository().also { instance = it }
            }
        }
    }

    /**
     * Send a friend request
     */
    suspend fun sendFriendRequest(receiverId: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false
        if (currentUserId == receiverId) return false
        
        // Check if already friends or request pending
        if (isFriend(currentUserId, receiverId)) return false
        
        // Get current user info for the request
        val currentUser = userRepository.getUserById(currentUserId) ?: return false

        val request = FriendRequest(
            senderId = currentUserId,
            senderName = currentUser.name,
            senderAvatarUrl = currentUser.avatarUrl ?: "",
            receiverId = receiverId,
            timestamp = System.currentTimeMillis()
        )

        return FirestoreManager.addDocument(
            FriendRequest.COLLECTION_NAME,
            request.toMap()
        ) != null
    }

    /**
     * Accept a friend request
     */
    suspend fun acceptFriendRequest(requestId: String): Boolean {
        val requestDoc = FirestoreManager.getDocument(FriendRequest.COLLECTION_NAME, requestId) ?: return false
        val request = FriendRequest.fromDocument(requestDoc) ?: return false
        
        val currentUserId = auth.currentUser?.uid ?: return false
        if (request.receiverId != currentUserId) return false

        // Start transaction (simplified as batch writes)
        try {
             // 1. Add to my friends
             firestore.collection("users").document(currentUserId)
                 .collection("friends").document(request.senderId)
                 .set(mapOf("since" to System.currentTimeMillis()))
                 .await()

             // 2. Add to their friends
             firestore.collection("users").document(request.senderId)
                 .collection("friends").document(currentUserId)
                 .set(mapOf("since" to System.currentTimeMillis()))
                 .await()

             // 3. Delete request
             FirestoreManager.deleteDocument(FriendRequest.COLLECTION_NAME, requestId)
             
             return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Reject a friend request
     */
    suspend fun rejectFriendRequest(requestId: String): Boolean {
        return FirestoreManager.deleteDocument(FriendRequest.COLLECTION_NAME, requestId)
    }

    /**
     * Get pending friend requests for current user
     */
    suspend fun getFriendRequests(): List<FriendRequest> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()
        
        return FirestoreManager.getCollectionWhere(
            FriendRequest.COLLECTION_NAME,
            "receiverId",
            currentUserId
        ) { FriendRequest.fromDocument(it) }
    }

    /**
     * Get my friends (returns detailed User objects)
     */
    suspend fun getFriends(): List<User> {
         val currentUserId = auth.currentUser?.uid ?: return emptyList()
         
         val friendsList = try {
             firestore.collection("users").document(currentUserId)
                 .collection("friends")
                 .get()
                 .await()
                 .documents
                 .mapNotNull { Friend.fromDocument(it) }
         } catch (e: Exception) {
             emptyList()
         }
         
         if (friendsList.isEmpty()) return emptyList()

         // Fetch details for each friend
         // Note: In a production app, we might store basic info in the friend doc to avoid N+1 reads,
         // or use `whereIn` queries if list is small (< 10).
         // Here detailed info is needed, let's fetch individually for now
         
         return friendsList.mapNotNull { friend ->
             userRepository.getUserById(friend.userId)
         }
    }
    
    /**
     * Check if two users are friends
     */
    suspend fun isFriend(userId1: String, userId2: String): Boolean {
        return try {
            val doc = firestore.collection("users").document(userId1)
                .collection("friends").document(userId2)
                .get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Unfriend
     */
     suspend fun unfriend(friendId: String): Boolean {
         val currentUserId = auth.currentUser?.uid ?: return false
         
         return try {
             // Delete from my friends
             firestore.collection("users").document(currentUserId)
                 .collection("friends").document(friendId)
                 .delete().await()
                 
             // Delete from their friends
             firestore.collection("users").document(friendId)
                 .collection("friends").document(currentUserId)
                 .delete().await()
                 
             true
         } catch (e: Exception) {
             e.printStackTrace()
             return false
         }
     }
}
