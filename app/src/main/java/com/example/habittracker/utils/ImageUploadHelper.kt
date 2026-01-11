package com.example.habittracker.utils

import android.content.Context
import android.net.Uri
import com.example.habittracker.data.supabase.SupabaseStorageRepository

/**
 * Utility class for image upload/download operations
 */
object ImageUploadHelper {

    private val storageRepository = SupabaseStorageRepository()

    /**
     * Upload challenge image
     */
    suspend fun uploadChallengeImage(uri: Uri, context: Context): String {
        return storageRepository.uploadImage(
            uri = uri,
            context = context,
            bucketName = SupabaseStorageRepository.BUCKET_CHALLENGES
        )
    }

    /**
     * Upload user avatar
     */
    suspend fun uploadUserAvatar(uri: Uri, context: Context): String {
        return storageRepository.uploadImage(
            uri = uri,
            context = context,
            bucketName = SupabaseStorageRepository.BUCKET_USERS
        )
    }

    /**
     * Upload post image
     */
    suspend fun uploadPostImage(uri: Uri, context: Context): String {
        return storageRepository.uploadImage(
            uri = uri,
            context = context,
            bucketName = SupabaseStorageRepository.BUCKET_POSTS
        )
    }

    /**
     * Delete image by URL
     */
    suspend fun deleteImage(imageUrl: String, bucketName: String): Boolean {
        return storageRepository.deleteImage(imageUrl, bucketName)
    }

    /**
     * Update challenge image
     */
    suspend fun updateChallengeImage(
        oldImageUrl: String?,
        newImageUri: Uri,
        context: Context
    ): String {
        return storageRepository.updateImage(
            oldImageUrl = oldImageUrl,
            newImageUri = newImageUri,
            context = context,
            bucketName = SupabaseStorageRepository.BUCKET_CHALLENGES
        )
    }

    /**
     * Update user avatar
     */
    suspend fun updateUserAvatar(
        oldImageUrl: String?,
        newImageUri: Uri,
        context: Context
    ): String {
        return storageRepository.updateImage(
            oldImageUrl = oldImageUrl,
            newImageUri = newImageUri,
            context = context,
            bucketName = SupabaseStorageRepository.BUCKET_USERS
        )
    }
}

