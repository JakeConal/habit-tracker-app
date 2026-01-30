package com.example.habittracker.data.service

import com.example.habittracker.data.model.QuoteRequest
import com.example.habittracker.data.model.QuoteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API Service for Daily Quotes
 */
interface QuoteApiService {

    @GET("api/quotes/motivational")
    suspend fun getMotivationalQuote(
        @Header("Authorization") auth: String = "Bearer 2uhDrOoTETMn3NRBf0Yx2LDZamOgRQqKMJ3Zrv2i",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<QuoteResponse>

    /**
     * Generate AI-powered motivational quote based on user's habit statistics
     */
    @POST("api/quote")
    suspend fun generateAiQuote(
        @Body request: QuoteRequest
    ): Response<QuoteResponse>
}