package com.example.habittracker.data.remote.fcm

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmApi {
    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: FcmRequest
    ): Response<FcmResponse>
}

