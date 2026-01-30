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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
