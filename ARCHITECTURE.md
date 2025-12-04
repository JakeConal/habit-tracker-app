# Kiến trúc MVVM - Habit Tracker App

## Tổng quan

Ứng dụng này được xây dựng theo kiến trúc **MVVM (Model-View-ViewModel)**, một pattern được Google khuyến nghị cho phát triển ứng dụng Android hiện đại.

## Cấu trúc thư mục

```
app/src/main/java/com/example/habittracker/
├── data/                          # Lớp dữ liệu
│   ├── models/                    # Các data class (Model)
│   │   └── Habit.kt              # Model đại diện cho một habit
│   └── repository/                # Repository pattern
│       └── HabitRepository.kt    # Quản lý data source
│
├── ui/                            # Lớp giao diện người dùng
│   ├── activities/                # Các Activity
│   │   └── MainActivity.kt       # Activity chính
│   ├── fragments/                 # Các Fragment (chưa có)
│   ├── viewmodels/                # Các ViewModel
│   │   └── HabitViewModel.kt     # ViewModel cho habits
│   └── adapters/                  # RecyclerView Adapters
│       └── HabitAdapter.kt       # Adapter hiển thị danh sách habits
│
├── di/                            # Dependency Injection (chưa implement)
│   └── (Dagger/Hilt modules)
│
└── utils/                         # Các utility classes
    └── Constants.kt              # Hằng số dùng chung
```

## Giải thích các thành phần

### 1. **Model** (`data/models/`)
- Chứa các data class đại diện cho dữ liệu của ứng dụng
- Ví dụ: `Habit.kt` định nghĩa cấu trúc của một thói quen
- **Trách nhiệm**: Định nghĩa cấu trúc dữ liệu

### 2. **Repository** (`data/repository/`)
- Là single source of truth cho dữ liệu
- Trừu tượng hóa data source (có thể từ database, API, cache...)
- **Trách nhiệm**: Cung cấp dữ liệu cho ViewModel, xử lý logic lấy/lưu dữ liệu

### 3. **ViewModel** (`ui/viewmodels/`)
- Nằm giữa View và Model
- Giữ và xử lý dữ liệu cho UI
- Sống sót qua configuration changes (xoay màn hình)
- Sử dụng `LiveData` hoặc `StateFlow` để UI observe
- **Trách nhiệm**: Chuẩn bị dữ liệu cho View, xử lý business logic

### 4. **View** (`ui/activities/`, `ui/fragments/`)
- Activity và Fragment
- Hiển thị dữ liệu và nhận input từ người dùng
- Observe ViewModel để cập nhật UI
- **Trách nhiệm**: Hiển thị UI và xử lý user interaction

### 5. **Adapter** (`ui/adapters/`)
- Adapter cho RecyclerView
- **Trách nhiệm**: Bind dữ liệu vào các view item

### 6. **Utils** (`utils/`)
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

## Ưu điểm của MVVM

1. **Separation of Concerns**: Tách biệt rõ ràng giữa UI và business logic
2. **Testability**: Dễ dàng test ViewModel độc lập với UI
3. **Maintainability**: Code dễ bảo trì và mở rộng
4. **Lifecycle Aware**: ViewModel tự động xử lý lifecycle
5. **Data Binding**: UI tự động cập nhật khi dữ liệu thay đổi

## Dependencies cần thiết

Thêm vào `app/build.gradle.kts`:

```kotlin
dependencies {
    // ViewModel và LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    
    // Coroutines cho async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Room Database (nếu dùng)
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    
    // Dependency Injection - Hilt (optional)
    implementation("com.google.dagger:hilt-android:2.48")
}
```

## Các bước tiếp theo

1. **Implement Database**: Thêm Room Database để lưu trữ habits
2. **Add Dependency Injection**: Sử dụng Hilt để quản lý dependencies
3. **Create Fragments**: Tách UI thành các fragment nhỏ hơn
4. **Add Navigation**: Sử dụng Navigation Component để điều hướng
5. **Testing**: Viết unit tests cho ViewModel và Repository

## Tài liệu tham khảo

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Room Database](https://developer.android.com/training/data-storage/room)
