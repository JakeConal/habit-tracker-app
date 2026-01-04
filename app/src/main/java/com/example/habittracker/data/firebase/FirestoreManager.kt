package com.example.habittracker.data.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreManager {

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    // Generic: Get all documents from a collection
    suspend fun <T> getCollection(
        collectionName: String,
        mapper: (DocumentSnapshot) -> T?
    ): List<T> {
        return try {
            db.collection(collectionName)
                .get()
                .await()
                .documents
                .mapNotNull { mapper(it) }
        } catch (e: Exception) {
            println("Error getting collection '$collectionName': ${e.message}")
            emptyList()
        }
    }

    // Generic: Get documents from a collection with where clause
    suspend fun <T> getCollectionWhere(
        collectionName: String,
        field: String,
        value: Any,
        mapper: (DocumentSnapshot) -> T?
    ): List<T> {
        return try {
            db.collection(collectionName)
                .whereEqualTo(field, value)
                .get()
                .await()
                .documents
                .mapNotNull { mapper(it) }
        } catch (e: Exception) {
            println("Error getting collection '$collectionName' where '$field' = '$value': ${e.message}")
            emptyList()
        }
    }

    // Generic: Get single document
    suspend fun getDocument(
        collectionName: String,
        docId: String
    ): DocumentSnapshot? {
        return try {
            db.collection(collectionName)
                .document(docId)
                .get()
                .await()
        } catch (e: Exception) {
            println("Error getting document '$docId': ${e.message}")
            null
        }
    }

    // Generic: Add document with specific ID
    suspend fun addDocumentWithId(
        collectionName: String,
        docId: String,
        data: Map<String, Any>
    ): String? {
        return try {
            db.collection(collectionName)
                .document(docId)
                .set(data)
                .await()
            docId
        } catch (e: Exception) {
            println("Error adding document with ID: ${e.message}")
            null
        }
    }

    // Generic: Add document
    suspend fun addDocument(
        collectionName: String,
        data: Map<String, Any>
    ): String? {
        return try {
            db.collection(collectionName)
                .add(data)
                .await()
                .id
        } catch (e: Exception) {
            println("Error adding document: ${e.message}")
            null
        }
    }

    // Generic: Update document
    suspend fun updateDocument(
        collectionName: String,
        docId: String,
        updates: Map<String, Any>
    ): Boolean {
        return try {
            db.collection(collectionName)
                .document(docId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            println("Error updating document: ${e.message}")
            false
        }
    }

    // Generic: Delete document
    suspend fun deleteDocument(
        collectionName: String,
        docId: String
    ): Boolean {
        return try {
            db.collection(collectionName)
                .document(docId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            println("Error deleting document: ${e.message}")
            false
        }
    }
}