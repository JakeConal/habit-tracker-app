package com.example.habittracker.di

/**
 * Hilt Module for providing application-level dependencies.
 * 
 * To enable Hilt, add these dependencies to build.gradle.kts:
 * 
 * plugins {
 *     id("com.google.dagger.hilt.android")
 *     id("kotlin-kapt")
 * }
 * 
 * dependencies {
 *     implementation("com.google.dagger:hilt-android:2.50")
 *     kapt("com.google.dagger:hilt-compiler:2.50")
 * }
 * 
 * Example usage:
 * 
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object AppModule {
 *     
 *     @Provides
 *     @Singleton
 *     fun provideContext(@ApplicationContext context: Context): Context = context
 *     
 *     @Provides
 *     @Singleton
 *     fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
 *         return context.getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
 *     }
 * }
 */

// TODO: Uncomment and implement when Hilt is added to the project
// @Module
// @InstallIn(SingletonComponent::class)
// object AppModule {
//     
// }

