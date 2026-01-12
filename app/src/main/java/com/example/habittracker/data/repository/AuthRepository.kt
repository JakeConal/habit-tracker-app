package com.example.habittracker.data.repository

import com.example.habittracker.data.model.Category
import com.example.habittracker.data.model.CategoryColor
import com.example.habittracker.data.model.CategoryIcon
import com.example.habittracker.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling Firebase Authentication operations
 * Supports Email/Password, Google Sign-In, and Anonymous authentication
 */
class AuthRepository private constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = FirestoreUserRepository.getInstance()

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository().also { instance = it }
            }
        }
    }

    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: email.substringBefore("@"),
                    avatarUrl = firebaseUser.photoUrl?.toString(),
                    email = firebaseUser.email,
                    lastLoginAt = System.currentTimeMillis()
                )
                // Update user info in Firestore
                userRepository.createOrUpdateUser(user)
                seedDefaultCategoriesForUser(user.id)
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register with email and password
     */
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Update display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                val user = User(
                    id = firebaseUser.uid,
                    name = name,
                    avatarUrl = firebaseUser.photoUrl?.toString(),
                    email = firebaseUser.email,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                // Create user profile in Firestore
                userRepository.createOrUpdateUser(user)
                seedDefaultCategoriesForUser(user.id)
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with Google using ID token
     */
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Google User",
                    avatarUrl = firebaseUser.photoUrl?.toString(),
                    email = firebaseUser.email,
                    lastLoginAt = System.currentTimeMillis()
                )
                // Create or update user profile in Firestore
                userRepository.createOrUpdateUser(user)
                seedDefaultCategoriesForUser(user.id)
                Result.success(user)
            } else {
                Result.failure(Exception("Google sign in failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in anonymously
     */
    suspend fun signInAnonymously(): Result<User> {
        return try {
            val result = auth.signInAnonymously().await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    name = "Guest User",
                    avatarUrl = null,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                // Create user profile in Firestore
                userRepository.createOrUpdateUser(user)
                seedDefaultCategoriesForUser(user.id)
                Result.success(user)
            } else {
                Result.failure(Exception("Anonymous sign in failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }

    private suspend fun seedDefaultCategoriesForUser(userId: String) {
        try {
            val categoryRepository = CategoryRepository.getInstance()
            val existingCategories = categoryRepository.getCategoriesForUser(userId)

            if (existingCategories.isEmpty()) {
                val defaultCategories = listOf(
                    Category(userId = userId, title = "Physical Health", icon = CategoryIcon.HEART, color = CategoryColor.RED),
                    Category(userId = userId, title = "Study", icon = CategoryIcon.BOOK, color = CategoryColor.BLUE),
                    Category(userId = userId, title = "Finance", icon = CategoryIcon.MONEY, color = CategoryColor.YELLOW),
                    Category(userId = userId, title = "Mental Health", icon = CategoryIcon.HEART, color = CategoryColor.PINK_LIGHT),
                    Category(userId = userId, title = "Career", icon = CategoryIcon.BRIEFCASE, color = CategoryColor.PURPLE),
                    Category(userId = userId, title = "Nutrition", icon = CategoryIcon.FOOD, color = CategoryColor.ORANGE_LIGHT),
                    Category(userId = userId, title = "Personal Growth", icon = CategoryIcon.GROWTH, color = CategoryColor.GREEN),
                    Category(userId = userId, title = "Sleep", icon = CategoryIcon.MOON, color = CategoryColor.INDIGO)
                )

                defaultCategories.forEach { category ->
                    try {
                        categoryRepository.addCategory(category)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
