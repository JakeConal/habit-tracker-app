package com.example.habittracker.data.remote.fcm

import com.google.gson.annotations.SerializedName

data class FcmRequest(
    @SerializedName("to") val to: String,
    @SerializedName("notification") val notification: NotificationPayload,
    @SerializedName("data") val data: Map<String, String>? = null
)

data class NotificationPayload(
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)

data class FcmResponse(
    @SerializedName("success") val success: Int,
    @SerializedName("failure") val failure: Int,
    @SerializedName("results") val results: List<Any>? = null
)


