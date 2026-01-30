# Habit Tracker App

A comprehensive Android mobile application for tracking daily habits, building consistency, improving productivity, and connecting with a community of like-minded individuals striving for self-improvement.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technical Stack](#technical-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Core Features](#core-features)
- [Installation](#installation)
- [Configuration](#configuration)
- [Dependencies](#dependencies)
- [Development](#development)
- [Firebase Setup](#firebase-setup)
- [License](#license)

## Overview

Habit Tracker is a feature-rich Android application built with modern Android development practices, designed to help users build and maintain positive habits through gamification, social features, and intelligent tracking mechanisms. The app combines personal habit management with community challenges and a competitive leaderboard system to keep users motivated and engaged.

## Features

### 🎯 Habit Management
- **Create Custom Habits**: Define habits with customizable parameters:
  - Name and description
  - Target quantity and measurement units (minutes, hours, pages, times, km, etc.)
  - Frequency (daily, specific days of the week)
  - Category classification with custom icons and colors
  - Optional Pomodoro timer integration for focused work sessions
- **Track Progress**: Mark habits as complete with visual feedback
- **Streak Tracking**: Automatic streak counting to maintain momentum
- **Calendar View**: Monthly calendar visualization of habit completion
- **Habit Details**: View and edit habit information, including:
  - Completion history
  - Current streak
  - Pomodoro settings
  - Category assignment

### ⏱️ Pomodoro Focus Timer
- **Integrated Focus Sessions**: Built-in Pomodoro timer for habits requiring focused work
- **Customizable Durations**: 
  - Focus duration (default 25 minutes)
  - Short break (default 5 minutes)
  - Long break (default 15 minutes)
  - Total sessions per habit (default 4)
- **Visual Progress**: Session dots showing completed and remaining focus sessions
- **Automatic Transitions**: Auto-switch between focus and break modes
- **Habit Completion**: Complete habits automatically after finishing all Pomodoro sessions

### 📊 Analytics & Statistics
- **Personal Dashboard**: Overview of:
  - Today's completion rate
  - Habit score (overall progress)
  - Today's completed vs. total habits
  - Unfinished habits drawer for quick access
- **Weekly Charts**: Visual bar chart showing:
  - Daily completion counts
  - Completion rates per day
  - Interactive tooltips with detailed information
- **Habit Statistics**: Comprehensive view of each habit:
  - Completion rate percentage
  - Total completions vs. expected
  - Weekly progress visualization
  - Color-coded status indicators (completed, missed, pending)
- **Calendar Analytics**: Monthly view with:
  - Current streak
  - Best streak
  - Total completion days
  - Monthly completion rate

### 🏆 Community Challenges
- **Browse Challenges**: Discover community-created challenges with:
  - 7-day, 30-day, or 100-day durations
  - Reward points for completion
  - Participant counts
  - Voting system for pending challenges
- **Create Challenges**: Submit your own challenges for community approval
- **Join Challenges**: Participate in approved challenges
- **Challenge Habits**: Special challenge-linked habits with:
  - Challenge branding and imagery
  - Duration tracking
  - Reward allocation upon completion
- **Voting System**: Community votes determine challenge approval (10 votes required)
- **Share to Feed**: Challenge creators can share pending challenges to the feed for votes

### 📱 Social Feed
- **Post Creation**: Share your progress with text and images
- **Engage with Community**:
  - Like posts
  - Comment on posts
  - Reply to comments
  - Share posts
- **Challenge Voting**: Vote for pending challenges directly from feed posts
- **User Profiles**: Visit other users' profiles to see their:
  - Posts
  - Friends
  - Progress
- **Real-time Updates**: Live feed updates using Firebase Firestore listeners

### 👥 Social Features
- **Friend System**:
  - Send friend requests
  - Accept/reject friend requests
  - View friends' profiles
  - Unfriend users
  - Search for users globally
- **Profile Management**:
  - Upload and change profile avatar
  - Edit personal information (name, date of birth, gender)
  - View your posts and friends
  - Track your rank and points
- **Leaderboard**: Competitive ranking system based on points:
  - Top 3 podium display
  - Full leaderboard with rankings
  - Current user rank indicator
  - Real-time rank updates

### 🔔 Notifications
- **In-App Notifications**: Receive notifications for:
  - Post likes
  - Comments on your posts
  - Replies to your comments
  - Friend requests
  - Friend request acceptances
- **Push Notifications**: Firebase Cloud Messaging (FCM) integration for real-time alerts
- **Daily Reminders**: Customizable daily habit reminders:
  - Set reminder time
  - Enable/disable reminders
  - Smart notification content based on incomplete habits
- **Notification Center**: Dropdown notification view with:
  - Unread count indicator
  - Mark as read functionality
  - Navigate to related content

### 🎨 Customization
- **Custom Categories**: Create and manage habit categories with:
  - Custom names
  - Icon selection (60+ icons)
  - Color themes (15+ color options)
  - Edit and delete categories
- **Default Categories**: Pre-seeded categories for new users:
  - Sports & Fitness
  - Study & Education
  - Work & Productivity
  - Health & Wellness
  - And more...

### 🔐 Authentication
- **Multiple Sign-In Options**:
  - Email/Password registration and login
  - Google Sign-In integration
  - Anonymous guest mode
- **Password Management**:
  - Password reset via email
  - Change password in settings
  - Secure Firebase Authentication
- **Account Management**:
  - Account deletion with data cleanup
  - Logout functionality

### ⚙️ Settings
- **Profile Settings**: Edit name, avatar, date of birth, gender
- **Notification Settings**: 
  - Enable/disable app notifications
  - Configure daily reminders
  - Set reminder time
- **Password Reset**: Change password for email/password accounts
- **Terms & Privacy**: View terms and privacy policy
- **Daily Quote Settings**: Enable/disable motivational quotes on dashboard
- **Account Deletion**: Permanent account removal with full data cleanup

### 💬 Additional Features
- **Daily Quotes**: AI-generated personalized motivational quotes based on your habit statistics
- **Quote Management**: View, regenerate, and refresh daily motivational content
- **Swipe-to-Refresh**: Pull-to-refresh functionality across feeds and lists
- **Dark/Light UI**: Modern, clean interface with gradient backgrounds
- **Edge-to-Edge Display**: Immersive full-screen experience
- **Smooth Animations**: Polished UI transitions and interactions
- **Offline Support**: Firebase Firestore offline persistence
- **Image Upload**: Upload images for posts and challenges
- **Camera Integration**: Take photos directly from the app

## Technical Stack

### Languages & Frameworks
- **Kotlin**: Primary programming language (100%)
- **Android SDK**: Target SDK 36, Min SDK 24
- **Material Design 3**: Modern UI components

### Architecture & Patterns
- **MVVM (Model-View-ViewModel)**: Clean architecture pattern
- **Single-Activity Architecture**: Navigation Component with fragments
- **Repository Pattern**: Data layer abstraction
- **StateFlow & LiveData**: Reactive state management
- **Kotlin Coroutines**: Asynchronous programming
- **ViewBinding**: Type-safe view access

### Backend & Database
- **Firebase Authentication**: User authentication and management
- **Firebase Firestore**: NoSQL cloud database for real-time data
- **Firebase Cloud Messaging (FCM)**: Push notifications
- **Firebase Storage**: Image and file storage
- **Firestore Offline Persistence**: Local caching for offline access

### Libraries & Dependencies

#### Core Android
- AndroidX Core KTX `1.17.0`
- AppCompat `1.7.1`
- Activity KTX `1.12.0`
- Fragment KTX `1.8.5`
- ConstraintLayout `2.2.1`
- SwipeRefreshLayout `1.1.0`
- FlexboxLayout `3.0.0`

#### Lifecycle & ViewModel
- Lifecycle ViewModel KTX `2.8.7`
- Lifecycle LiveData KTX `2.8.7`
- Lifecycle Runtime KTX `2.8.7`

#### Navigation
- Navigation Fragment KTX `2.7.7`
- Navigation UI KTX `2.7.7`

#### UI Components
- Material Components `1.13.0`
- ViewPager2 `1.1.0`
- RecyclerView (via Material)
- CardView (via Material)

#### Firebase
- Firebase BoM `33.7.0`
- Firebase Auth `24.0.1`
- Firebase Database `22.0.1`
- Firebase Firestore KTX
- Google Services `4.4.4`

#### Credential Management
- Credentials `1.5.0`
- Credentials Play Services Auth `1.5.0`
- Google ID `1.1.1`

#### Image Loading
- Glide `4.16.0`
- Glide KSP Compiler `4.16.0`

#### Networking
- Retrofit `2.9.0`
- Retrofit Gson Converter `2.9.0`

#### Coroutines
- Kotlinx Coroutines Android `1.9.0`
- Kotlinx Coroutines Core `1.9.0`

#### Build Tools
- Kotlin Gradle Plugin `2.0.21`
- Android Gradle Plugin `8.13.2`
- KSP `2.0.21-1.0.28`

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with a clear separation of concerns:

```
app/
├── data/
│   ├── model/         # Data classes (Habit, User, Post, Challenge, etc.)
│   ├── repository/    # Data access layer (HabitRepository, AuthRepository, etc.)
│   ├── service/       # Firebase services, API services
│   ├── remote/        # Remote data sources (FCM, API)
│   └── firebase/      # Firebase managers
├── ui/
│   ├── main/          # MainActivity and main navigation
│   ├── auth/          # Login, Register, Password Reset
│   ├── dashboard/     # Home screen with habits and quotes
│   ├── habit/         # Habit creation, editing, and details
│   ├── category/      # Category management
│   ├── progress/      # Statistics and analytics
│   ├── social/        # Community, Profile, Friends
│   ├── feed/          # Social feed and posts
│   ├── challenge/     # Challenges
│   ├── leaderboard/   # Leaderboard and rankings
│   ├── pomodoro/      # Focus timer
│   ├── notification/  # Notifications
│   └── setting/       # Settings and preferences
├── util/              # Utility classes and helpers
└── worker/            # Background workers (reminders, etc.)
```

### Data Flow

1. **UI Layer (Fragments/Activities)**: 
   - Observes ViewModel state using StateFlow/LiveData
   - Handles user interactions
   - Updates UI based on state changes

2. **ViewModel Layer**:
   - Manages UI state
   - Handles business logic
   - Communicates with repositories
   - Uses Kotlin Coroutines for async operations

3. **Repository Layer**:
   - Abstracts data sources
   - Handles data operations (CRUD)
   - Provides clean API to ViewModels
   - Manages Firebase operations

4. **Data Layer**:
   - Model classes (data classes)
   - Firebase Firestore integration
   - Local caching and offline support

## Project Structure

```
habit-tracker-app/
├── app/
│   ├── build.gradle.kts           # App-level build configuration
│   ├── google-services.json       # Firebase configuration
│   ├── proguard-rules.pro         # ProGuard rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/habittracker/
│       │   │   ├── HabitTrackerApplication.kt
│       │   │   ├── data/
│       │   │   ├── ui/
│       │   │   ├── util/
│       │   │   └── worker/
│       │   └── res/
│       │       ├── color/
│       │       ├── drawable/
│       │       ├── layout/
│       │       ├── menu/
│       │       ├── mipmap/
│       │       ├── navigation/
│       │       ├── values/
│       │       └── xml/
│       ├── androidTest/
│       └── test/
├── gradle/
│   ├── libs.versions.toml         # Version catalog
│   └── wrapper/
├── build.gradle.kts                # Project-level build file
├── settings.gradle.kts             # Settings configuration
├── gradle.properties               # Gradle properties
├── gradlew                         # Gradle wrapper (Unix)
├── gradlew.bat                     # Gradle wrapper (Windows)
├── LICENSE                         # License file
└── README.md                       # This file
```

## Core Features

### Habit Tracking Flow

1. **Create Habit**: User creates a habit with details (name, quantity, frequency, category)
2. **Daily View**: Dashboard shows habits scheduled for today
3. **Complete Habit**: User marks habit as complete (with optional Pomodoro timer)
4. **Track Progress**: System updates streak and completion dates
5. **View Statistics**: Analytics show progress over time

### Challenge Flow

1. **Create Challenge**: User submits a challenge (pending approval)
2. **Share for Votes**: Creator shares challenge to feed for community votes
3. **Community Votes**: Users vote on pending challenges in the feed
4. **Auto-Approve**: Challenge approved after 10 votes
5. **Join Challenge**: Users join approved challenges
6. **Track Progress**: Challenge habit created and tracked like regular habits
7. **Earn Rewards**: Receive points upon challenge completion

### Social Interaction Flow

1. **Post Creation**: User shares progress with text/image
2. **Feed Display**: Post appears in community feed
3. **Engagement**: Other users like, comment, or share
4. **Notifications**: User receives notifications for interactions
5. **Profile View**: Click on user to view their profile and activity

## Installation

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 17
- Android SDK 36
- Minimum Android device/emulator: API 24 (Android 7.0)

### Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/JakeConal/habit-tracker-app.git
   cd habit-tracker-app
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Configure Firebase**:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Authentication, Firestore, Storage, and Cloud Messaging in Firebase Console

4. **Update FCM Server Key** (for push notifications):
   - Open `app/src/main/java/com/example/habittracker/data/remote/fcm/NotificationSender.kt`
   - Replace `SERVER_KEY` with your Firebase Cloud Messaging Legacy Server Key
   - Find it in Firebase Console → Project Settings → Cloud Messaging

5. **Build and Run**:
   ```bash
   ./gradlew clean build
   ```
   - Or click "Run" in Android Studio
   - Select your device/emulator

## Configuration

### Firebase Configuration

The app requires Firebase services:

1. **Authentication**: Enable Email/Password and Google Sign-In
2. **Firestore Database**: Create collections:
   - `users`
   - `habits`
   - `categories`
   - `posts`
   - `comments`
   - `challenges`
   - `userChallenges`
   - `notifications`
   - `friendships`
   - `friendRequests`

3. **Storage**: Enable Firebase Storage for image uploads

4. **Cloud Messaging**: Enable FCM for push notifications

### Google Sign-In Configuration

1. Add your SHA-1 certificate fingerprint to Firebase project
2. Download updated `google-services.json`
3. Add web client ID to `res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
   ```

### Firestore Security Rules

Basic security rules (customize as needed):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /habits/{habitId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
    
    match /posts/{postId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
    
    match /challenges/{challengeId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null;
    }
  }
}
```

## Dependencies

Key dependencies are managed through Gradle Version Catalog (`gradle/libs.versions.toml`):

```toml
[versions]
agp = "8.13.2"
kotlin = "2.0.21"
coreKtx = "1.17.0"
material = "1.13.0"
firebaseAuth = "24.0.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth", version.ref = "firebaseAuth" }
# ... more dependencies
```

## Development

### Building the App

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### Code Style

The project follows Kotlin coding conventions:
- Use `camelCase` for variables and functions
- Use `PascalCase` for classes
- Use meaningful variable names
- Add KDoc comments for public APIs

### Adding New Features

1. Create data model in `data/model/`
2. Create repository in `data/repository/`
3. Create ViewModel in appropriate UI package
4. Create Fragment/Activity with ViewBinding
5. Update navigation graph if needed
6. Add to AndroidManifest.xml if it's an Activity

## Firebase Setup

### Required Firebase Services

1. **Authentication**
   - Email/Password provider
   - Google Sign-In provider
   - Anonymous authentication

2. **Cloud Firestore**
   - Create database in production mode
   - Set up security rules
   - Enable offline persistence

3. **Cloud Storage**
   - Create default bucket
   - Set up storage rules for images

4. **Cloud Messaging**
   - Get Legacy Server Key
   - Configure in app

### Firestore Collections Structure

```
users/
  {userId}/
    - id: string
    - name: string
    - avatarUrl: string?
    - points: int
    - rank: int
    - email: string?
    - joinedChallengeIds: array
    - votedChallengeIds: array
    - fcmToken: string?

habits/
  {habitId}/
    - id: string
    - userId: string
    - name: string
    - quantity: int
    - unit: string
    - frequency: array
    - categoryId: string
    - completedDates: array
    - streak: int
    - isPomodoroRequired: boolean
    - focusDuration: int
    - isChallengeHabit: boolean
    - challengeId: string?

posts/
  {postId}/
    - id: string
    - userId: string
    - authorName: string
    - authorAvatarUrl: string
    - content: string
    - imageUrl: string?
    - timestamp: long
    - likeCount: int
    - commentCount: int
    - likedBy: array

challenges/
  {challengeId}/
    - id: string
    - title: string
    - description: string
    - detail: string
    - imgURL: string
    - duration: string (SEVEN_DAYS, THIRTY_DAYS, HUNDRED_DAYS)
    - reward: int
    - creatorId: string
    - participantCount: int
    - status: string (PENDING, APPROVED)
    - votes: int
    - votedBy: array

notifications/
  {notificationId}/
    - id: string
    - recipientId: string
    - senderId: string
    - type: string
    - postId: string?
    - commentId: string?
    - read: boolean
    - timestamp: long
```

## Technical Implementation Details

This section provides in-depth technical documentation for each major feature, including data flow, state management, and implementation specifics.

### Habit Management System

#### Technical Architecture

The habit management system follows a clean MVVM architecture with reactive data flows:

```
User Input → ViewModel → Repository → Firestore
                ↓
            StateFlow
                ↓
         UI Updates (Fragment)
```

#### Create Habit Feature

**Input Flow:**
```kotlin
// User Input Components
- EditText: Habit name
- EditText: Quantity (numeric)
- Spinner: Measurement unit selector
- CheckBox: Frequency (multi-select for days)
- Button: Category selector
- Switch: Pomodoro requirement toggle
- EditText: Focus duration, break durations, sessions
```

**Process Flow:**

1. **Data Validation (ViewModel Layer)**
```kotlin
// CreateHabitViewModel.kt
fun createHabit() {
    viewModelScope.launch {
        try {
            _isLoading.value = true
            
            // Validation
            if (_title.value.isBlank()) {
                _error.emit("Habit title cannot be empty")
                return@launch
            }
            
            // Get authenticated user
            val currentUserId = authRepository.getCurrentUser()?.uid
            if (currentUserId == null) {
                _error.emit("User not authenticated")
                return@launch
            }
            
            // Create Habit object
            val habit = Habit(
                userId = currentUserId,
                name = _title.value,
                quantity = _quantity.value,
                unit = _measurement.value,
                frequency = _frequency.value,
                createdAt = System.currentTimeMillis(),
                categoryId = _categoryId.value,
                completedDates = emptyList(),
                streak = 0,
                isPomodoroRequired = _isPomodoroRequired.value,
                focusDuration = _focusDuration.value,
                shortBreak = _shortBreak.value,
                longBreak = _longBreak.value,
                totalSessions = _totalSessions.value
            )
            
            // Save to repository
            val habitId = repository.addHabit(habit)
            
            if (habitId != null) {
                _habitCreated.emit(true)
            } else {
                _error.emit("Failed to create habit")
            }
        } catch (e: Exception) {
            _error.emit(e.message ?: "Failed to create habit")
        } finally {
            _isLoading.value = false
        }
    }
}
```

2. **Repository Layer Processing**
```kotlin
// HabitRepository.kt
suspend fun addHabit(habit: Habit): String? {
    return try {
        val habitId = FirestoreManager.addDocument(
            Habit.COLLECTION_NAME,
            habit.toMap()
        )
        
        if (habitId != null) {
            // Refresh local cache
            getHabitsForUser(habit.userId)
        }
        habitId
    } catch (e: Exception) {
        println("Error adding habit: ${e.message}")
        null
    }
}
```

3. **Firestore Manager Layer**
```kotlin
// FirestoreManager.kt
suspend fun addDocument(
    collectionName: String,
    data: Map<String, Any?>
): String? {
    return try {
        val docRef = db.collection(collectionName).document()
        val documentId = docRef.id
        
        // Add document ID to data
        val dataWithId = data.toMutableMap()
        dataWithId["id"] = documentId
        
        docRef.set(dataWithId).await()
        documentId
    } catch (e: Exception) {
        null
    }
}
```

**Output Flow:**

1. **Success State:**
   - `_habitCreated` SharedFlow emits `true`
   - UI observes and shows success message
   - Activity finishes and returns to dashboard
   - Dashboard automatically refreshes with new habit

2. **Error State:**
   - `_error` SharedFlow emits error message
   - UI shows error toast/snackbar
   - User remains on creation screen

**State Management:**
```kotlin
// StateFlow for reactive UI updates
private val _title = MutableStateFlow("")
val title: StateFlow<String> = _title.asStateFlow()

private val _quantity = MutableStateFlow(30)
val quantity: StateFlow<Int> = _quantity.asStateFlow()

private val _isLoading = MutableStateFlow(false)
val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

// SharedFlow for one-time events
private val _habitCreated = MutableSharedFlow<Boolean>()
val habitCreated: SharedFlow<Boolean> = _habitCreated.asSharedFlow()
```

**UI Observation:**
```kotlin
// CreateHabitActivity.kt
private fun observeViewModel() {
    // Observe habit creation success
    lifecycleScope.launch {
        viewModel.habitCreated.collect { success ->
            if (success) {
                showSuccess(getString(R.string.habit_created))
                finish()
            }
        }
    }
    
    // Observe errors
    lifecycleScope.launch {
        viewModel.error.collect { errorMessage ->
            errorMessage?.let {
                showError(it)
            }
        }
    }
    
    // Observe loading state
    lifecycleScope.launch {
        viewModel.isLoading.collect { isLoading ->
            binding.btnCreate.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
```

#### Habit Completion Feature

**Input Flow:**
```kotlin
// User clicks check button on habit card
habitAdapter.onCheckClick = { habit ->
    viewModel.toggleHabitCompletion(habit)
}
```

**Process Flow:**

1. **Validation & State Check**
```kotlin
// HomeViewModel.kt
fun toggleHabitCompletion(habit: Habit) {
    viewModelScope.launch {
        try {
            // Check if habit scheduled for today
            val today = DateUtils.getCurrentDateString()
            val calendar = Calendar.getInstance()
            
            if (!shouldHabitBeDoneToday(habit, calendar)) {
                _error.emit("This habit is not scheduled for today")
                return@launch
            }
            
            // Check if already completed
            if (habit.completedDates.contains(today)) {
                _error.emit("Habit already completed for today")
                return@launch
            }
            
            // Perform completion
            habitRepository.toggleHabitCompletion(habit.id)
            
            // Reload habits
            loadHabits()
        } catch (e: Exception) {
            _error.emit("Error updating habit: ${e.message}")
        }
    }
}
```

2. **Repository Processing with Streak Calculation**
```kotlin
// HabitRepository.kt
suspend fun toggleHabitCompletion(habitId: String): Boolean {
    return try {
        val habit = getHabitById(habitId)
        if (habit != null) {
            val today = DateUtils.getCurrentDateString()
            val updatedCompletedDates = habit.completedDates.toMutableList()
            val isCompleting = !updatedCompletedDates.contains(today)
            
            if (isCompleting) {
                updatedCompletedDates.add(today)
                
                // Calculate new streak
                val newStreak = calculateStreak(updatedCompletedDates, habit.frequency)
                
                val updatedHabit = habit.copy(
                    completedDates = updatedCompletedDates,
                    streak = newStreak
                )
                updateHabit(updatedHabit)
            } else {
                updatedCompletedDates.remove(today)
                val updatedHabit = habit.copy(
                    completedDates = updatedCompletedDates,
                    streak = maxOf(0, habit.streak - 1)
                )
                updateHabit(updatedHabit)
            }
        } else {
            false
        }
    } catch (e: Exception) {
        println("Error toggling habit completion: ${e.message}")
        false
    }
}

private fun calculateStreak(completedDates: List<String>, frequency: List<String>): Int {
    if (completedDates.isEmpty()) return 0
    
    val sortedDates = completedDates.sorted().reversed()
    val today = DateUtils.getCurrentDateString()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    var streak = 0
    val calendar = Calendar.getInstance()
    
    // Start from today and count backwards
    while (true) {
        val dateStr = dateFormat.format(calendar.time)
        
        // If we've gone past our earliest completion, stop
        if (dateStr < sortedDates.last()) break
        
        // Check if this day should count (based on frequency)
        val shouldCount = when {
            frequency.contains("Daily") -> true
            else -> {
                val dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                frequency.contains(dayName)
            }
        }
        
        if (shouldCount) {
            if (sortedDates.contains(dateStr)) {
                streak++
            } else {
                // Streak broken
                break
            }
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }
    
    return streak
}
```

3. **Firestore Update**
```kotlin
// FirestoreManager.kt
suspend fun updateDocument(
    collectionName: String,
    documentId: String,
    data: Map<String, Any?>
): Boolean {
    return try {
        db.collection(collectionName)
            .document(documentId)
            .update(data)
            .await()
        true
    } catch (e: Exception) {
        false
    }
}
```

**Output Flow:**

1. **UI Update via StateFlow:**
```kotlin
// Habits are stored in StateFlow
private val _habits = MutableStateFlow<List<Habit>>(emptyList())
val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

// UI observes and automatically updates
lifecycleScope.launch {
    viewModel.habits.collect { habits ->
        habitsAdapter.submitList(habits)
    }
}
```

2. **Visual Feedback:**
   - Check icon appears/disappears
   - Streak counter updates
   - Completion percentage recalculates
   - Statistics update in real-time

**Data Model:**
```kotlin
data class Habit(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val unit: String = "",
    val frequency: List<String> = listOf("Daily"),
    val createdAt: Long = System.currentTimeMillis(),
    val categoryId: String = "",
    val completedDates: List<String> = emptyList(), // ["2026-01-30", "2026-01-29"]
    val streak: Int = 0,
    val isPomodoroRequired: Boolean = false,
    val focusDuration: Int = 25,
    val shortBreak: Int = 5,
    val longBreak: Int = 15,
    val totalSessions: Int = 4,
    val isChallengeHabit: Boolean = false,
    val challengeId: String? = null,
    val challengeImageUrl: String? = null,
    val challengeDescription: String? = null,
    val challengeDurationDays: Int? = null,
    val isChallengeRewarded: Boolean = false
)
```

### Pomodoro Focus Timer System

#### Technical Architecture

The Pomodoro system uses a ViewModel-managed countdown timer with state persistence:

```
User Action → ViewModel Timer Control → CountDownTimer
                        ↓
                   UI State Flow
                        ↓
                  UI Updates (Progress, Time, Mode)
```

#### Timer State Management

**State Model:**
```kotlin
// FocusTimerViewModel.kt
data class FocusTimerUiState(
    val taskName: String = "Walk",
    val mode: TimerMode = TimerMode.FOCUS,
    val status: TimerStatus = TimerStatus.IDLE,
    val totalTimeMillis: Long = 25 * 60 * 1000L,
    val remainingTimeMillis: Long = 25 * 60 * 1000L,
    val progress: Float = 100f,
    val totalSessions: Int = 4,
    val currentSession: Int = 1,
    val completedSessions: Int = 0,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15
)

enum class TimerStatus {
    IDLE,    // Timer not started
    RUNNING, // Timer counting down
    PAUSED   // Timer paused
}

enum class TimerMode {
    FOCUS,      // Focus session (25 min default)
    BREAK,      // Short break (5 min default)
    LONG_BREAK  // Long break (15 min default)
}
```

**Input Flow:**

1. **Configuration from Habit:**
```kotlin
// Intent extras from ViewHabitDetailActivity
val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Habit"
val focusDuration = intent.getIntExtra(EXTRA_FOCUS_DURATION, 25)
val shortBreak = intent.getIntExtra(EXTRA_SHORT_BREAK, 5)
val longBreak = intent.getIntExtra(EXTRA_LONG_BREAK, 15)
val totalSessions = intent.getIntExtra(EXTRA_TOTAL_SESSIONS, 4)

// Configure ViewModel
viewModel.setTaskName(habitName)
viewModel.setFocusDuration(focusDuration)
viewModel.setBreakDuration(shortBreak)
viewModel.setLongBreakDuration(longBreak)
viewModel.setTotalSessions(totalSessions)
viewModel.setMode(FocusTimerViewModel.TimerMode.FOCUS)
```

2. **User Controls:**
```kotlin
// Play/Pause button
binding.btnPlayPause.setOnClickListener {
    viewModel.togglePlayPause()
}

// Stop button
binding.btnStop.setOnClickListener {
    viewModel.stopTimer()
}

// Mode selectors
binding.btnFocusMode.setOnClickListener {
    viewModel.setMode(TimerMode.FOCUS)
}
binding.btnBreakMode.setOnClickListener {
    viewModel.setMode(TimerMode.BREAK)
}
```

**Process Flow:**

1. **Timer Start:**
```kotlin
// FocusTimerViewModel.kt
fun startTimer() {
    val currentState = _uiState.value
    
    when (currentState.status) {
        TimerStatus.IDLE, TimerStatus.PAUSED -> {
            _uiState.value = currentState.copy(status = TimerStatus.RUNNING)
            startCountDown(currentState.remainingTimeMillis)
        }
        TimerStatus.RUNNING -> {
            // Already running
        }
    }
}

private fun startCountDown(timeMillis: Long) {
    countDownTimer = object : CountDownTimer(timeMillis, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val currentState = _uiState.value
            val progress = (millisUntilFinished.toFloat() / currentState.totalTimeMillis) * 100f
            
            _uiState.value = currentState.copy(
                remainingTimeMillis = millisUntilFinished,
                progress = progress
            )
        }
        
        override fun onFinish() {
            onTimerComplete()
        }
    }.start()
}
```

2. **Timer Completion Logic:**
```kotlin
private fun onTimerComplete() {
    val currentState = _uiState.value
    
    when (currentState.mode) {
        TimerMode.FOCUS -> {
            // Focus session completed
            val newCompletedSessions = currentState.completedSessions + 1
            
            if (newCompletedSessions >= currentState.totalSessions) {
                // All sessions completed - trigger habit completion
                _uiState.value = currentState.copy(
                    status = TimerStatus.IDLE,
                    completedSessions = newCompletedSessions,
                    remainingTimeMillis = 0,
                    progress = 0f
                )
            } else {
                // Determine next break type
                val nextMode = if (newCompletedSessions % 2 != 0) {
                    TimerMode.BREAK  // Short break after odd sessions
                } else {
                    TimerMode.LONG_BREAK  // Long break after even sessions
                }
                
                _uiState.value = currentState.copy(
                    status = TimerStatus.IDLE,
                    completedSessions = newCompletedSessions,
                    currentSession = currentState.currentSession + 1
                )
                
                // Auto-switch to break mode
                setMode(nextMode)
                startTimer()
            }
        }
        TimerMode.BREAK, TimerMode.LONG_BREAK -> {
            // Break completed, switch back to focus
            setMode(TimerMode.FOCUS)
            startTimer()
        }
    }
}
```

3. **Mode Switching:**
```kotlin
fun setMode(mode: TimerMode) {
    if (_uiState.value.status == TimerStatus.RUNNING) {
        pauseTimer()
    }
    
    val newTotalTime = when (mode) {
        TimerMode.FOCUS -> focusDurationMinutes * 60 * 1000L
        TimerMode.BREAK -> breakDurationMinutes * 60 * 1000L
        TimerMode.LONG_BREAK -> longBreakDurationMinutes * 60 * 1000L
    }
    
    _uiState.value = _uiState.value.copy(
        mode = mode,
        totalTimeMillis = newTotalTime,
        remainingTimeMillis = newTotalTime,
        progress = 100f,
        status = TimerStatus.IDLE
    )
}
```

**Output Flow:**

1. **Real-time UI Updates:**
```kotlin
// FocusTimerActivity.kt
private fun observeViewModel() {
    lifecycleScope.launch {
        viewModel.uiState.collect { state ->
            updateUI(state)
        }
    }
}

private fun updateUI(state: FocusTimerUiState) {
    // Update timer display
    binding.tvTimerDisplay.text = viewModel.formatTime(state.remainingTimeMillis)
    
    // Update circular progress
    binding.circularProgress.progress = state.progress.toInt()
    
    // Update mode label
    binding.tvTimerTypeLabel.text = when (state.mode) {
        TimerMode.FOCUS -> getString(R.string.focus_time)
        TimerMode.BREAK -> getString(R.string.break_time)
        TimerMode.LONG_BREAK -> "Long Break"
    }
    
    // Update play/pause button
    updatePlayPauseButton(state.status)
    
    // Update session dots
    updateSessionDots(state.completedSessions, state.totalSessions)
    
    // Check completion
    if (state.completedSessions >= state.totalSessions && 
        state.mode == TimerMode.FOCUS) {
        onAllSessionsCompleted()
    }
}
```

2. **Session Progress Visualization:**
```kotlin
private fun updateSessionDots(completedSessions: Int, totalSessions: Int) {
    sessionDots.forEachIndexed { index, dot ->
        if (index < totalSessions) {
            dot.visibility = View.VISIBLE
            if (index < completedSessions) {
                dot.setBackgroundResource(R.drawable.bg_session_dot_active)
            } else {
                dot.setBackgroundResource(R.drawable.bg_session_dot_inactive)
            }
        } else {
            dot.visibility = View.GONE
        }
    }
}
```

3. **Habit Completion on Timer Finish:**
```kotlin
private fun onAllSessionsCompleted() {
    val resultIntent = Intent().apply {
        putExtra("habit_completed", true)
    }
    setResult(RESULT_OK, resultIntent)
    finish()
}

// In ViewHabitDetailActivity
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_FOCUS_TIMER && resultCode == RESULT_OK) {
        val completed = data?.getBooleanExtra("habit_completed", false) ?: false
        if (completed) {
            viewModel.completeHabit()
        }
    }
}
```

**Time Formatting:**
```kotlin
fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
```

### Statistics & Analytics System

#### Technical Architecture

The statistics system uses reactive flows with complex data transformations:

```
Firestore Habits Collection
        ↓
  Repository Flow
        ↓
  Combined Flows (habits + categories)
        ↓
  Transformation Functions
        ↓
  StateFlow Results
        ↓
  UI Observation & Display
```

#### Data Processing Pipeline

**Input Sources:**
```kotlin
// StatisticsViewModel.kt
private val habitRepository = HabitRepository.getInstance()
private val categoryRepository = CategoryRepository.getInstance()
private val authRepository = AuthRepository.getInstance()

// Primary data source
val habits: StateFlow<List<Habit>> = habitRepository.habits
    .map { habitsList ->
        // Filter out expired challenge habits
        habitsList.filter { habit ->
            if (!habit.isChallengeHabit) {
                true
            } else if (habit.challengeDurationDays != null) {
                val durationMillis = habit.challengeDurationDays * 24 * 60 * 60 * 1000L
                val currentTime = System.currentTimeMillis()
                val expiryTime = habit.createdAt + durationMillis
                currentTime <= expiryTime
            } else {
                true
            }
        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

**Process Flow - Habit Statistics Calculation:**

1. **Completion Rate Calculation:**
```kotlin
private fun calculateHabitStatisticsInternal(
    habits: List<Habit>,
    categoriesMap: Map<String, Category>
): List<HabitStatistics> {
    return habits.map { habit ->
        val category = categoriesMap[habit.categoryId]
        
        // Calculate expected completions based on frequency
        val expectedCompletions = calculateExpectedCompletions(habit)
        val actualCompletions = habit.completedDates.size
        val completionRate = if (expectedCompletions > 0) {
            (actualCompletions.toFloat() / expectedCompletions) * 100f
        } else {
            0f
        }
        
        // Determine visual styling
        var iconRes = category?.icon?.resId ?: R.drawable.ic_other
        var iconBgRes = category?.color?.resId ?: R.drawable.bg_category_icon_pink_light
        var badgeBgRes = R.drawable.badge_color_cyan
        
        if (habit.isChallengeHabit) {
            iconRes = R.drawable.ic_trophy
            iconBgRes = R.drawable.bg_rainbow_gradient
            badgeBgRes = when (habit.challengeDurationDays) {
                7 -> R.drawable.badge_color_cyan
                30 -> R.drawable.badge_color_green
                100 -> R.drawable.badge_color_yellow
                else -> R.drawable.badge_color_cyan
            }
        }
        
        HabitStatistics(
            habitId = habit.id,
            habitName = habit.name,
            completionRate = completionRate.coerceIn(0f, 100f),
            iconRes = iconRes,
            iconBgRes = iconBgRes,
            frequency = FrequencyFormatter.formatFrequency(habit.frequency),
            badgeBgRes = badgeBgRes,
            totalCompletions = actualCompletions,
            expectedCompletions = expectedCompletions,
            weeklyDays = getWeeklyDaysForHabit(habit)
        )
    }.sortedByDescending { it.completionRate }
}
```

2. **Expected Completions Logic:**
```kotlin
private fun calculateExpectedCompletions(habit: Habit): Int {
    val calendar = Calendar.getInstance()
    val today = calendar.time
    val createdDate = Calendar.getInstance().apply {
        timeInMillis = habit.createdAt
    }.time
    
    val daysSinceCreation = ((today.time - createdDate.time) / (1000 * 60 * 60 * 24)).toInt() + 1
    
    return when {
        habit.frequency.contains("Daily") -> daysSinceCreation
        habit.frequency.isEmpty() -> 0
        else -> {
            // Count how many days per week
            val daysPerWeek = habit.frequency.size
            val weeks = daysSinceCreation / 7
            val remainingDays = daysSinceCreation % 7
            (weeks * daysPerWeek) + minOf(daysPerWeek, remainingDays)
        }
    }
}
```

3. **Weekly Progress Calculation:**
```kotlin
private fun getWeeklyDaysForHabit(habit: Habit): List<Pair<String, DayStatus>> {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(calendar.time)
    val habitCreatedDate = dateFormat.format(Date(habit.createdAt))
    
    // Start from last Sunday
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    
    val weeklyDays = mutableListOf<Pair<String, DayStatus>>()
    
    for (i in 0..6) {
        val dateStr = dateFormat.format(calendar.time)
        val isCompleted = habit.completedDates.contains(dateStr)
        val shouldDoHabit = shouldHabitBeDoneOnDay(habit, calendar)
        val isFuture = dateStr > today
        val isToday = dateStr == today
        
        val status = when {
            dateStr < habitCreatedDate -> DayStatus.NOT_HABIT_DAY
            isCompleted -> DayStatus.COMPLETED
            !shouldDoHabit -> DayStatus.NOT_HABIT_DAY
            isFuture -> DayStatus.NOT_HABIT_DAY
            isToday -> DayStatus.TODAY_PENDING
            else -> DayStatus.MISSED
        }
        
        weeklyDays.add(dateStr to status)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    
    return weeklyDays
}

private fun shouldHabitBeDoneOnDay(habit: Habit, calendar: Calendar): Boolean {
    return when {
        habit.frequency.contains("Daily") -> true
        else -> {
            val dayName = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.LONG,
                Locale.getDefault()
            )
            habit.frequency.contains(dayName)
        }
    }
}

enum class DayStatus {
    COMPLETED,       // Green - habit completed
    MISSED,          // Red - habit scheduled but not done
    NOT_HABIT_DAY,   // Gray - not scheduled
    HABIT_DAY,       // Blue outline - scheduled but not done
    TODAY_PENDING    // Yellow - today, not yet completed
}
```

**Process Flow - Weekly Chart Data:**

```kotlin
private fun calculateWeeklyChartDataInternal(habits: List<Habit>): List<WeeklyChartData> {
    val calendar = Calendar.getInstance()
    val weekData = mutableListOf<WeeklyChartData>()
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Start from last Sunday
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    
    for (i in 0..6) {
        val dateStr = dateFormat.format(calendar.time)
        val dayName = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        
        // Count completions for this day
        var completionCount = 0
        var totalHabitsForDay = 0
        
        habits.forEach { habit ->
            val habitCreatedDate = dateFormat.format(Date(habit.createdAt))
            if (shouldHabitBeDoneOnDay(habit, calendar) && dateStr >= habitCreatedDate) {
                totalHabitsForDay++
                if (habit.completedDates.contains(dateStr)) {
                    completionCount++
                }
            }
        }
        
        val completionRate = if (totalHabitsForDay > 0) {
            (completionCount.toFloat() / totalHabitsForDay) * 100f
        } else {
            0f
        }
        
        weekData.add(
            WeeklyChartData(
                dayName = dayName,
                completionCount = completionCount,
                totalHabits = totalHabitsForDay,
                completionRate = completionRate
            )
        )
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    
    return weekData
}
```

**Output Flow - UI Updates:**

1. **Habit Score Display:**
```kotlin
// StatisticsTabFragment.kt
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.todayCompletionRate.collect { rate ->
        val rateInt = rate.toInt()
        binding.tvHabitScore.text = getString(R.string.percentage_format, rateInt)
        binding.progressCircle.progress = rateInt
    }
}
```

2. **Weekly Chart Visualization:**
```kotlin
private fun updateWeeklyChart(weekData: List<WeeklyChartData>) {
    if (weekData.isEmpty()) return
    
    val maxCompletionCount = weekData.maxOfOrNull { it.completionCount } ?: 0
    val yAxisMax = calculateYAxisMax(maxCompletionCount)
    updateYAxisLabels(yAxisMax)
    
    val bars = listOf(
        binding.barSun, binding.barMon, binding.barTue,
        binding.barWed, binding.barThu, binding.barFri, binding.barSat
    )
    
    val maxHeightDp = 180
    val density = resources.displayMetrics.density
    
    weekData.forEachIndexed { index, data ->
        if (index < bars.size) {
            val bar = bars[index]
            
            // Calculate height based on completion count
            val heightDp = if (data.completionCount > 0 && yAxisMax > 0) {
                ((data.completionCount.toFloat() / yAxisMax) * maxHeightDp).coerceAtLeast(10f)
            } else {
                10f
            }
            
            val heightPx = (heightDp * density).toInt()
            val layoutParams = bar.layoutParams
            layoutParams.height = heightPx
            bar.layoutParams = layoutParams
            
            // Update alpha based on completion rate
            val alpha = if (data.totalHabits > 0) {
                (0.4f + (data.completionRate / 100f) * 0.6f).coerceIn(0.4f, 1.0f)
            } else {
                0.3f
            }
            bar.alpha = alpha
        }
    }
}
```

3. **Interactive Tooltips:**
```kotlin
private fun showTooltip(anchorView: View, data: WeeklyChartData) {
    val tooltipView = LayoutInflater.from(requireContext())
        .inflate(R.layout.tooltip_bar_chart, binding.tooltipContainer, false)
    
    val tvCompleted = tooltipView.findViewById<TextView>(R.id.tvTooltipCompleted)
    val tvCompletionRate = tooltipView.findViewById<TextView>(R.id.tvTooltipCompletionRate)
    
    tvCompleted.text = getString(
        R.string.completed_habits_format,
        data.completionCount,
        data.totalHabits
    )
    tvCompletionRate.text = getString(
        R.string.completion_rate_format,
        data.completionRate.toInt()
    )
    
    // Position tooltip above the bar
    val anchorLocation = IntArray(2)
    anchorView.getLocationInWindow(anchorLocation)
    
    val tooltipParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    tooltipParams.gravity = Gravity.TOP or Gravity.START
    tooltipParams.leftMargin = anchorLocation[0] - (tooltipView.measuredWidth / 2)
    tooltipParams.topMargin = anchorLocation[1] - tooltipView.measuredHeight - 16
    
    binding.tooltipContainer.removeAllViews()
    binding.tooltipContainer.addView(tooltipView, tooltipParams)
    binding.tooltipContainer.visibility = View.VISIBLE
    
    currentTooltipView = tooltipView
}
```

**Streak Calculation:**

```kotlin
private fun calculateCurrentStreak(completionDates: List<String>): Int {
    if (completionDates.isEmpty()) return 0
    
    val today = DateUtils.getCurrentDateString()
    val yesterday = DateUtils.getYesterdayDateString()
    
    // Streak broken if not completed today or yesterday
    if (!completionDates.contains(today) && !completionDates.contains(yesterday)) {
        return 0
    }
    
    var streak = 0
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    while (true) {
        val dateStr = dateFormat.format(calendar.time)
        if (completionDates.contains(dateStr)) {
            streak++
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            break
        }
    }
    
    return streak
}

private fun calculateBestStreak(completionDates: List<String>): Int {
    if (completionDates.isEmpty()) return 0
    
    val sortedDates = completionDates.sorted()
    var maxStreak = 1
    var currentStreak = 1
    
    for (i in 1 until sortedDates.size) {
        val prevDate = Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(sortedDates[i - 1])!!
        }
        val currDate = Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(sortedDates[i])!!
        }
        
        val daysDiff = ((currDate.timeInMillis - prevDate.timeInMillis) / 
            (1000 * 60 * 60 * 24)).toInt()
        
        if (daysDiff == 1) {
            currentStreak++
            maxStreak = maxOf(maxStreak, currentStreak)
        } else {
            currentStreak = 1
        }
    }
    
    return maxStreak
}
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Continued Technical Implementation

### Challenge System Architecture

#### Technical Flow

```
Challenge Creation → Pending Status → Community Voting → Auto-Approval (10 votes)
                                                ↓
                                         Challenge Habits
                                                ↓
                                      Completion & Rewards
```

#### Challenge Creation Process

**Input Flow:**
```kotlin
// ChallengeCreateActivity.kt
private fun validateAndCreateChallenge() {
    val title = binding.etChallengeTitle.text.toString().trim()
    val description = binding.etChallengeDescription.text.toString().trim()
    val detail = binding.etChallengeDetail.text.toString().trim()
    val keyResults = binding.etKeyResults.text.toString().trim()
    val rewardPointsStr = binding.etRewardPoints.text.toString().trim()
    
    // Validation
    if (title.length < 5) {
        showError("Title must be at least 5 characters")
        return
    }
    
    if (description.isEmpty()) {
        showError("Please add a description")
        return
    }
    
    val rewardPoints = try {
        rewardPointsStr.toInt()
    } catch (e: NumberFormatException) {
        showError("Invalid reward points")
        return
    }
    
    // Get selected duration
    val duration = when (binding.rgDuration.checkedRadioButtonId) {
        R.id.rbSevenDays -> ChallengeDuration.SEVEN_DAYS
        R.id.rbThirtyDays -> ChallengeDuration.THIRTY_DAYS
        R.id.rbHundredDays -> ChallengeDuration.HUNDRED_DAYS
        else -> ChallengeDuration.SEVEN_DAYS
    }
    
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    if (currentUserId == null) {
        showError("Please login first")
        return
    }
    
    createChallengeInFirestore(title, description, detail, keyResults, 
        duration, rewardPoints, currentUserId)
}
```

**Process Flow - Image Upload:**
```kotlin
private suspend fun uploadChallengeImage(uri: Uri): String? {
    return try {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("challenges/${UUID.randomUUID()}.jpg")
        
        imageRef.putFile(uri).await()
        val downloadUrl = imageRef.downloadUrl.await()
        downloadUrl.toString()
    } catch (e: Exception) {
        null
    }
}
```

**Process Flow - Firestore Creation:**
```kotlin
private fun createChallengeInFirestore(
    title: String,
    description: String,
    detail: String,
    keyResults: String,
    duration: ChallengeDuration,
    reward: Int,
    creatorId: String
) {
    lifecycleScope.launch {
        try {
            // Upload image if selected
            val imageUrl = if (selectedImageUri != null) {
                uploadChallengeImage(selectedImageUri!!)
            } else {
                null
            }
            
            // Create challenge object
            val challenge = Challenge(
                title = title,
                description = description,
                detail = detail,
                keyResults = keyResults,
                imgURL = imageUrl ?: "",
                duration = duration,
                reward = reward,
                creatorId = creatorId,
                createdAt = System.currentTimeMillis(),
                participantCount = 0,
                status = ChallengeStatus.PENDING,
                votes = 0,
                votedBy = emptyList()
            )
            
            // Save to Firestore
            val challengeId = ChallengeRepository().createChallenge(challenge)
            
            if (challengeId != null) {
                Toast.makeText(this@ChallengeCreateActivity, 
                    "Challenge created! Share it to get votes", 
                    Toast.LENGTH_LONG).show()
                finish()
            } else {
                showError("Failed to create challenge")
            }
        } catch (e: Exception) {
            showError("Error: ${e.message}")
        }
    }
}
```

**Data Model:**
```kotlin
data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val detail: String = "",
    val keyResults: String = "",
    val imgURL: String = "",
    val duration: ChallengeDuration = ChallengeDuration.SEVEN_DAYS,
    val reward: Int = 0,
    val creatorId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val participantCount: Int = 0,
    val status: ChallengeStatus = ChallengeStatus.PENDING,
    val votes: Int = 0,
    val votedBy: List<String> = emptyList()
)

enum class ChallengeStatus {
    PENDING,   // Waiting for 10 votes
    APPROVED,  // Available for joining
    REJECTED   // Not shown
}

enum class ChallengeDuration(val duration: String, val color: BadgeColor, val days: Int) {
    SEVEN_DAYS("7 Days Challenge", BadgeColor.CYAN, 7),
    THIRTY_DAYS("30 Days Challenge", BadgeColor.GREEN, 30),
    HUNDRED_DAYS("100 Days Challenge", BadgeColor.YELLOW, 100)
}
```

#### Voting System

**Input Flow:**
```kotlin
// FeedFragment.kt - User clicks vote button on challenge post
postAdapter = PostAdapter(
    currentUserId = currentUserId,
    onVoteClick = { post ->
        if (post.isChallengePost) {
            voteForChallenge(post)
        }
    }
)
```

**Process Flow - Vote Registration:**
```kotlin
private fun voteForChallenge(post: Post) {
    lifecycleScope.launch {
        val currentUserId = UserPreferences.getUserId(requireContext())
        
        // Check if already voted
        if (post.votedBy.contains(currentUserId)) {
            Toast.makeText(requireContext(), 
                "You have already voted for this challenge", 
                Toast.LENGTH_SHORT).show()
            return@launch
        }
        
        // Get challenge ID from post
        val challengeId = post.challengeId ?: return@launch
        
        // Vote via repository
        val success = ChallengeRepository().voteForChallenge(challengeId, currentUserId)
        
        if (success) {
            Toast.makeText(requireContext(), 
                "Vote recorded!", 
                Toast.LENGTH_SHORT).show()
            
            // Refresh feed to show updated vote count
            viewModel.fetchPosts(isRefresh = true)
        } else {
            Toast.makeText(requireContext(), 
                "Failed to vote", 
                Toast.LENGTH_SHORT).show()
        }
    }
}
```

**Process Flow - Repository Vote Logic:**
```kotlin
// ChallengeRepository.kt
suspend fun voteForChallenge(challengeId: String, userId: String): Boolean {
    return try {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        
        // Get current challenge
        val challenge = getChallengeById(challengeId) ?: return false
        
        // Check if already voted
        if (challenge.votedBy.contains(userId)) {
            return false
        }
        
        // Update all posts sharing this challenge
        try {
            val postsSnapshot = db.collection("posts")
                .whereEqualTo("challengeId", challengeId)
                .get()
                .await()
            
            postsSnapshot.documents.forEach { doc ->
                batch.update(doc.reference, "voteCount", FieldValue.increment(1))
                batch.update(doc.reference, "votedBy", FieldValue.arrayUnion(userId))
            }
        } catch (e: Exception) {
            println("Error syncing posts: ${e.message}")
        }
        
        // Update challenge
        val newVoteCount = challenge.votedBy.size + 1
        val challengeRef = db.collection("challenges").document(challengeId)
        val challengeUpdates = mutableMapOf<String, Any>(
            "votes" to FieldValue.increment(1),
            "votedBy" to FieldValue.arrayUnion(userId)
        )
        
        // Auto-approve if reached 10 votes
        if (newVoteCount >= 10 && challenge.status == ChallengeStatus.PENDING) {
            challengeUpdates["status"] = ChallengeStatus.APPROVED.name
        }
        
        batch.update(challengeRef, challengeUpdates)
        batch.commit().await()
        true
    } catch (e: Exception) {
        println("Error voting: ${e.message}")
        false
    }
}
```

#### Challenge Joining Process

**Input Flow:**
```kotlin
// ChallengeDetailActivity.kt
binding.btnJoinNow.setOnClickListener {
    joinChallenge()
}
```

**Process Flow - Join Challenge:**
```kotlin
private fun joinChallenge() {
    lifecycleScope.launch {
        val currentUserId = auth.currentUser?.uid
        val challengeId = challenge?.id
        
        if (currentUserId != null && challengeId != null && challenge != null) {
            try {
                // 1. Create UserChallenge relationship
                val joinSuccess = userChallengeRepository.joinChallenge(
                    currentUserId, 
                    challengeId
                )
                
                if (joinSuccess) {
                    // 2. Create special challenge habit
                    val challengeHabit = Habit(
                        userId = currentUserId,
                        name = challenge!!.title,
                        quantity = 1,
                        unit = "times",
                        frequency = listOf("Daily"),
                        isChallengeHabit = true,
                        challengeId = challengeId,
                        challengeImageUrl = challenge!!.imgURL,
                        challengeDescription = challenge!!.detail,
                        challengeDurationDays = challenge!!.duration.days
                    )
                    HabitRepository.getInstance().addHabit(challengeHabit)
                    
                    // 3. Update participant count
                    challengeRepository.updateParticipantCount(challengeId)
                    
                    // 4. Update user's joined challenges
                    userRepository.addJoinedChallenge(currentUserId, challengeId)
                    
                    isUserJoined = true
                    updateJoinButton()
                    Toast.makeText(this@ChallengeDetailActivity,
                        "Successfully joined! Check your habits.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChallengeDetailActivity,
                        "Failed to join challenge",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChallengeDetailActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

**Data Models:**
```kotlin
data class UserChallenge(
    val id: String = "", // "userId_challengeId"
    val userId: String = "",
    val challengeId: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val progress: Int = 0, // 0-100
    val status: UserChallengeStatus = UserChallengeStatus.ONGOING
)

enum class UserChallengeStatus {
    ONGOING,    // Currently participating
    COMPLETED,  // Finished successfully
    ABANDONED   // Gave up or expired
}
```

**Output Flow - Challenge Habit:**
```kotlin
// The challenge habit appears in the user's dashboard
// It's automatically filtered to expire after the duration
val habitsList = habitRepository.getHabitsForUser(userId)
val activeHabits = habitsList.filter { habit ->
    if (!habit.isChallengeHabit) {
        true
    } else if (habit.challengeDurationDays != null) {
        val durationMillis = habit.challengeDurationDays * 24 * 60 * 60 * 1000L
        val currentTime = System.currentTimeMillis()
        val expiryTime = habit.createdAt + durationMillis
        currentTime <= expiryTime
    } else {
        true
    }
}
```

### Social Feed System

#### Technical Architecture

```
Post Creation → Image Upload (optional) → Firestore Document
                                                ↓
                                    Real-time Listener
                                                ↓
                                        Feed Updates
                                                ↓
                        Engagement (Like/Comment/Share)
                                                ↓
                                        Notifications
```

#### Post Creation Flow

**Input Flow:**
```kotlin
// CreatePostActivity.kt
private fun createPost() {
    val content = binding.etPostContent.text.toString().trim()
    
    if (content.isEmpty()) {
        showError("Please write something")
        return
    }
    
    lifecycleScope.launch {
        try {
            _isCreating.value = true
            
            // Upload image if selected
            val imageUrl = if (selectedImageUri != null) {
                uploadImage(selectedImageUri!!)
            } else {
                null
            }
            
            // Get user info
            val userId = UserPreferences.getUserId(this@CreatePostActivity)
            val userName = UserPreferences.getUserName(this@CreatePostActivity)
            val userAvatar = UserPreferences.getUserAvatar(this@CreatePostActivity)
            
            // Create post object
            val post = Post(
                userId = userId,
                authorName = userName,
                authorAvatarUrl = userAvatar,
                content = content,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis(),
                likeCount = 0,
                commentCount = 0,
                likedBy = emptyList()
            )
            
            // Save to repository
            val postId = PostRepository.getInstance().createPost(post)
            
            if (postId != null) {
                val resultIntent = Intent().apply {
                    putExtra("new_post_created", true)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                showError("Failed to create post")
            }
        } catch (e: Exception) {
            showError("Error: ${e.message}")
        } finally {
            _isCreating.value = false
        }
    }
}
```

**Process Flow - Image Upload:**
```kotlin
private suspend fun uploadImage(uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("posts/${UUID.randomUUID()}.jpg")
            
            // Compress image before upload
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()
            
            // Upload
            val uploadTask = imageRef.putBytes(data).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            null
        }
    }
}
```

**Process Flow - Firestore Creation:**
```kotlin
// PostRepository.kt
suspend fun createPost(post: Post): String? {
    return try {
        val postId = FirestoreManager.addDocument("posts", post.toMap())
        postId
    } catch (e: Exception) {
        println("Error creating post: ${e.message}")
        null
    }
}
```

**Data Model:**
```kotlin
data class Post(
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isChallengePost: Boolean = false,
    val challengeId: String? = null,
    val voteCount: Int = 0,
    val votedBy: List<String> = emptyList(),
    // For shared posts
    val isSharedPost: Boolean = false,
    val originalPostId: String? = null,
    val originalUserId: String? = null,
    val originalAuthorName: String? = null,
    val originalAuthorAvatarUrl: String? = null,
    val originalContent: String? = null,
    val originalImageUrl: String? = null
)
```

#### Real-time Feed Updates

**Process Flow - Firestore Listener:**
```kotlin
// PostRepository.kt
fun listenToPosts(limit: Long): Flow<List<Post>> = callbackFlow {
    val query = db.collection("posts")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(limit)
    
    val subscription = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.e("PostRepository", "Listen error: ${error.message}")
            trySend(emptyList())
            return@addSnapshotListener
        }
        
        if (snapshot != null) {
            val posts = snapshot.documents.mapNotNull { Post.fromDocument(it) }
            trySend(posts)
        }
    }
    
    awaitClose {
        subscription.remove()
    }
}
```

**ViewModel Integration:**
```kotlin
// FeedViewModel.kt
private val _posts = MutableStateFlow<List<Post>>(emptyList())
val posts: StateFlow<List<Post>> = _posts.asStateFlow()

private var currentLimit = 10L

fun fetchPosts(isRefresh: Boolean = false) {
    if (isRefresh) {
        currentLimit = 10L
        _posts.value = emptyList()
    }
    
    viewModelScope.launch {
        _isLoading.value = true
        try {
            postRepository.listenToPosts(currentLimit).collect { postsList ->
                _posts.value = postsList
                hasMoreData = postsList.size >= currentLimit
            }
        } catch (e: Exception) {
            _error.emit("Error loading posts: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}

fun loadMorePosts() {
    if (!_isLoading.value && hasMoreData) {
        currentLimit += 10
        fetchPosts(isRefresh = false)
    }
}
```

**UI Observation:**
```kotlin
// FeedFragment.kt
private fun observeViewModel() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.posts.collectLatest { posts ->
            postAdapter.submitList(posts)
            swipeRefreshFeed.isRefreshing = false
        }
    }
}

private fun setupRecyclerView(view: View) {
    rvFeed = view.findViewById(R.id.rvFeed)
    val linearLayoutManager = LinearLayoutManager(context)
    
    rvFeed.apply {
        layoutManager = linearLayoutManager
        adapter = postAdapter
        
        // Infinite scroll
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val totalItemCount = linearLayoutManager.itemCount
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                
                if (!viewModel.isLoading.value && viewModel.hasMoreData &&
                    totalItemCount <= (lastVisibleItem + 2)) {
                    viewModel.loadMorePosts()
                }
            }
        })
    }
}
```

#### Like System

**Input Flow:**
```kotlin
// PostAdapter.kt - User clicks like button
holder.btnLike.setOnClickListener {
    onLikeClick(post)
}
```

**Process Flow - Toggle Like:**
```kotlin
// FeedFragment.kt
private fun toggleLike(post: Post) {
    lifecycleScope.launch {
        val currentUserId = UserPreferences.getUserId(requireContext())
        val currentUserName = UserPreferences.getUserName(requireContext())
        val currentUserAvatar = UserPreferences.getUserAvatar(requireContext())
        
        val success = PostRepository.getInstance().toggleLike(
            post.id,
            currentUserId,
            currentUserName,
            currentUserAvatar
        )
        
        if (!success) {
            Toast.makeText(requireContext(), "Failed to update like", Toast.LENGTH_SHORT).show()
        }
        // Feed automatically updates via real-time listener
    }
}
```

**Process Flow - Repository Like Logic:**
```kotlin
// PostRepository.kt
suspend fun toggleLike(
    postId: String,
    userId: String,
    userName: String,
    userAvatar: String
): Boolean {
    return try {
        val post = getPostById(postId) ?: return false
        
        val isLiked = post.likedBy.contains(userId)
        val updates = if (isLiked) {
            // Unlike
            mapOf(
                "likeCount" to FieldValue.increment(-1),
                "likedBy" to FieldValue.arrayRemove(userId)
            )
        } else {
            // Like
            mapOf(
                "likeCount" to FieldValue.increment(1),
                "likedBy" to FieldValue.arrayUnion(userId)
            )
        }
        
        // Update Firestore
        val success = FirestoreManager.updateDocument("posts", postId, updates)
        
        // Send notification only on like (not unlike)
        if (success && !isLiked && post.userId != userId) {
            sendNotification(
                recipientId = post.userId,
                senderId = userId,
                senderName = userName,
                senderAvatar = userAvatar,
                postId = postId,
                type = Notification.NotificationType.LIKE_POST
            )
        }
        
        success
    } catch (e: Exception) {
        println("Error toggling like: ${e.message}")
        false
    }
}
```

#### Comment System

**Input Flow:**
```kotlin
// CommentsActivity.kt
binding.btnSendComment.setOnClickListener {
    val commentText = binding.etComment.text.toString().trim()
    if (commentText.isNotEmpty()) {
        postComment(commentText)
        binding.etComment.text?.clear()
    }
}
```

**Process Flow - Create Comment:**
```kotlin
private fun postComment(commentText: String) {
    lifecycleScope.launch {
        try {
            val userId = UserPreferences.getUserId(this@CommentsActivity)
            val userName = UserPreferences.getUserName(this@CommentsActivity)
            val userAvatar = UserPreferences.getUserAvatar(this@CommentsActivity)
            
            val comment = Comment(
                postId = postId,
                userId = userId,
                authorName = userName,
                authorAvatarUrl = userAvatar,
                content = commentText,
                timestamp = System.currentTimeMillis(),
                likeCount = 0,
                likedBy = emptyList(),
                replyCount = 0,
                parentCommentId = null
            )
            
            val commentId = PostRepository.getInstance().addComment(comment)
            
            if (commentId != null) {
                // Update comment count
                currentCommentCount++
                
                // Refresh comments list
                loadComments()
            } else {
                showError("Failed to post comment")
            }
        } catch (e: Exception) {
            showError("Error: ${e.message}")
        }
    }
}
```

**Process Flow - Repository Comment Logic:**
```kotlin
// PostRepository.kt
suspend fun addComment(comment: Comment): String? {
    return try {
        // Add comment to Firestore
        val commentId = FirestoreManager.addDocument("comments", comment.toMap())
        
        if (commentId != null) {
            // Update post comment count
            FirestoreManager.updateDocument(
                "posts",
                comment.postId,
                mapOf("commentCount" to FieldValue.increment(1))
            )
            
            // Get post to send notification
            val post = getPostById(comment.postId)
            if (post != null && post.userId != comment.userId) {
                sendNotification(
                    recipientId = post.userId,
                    senderId = comment.userId,
                    senderName = comment.authorName,
                    senderAvatar = comment.authorAvatarUrl,
                    postId = comment.postId,
                    commentId = commentId,
                    type = Notification.NotificationType.COMMENT_POST
                )
            }
        }
        
        commentId
    } catch (e: Exception) {
        println("Error adding comment: ${e.message}")
        null
    }
}
```

**Data Model:**
```kotlin
data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val replyCount: Int = 0,
    val parentCommentId: String? = null // null for top-level comments
)
```

### Notification System

#### Technical Architecture

```
User Action → Repository Notification Creation → Firestore
                                                      ↓
                                            Real-time Listener
                                                      ↓
                                          Notification ViewModel
                                                      ↓
                                          UI Update + FCM Push
```

#### Notification Creation Flow

**Process Flow - Create Notification:**
```kotlin
// PostRepository.kt
private suspend fun sendNotification(
    recipientId: String,
    senderId: String,
    senderName: String,
    senderAvatar: String,
    postId: String,
    commentId: String? = null,
    type: Notification.NotificationType
) {
    try {
        val notification = Notification(
            recipientId = recipientId,
            senderId = senderId,
            senderName = senderName,
            senderAvatarUrl = senderAvatar,
            postId = postId,
            commentId = commentId,
            type = type,
            read = false,
            timestamp = System.currentTimeMillis()
        )
        
        // Save to Firestore
        val result = notificationRepository.sendNotification(notification)
        
        if (result.isSuccess) {
            Log.d("PostRepository", "Notification saved")
            
            // Send FCM push notification
            val recipientUser = userRepository.getUserById(recipientId)
            val fcmToken = recipientUser?.fcmToken
            
            if (!fcmToken.isNullOrEmpty()) {
                val title = when (type) {
                    Notification.NotificationType.COMMENT_POST -> "New Comment"
                    Notification.NotificationType.LIKE_POST -> "New Like"
                    Notification.NotificationType.SHARE_POST -> "Post Shared"
                    else -> "Notification"
                }
                
                val body = when (type) {
                    Notification.NotificationType.COMMENT_POST -> 
                        "$senderName commented on your post"
                    Notification.NotificationType.LIKE_POST -> 
                        "$senderName liked your post"
                    Notification.NotificationType.SHARE_POST -> 
                        "$senderName shared your post"
                    else -> "New notification"
                }
                
                NotificationSender.sendNotification(
                    toToken = fcmToken,
                    title = title,
                    body = body,
                    data = mapOf("postId" to postId)
                )
            }
        }
    } catch (e: Exception) {
        Log.e("PostRepository", "Notification error: ${e.message}")
    }
}
```

**Data Model:**
```kotlin
data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatarUrl: String = "",
    val type: NotificationType = NotificationType.LIKE_POST,
    val postId: String? = null,
    val commentId: String? = null,
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class NotificationType {
        LIKE_POST,
        COMMENT_POST,
        SHARE_POST,
        LIKE_COMMENT,
        DISLIKE_COMMENT,
        REPLY_COMMENT,
        FRIEND_REQUEST,
        FRIEND_REQUEST_ACCEPTED
    }
}
```

#### Real-time Notification Listening

**Process Flow - Firestore Listener:**
```kotlin
// NotificationRepository.kt
fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
    val query = db.collection("notifications")
        .whereEqualTo("recipientId", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(50)
    
    val subscription = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
            trySend(emptyList())
            return@addSnapshotListener
        }
        
        if (snapshot != null) {
            val notifications = snapshot.documents.mapNotNull { 
                Notification.fromDocument(it)
            }
            trySend(notifications)
        }
    }
    
    awaitClose {
        subscription.remove()
    }
}

// Listen only for NEW unread notifications
fun getNewNotifications(userId: String): Flow<Notification> = callbackFlow {
    val query = db.collection("notifications")
        .whereEqualTo("recipientId", userId)
        .whereEqualTo("read", false)
        .orderBy("timestamp", Query.Direction.DESCENDING)
    
    val subscription = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
            return@addSnapshotListener
        }
        
        snapshot?.documentChanges?.forEach { change ->
            if (change.type == DocumentChange.Type.ADDED) {
                val notification = Notification.fromDocument(change.document)
                notification?.let { trySend(it) }
            }
        }
    }
    
    awaitClose {
        subscription.remove()
    }
}
```

**ViewModel Integration:**
```kotlin
// NotificationViewModel.kt
class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationRepository = NotificationRepository.getInstance()
    private val firestoreUserRepository = FirestoreUserRepository.getInstance()
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    init {
        observeUser()
    }
    
    private fun observeUser() {
        viewModelScope.launch {
            firestoreUserRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    loadNotifications(user.id)
                } else {
                    _notifications.value = emptyList()
                    _unreadCount.value = 0
                }
            }
        }
    }
    
    private suspend fun loadNotifications(userId: String) {
        try {
            notificationRepository.getNotifications(userId).collect { list ->
                _notifications.value = list
                _unreadCount.value = list.count { !it.read }
            }
        } catch (e: Exception) {
            Log.e("NotificationViewModel", "Error: ${e.message}")
        }
    }
    
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            val unreadNotifications = _notifications.value.filter { !it.read }
            unreadNotifications.forEach { notification ->
                notificationRepository.markAsRead(notification.id)
            }
        }
    }
}
```

#### In-App Notification Display

**MainActivity Integration:**
```kotlin
// MainActivity.kt
private fun setupNotificationListener() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            FirestoreUserRepository.getInstance().currentUser.collectLatest { user ->
                if (user != null) {
                    NotificationRepository.getInstance()
                        .getNewNotifications(user.id)
                        .collect { notification ->
                            if (UserPreferences.areNotificationsEnabled(this@MainActivity)) {
                                showInAppNotification(notification)
                            }
                        }
                }
            }
        }
    }
}

private fun showInAppNotification(notification: Notification) {
    try {
        val text = when (notification.type) {
            Notification.NotificationType.LIKE_POST -> 
                "${notification.senderName} liked your post"
            Notification.NotificationType.COMMENT_POST -> 
                "${notification.senderName} commented on your post"
            Notification.NotificationType.FRIEND_REQUEST -> 
                "${notification.senderName} sent you a friend request"
            Notification.NotificationType.FRIEND_REQUEST_ACCEPTED -> 
                "${notification.senderName} accepted your friend request"
            else -> "New notification"
        }
        
        triggerSystemNotification("Habit Tracker", text, notification.postId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun triggerSystemNotification(title: String, messageBody: String, postId: String? = null) {
    val intent = Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (postId != null) {
            putExtra("postId", postId)
        }
    }
    
    val pendingIntent = PendingIntent.getActivity(
        this, System.currentTimeMillis().toInt(), intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    
    val channelId = NotificationHelper.DEFAULT_CHANNEL_ID
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    
    val notificationBuilder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(messageBody)
        .setAutoCancel(true)
        .setSound(defaultSoundUri)
        .setContentIntent(pendingIntent)
    
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
}
```

#### FCM Push Notifications

**Process Flow - FCM Integration:**
```kotlin
// MyFirebaseMessagingService.kt
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${remoteMessage.data}")
        }
        
        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Body: ${it.body}")
            sendNotification(it.title, it.body, remoteMessage.data)
        }
    }
    
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }
    
    private fun sendRegistrationToServer(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                FirestoreUserRepository.getInstance()
                    .updateFcmToken(currentUser.uid, token)
            }
        }
    }
    
    private fun sendNotification(
        title: String?,
        messageBody: String?,
        data: Map<String, String>? = null
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        
        data?.forEach { (key, value) ->
            intent.putExtra(key, value)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )
        
        val channelId = NotificationHelper.DEFAULT_CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title ?: "Habit Tracker")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        NotificationHelper.createNotificationChannels(this)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
```

**FCM Sender (Server-side through Retrofit):**
```kotlin
// NotificationSender.kt
object NotificationSender {
    private const val BASE_URL = "https://fcm.googleapis.com/"
    private const val SERVER_KEY = "key=YOUR_LEGACY_SERVER_KEY"
    
    private val api: FcmApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmApi::class.java)
    }
    
    suspend fun sendNotification(
        toToken: String,
        title: String,
        body: String,
        data: Map<String, String>? = null
    ) {
        try {
            val request = FcmRequest(
                to = toToken,
                notification = NotificationPayload(title, body),
                data = data
            )
            val response = api.sendNotification(SERVER_KEY, request = request)
            if (response.isSuccessful) {
                Log.d("NotificationSender", "Success: ${response.body()}")
            } else {
                Log.e("NotificationSender", "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("NotificationSender", "Exception: ${e.message}")
        }
    }
}

interface FcmApi {
    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: FcmRequest
    ): Response<FcmResponse>
}

data class FcmRequest(
    @SerializedName("to") val to: String,
    @SerializedName("notification") val notification: NotificationPayload,
    @SerializedName("data") val data: Map<String, String>? = null
)

data class NotificationPayload(
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
```

### Category Management System

#### Technical Flow

```
Category Selection → Visual Customization → Firestore Storage
                                                  ↓
                                        Habit Assignment
                                                  ↓
                                        Filtering & Grouping
```

#### Category Creation

**Input Flow:**
```kotlin
// CreateCategoryActivity.kt
binding.btnCreateCategory.setOnClickListener {
    val categoryName = binding.etCategoryName.text.toString().trim()
    
    if (categoryName.isEmpty()) {
        showError("Category name cannot be empty")
        return@setOnClickListener
    }
    
    createCategory(categoryName)
}
```

**Process Flow - Create Category:**
```kotlin
private fun createCategory(name: String) {
    lifecycleScope.launch {
        try {
            val userId = UserPreferences.getUserId(this@CreateCategoryActivity)
            
            // User selects icon and color from pickers
            val selectedIcon = selectedIcon ?: CategoryIcon.DEFAULT
            val selectedColor = selectedColor ?: CategoryColor.PINK
            
            val category = Category(
                id = "",
                name = name,
                icon = selectedIcon,
                color = selectedColor,
                userId = userId,
                createdAt = System.currentTimeMillis(),
                habitCount = 0
            )
            
            val categoryId = CategoryRepository.getInstance().createCategory(category)
            
            if (categoryId != null) {
                Toast.makeText(
                    this@CreateCategoryActivity,
                    "Category created",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                showError("Failed to create category")
            }
        } catch (e: Exception) {
            showError("Error: ${e.message}")
        }
    }
}
```

**Data Model:**
```kotlin
data class Category(
    val id: String = "",
    val name: String = "",
    val icon: CategoryIcon = CategoryIcon.DEFAULT,
    val color: CategoryColor = CategoryColor.PINK,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val habitCount: Int = 0
)

enum class CategoryIcon(val resId: Int) {
    DEFAULT(R.drawable.ic_other),
    FITNESS(R.drawable.ic_fitness),
    HEALTH(R.drawable.ic_health),
    WORK(R.drawable.ic_work),
    READING(R.drawable.ic_reading),
    MEDITATION(R.drawable.ic_meditation),
    CODING(R.drawable.ic_coding),
    LEARNING(R.drawable.ic_learning)
}

enum class CategoryColor(val resId: Int) {
    PINK(R.drawable.bg_category_icon_pink_light),
    BLUE(R.drawable.bg_category_icon_blue),
    GREEN(R.drawable.bg_category_icon_green),
    ORANGE(R.drawable.bg_category_icon_orange_light),
    INDIGO(R.drawable.bg_category_icon_indigo)
}
```

**Repository Logic:**
```kotlin
// CategoryRepository.kt
suspend fun createCategory(category: Category): String? {
    return try {
        val db = FirebaseFirestore.getInstance()
        val categoryMap = category.toMap()
        val docRef = db.collection("categories").add(categoryMap).await()
        docRef.id
    } catch (e: Exception) {
        println("Error creating category: ${e.message}")
        null
    }
}

suspend fun updateCategory(categoryId: String, updates: Map<String, Any>): Boolean {
    return try {
        val db = FirebaseFirestore.getInstance()
        db.collection("categories")
            .document(categoryId)
            .update(updates)
            .await()
        true
    } catch (e: Exception) {
        println("Error updating category: ${e.message}")
        false
    }
}

suspend fun deleteCategory(categoryId: String): Boolean {
    return try {
        val db = FirebaseFirestore.getInstance()
        
        // First, update all habits using this category to default
        val habits = db.collection("habits")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .await()
        
        habits.documents.forEach { doc ->
            doc.reference.update("categoryId", "default").await()
        }
        
        // Then delete the category
        db.collection("categories")
            .document(categoryId)
            .delete()
            .await()
        true
    } catch (e: Exception) {
        println("Error deleting category: ${e.message}")
        false
    }
}
```

#### Category-Based Filtering

**Process Flow - Filter Habits by Category:**
```kotlin
// HomeFragment.kt
private fun setupCategoryFilters() {
    lifecycleScope.launch {
        categoryViewModel.categories.collectLatest { categories ->
            // Add "All" category
            val allCategories = listOf(
                Category(id = "all", name = "All")
            ) + categories
            
            categoryFilterAdapter.submitList(allCategories)
        }
    }
}

private fun onCategorySelected(category: Category) {
    selectedCategoryId = category.id
    filterHabits()
}

private fun filterHabits() {
    lifecycleScope.launch {
        viewModel.habits.collect { allHabits ->
            val filtered = if (selectedCategoryId == "all") {
                allHabits
            } else {
                allHabits.filter { it.categoryId == selectedCategoryId }
            }
            habitAdapter.submitList(filtered)
        }
    }
}
```

### Profile & Gamification System

#### Technical Architecture

```
User Actions → Point Calculation → Ranking System → Badge Awards
                                          ↓
                                   Profile Display
                                          ↓
                                   Leaderboard Positioning
```

#### Points System

**Process Flow - Award Points:**
```kotlin
// CompletionRepository.kt
suspend fun markHabitAsCompleted(habitId: String, userId: String): Boolean {
    return try {
        // 1. Mark completion
        val completionResult = createCompletion(habitId, userId)
        
        if (completionResult) {
            // 2. Award points (10 points per completion)
            val pointsAwarded = 10
            FirestoreUserRepository.getInstance()
                .addPoints(userId, pointsAwarded)
            
            // 3. Update user stats
            FirestoreUserRepository.getInstance()
                .incrementHabitsCompleted(userId)
            
            // 4. Check for badges
            checkAndAwardBadges(userId)
            
            true
        } else {
            false
        }
    } catch (e: Exception) {
        println("Error completing habit: ${e.message}")
        false
    }
}
```

**Process Flow - Points Repository:**
```kotlin
// FirestoreUserRepository.kt
suspend fun addPoints(userId: String, points: Int) {
    try {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .update("points", FieldValue.increment(points.toLong()))
            .await()
        
        // Recalculate rank
        updateUserRank(userId)
    } catch (e: Exception) {
        println("Error adding points: ${e.message}")
    }
}

private suspend fun updateUserRank(userId: String) {
    try {
        val db = FirebaseFirestore.getInstance()
        
        // Get user's points
        val userDoc = db.collection("users").document(userId).get().await()
        val userPoints = userDoc.getLong("points") ?: 0
        
        // Count users with higher points
        val higherRankedUsers = db.collection("users")
            .whereGreaterThan("points", userPoints)
            .get()
            .await()
        
        val rank = higherRankedUsers.size() + 1
        
        // Update rank
        db.collection("users")
            .document(userId)
            .update("rank", rank)
            .await()
    } catch (e: Exception) {
        println("Error updating rank: ${e.message}")
    }
}
```

#### Badge System

**Badge Criteria:**
```kotlin
enum class BadgeType(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val criteria: (User) -> Boolean
) {
    FIRST_HABIT(
        "first_habit",
        "First Habit",
        "Create your first habit",
        R.drawable.badge_first_habit,
        { user -> user.habitsCreated >= 1 }
    ),
    FIVE_HABITS(
        "five_habits",
        "5 Habits",
        "Create 5 habits",
        R.drawable.badge_five_habits,
        { user -> user.habitsCreated >= 5 }
    ),
    TEN_COMPLETIONS(
        "ten_completions",
        "Dedicated",
        "Complete 10 habits",
        R.drawable.badge_ten_completions,
        { user -> user.habitsCompleted >= 10 }
    ),
    FIFTY_COMPLETIONS(
        "fifty_completions",
        "Committed",
        "Complete 50 habits",
        R.drawable.badge_fifty_completions,
        { user -> user.habitsCompleted >= 50 }
    ),
    SEVEN_DAY_STREAK(
        "seven_day_streak",
        "Week Warrior",
        "Maintain a 7-day streak",
        R.drawable.badge_seven_streak,
        { user -> user.currentStreak >= 7 }
    ),
    THIRTY_DAY_STREAK(
        "thirty_day_streak",
        "Month Master",
        "Maintain a 30-day streak",
        R.drawable.badge_thirty_streak,
        { user -> user.currentStreak >= 30 }
    ),
    HUNDRED_DAY_STREAK(
        "hundred_day_streak",
        "Centurion",
        "Maintain a 100-day streak",
        R.drawable.badge_hundred_streak,
        { user -> user.currentStreak >= 100 }
    ),
    SOCIAL_BUTTERFLY(
        "social_butterfly",
        "Social Butterfly",
        "Make 10 friends",
        R.drawable.badge_social,
        { user -> user.friendsList.size >= 10 }
    ),
    CHALLENGE_CREATOR(
        "challenge_creator",
        "Challenge Creator",
        "Create a challenge",
        R.drawable.badge_creator,
        { user -> user.challengesCreated >= 1 }
    ),
    CHALLENGE_COMPLETER(
        "challenge_completer",
        "Challenge Champion",
        "Complete a challenge",
        R.drawable.badge_champion,
        { user -> user.challengesCompleted >= 1 }
    )
}
```

**Process Flow - Check Badges:**
```kotlin
private suspend fun checkAndAwardBadges(userId: String) {
    try {
        val user = FirestoreUserRepository.getInstance().getUserById(userId) ?: return
        
        val currentBadges = user.badges.toMutableList()
        var newBadgesAwarded = false
        
        // Check each badge type
        BadgeType.values().forEach { badgeType ->
            // Skip if already has badge
            if (!currentBadges.contains(badgeType.id)) {
                // Check if criteria is met
                if (badgeType.criteria(user)) {
                    currentBadges.add(badgeType.id)
                    newBadgesAwarded = true
                    
                    // Send notification
                    sendBadgeNotification(userId, badgeType)
                }
            }
        }
        
        // Update user's badges if any were awarded
        if (newBadgesAwarded) {
            FirestoreUserRepository.getInstance()
                .updateBadges(userId, currentBadges)
        }
    } catch (e: Exception) {
        println("Error checking badges: ${e.message}")
    }
}

private suspend fun sendBadgeNotification(userId: String, badge: BadgeType) {
    // Send in-app notification
    val notification = Notification(
        recipientId = userId,
        senderId = "system",
        senderName = "Habit Tracker",
        senderAvatarUrl = "",
        type = Notification.NotificationType.BADGE_EARNED,
        badgeId = badge.id,
        timestamp = System.currentTimeMillis()
    )
    NotificationRepository.getInstance().sendNotification(notification)
}
```

#### Leaderboard System

**Process Flow - Fetch Leaderboard:**
```kotlin
// LeaderboardViewModel.kt
fun loadLeaderboard(limit: Int = 50) {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            val users = userRepository.getTopUsers(limit)
            _leaderboard.value = users
        } catch (e: Exception) {
            _error.emit("Error loading leaderboard: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}
```

**Repository Implementation:**
```kotlin
// FirestoreUserRepository.kt
suspend fun getTopUsers(limit: Int): List<User> {
    return try {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        
        snapshot.documents.mapNotNull { User.fromDocument(it) }
    } catch (e: Exception) {
        println("Error fetching leaderboard: ${e.message}")
        emptyList()
    }
}
```

**UI Display:**
```kotlin
// LeaderboardFragment.kt
private fun observeLeaderboard() {
    lifecycleScope.launch {
        viewModel.leaderboard.collectLatest { users ->
            leaderboardAdapter.submitList(users)
        }
    }
}

// LeaderboardAdapter.kt
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val user = getItem(position)
    val rank = position + 1
    
    holder.binding.apply {
        tvRank.text = rank.toString()
        tvUserName.text = user.name
        tvPoints.text = "${user.points} pts"
        
        // Show medal for top 3
        when (rank) {
            1 -> {
                ivMedal.visibility = View.VISIBLE
                ivMedal.setImageResource(R.drawable.ic_medal_gold)
                tvRank.setBackgroundResource(R.drawable.bg_badge_rank_1)
            }
            2 -> {
                ivMedal.visibility = View.VISIBLE
                ivMedal.setImageResource(R.drawable.ic_medal_silver)
                tvRank.setBackgroundResource(R.drawable.bg_badge_rank_2)
            }
            3 -> {
                ivMedal.visibility = View.VISIBLE
                ivMedal.setImageResource(R.drawable.ic_medal_bronze)
                tvRank.setBackgroundResource(R.drawable.bg_badge_rank_3)
            }
            else -> {
                ivMedal.visibility = View.GONE
                tvRank.background = null
            }
        }
        
        // Load avatar
        Glide.with(itemView.context)
            .load(user.avatarUrl)
            .placeholder(R.drawable.ic_default_avatar)
            .into(ivAvatar)
    }
}
```

### Reminder & Scheduled Notifications

#### Technical Architecture

```
User Sets Reminder → AlarmManager → BroadcastReceiver → NotificationHelper
                                                               ↓
                                                    System Notification
```

#### Set Reminder

**Input Flow:**
```kotlin
// ViewHabitActivity.kt
binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) {
        showTimePickerDialog()
    } else {
        cancelReminder()
    }
}

private fun showTimePickerDialog() {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    
    val timePicker = TimePickerDialog(
        this,
        { _, selectedHour, selectedMinute ->
            setReminder(selectedHour, selectedMinute)
        },
        hour,
        minute,
        false
    )
    timePicker.show()
}
```

**Process Flow - Schedule Reminder:**
```kotlin
private fun setReminder(hour: Int, minute: Int) {
    lifecycleScope.launch {
        try {
            val habitId = habit?.id ?: return@launch
            
            // Calculate trigger time
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                
                // If time has passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            // Create intent for BroadcastReceiver
            val intent = Intent(this@ViewHabitActivity, HabitReminderReceiver::class.java).apply {
                putExtra("habitId", habitId)
                putExtra("habitName", habit?.name)
                action = "com.example.habittracker.HABIT_REMINDER"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                this@ViewHabitActivity,
                habitId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule with AlarmManager
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Request permission
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(settingsIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            // Save reminder time to Firestore
            HabitRepository.getInstance().updateHabitReminder(
                habitId,
                hour,
                minute,
                true
            )
            
            Toast.makeText(
                this@ViewHabitActivity,
                "Reminder set for ${String.format("%02d:%02d", hour, minute)}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this@ViewHabitActivity,
                "Failed to set reminder: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
```

**BroadcastReceiver:**
```kotlin
// HabitReminderReceiver.kt
class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra("habitId") ?: return
        val habitName = intent.getStringExtra("habitName") ?: "Habit"
        
        // Show notification
        showReminderNotification(context, habitId, habitName)
        
        // Reschedule for next day
        rescheduleReminder(context, intent)
    }
    
    private fun showReminderNotification(context: Context, habitId: String, habitName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationHelper.REMINDER_CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your habits"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("habitId", habitId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, NotificationHelper.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for $habitName!")
            .setContentText("Don't forget to complete your habit today")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        notificationManager.notify(habitId.hashCode(), notification)
    }
    
    private fun rescheduleReminder(context: Context, originalIntent: Intent) {
        val habitId = originalIntent.getStringExtra("habitId") ?: return
        
        // Schedule for same time tomorrow
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            originalIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}
```

### Data Persistence & Caching

#### Technical Strategy

```
Network Request → Firestore → Local Cache (StateFlow) → UI
        ↓                                                ↑
   Real-time                                       Immediate
   Updates ─────────────────────────────────────────────┘
```

#### Repository Pattern with Caching

**StateFlow Cache:**
```kotlin
// HabitRepository.kt
class HabitRepository private constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val authRepository = AuthRepository.getInstance()
    
    // Cache using StateFlow
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()
    
    private var habitsListener: ListenerRegistration? = null
    
    init {
        observeCurrentUser()
    }
    
    private fun observeCurrentUser() {
        CoroutineScope(Dispatchers.Main).launch {
            authRepository.currentUserId.collect { userId ->
                if (userId != null) {
                    startListeningToHabits(userId)
                } else {
                    stopListeningToHabits()
                    _habits.value = emptyList()
                }
            }
        }
    }
    
    private fun startListeningToHabits(userId: String) {
        stopListeningToHabits()
        
        habitsListener = db.collection("habits")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Habits listener error: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val habitsList = snapshot.documents.mapNotNull { 
                        Habit.fromDocument(it) 
                    }
                    _habits.value = habitsList
                    Log.d(TAG, "Habits updated: ${habitsList.size} habits")
                }
            }
    }
    
    private fun stopListeningToHabits() {
        habitsListener?.remove()
        habitsListener = null
    }
    
    companion object {
        @Volatile
        private var instance: HabitRepository? = null
        
        fun getInstance(): HabitRepository {
            return instance ?: synchronized(this) {
                instance ?: HabitRepository().also { instance = it }
            }
        }
    }
}
```

**ViewModel Consumption:**
```kotlin
// HomeViewModel.kt
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val habitRepository = HabitRepository.getInstance()
    
    // Direct subscription to cached data
    val habits: StateFlow<List<Habit>> = habitRepository.habits
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    
    // Derived state for filtered habits
    val todayHabits: StateFlow<List<Habit>> = habits
        .map { allHabits ->
            allHabits.filter { habit ->
                val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                val dayName = when (today) {
                    Calendar.MONDAY -> "Monday"
                    Calendar.TUESDAY -> "Tuesday"
                    Calendar.WEDNESDAY -> "Wednesday"
                    Calendar.THURSDAY -> "Thursday"
                    Calendar.FRIDAY -> "Friday"
                    Calendar.SATURDAY -> "Saturday"
                    Calendar.SUNDAY -> "Sunday"
                    else -> "Monday"
                }
                habit.frequency.contains(dayName)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
```

**Offline Support:**
```kotlin
// Enable Firestore offline persistence
FirebaseFirestore.getInstance().apply {
    firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
        .build()
}
```

### Error Handling & Logging

#### Exception Handling Strategy

**Repository Level:**
```kotlin
suspend fun addHabit(habit: Habit): String? {
    return try {
        val habitMap = habit.toMap()
        val docRef = db.collection("habits").add(habitMap).await()
        Log.d(TAG, "Habit added: ${docRef.id}")
        docRef.id
    } catch (e: FirebaseFirestoreException) {
        Log.e(TAG, "Firestore error: ${e.code} - ${e.message}")
        null
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error: ${e.message}")
        null
    }
}
```

**ViewModel Level:**
```kotlin
fun createHabit(habit: Habit) {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            val habitId = habitRepository.addHabit(habit)
            if (habitId != null) {
                _habitCreated.emit(true)
            } else {
                _error.emit("Failed to create habit")
            }
        } catch (e: Exception) {
            _error.emit("Error: ${e.localizedMessage ?: "Unknown error"}")
            Log.e(TAG, "Create habit error", e)
        } finally {
            _isLoading.value = false
        }
    }
}
```

**UI Level:**
```kotlin
private fun observeErrors() {
    lifecycleScope.launch {
        viewModel.error.collect { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Snackbar.make(
                    binding.root,
                    errorMessage,
                    Snackbar.LENGTH_LONG
                ).apply {
                    setAction("RETRY") {
                        viewModel.retry()
                    }
                    show()
                }
            }
        }
    }
}
```

**Crash Reporting:**
```kotlin
// Application class
class HabitTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Setup Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
        }
        
        // Custom exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("UncaughtException", "Thread: ${thread.name}", throwable)
            FirebaseCrashlytics.getInstance().recordException(throwable)
            
            // Default handler
            Thread.getDefaultUncaughtExceptionHandler()
                ?.uncaughtException(thread, throwable)
        }
    }
}
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For support, email jakeconal@example.com or open an issue in the GitHub repository.

## Roadmap

Planned features for future releases:
- [ ] iOS version
- [ ] Habit templates
- [ ] Team challenges
- [ ] Achievement badges
- [ ] Export habit data
- [ ] Habit streaks freeze feature
- [ ] Customizable themes
- [ ] Widget support
- [ ] Apple Watch integration
- [ ] Advanced analytics with charts

---

**Built with ❤️ using Kotlin and Firebase**
