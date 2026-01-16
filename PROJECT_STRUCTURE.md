# 📂 Habit Tracker App - Cấu trúc Project MVVM

## 🏗️ Kiến trúc tổng quan

Dự án sử dụng **MVVM (Model-View-ViewModel)** với **Single-Activity Architecture** và **Feature-based Structure**.

### Single-Activity Architecture
- **MainActivity** là container duy nhất
- Tất cả màn hình được implement bằng **Fragments**
- **Navigation Component** quản lý điều hướng
- **BottomNavigationView** kết nối với NavController

## 📁 Cấu trúc thư mục

```
app/src/main/
├── java/com/example/habittracker/
│   ├── data/
│   │   ├── model/
│   │   │   ├── Habit.kt
│   │   │   ├── HabitCategory.kt
│   │   │   ├── HabitLog.kt
│   │   │   ├── Streak.kt
│   │   │   ├── Reminder.kt
│   │   │   ├── Quote.kt
│   │   │   ├── User.kt
│   │   │   ├── Friend.kt
│   │   │   ├── Post.kt
│   │   │   ├── Comment.kt
│   │   │   ├── Challenge.kt
│   │   │   ├── Leaderboard.kt
│   │   │   └── Report.kt
│   │   │
│   │   ├── repository/
│   │   │   ├── HabitRepository.kt
│   │   │   ├── CategoryRepository.kt
│   │   │   ├── StreakRepository.kt
│   │   │   ├── ReminderRepository.kt
│   │   │   ├── QuoteRepository.kt
│   │   │   ├── UserRepository.kt
│   │   │   ├── SocialRepository.kt
│   │   │   ├── ChallengeRepository.kt
│   │   │   ├── LeaderboardRepository.kt
│   │   │   └── ReportRepository.kt
│   │   │
│   │   ├── local/                      # Room Database
│   │   │   ├── AppDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── HabitDao.kt
│   │   │   │   ├── CategoryDao.kt
│   │   │   │   ├── HabitLogDao.kt
│   │   │   │   ├── StreakDao.kt
│   │   │   │   ├── ReminderDao.kt
│   │   │   │   └── QuoteDao.kt
│   │   │   └── entity/
│   │   │       ├── HabitEntity.kt
│   │   │       ├── CategoryEntity.kt
│   │   │       └── ...
│   │   │
│   │   ├── remote/                     # API Service
│   │   │   ├── ApiService.kt
│   │   │   ├── AuthService.kt
│   │   │   ├── SocialService.kt
│   │   │   ├── ChallengeService.kt
│   │   │   └── LeaderboardService.kt
│   │   │
│   │   └── preferences/                # SharedPreferences/DataStore
│   │       └── UserPreferences.kt
│   │
│   ├── ui/
│   │   ├── auth/                       # Feature: User Account
│   │   │   ├── login/
│   │   │   │   ├── LoginFragment.kt
│   │   │   │   └── LoginViewModel.kt
│   │   │   ├── register/
│   │   │   │   ├── RegisterFragment.kt
│   │   │   │   └── RegisterViewModel.kt
│   │   │   └── guest/
│   │   │       ├── GuestModeFragment.kt
│   │   │       └── GuestViewModel.kt
│   │   │
│   │   ├── main/                       # Main Container (Single Activity)
│   │   │   └── MainActivity.kt
│   │   │
│   │   ├── dashboard/                  # Dashboard + Quote + Streak
│   │   │   ├── HomeFragment.kt
│   │   │   ├── DashboardViewModel.kt
│   │   │   └── DashboardAdapter.kt
│   │   │
│   │   ├── category/                   # Feature: Manage Categories
│   │   │   ├── CategoryListFragment.kt
│   │   │   ├── CategoryViewModel.kt
│   │   │   ├── CategoryAdapter.kt
│   │   │   ├── AddCategoryDialog.kt
│   │   │   └── EditCategoryDialog.kt
│   │   │
│   │   ├── habit/                      # Feature: Manage Habits
│   │   │   ├── list/
│   │   │   │   ├── HabitListFragment.kt
│   │   │   │   ├── HabitListViewModel.kt
│   │   │   │   └── HabitAdapter.kt
│   │   │   ├── detail/
│   │   │   │   ├── HabitDetailFragment.kt
│   │   │   │   └── HabitDetailViewModel.kt
│   │   │   ├── add/
│   │   │   │   ├── AddHabitFragment.kt
│   │   │   │   └── AddHabitViewModel.kt
│   │   │   └── edit/
│   │   │       ├── EditHabitFragment.kt
│   │   │       └── EditHabitViewModel.kt
│   │   │
│   │   ├── pomodoro/                   # Feature: Pomodoro Timer
│   │   │   ├── PomodoroFragment.kt
│   │   │   ├── PomodoroViewModel.kt
│   │   │   └── TimerService.kt
│   │   │
│   │   ├── progress/                   # Feature: Track Progress
│   │   │   ├── StatisticFragment.kt
│   │   │   ├── ProgressViewModel.kt
│   │   │   ├── calendar/
│   │   │   │   ├── CalendarView.kt
│   │   │   │   └── CalendarAdapter.kt
│   │   │   └── chart/
│   │   │       ├── ChartView.kt
│   │   │       └── ChartAdapter.kt
│   │   │
│   │   ├── streak/                     # Feature: Manage Streaks
│   │   │   ├── StreakFragment.kt
│   │   │   └── StreakViewModel.kt
│   │   │
│   │   ├── reminder/                   # Feature: Reminders
│   │   │   ├── ReminderFragment.kt
│   │   │   ├── ReminderViewModel.kt
│   │   │   ├── ReminderAdapter.kt
│   │   │   ├── AddReminderDialog.kt
│   │   │   └── NotificationReceiver.kt
│   │   │
│   │   ├── quote/                      # Feature: Motivational Quotes
│   │   │   ├── QuoteFragment.kt
│   │   │   ├── QuoteViewModel.kt
│   │   │   └── QuoteAdapter.kt
│   │   │
│   │   ├── social/                     # Social Features (Community)
│   │   │   ├── CommunityFragment.kt        # Main community container với tabs
│   │   │   ├── CommunityPagerAdapter.kt    # ViewPager adapter cho nested fragments
│   │   │   ├── friend/                     # Feature: Friends
│   │   │   │   ├── FriendListFragment.kt
│   │   │   │   ├── FriendViewModel.kt
│   │   │   │   ├── FriendAdapter.kt
│   │   │   │   ├── FriendRequestFragment.kt
│   │   │   │   └── SearchFriendFragment.kt
│   │   │   │
│   │   │   ├── feed/                       # Feature: Community Posts
│   │   │   │   ├── FeedFragment.kt
│   │   │   │   ├── FeedViewModel.kt
│   │   │   │   ├── PostAdapter.kt
│   │   │   │   ├── CreatePostFragment.kt
│   │   │   │   └── EditPostFragment.kt
│   │   │   │
│   │   │   ├── interaction/                # Feature: Post Interactions
│   │   │   │   ├── CommentBottomSheet.kt
│   │   │   │   ├── CommentAdapter.kt
│   │   │   │   └── InteractionViewModel.kt
│   │   │   │
│   │   │   └── profile/
│   │   │       ├── UserProfileFragment.kt
│   │   │       ├── ProfileViewModel.kt
│   │   │       └── UserPostsAdapter.kt
│   │   │
│   │   ├── challenge/                  # Features: Challenges
│   │   │   ├── list/
│   │   │   │   ├── ChallengesFragment.kt
│   │   │   │   ├── ChallengeViewModel.kt
│   │   │   │   └── ChallengeAdapter.kt
│   │   │   ├── detail/
│   │   │   │   ├── ChallengeDetailFragment.kt
│   │   │   │   └── ChallengeDetailViewModel.kt
│   │   │   ├── create/
│   │   │   │   ├── CreateChallengeFragment.kt
│   │   │   │   └── CreateChallengeViewModel.kt
│   │   │   └── join/
│   │   │       ├── JoinChallengeDialog.kt
│   │   │       └── JoinChallengeViewModel.kt
│   │   │
│   │   ├── leaderboard/                # Feature: Leaderboards
│   │   │   ├── LeaderboardFragment.kt
│   │   │   ├── LeaderboardViewModel.kt
│   │   │   └── LeaderboardAdapter.kt
│   │   │
│   │   ├── settings/                   # Feature: Settings & Profile
│   │   │   ├── ProfileFragment.kt          # Profile tab trong bottom nav
│   │   │   ├── SettingsFragment.kt
│   │   │   ├── SettingsViewModel.kt
│   │   │   ├── LanguageSettingFragment.kt
│   │   │   ├── ThemeSettingFragment.kt
│   │   │   └── AccountSettingFragment.kt
│   │   │
│   │   ├── report/                     # Feature: Report Generating
│   │   │   ├── ReportFragment.kt
│   │   │   ├── ReportViewModel.kt
│   │   │   ├── ReportGenerator.kt
│   │   │   └── ReportPreviewDialog.kt
│   │   │
│   │   └── common/                     # Shared Components
│   │       ├── BaseActivity.kt
│   │       ├── BaseFragment.kt
│   │       ├── BaseViewModel.kt
│   │       ├── LoadingDialog.kt
│   │       ├── ConfirmDialog.kt
│   │       └── EmptyStateView.kt
│   │
│   ├── di/                             # Dependency Injection (Hilt)
│   │   ├── AppModule.kt
│   │   ├── DatabaseModule.kt
│   │   ├── NetworkModule.kt
│   │   ├── RepositoryModule.kt
│   │   └── ServiceModule.kt
│   │
│   ├── service/                        # Background Services
│   │   ├── ReminderService.kt
│   │   ├── StreakUpdateService.kt
│   │   ├── SyncService.kt
│   │   └── PomodoroTimerService.kt
│   │
│   ├── worker/                         # WorkManager
│   │   ├── DailyQuoteWorker.kt
│   │   ├── StreakCheckWorker.kt
│   │   └── DataSyncWorker.kt
│   │
│   ├── util/                           # Utilities
│   │   ├── Constants.kt
│   │   ├── Extensions.kt
│   │   ├── DateUtils.kt
│   │   ├── StreakCalculator.kt
│   │   ├── NotificationHelper.kt
│   │   ├── PermissionHelper.kt
│   │   ├── NetworkHelper.kt
│   │   └── PdfGenerator.kt
│   │
│   └── HabitTrackerApplication.kt
│
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── fragment_home.xml
    │   ├── fragment_statistic.xml
    │   ├── fragment_community.xml
    │   ├── fragment_profile.xml
    │   ├── fragment_habit_list.xml
    │   ├── fragment_pomodoro.xml
    │   ├── fragment_feed.xml
    │   ├── fragment_challenges.xml
    │   ├── fragment_leaderboard.xml
    │   ├── fragment_settings.xml
    │   ├── item_habit.xml
    │   ├── item_category.xml
    │   ├── item_post.xml
    │   ├── item_comment.xml
    │   ├── item_friend.xml
    │   ├── item_challenge_card.xml
    │   ├── item_leaderboard.xml
    │   ├── dialog_add_category.xml
    │   ├── dialog_add_reminder.xml
    │   ├── dialog_confirm.xml
    │   └── bottom_sheet_comment.xml
    │
    ├── navigation/
    │   └── nav_graph_main.xml          # Main navigation graph
    │
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   ├── themes.xml
    │   ├── dimens.xml
    │   └── styles.xml
    │
    ├── values-vi/                      # Vietnamese localization
    │   └── strings.xml
    │
    ├── drawable/
    ├── menu/
    │   └── bottom_nav_menu.xml
    │
    └── xml/
```

