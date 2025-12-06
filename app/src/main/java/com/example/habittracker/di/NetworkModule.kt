package com.example.habittracker.di

/**
 * Hilt Module for providing Network (Retrofit) dependencies.
 * 
 * Example usage:
 * 
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     
 *     private const val BASE_URL = "https://api.example.com/"
 *     
 *     @Provides
 *     @Singleton
 *     fun provideOkHttpClient(): OkHttpClient {
 *         return OkHttpClient.Builder()
 *             .addInterceptor(HttpLoggingInterceptor().apply {
 *                 level = HttpLoggingInterceptor.Level.BODY
 *             })
 *             .connectTimeout(30, TimeUnit.SECONDS)
 *             .readTimeout(30, TimeUnit.SECONDS)
 *             .writeTimeout(30, TimeUnit.SECONDS)
 *             .build()
 *     }
 *     
 *     @Provides
 *     @Singleton
 *     fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
 *         return Retrofit.Builder()
 *             .baseUrl(BASE_URL)
 *             .client(okHttpClient)
 *             .addConverterFactory(GsonConverterFactory.create())
 *             .build()
 *     }
 *     
 *     @Provides
 *     @Singleton
 *     fun provideApiService(retrofit: Retrofit): ApiService {
 *         return retrofit.create(ApiService::class.java)
 *     }
 * }
 */

// TODO: Uncomment and implement when Retrofit and Hilt are added to the project
// @Module
// @InstallIn(SingletonComponent::class)
// object NetworkModule {
//     
// }

