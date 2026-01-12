package com.example.habittracker.data.supabase

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class SupabaseStorageRepository {

    private val storage = SupabaseManager.storage

    // Bucket names
    companion object {
        const val BUCKET_CHALLENGES = "challenges"
        const val BUCKET_USERS = "users"
        const val BUCKET_POSTS = "posts"
    }

    /**
     * Upload image to Supabase Storage
     * @param uri Image URI from device
     * @param context Android context
     * @param bucketName Bucket name (challenges, users, posts)
     * @param fileName Optional custom file name, if null will generate UUID
     * @return Public URL of uploaded image
     */
    suspend fun uploadImage(
        uri: Uri,
        context: Context,
        bucketName: String = BUCKET_CHALLENGES,
        fileName: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            // Generate file name if not provided
            val finalFileName = fileName ?: "${UUID.randomUUID()}.jpg"

            // Read file from URI
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open input stream")

            val bytes = inputStream.readBytes()
            inputStream.close()

            // Upload to Supabase Storage
            try {
                storage.from(bucketName).upload(finalFileName, bytes, upsert = false)
            } catch (e: Exception) {
                if (e.message?.contains("Object does not exist") == true) {
                    throw Exception("Bucket '$bucketName' does not exist in Supabase storage. Please create it.")
                }
                throw e
            }

            // Get public URL
            val publicUrl = storage.from(bucketName).publicUrl(finalFileName)

            publicUrl
        } catch (e: Exception) {
            e.printStackTrace()
            // Rethrow or return empty string depending on requirement.
            // Here rethrowing to let ViewModel handle it.
            throw e
        }
    }

    /**
     * Upload image from byte array
     * @param bytes Image bytes
     * @param bucketName Bucket name
     * @param fileName Optional custom file name
     * @return Public URL of uploaded image
     */
    suspend fun uploadImageBytes(
        bytes: ByteArray,
        bucketName: String = BUCKET_CHALLENGES,
        fileName: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val finalFileName = fileName ?: "${UUID.randomUUID()}.jpg"

            // Upload to Supabase Storage
            storage.from(bucketName).upload(finalFileName, bytes, upsert = false)

            // Get public URL
            val publicUrl = storage.from(bucketName).publicUrl(finalFileName)

            publicUrl
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Delete image from Supabase Storage
     * @param imageUrl Full public URL of the image
     * @param bucketName Bucket name
     */
    suspend fun deleteImage(imageUrl: String, bucketName: String = BUCKET_CHALLENGES): Boolean {
        return try {
            // Extract file name from URL
            val fileName = imageUrl.substringAfterLast("/")

            storage.from(bucketName).delete(fileName)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get public URL for an image
     * @param fileName File name in storage
     * @param bucketName Bucket name
     * @return Public URL
     */
    fun getPublicUrl(fileName: String, bucketName: String = BUCKET_CHALLENGES): String {
        return storage.from(bucketName).publicUrl(fileName)
    }

    /**
     * Update image (delete old and upload new)
     * @param oldImageUrl Old image URL to delete
     * @param newImageUri New image URI to upload
     * @param context Android context
     * @param bucketName Bucket name
     * @return New image public URL
     */
    suspend fun updateImage(
        oldImageUrl: String?,
        newImageUri: Uri,
        context: Context,
        bucketName: String = BUCKET_CHALLENGES
    ): String {
        // Delete old image if exists
        if (!oldImageUrl.isNullOrEmpty()) {
            deleteImage(oldImageUrl, bucketName)
        }

        // Upload new image
        return uploadImage(newImageUri, context, bucketName)
    }

    /**
     * Create bucket if it doesn't exist
     * @param bucketName Name of the bucket to create
     * @return true if bucket exists or was created successfully
     */
    suspend fun createBucketIfNotExists(bucketName: String): Boolean {
        // Buckets should be created manually in Supabase dashboard
        return true
    }

    /**
     * Initialize all required buckets
     */
    suspend fun initializeBuckets(): Boolean {
        // Buckets should be created manually in Supabase dashboard
        return true
    }

    /**
     * Test Supabase connection and bucket access
     */
    suspend fun testConnection(): String {
        return try {
            // Test basic storage access
            val testBucket = "challenges"
            val testData = "test".toByteArray()
            val testFileName = "test_${System.currentTimeMillis()}.txt"

            storage.from(testBucket).upload(testFileName, testData, upsert = true)

            // Test public URL
            val publicUrl = storage.from(testBucket).publicUrl(testFileName)

            "Supabase connection test successful"
        } catch (e: Exception) {
            "Supabase connection test failed: ${e.message}"
        }
    }
}