## 📊 Mapping Features với Cấu trúc

| Feature | Module/Folder |
|---------|---------------|
| **Feature 1**: Manage Categories | `ui/category/` |
| **Feature 2**: Manage Habits | `ui/habit/` |
| **Feature 3**: Pomodoro Timer | `ui/pomodoro/` + `service/PomodoroTimerService.kt` |
| **Feature 4**: Track Progress | `ui/progress/` |
| **Feature 5**: Manage Streaks | `ui/streak/` + `worker/StreakCheckWorker.kt` |
| **Feature 6**: Reminders | `ui/reminder/` + `service/ReminderService.kt` |
| **Feature 7**: Motivational Quotes | `ui/quote/` + `worker/DailyQuoteWorker.kt` |
| **Feature 12**: Friends | `ui/social/friend/` |
| **Feature 13**: Community Posts | `ui/social/feed/` |
| **Feature 14**: Post Interactions | `ui/social/interaction/` |
| **Feature 15**: Create Challenges | `ui/challenge/create/` |
| **Feature 16**: Join Challenge | `ui/challenge/join/` |
| **Feature 17**: Leaderboards | `ui/leaderboard/` |
| **Feature 18**: User Account | `ui/auth/` + `ui/settings/` |
| **Feature 19**: Settings | `ui/settings/` |
| **Feature 20**: Report Generating | `ui/report/` + `util/PdfGenerator.kt` |

