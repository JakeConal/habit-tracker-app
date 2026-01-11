package com.example.habittracker.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.engine.android.*

object SupabaseManager {

    private const val SUPABASE_URL = "https://fefukrrrnmmvucfqjfne.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZlZnVrcnJybm1tdnVjZnFqZm5lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjgwNDkzNDQsImV4cCI6MjA4MzYyNTM0NH0.6uY7W44o4S5Cp1z1UEWXUvp5fXHP0WGe_hTJjrQII3M"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Storage)
        }
    }

    val storage: Storage
        get() = client.storage
}

