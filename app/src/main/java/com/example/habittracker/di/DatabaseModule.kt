package com.example.habittracker.di

/**
 * Hilt Module for providing Room Database dependencies.
 *
 * Example usage:
 *
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object DatabaseModule {
 *
 *     @Provides
 *     @Singleton
 *     fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
 *         return Room.databaseBuilder(
 *             context,
 *             AppDatabase::class.java,
 *             "habit_tracker_db"
 *         ).build()
 *     }
 *
 *     @Provides
 *     fun provideHabitDao(database: AppDatabase): HabitDao {
 *         return database.habitDao()
 *     }
 *
 *     @Provides
 *     fun provideCategoryDao(database: AppDatabase): CategoryDao {
 *         return database.categoryDao()
 *     }
 * }
 */

// TODO: Uncomment and implement when Room and Hilt are added to the project
// @Module
// @InstallIn(SingletonComponent::class)
// object DatabaseModule {
//     
// }