## 🔑 Data Models Chi Tiết

### Core Models

```kotlin
// Habit.kt
data class Habit(
    val id: String,
    val userId: String,
    val categoryId: String,
    val name: String,
    val quantity: Int,
    val unit: String,
    val frequency: List<String>,
    val isCompleted: Boolean,
    val streak: Int,
    val createdAt: Long,
    val completedDates: List<String>
)

// HabitLog.kt
data class HabitLog(
    val id: String,
    val habitId: String,
    val completedAt: Long,
    val actualValue: Int,
    val note: String?
)

// Streak.kt
data class Streak(
    val habitId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedDate: Long
)
```

## 🎯 Navigation Flow (Single-Activity)

```
MainActivity (Single Activity Container)
    │
    └── NavHostFragment
        │
        ├── HomeFragment (nav_home) ─────────────────── Dashboard
        │   ├── Daily Quote
        │   ├── Current Streaks
        │   └── Today's Habits
        │
        ├── StatisticFragment (nav_statistic) ───────── Progress/Stats
        │   ├── Calendar View
        │   └── Charts
        │
        ├── CommunityFragment (nav_community) ────────── Social Hub
        │   └── ViewPager2 + TabLayout
        │       ├── FeedFragment (tab 0) ─── Posts
        │       ├── ChallengesFragment (tab 1) ─── Challenges
        │       └── LeaderboardFragment (tab 2) ─── Rankings
        │
        └── ProfileFragment (nav_profile) ───────────── User Profile
            ├── User Stats
            ├── Settings
            └── Account Management
```

