package com.example.habittracker.data.repository

import android.content.Context
import android.net.Uri
import com.example.habittracker.data.model.Comment
import com.example.habittracker.data.model.Post
import com.example.habittracker.data.supabase.SupabaseStorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Suppress("UNCHECKED_CAST")
class PostRepository private constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val posts = snapshot.documents.mapNotNull { Post.fromDocument(it) }
            Result.success(posts)
        } catch (e: Exception) {
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

    suspend fun sharePost(postId: String): Result<Boolean> {
        return try {
            val postRef = db.collection("posts").document(postId)
            postRef.update("shareCount", FieldValue.increment(1)).await()
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
                originalAuthorAvatarUrl = originalPost.originalAuthorAvatarUrl ?: originalPost.authorAvatarUrl
            )

            postRef.set(sharedPost.toMap()).await()

            // Increment share count on the ORIGINAL post
            val originalId = originalPost.originalPostId ?: originalPost.id
            sharePost(originalId)

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

    suspend fun toggleLikePost(postId: String, isLiked: Boolean): Result<Boolean> {
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
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Toggle like/dislike for comments/replies

    suspend fun toggleLikeComment(postId: String, commentId: String, userId: String): Result<Boolean> {
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
            Result.success(true)
         } catch (e: Exception) {
             Result.failure(e)
         }
    }

    suspend fun toggleDislikeComment(postId: String, commentId: String, userId: String): Result<Boolean> {
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
            Result.success(true)
         } catch (e: Exception) {
             Result.failure(e)
         }
    }

    suspend fun toggleLikeReply(postId: String, commentId: String, replyId: String, userId: String): Result<Boolean> {
        // Toggling like on a reply (which is inside an array) is complex in Firestore.
        // We have to read the document, find the reply, modify it, and write it back.
        return try {
            val db = FirebaseFirestore.getInstance()
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)

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

                        if (likedBy.contains(userId)) {
                            likedBy.remove(userId)
                            likesCount = (likesCount - 1).coerceAtLeast(0)
                        } else {
                            likedBy.add(userId)
                            likesCount += 1
                            if (dislikedBy.contains(userId)) {
                                dislikedBy.remove(userId)
                                dislikesCount = (dislikesCount - 1).coerceAtLeast(0)
                            }
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

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun toggleDislikeReply(postId: String, commentId: String, replyId: String, userId: String): Result<Boolean> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)

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

                         if (dislikedBy.contains(userId)) {
                            dislikedBy.remove(userId)
                             dislikesCount = (dislikesCount - 1).coerceAtLeast(0)
                        } else {
                            dislikedBy.add(userId)
                             dislikesCount += 1
                            if (likedBy.contains(userId)) {
                                likedBy.remove(userId)
                                likesCount = (likesCount - 1).coerceAtLeast(0)
                            }
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

            Result.success(true)
        } catch (e: Exception) {
             e.printStackTrace()
            Result.failure(e)
        }
    }
}
