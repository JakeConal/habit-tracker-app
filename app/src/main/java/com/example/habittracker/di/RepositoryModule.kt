package com.example.habittracker.di

/**
 * Hilt Module for providing Repository dependencies.
 *
 * Example usage:
 *
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object RepositoryModule {
 *
 *     @Provides
 *     @Singleton
 *     fun provideHabitRepository(
 *         habitDao: HabitDao,
 *         apiService: ApiService
 *     ): HabitRepository {
 *         return HabitRepository(habitDao, apiService)
 *     }
 *
 *     @Provides
 *     @Singleton
 *     fun provideCategoryRepository(
 *         categoryDao: CategoryDao
 *     ): CategoryRepository {
 *         return CategoryRepository(categoryDao)
 *     }
 *
 *     @Provides
 *     @Singleton
 *     fun provideUserRepository(
 *         authService: AuthService,
 *         userPreferences: UserPreferences
 *     ): UserRepository {
 *         return UserRepository(authService, userPreferences)
 *     }
 * }
 */

// TODO: Uncomment and implement when Hilt is added to the project
// @Module
// @InstallIn(SingletonComponent::class)
// object RepositoryModule {
//     
// }

