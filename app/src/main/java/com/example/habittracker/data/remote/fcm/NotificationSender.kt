package com.example.habittracker.data.remote.fcm

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NotificationSender {
    private const val BASE_URL = "https://fcm.googleapis.com/"
    // TODO: Replace with your actual Legacy Server Key from Firebase Console -> Project Settings -> Cloud Messaging.

    // If you cannot enable "Cloud Messaging API (Legacy)" in Google Cloud Console due to loading errors, try accessing it directly via Firebase Console.
    private const val SERVER_KEY = "key=AIzaSyAbIZcHgndBtGS3HDhdqyfiEQj_msh0YiM"

    private val api: FcmApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmApi::class.java)
    }

    suspend fun sendNotification(toToken: String, title: String, body: String, data: Map<String, String>? = null) {
        try {
            val request = FcmRequest(
                to = toToken,
                notification = NotificationPayload(title, body),
                data = data
            )
            val response = api.sendNotification(SERVER_KEY, request = request)
            if (response.isSuccessful) {
                Log.d("NotificationSender", "Success: ${response.body()}")
            } else {
                Log.e("NotificationSender", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("NotificationSender", "Exception: ${e.message}")
        }
    }
}

