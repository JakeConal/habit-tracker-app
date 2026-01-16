package com.example.habittracker.data.repository

import android.content.Context
import android.net.Uri
import com.example.habittracker.data.model.Comment
import com.example.habittracker.data.model.Notification
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.remote.fcm.NotificationSender
import com.example.habittracker.data.supabase.SupabaseStorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Suppress("UNCHECKED_CAST")
class PostRepository private constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationRepository = NotificationRepository.getInstance()

    companion object {
        @Volatile
        private var instance: PostRepository? = null

        fun getInstance(): PostRepository {
            return instance ?: synchronized(this) {
                instance ?: PostRepository().also { instance = it }
            }
        }
    }

    // --- Post Functions ---


    suspend fun createPost(context: Context, post: Post, imageUri: Uri?): Result<Boolean> {
        return try {
            val imageUrl = if (imageUri != null) {
                // Use SupabaseStorageRepository to upload image
                val supabaseStorage = SupabaseStorageRepository()
                supabaseStorage.uploadImage(
                   uri = imageUri,
                   context = context,
                   bucketName = SupabaseStorageRepository.BUCKET_POSTS
                )
            } else {
                null
            }

            val postRef = db.collection("posts").document()

            val newPost = post.copy(
                id = postRef.id,
                imageUrl = imageUrl,
                shareCount = 0 // Ensure starts at 0
            )

            // Save the post
            postRef.set(newPost).await()

            // If this is a shared post (internal share), increment share count of original post
            if (!post.originalPostId.isNullOrEmpty()) {
                val originalRef = db.collection("posts").document(post.originalPostId)
                originalRef.update("shareCount", FieldValue.increment(1))

                // Notify original post owner
                if (!post.originalUserId.isNullOrEmpty()) {
                    notifyUser(
                        recipientId = post.originalUserId,
                        postId = newPost.id,
                        type = Notification.NotificationType.SHARE_POST,
                        customSenderName = newPost.authorName,
                        customSenderAvatar = newPost.authorAvatarUrl
                    )
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // New function to get all posts (required by FeedViewModel)
    suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val snapshot = db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val posts = snapshot.documents.mapNotNull { Post.fromDocument(it) }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // New function to get posts by user (required by ProfileViewModel)
    suspend fun getPostsByUser(userId: String): Result<List<Post>> {
        return try {
            val snapshot = db.collection("posts")
                .whereEqualTo("userId", userId)
                // .orderBy("timestamp", Query.Direction.DESCENDING) // Removed to avoid needing composite index
                .get()
                .await()
            val posts = snapshot.documents.mapNotNull { Post.fromDocument(it) }
                .sortedByDescending { it.timestamp } // Sort client-side
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // New function to update author info in all posts by user
    suspend fun updateUserPosts(userId: String, newName: String, newAvatarUrl: String?): Result<Boolean> {
        return try {
            val snapshot = db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val batch = db.batch()
            for (document in snapshot.documents) {
                val postRef = db.collection("posts").document(document.id)
                batch.update(postRef, "authorName", newName)
                if (newAvatarUrl != null) {
                    batch.update(postRef, "authorAvatarUrl", newAvatarUrl)
                }
            }
            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
             e.printStackTrace()
            Result.failure(e)
        }
    }



    suspend fun getPost(postId: String): Result<Post> {
        return try {
            val snapshot = db.collection("posts").document(postId).get().await()
            val post = Post.fromDocument(snapshot)
            if (post != null) {
                Result.success(post)
            } else {
                Result.failure(Exception("Post not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Comment Functions ---

    @Suppress("UNCHECKED_CAST")
    suspend fun getComments(postId: String): Result<List<Comment>> {
        return try {
            val snapshot = db.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { Comment.fromDocument(it) })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun listenToComments(postId: String): kotlinx.coroutines.flow.Flow<List<Comment>> = callbackFlow {
        val listenerRegistration = db.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { Comment.fromDocument(it) }
                    trySend(comments)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun commentPost(postId: String, comment: Comment): Result<Comment> {
        return try {
            val docRef = db.collection("posts").document(postId).collection("comments").document()

            // Ensure ID is generated from Firestore doc ref
            val finalComment = comment.copy(
                id = docRef.id
            )

            val batch = db.batch()
            val postRef = db.collection("posts").document(postId)

            batch.set(docRef, finalComment)
            batch.update(postRef, "commentCount", FieldValue.increment(1))

            batch.commit().await()

            // Notify post owner
            try {
                val postSnapshot = postRef.get().await()
                val postOwnerId = postSnapshot.getString("userId")
                if (postOwnerId != null) {
                    notifyUser(
                        postOwnerId,
                        postId,
                        finalComment.id,
                        Notification.NotificationType.COMMENT_POST,
                        finalComment.authorName,
                        finalComment.authorAvatarUrl
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(finalComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<Boolean> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
            val postRef = db.collection("posts").document(postId)

            val batch = db.batch()
            batch.delete(commentRef)
            batch.update(postRef, "commentCount", FieldValue.increment(-1))

            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replyToComment(postId: String, commentId: String, reply: Comment): Result<Comment> {
        return try {
            val replyId = UUID.randomUUID().toString()
            val finalReply = reply.copy(id = replyId)

            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
            commentRef.update("replies", FieldValue.arrayUnion(finalReply)).await()

            // Notify comment owner
            try {
                val commentSnapshot = commentRef.get().await()
                val commentOwnerId = commentSnapshot.getString("userId")
                if (commentOwnerId != null) {
                    notifyUser(commentOwnerId, postId, commentId, Notification.NotificationType.REPLY_COMMENT, finalReply.authorName, finalReply.authorAvatarUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(finalReply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReply(postId: String, commentId: String, reply: Comment): Result<Boolean> {
        return try {
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
            // We need the exact object to remove from array
            commentRef.update("replies", FieldValue.arrayRemove(reply)).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sharePost(postId: String, senderName: String? = null, senderAvatar: String? = null): Result<Boolean> {
        return try {
            val postRef = db.collection("posts").document(postId)
            postRef.update("shareCount", FieldValue.increment(1)).await()

            // Notify post owner
            try {
                val postSnapshot = postRef.get().await()
                val postOwnerId = postSnapshot.getString("userId")
                if (postOwnerId != null) {
                    android.util.Log.d("PostRepository", "Notifying user $postOwnerId about SHARE_POST")
                    notifyUser(
                        recipientId = postOwnerId,
                        postId = postId,
                        type = Notification.NotificationType.SHARE_POST,
                        customSenderName = senderName,
                        customSenderAvatar = senderAvatar
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sharePostToFeed(originalPost: Post, currentUserId: String, currentUserName: String, currentUserAvatar: String?): Result<Boolean> {
        return try {
             // Create a new post that references the original one
            val postRef = db.collection("posts").document()
            val sharedPost = Post(
                id = postRef.id,
                userId = currentUserId,
                authorName = currentUserName,
                authorAvatarUrl = currentUserAvatar,
                content = "", // Content can be empty or something like "Shared a post"
                timestamp = System.currentTimeMillis(),

                // Copy original content visuals for display
                imageUrl = originalPost.imageUrl,

                // Set reference fields
                originalPostId = originalPost.originalPostId ?: originalPost.id, // Chain references to the ROOT post
                originalUserId = originalPost.originalUserId ?: originalPost.userId,
                originalAuthorName = originalPost.originalAuthorName ?: originalPost.authorName,
                originalAuthorAvatarUrl = originalPost.originalAuthorAvatarUrl ?: originalPost.authorAvatarUrl,
                originalContent = originalPost.originalContent ?: originalPost.content,
                originalImageUrl = originalPost.originalImageUrl ?: originalPost.imageUrl
            )

            postRef.set(sharedPost.toMap()).await()

            // Increment share count on the ORIGINAL post
            val originalId = originalPost.originalPostId ?: originalPost.id
            sharePost(originalId, currentUserName, currentUserAvatar)

            // Notify original post owner - Handled by sharePost now
            // val originalOwnerId = originalPost.originalUserId ?: originalPost.userId
            // notifyUser(originalOwnerId, originalPost.id, null, Notification.NotificationType.SHARE_POST, currentUserName, currentUserAvatar)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hidePost(postId: String, userId: String): Result<Boolean> {
        return try {
            val postRef = db.collection("posts").document(postId)
            postRef.update("hiddenBy", FieldValue.arrayUnion(userId)).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Boolean> {
        return try {
            val postRef = db.collection("posts").document(postId)
            postRef.delete().await()
            // Should probably delete comments subcollection too but Firestore requires recursive delete which is client-side heavy or cloud function.
            // keeping it simple for now.
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserContent(userId: String): Result<Boolean> {
        return try {
            val posts = getPostsByUser(userId).getOrDefault(emptyList())
            for (post in posts) {
                deletePost(post.id)
            }
            // Also delete comments made by this user on other posts
            // This is complex as comments are in subcollections. 
            // For now, we mainly focus on posts.
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLikePost(postId: String, isLiked: Boolean, senderName: String? = null, senderAvatar: String? = null): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No user"))
            val postRef = db.collection("posts").document(postId)

            val batch = db.batch()
            if (isLiked) {
                batch.update(postRef, "likeCount", FieldValue.increment(1))
                batch.update(postRef, "likedBy", FieldValue.arrayUnion(userId))
            } else {
                batch.update(postRef, "likeCount", FieldValue.increment(-1))
                batch.update(postRef, "likedBy", FieldValue.arrayRemove(userId))
            }
            batch.commit().await()

            if (isLiked) {
                try {
                     val postSnapshot = postRef.get().await()
                     val postOwnerId = postSnapshot.getString("userId")
                     if (postOwnerId != null) {
                         notifyUser(postOwnerId, postId, null, Notification.NotificationType.LIKE_POST, senderName, senderAvatar)
                     }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Toggle like/dislike for comments/replies

    suspend fun toggleLikeComment(postId: String, commentId: String, userId: String, senderName: String? = null, senderAvatar: String? = null): Result<Boolean> {
         return try {
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
            val doc = commentRef.get().await()
            val likedBy = (doc.get("likedBy") as? List<String>) ?: emptyList()
            val dislikedBy = (doc.get("dislikedBy") as? List<String>) ?: emptyList()

            val updates = hashMapOf<String, Any>()

            if (likedBy.contains(userId)) {
                // Remove like
                updates["likedBy"] = FieldValue.arrayRemove(userId)
                updates["likesCount"] = FieldValue.increment(-1)
            } else {
                // Add like, remove dislike if exists
                updates["likedBy"] = FieldValue.arrayUnion(userId)
                updates["likesCount"] = FieldValue.increment(1)
                if (dislikedBy.contains(userId)) {
                     updates["dislikedBy"] = FieldValue.arrayRemove(userId)
                     updates["dislikesCount"] = FieldValue.increment(-1)
                }
            }
            commentRef.update(updates).await()

            if (!likedBy.contains(userId)) {
                 val commentOwnerId = doc.getString("userId")
                 if (commentOwnerId != null) {
                      android.util.Log.d("PostRepository", "Notifying user $commentOwnerId about LIKE_COMMENT")
                      notifyUser(commentOwnerId, postId, commentId, Notification.NotificationType.LIKE_COMMENT, senderName, senderAvatar)
                 }
            }

            Result.success(true)
         } catch (e: Exception) {
             Result.failure(e)
         }
    }

    suspend fun toggleDislikeComment(postId: String, commentId: String, userId: String, senderName: String? = null, senderAvatar: String? = null): Result<Boolean> {
         return try {
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
            val doc = commentRef.get().await()
            val likedBy = (doc.get("likedBy") as? List<String>) ?: emptyList()
            val dislikedBy = (doc.get("dislikedBy") as? List<String>) ?: emptyList()

            val updates = hashMapOf<String, Any>()

            if (dislikedBy.contains(userId)) {
                // Remove dislike
                updates["dislikedBy"] = FieldValue.arrayRemove(userId)
                updates["dislikesCount"] = FieldValue.increment(-1)
            } else {
                // Add dislike, remove like if exists
                updates["dislikedBy"] = FieldValue.arrayUnion(userId)
                updates["dislikesCount"] = FieldValue.increment(1)
                if (likedBy.contains(userId)) {
                     updates["likedBy"] = FieldValue.arrayRemove(userId)
                     updates["likesCount"] = FieldValue.increment(-1)
                }
            }
            commentRef.update(updates).await()

            if (!dislikedBy.contains(userId)) {
                 val commentOwnerId = doc.getString("userId")
                 if (commentOwnerId != null) {
                      android.util.Log.d("PostRepository", "Notifying user $commentOwnerId about DISLIKE_COMMENT")
                      notifyUser(commentOwnerId, postId, commentId, Notification.NotificationType.DISLIKE_COMMENT, senderName, senderAvatar)
                 }
            }

            Result.success(true)
         } catch (e: Exception) {
             Result.failure(e)
         }
    }

    suspend fun toggleLikeReply(postId: String, commentId: String, replyId: String, userId: String, senderName: String? = null, senderAvatar: String? = null): Result<Boolean> {
        // Toggling like on a reply (which is inside an array) is complex in Firestore.
        // We have to read the document, find the reply, modify it, and write it back.
        return try {
            val db = FirebaseFirestore.getInstance()
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)

            var targetUserId: String? = null
            var isLikeAction = false

            db.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)

                // Manually parse replies (since it's a list of maps/objects)
                // Using Comment.fromDocument logic but for single map
                val repliesData = snapshot.get("replies") as? List<Map<String, Any>> ?: emptyList()
                val updatedReplies = repliesData.map { replyMap ->
                    val rId = replyMap["id"] as? String
                    if (rId == replyId) {
                        val likedBy = (replyMap["likedBy"] as? List<String>)?.toMutableList() ?: mutableListOf()
                        var likesCount = (replyMap["likesCount"] as? Number)?.toInt() ?: 0
                        val dislikedBy = (replyMap["dislikedBy"] as? List<String>)?.toMutableList() ?: mutableListOf()
                        var dislikesCount = (replyMap["dislikesCount"] as? Number)?.toInt() ?: 0

                        targetUserId = replyMap["userId"] as? String

                        if (likedBy.contains(userId)) {
                            likedBy.remove(userId)
                            likesCount = (likesCount - 1).coerceAtLeast(0)
                            isLikeAction = false
                        } else {
                            likedBy.add(userId)
                            likesCount += 1
                            if (dislikedBy.contains(userId)) {
                                dislikedBy.remove(userId)
                                dislikesCount = (dislikesCount - 1).coerceAtLeast(0)
                            }
                            isLikeAction = true
                        }

                        replyMap.toMutableMap().apply {
                            put("likedBy", likedBy)
                            put("likesCount", likesCount)
                            put("dislikedBy", dislikedBy)
                            put("dislikesCount", dislikesCount)
                        }
                    } else {
                        replyMap
                    }
                }

                transaction.update(commentRef, "replies", updatedReplies)
            }.await()

            if (isLikeAction && targetUserId != null) {
                 notifyUser(targetUserId!!, postId, commentId, Notification.NotificationType.LIKE_COMMENT, senderName, senderAvatar) // Using LIKE_COMMENT for reply too, or needed differentiation?
            }

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun toggleDislikeReply(postId: String, commentId: String, replyId: String, userId: String, senderName: String? = null, senderAvatar: String? = null): Result<Boolean> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)

            var targetUserId: String? = null
            var isDislikeAction = false

            db.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                val repliesData = snapshot.get("replies") as? List<Map<String, Any>> ?: emptyList()

                val updatedReplies = repliesData.map { replyMap ->
                    val rId = replyMap["id"] as? String
                    if (rId == replyId) {
                        val likedBy = (replyMap["likedBy"] as? List<String>)?.toMutableList() ?: mutableListOf()
                        var likesCount = (replyMap["likesCount"] as? Number)?.toInt() ?: 0
                        val dislikedBy = (replyMap["dislikedBy"] as? List<String>)?.toMutableList() ?: mutableListOf()
                        var dislikesCount = (replyMap["dislikesCount"] as? Number)?.toInt() ?: 0

                        targetUserId = replyMap["userId"] as? String

                         if (dislikedBy.contains(userId)) {
                            dislikedBy.remove(userId)
                             dislikesCount = (dislikesCount - 1).coerceAtLeast(0)
                             isDislikeAction = false
                        } else {
                            dislikedBy.add(userId)
                             dislikesCount += 1
                            if (likedBy.contains(userId)) {
                                likedBy.remove(userId)
                                likesCount = (likesCount - 1).coerceAtLeast(0)
                            }
                            isDislikeAction = true
                        }

                        replyMap.toMutableMap().apply {
                            put("likedBy", likedBy)
                            put("likesCount", likesCount)
                            put("dislikedBy", dislikedBy)
                            put("dislikesCount", dislikesCount)
                        }
                    } else {
                        replyMap
                    }
                }

                transaction.update(commentRef, "replies", updatedReplies)
            }.await()

            if (isDislikeAction && targetUserId != null) {
                 notifyUser(targetUserId!!, postId, commentId, Notification.NotificationType.DISLIKE_COMMENT, senderName, senderAvatar)
            }

            Result.success(true)
        } catch (e: Exception) {
             e.printStackTrace()
             Result.failure(e)
        }
    }

    private suspend fun notifyUser(
        recipientId: String,
        postId: String,
        commentId: String? = null,
        type: Notification.NotificationType,
        customSenderName: String? = null,
        customSenderAvatar: String? = null
    ) {
        try {
            if (recipientId.isEmpty()) {
                android.util.Log.e("PostRepository", "Notification aborted: recipientId is empty")
                return
            }

            val currentUserId = auth.currentUser?.uid ?: return

            // Allow self-notification for testing if needed
            // if (recipientId == currentUserId && type != Notification.NotificationType.LIKE_POST) return

            // Check if recipient has enabled notifications
            val recipientDoc = db.collection(com.example.habittracker.data.model.User.COLLECTION_NAME).document(recipientId).get().await()
            val notificationsEnabled = recipientDoc.getBoolean("notificationsEnabled") ?: true
            if (!notificationsEnabled) {
                android.util.Log.d("PostRepository", "Notification aborted: Recipient has disabled notifications")
                return
            }

            val fcmToken = recipientDoc.getString("fcmToken")

            var senderName = customSenderName
            var senderAvatar = customSenderAvatar

            if (senderName == null) {
                try {
                    val userDoc = db.collection("users").document(currentUserId).get().await()
                    senderName = userDoc.getString("name")
                    // If name is found but empty, use "User"
                    if (senderName.isNullOrEmpty()) senderName = "User"

                    senderAvatar = userDoc.getString("avatarUrl")
                } catch (e: Exception) {
                    senderName = "User"
                    android.util.Log.e("PostRepository", "Failed to fetch sender name: ${e.message}")
                }
            }

            // Ensure senderName is not null for Notification object
            val finalSenderName = senderName ?: "User"

            val notification = Notification(
                recipientId = recipientId,
                senderId = currentUserId,
                senderName = finalSenderName,
                senderAvatarUrl = senderAvatar ?: "",
                postId = postId,
                commentId = commentId,
                type = type
            )
            val result = notificationRepository.sendNotification(notification)
            if (result.isSuccess) {
                android.util.Log.d("PostRepository", "Notification saved to Firestore for $recipientId")
            } else {
                android.util.Log.e("PostRepository", "Failed to save notification: ${result.exceptionOrNull()?.message}")
            }

            // Send FCM Push Notification
            if (!fcmToken.isNullOrEmpty()) {
                val title = "Habit Tracker"
                var body = "You have a new notification"

                body = when (type) {
                    Notification.NotificationType.COMMENT_POST -> "$finalSenderName commented on your post"
                    Notification.NotificationType.LIKE_POST -> "$finalSenderName liked your post"
                    Notification.NotificationType.SHARE_POST -> "$finalSenderName shared your post"
                    Notification.NotificationType.LIKE_COMMENT -> "$finalSenderName liked your comment"
                    Notification.NotificationType.DISLIKE_COMMENT -> "$finalSenderName disliked your comment"
                    Notification.NotificationType.REPLY_COMMENT -> "$finalSenderName replied to your comment"
                }

                android.util.Log.d("PostRepository", "Sending FCM to $fcmToken: $body")
                NotificationSender.sendNotification(fcmToken, title, body, mapOf("postId" to postId))
            } else {
                 android.util.Log.d("PostRepository", "FCM token is null or empty for recipient $recipientId")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("PostRepository", "Notification error: ${e.message}")
        }
    }
}