## 🔄 Data Flow (MVVM)

```
User Action → View (Fragment)
                ↓
            ViewModel (xử lý logic)
                ↓
            Repository (lấy/lưu dữ liệu)
                ↓
            Data Sources (Database/API)
                ↓
            Repository
                ↓
            ViewModel (update state)
                ↓
            View (observe & update UI)
```

## 📦 Dependencies Chính

```kotlin
// build.gradle.kts (app)
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    
    // Fragment & ViewPager2
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Datastore (cho Settings)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Chart Library (MPAndroidChart)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Image Loading (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // PDF Generation
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    // Firebase (cho authentication & social features)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
}
```

## 🔐 Quyền cần thiết (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## ✅ Checklist khi tạo Feature mới

- [ ] Tạo folder `ui/[feature_name]/`
- [ ] Tạo Fragment (ưu tiên) hoặc Activity
- [ ] Tạo ViewModel tương ứng
- [ ] Tạo layout file `fragment_[feature].xml`
- [ ] Tạo Repository trong `data/repository/`
- [ ] Tạo Model trong `data/model/`
- [ ] Tạo Adapter nếu có RecyclerView
- [ ] Thêm destination vào `nav_graph_main.xml`
- [ ] Thêm DI bindings nếu dùng Hilt

## 💡 Best Practices

1. **Single-Activity Architecture**: Sử dụng Fragment cho mọi màn hình
2. **Offline-First Architecture**: Lưu tất cả dữ liệu local trước, sync với server sau
3. **WorkManager**: Dùng cho daily tasks (quotes, streak check, reminders)
4. **Repository Pattern**: Combine local + remote data sources
5. **Sealed Classes**: Cho UI states (Loading, Success, Error)
6. **Dependency Injection**: Hilt cho toàn bộ dependencies
7. **Navigation Component**: Quản lý navigation giữa các screens
8. **DataStore**: Cho user preferences thay vì SharedPreferences
9. **Proper Error Handling**: Try-catch với proper user feedback
10. **Nested Fragments**: Sử dụng `childFragmentManager` cho ViewPager2 trong Fragment

## 🚀 Development Flow

1. **Phase 1** (Core): Auth, Habits, Categories
2. **Phase 2** (Tracking): Progress, Streaks, Reminders
3. **Phase 3** (Advanced): Pomodoro, Reports
4. **Phase 4** (Social): Friends, Posts, Challenges, Leaderboards

---

Cấu trúc này giúp:
- ✅ Single source of navigation
- ✅ Dễ scale khi thêm features
- ✅ Clear separation of concerns
- ✅ Dễ test từng feature độc lập
- ✅ Team có thể làm song song nhiều features
- ✅ Maintain dễ dàng trong tương lai
- ✅ Bottom navigation state được giữ đúng
