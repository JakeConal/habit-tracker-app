package com.example.habittracker.data.api

import com.example.habittracker.data.model.QuoteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * API Service for Daily Quotes
 */
interface QuoteApiService {

    @GET("api/quotes/motivational")
    suspend fun getMotivationalQuote(
        @Header("Authorization") auth: String = "Bearer 2uhDrOoTETMn3NRBf0Yx2LDZamOgRQqKMJ3Zrv2i",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<QuoteResponse>
}
