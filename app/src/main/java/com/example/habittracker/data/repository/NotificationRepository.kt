package com.example.habittracker.data.repository

import com.example.habittracker.data.model.Notification
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()

    companion object {
        @Volatile
        private var instance: NotificationRepository? = null

        fun getInstance(): NotificationRepository {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepository().also { instance = it }
            }
        }
    }

    suspend fun sendNotification(notification: Notification): Result<Boolean> {
        return try {
            val docRef = db.collection("notifications").document()
            val finalNotification = notification.copy(id = docRef.id)
            docRef.set(finalNotification).await()
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val query = db.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val notifications = snapshot.toObjects(Notification::class.java)
                trySend(notifications)
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    fun getNewNotifications(userId: String): Flow<Notification> = callbackFlow {
        // Removed orderBy and limit to avoid requiring a composite index during development
        val query = db.collection("notifications")
            .whereEqualTo("recipientId", userId)

        var isFirst = true

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                if (isFirst) {
                    isFirst = false
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val notification = change.document.toObject(Notification::class.java)
                        trySend(notification)
                    }
                }
            }
        }

        awaitClose { listenerRegistration.remove() }
    }
}
