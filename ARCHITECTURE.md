# Kiến trúc MVVM - Habit Tracker App

## Tổng quan

Ứng dụng này được xây dựng theo kiến trúc **MVVM (Model-View-ViewModel)** với **Feature-based Structure**, một pattern được Google khuyến nghị cho phát triển ứng dụng Android hiện đại.

## Cấu trúc thư mục

```
app/src/main/java/com/example/habittracker/
├── data/                           # Lớp dữ liệu
│   ├── model/                      # Các data class (Model)
│   │   ├── Habit.kt               # Model đại diện cho một habit
│   │   └── Challenge.kt           # Model đại diện cho một challenge
│   ├── repository/                 # Repository pattern
│   │   └── HabitRepository.kt     # Quản lý data source
│   ├── local/                      # Room Database (DAOs, Entities)
│   ├── remote/                     # Retrofit API Services
│   └── preferences/                # DataStore/SharedPreferences
│
├── ui/                             # Lớp giao diện người dùng (Feature-based)
│   ├── main/                       # Main container
│   │   └── MainActivity.kt
│   ├── auth/                       # Authentication features
│   │   ├── login/
│   │   ├── register/
│   │   └── guest/
│   ├── dashboard/                  # Dashboard feature
│   ├── category/                   # Category management
│   ├── habit/                      # Habit management
│   │   ├── list/                  # Habit list
│   │   │   ├── HabitListViewModel.kt
│   │   │   └── HabitAdapter.kt
│   │   ├── detail/                # Habit detail
│   │   ├── add/                   # Add habit
│   │   └── edit/                  # Edit habit
│   ├── pomodoro/                   # Pomodoro timer
│   ├── progress/                   # Progress tracking
│   │   ├── calendar/
│   │   └── chart/
│   ├── streak/                     # Streak management
│   ├── reminder/                   # Reminders
│   ├── quote/                      # Motivational quotes
│   ├── social/                     # Social features
│   │   ├── CommunityActivity.kt
│   │   ├── CommunityPagerAdapter.kt
│   │   ├── feed/                  # Community posts
│   │   ├── friend/                # Friends management
│   │   ├── interaction/           # Post interactions
│   │   └── profile/               # User profile
│   ├── challenge/                  # Challenges
│   │   ├── list/
│   │   │   ├── ChallengesFragment.kt
│   │   │   └── ChallengeAdapter.kt
│   │   ├── detail/
│   │   ├── create/
│   │   └── join/
│   ├── leaderboard/                # Leaderboards
│   │   └── LeaderboardFragment.kt
│   ├── settings/                   # App settings
│   ├── report/                     # Report generation
│   └── common/                     # Shared UI components
│       ├── BaseActivity.kt
│       ├── BaseFragment.kt
│       └── BaseViewModel.kt
│
├── di/                             # Dependency Injection (Hilt)
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
│
├── service/                        # Background Services
│
├── worker/                         # WorkManager Workers
│
├── util/                           # Các utility classes
│   └── Constants.kt               # Hằng số dùng chung
│
└── HabitTrackerApplication.kt      # Application class
```

## Giải thích các thành phần

### 1. **Model** (`data/model/`)
- Chứa các data class đại diện cho dữ liệu của ứng dụng
- Ví dụ: `Habit.kt`, `Challenge.kt`
- **Trách nhiệm**: Định nghĩa cấu trúc dữ liệu

### 2. **Repository** (`data/repository/`)
- Là single source of truth cho dữ liệu
- Trừu tượng hóa data source (có thể từ database, API, cache...)
- **Trách nhiệm**: Cung cấp dữ liệu cho ViewModel, xử lý logic lấy/lưu dữ liệu

### 3. **ViewModel** (`ui/[feature]/`)
- Nằm giữa View và Model
- Giữ và xử lý dữ liệu cho UI
- Sống sót qua configuration changes (xoay màn hình)
- Sử dụng `StateFlow` hoặc `LiveData` để UI observe
- **Trách nhiệm**: Chuẩn bị dữ liệu cho View, xử lý business logic

### 4. **View** (`ui/[feature]/`)
- Activity và Fragment được tổ chức theo feature
- Mỗi feature có folder riêng chứa tất cả files liên quan
- Hiển thị dữ liệu và nhận input từ người dùng
- Observe ViewModel để cập nhật UI
- **Trách nhiệm**: Hiển thị UI và xử lý user interaction

### 5. **Common** (`ui/common/`)
- Base classes cho Activity, Fragment, ViewModel
- Shared UI components
- **Trách nhiệm**: Code dùng chung cho tất cả features

### 6. **DI** (`di/`)
- Hilt modules cho dependency injection
- **Trách nhiệm**: Quản lý và cung cấp dependencies

### 7. **Utils** (`util/`)
- Các class tiện ích, helper functions, constants
- **Trách nhiệm**: Code dùng chung trong toàn bộ app

## Luồng dữ liệu trong MVVM

```
User Action → View (Activity/Fragment)
                ↓
            ViewModel (xử lý logic)
                ↓
            Repository (lấy/lưu dữ liệu)
                ↓
            Data Source (Database/API)
                ↓
            Repository
                ↓
            ViewModel (update state)
                ↓
            View (observe & update UI)
```

## Ưu điểm của MVVM + Feature-based Structure

1. **Separation of Concerns**: Tách biệt rõ ràng giữa UI và business logic
2. **Testability**: Dễ dàng test ViewModel độc lập với UI
3. **Maintainability**: Code dễ bảo trì và mở rộng
4. **Lifecycle Aware**: ViewModel tự động xử lý lifecycle
5. **Scalability**: Dễ thêm features mới
6. **Team Collaboration**: Team có thể làm song song nhiều features

## Dependencies chính

Thêm vào `app/build.gradle.kts`:

```kotlin
dependencies {
    // ViewModel và LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Coroutines cho async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
```

## Tài liệu tham khảo

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

## Xem thêm

- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Chi tiết cấu trúc đầy đủ của project
- [.cursorrules](.cursorrules) - Quy tắc và conventions cho Cursor AI
