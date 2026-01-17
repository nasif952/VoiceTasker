# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**VoiceTasker** is an Android task management application that allows users to create, manage, and receive reminders for tasks using voice input and text. The app is built with modern Android architecture and libraries including Jetpack Compose, Hilt dependency injection, Room database, and Retrofit networking.

**Current Phase**: Phase 2 - Core Features Complete with Mock API (~60% complete)

### Completed Features (Working with Mock API)
- **Authentication**: Login/Register with form validation (FakeAuthRepository)
- **Task CRUD**: Full Create, Read, Update, Delete functionality (FakeTaskRepository)
- **Task List**: Display tasks with priority color-coding, completion status
- **Task Create**: Form with title, description, priority buttons, date picker
- **Task Edit**: Edit existing tasks with pre-populated form
- **Toggle Complete**: Checkbox to mark tasks complete/incomplete
- **Delete Tasks**: Remove tasks from list
- **Date Picker**: Material 3 DatePickerDialog for due date selection
- **Priority Selection**: Color-coded buttons (HIGH=red, MEDIUM=primary, LOW=tertiary)
- **Navigation**: Full navigation flow between screens

### Currently Using Mock API
All features work with in-memory fake repositories for testing:
- `FakeAuthRepository` - Mock login/register with hardcoded credentials
- `FakeTaskRepository` - In-memory task storage with simulated delays

### Next Phase: Real Backend Integration
- Connect to real API (Supabase or custom backend)
- Implement proper token storage
- Add offline-first sync with Room database
- Voice input feature

## Build & Development Commands

### Setup & Sync
```bash
# Sync Gradle dependencies
./gradlew sync

# Clean build (use if build fails or you want fresh state)
./gradlew clean build
```

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (with ProGuard minification)
./gradlew assembleRelease

# Build without running tests
./gradlew build -x test
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run instrumented tests on device/emulator
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests com.voicetasker.features.auth.presentation.LoginViewModelTest

# Generate coverage report
./gradlew testDebugUnitTestCoverage
```

### Running the App
From Android Studio: Press **Shift+F10** or use Run → Run 'app'

From command line:
```bash
./gradlew installDebug  # Install on connected device
adb shell am start -n com.voicetasker.debug/com.voicetasker.MainActivity  # Launch app
```

### Troubleshooting Builds
```bash
# If sync or build fails
./gradlew clean
./gradlew build --no-daemon

# Invalidate IDE cache
# In Android Studio: File → Invalidate Caches → Invalidate and Restart
```

## Architecture Overview

The project follows **Clean Architecture + MVVM** pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│           UI Layer (Jetpack Compose)                │
│   (LoginScreen, RegisterScreen, HomeScreen, etc.)   │
└───────────────────┬─────────────────────────────────┘
                    │ Uses
┌───────────────────▼─────────────────────────────────┐
│         ViewModel (State Management)                │
│  (LoginViewModel, TaskListViewModel, etc.)          │
└───────────────────┬─────────────────────────────────┘
                    │ Calls
┌───────────────────▼─────────────────────────────────┐
│    Repository (Business Logic & Coordination)       │
│  (AuthRepository, TaskRepository)                   │
└───────────────────┬─────────────────────────────────┘
                    │ Coordinates
        ┌───────────┴───────────┐
        │                       │
┌───────▼──────────┐  ┌────────▼──────────┐
│  Remote (API)    │  │ Local (Database)  │
│  (Retrofit)      │  │ (Room)            │
└──────────────────┘  └───────────────────┘
```

### Layer Details

**UI Layer** (`app/src/main/java/com/voicetasker/ui/`)
- **Screens**: Jetpack Compose components in `screens/` subdirectory
  - `auth/LoginScreen.kt`, `RegisterScreen.kt` - Authentication flows
  - `home/HomeScreen.kt` - Task list display
  - `task/TaskCreateScreen.kt` - Task creation with voice support
- **Navigation**: `navigation/NavRoute.kt` - Route definitions and navigation setup
- **Theme**: Material 3 theme configuration in `theme/`

**ViewModel Layer** (`features/*/presentation/`)
- State management using `mutableStateOf()` and Compose state
- Business logic coordination (calls repositories)
- Lifecycle-aware coroutine management via `viewModelScope`
- Example: `features/auth/presentation/LoginViewModel.kt`

**Repository Layer** (`features/*/data/repository/`)
- Coordinates between API and local database
- Handles offline-first sync patterns
- Error handling and Result type wrapping
- Example: `features/auth/data/repository/AuthRepository.kt`

**Data Layer**
- **Network**: `core/network/api/` - Retrofit service interfaces (AuthApi, TaskApi)
- **Database**: `core/database/` - Room entities, DAOs, and database class
  - Entities: `UserEntity.kt`, `TaskEntity.kt`
  - DAOs: `UserDao.kt`, `TaskDao.kt` (currently minimal)
  - Main DB: `VoiceTaskerDatabase.kt`
- **Models**: `core/model/` - Domain models (User, Task)

**Dependency Injection** (`di/`)
- **AppModule.kt**: Application-level singletons (currently has TODO placeholders)
- **NetworkModule.kt**: Retrofit and OkHttp configuration
- **DatabaseModule.kt**: Room database instance provision
- All modules use Hilt for automatic injection via `@HiltViewModel`, `@Inject` constructor params

### Feature Organization

Features are organized in `features/` with the following structure per feature:
```
features/
├── auth/
│   ├── presentation/        # ViewModels
│   ├── data/
│   │   └── repository/      # AuthRepository
│   └── ...
├── task/
│   ├── presentation/
│   ├── data/
│   │   └── repository/
│   └── ...
└── voice/  (planned)
    └── (to be implemented)
```

Each feature is self-contained - ViewModels only depend on that feature's repository.

## Key Technical Decisions

### Dependency Injection: Hilt
- All ViewModels are injected via `@HiltViewModel` annotation
- Repositories and services injected via constructor injection with `@Inject`
- Singleton scope for repositories to maintain state
- **Why**: Type-safe, compile-time checks, minimal boilerplate

### State Management: Mutable State + Compose
- Currently using `mutableStateOf<T>()` for UI state
- Direct updates: `viewModel.email.value = "text"`
- **Current limitation**: No state flow, direct mutation, not ideal for testing
- **Future consideration**: Migrate to StateFlow for reactive updates and better testing

### Database: Room with Coroutines
- Async operations via suspend functions
- Type-safe queries (compile-time checked)
- Database version in `VoiceTaskerDatabase.kt` - increment when schema changes
- Current schema supports offline-first with `sync_status` field

### Networking: Retrofit + kotlinx.serialization
- Retrofit for HTTP client management
- kotlinx.serialization for JSON serialization (not Gson)
- Type-safe request/response models
- `core/network/api/` defines service interfaces (contracts)

## Code Structure

### Important Files to Know

| File | Purpose |
|------|---------|
| `app/src/main/AndroidManifest.xml` | Permissions, receivers, components registration |
| `app/build.gradle.kts` | Dependencies, compileSdk, minSdk, targetSdk versions |
| `gradle/libs.versions.toml` | Centralized version catalog for all dependencies |
| `VoiceTaskerApp.kt` | Application class - initializes Hilt and notification channels |
| `MainActivity.kt` | Entry point activity - sets up Compose |
| `ui/VoiceTaskerApp.kt` | Root composable with navigation setup |

### Permission Model

Permissions declared in `AndroidManifest.xml` categorize by feature:
- **Network**: `INTERNET`, `ACCESS_NETWORK_STATE`
- **Voice/Audio**: `RECORD_AUDIO`
- **Notifications**: `POST_NOTIFICATIONS`, `VIBRATE`
- **Reminders/Alarms**: `SCHEDULE_EXACT_ALARM`, `BOOT_COMPLETED`
- **Services**: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE`

Runtime permissions (RECORD_AUDIO, POST_NOTIFICATIONS) must be requested when features are accessed - not done yet, implement in respective features.

## Development Patterns & Conventions

### ViewModel Pattern
- Use `@HiltViewModel` annotation
- Inject dependencies via constructor
- Create mutable state for UI: `val email = mutableStateOf("")`
- Launch coroutines in `viewModelScope` for async operations
- Example:
  ```kotlin
  @HiltViewModel
  class LoginViewModel @Inject constructor(
      private val authRepository: AuthRepository
  ) : ViewModel() {
      val email = mutableStateOf("")

      fun login() {
          viewModelScope.launch {
              val result = authRepository.login(email.value, password.value)
              // Handle result
          }
      }
  }
  ```

### Repository Pattern
- Inject both API and database DAOs
- Handle Result type (success/failure) wrapping
- Coordinate API calls with local caching
- Example:
  ```kotlin
  class AuthRepository @Inject constructor(
      private val authApi: AuthApi,
      private val userDao: UserDao
  ) {
      suspend fun login(email: String, password: String): Result<User> {
          return try {
              val response = authApi.login(LoginRequest(email, password))
              userDao.insert(response.toEntity())
              Result.success(response)
          } catch (e: Exception) {
              Result.failure(e)
          }
      }
  }
  ```

### Compose UI Pattern
- Keep composables small and focused
- Use `@Composable` annotation only on composables
- Pass lambdas for callbacks: `onClick: () -> Unit`
- Use preview annotations for testing: `@Preview`
- Lazy emit when needed: `LazyColumn { items() { } }`

### Naming Conventions
- **Classes**: PascalCase (e.g., `LoginViewModel`, `AuthRepository`)
- **Functions/Properties**: camelCase (e.g., `loginUser`, `userEmail`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `API_BASE_URL`)
- **Private members**: Prefix with underscore not typically used, use regular camelCase

### Testing Conventions
- Unit tests in `test/` directory, mirror source structure
- Instrumented tests in `androidTest/` directory
- Use MockK for mocking (configured in dependencies)
- Use Turbine for Flow testing (if migrating to StateFlow)
- Test ViewModels by creating instance with fake repository

## Known Gaps & TODOs

### Completed
- [x] AppModule.kt - Provides FakeAuthRepository and FakeTaskRepository
- [x] ViewModels - LoginViewModel, RegisterViewModel, TaskListViewModel, TaskCreateViewModel, TaskEditViewModel all working
- [x] Task CRUD - Full functionality with mock repository
- [x] Form validation - Email, password, task title validation
- [x] Navigation - Complete flow between all screens

### Remaining Work
1. **Real API Integration** - Replace Fake repositories with real Retrofit API calls
2. **Room Database** - Implement local persistence for offline-first support
3. **TaskDao.kt** - Only has stub definitions, needs actual Room query implementations
4. **Token Storage** - Securely store auth tokens (EncryptedSharedPreferences)
5. **Voice Recording** - Not implemented, will be core feature
6. **Notifications/Reminders** - Receivers registered but logic not implemented
7. **Testing** - Test infrastructure in place but no tests written yet
8. **Error Handling** - Basic error handling done, needs improvement for network errors

### Database Initialization
- Room database uses in-memory database by default (for testing)
- Update `DatabaseModule.kt` to use persistent database: `.build().createFromAsset("database.db")` or create fresh

## API Integration Notes

When implementing API calls:
1. Add API endpoints to `core/network/api/` (AuthApi.kt, TaskApi.kt already exist)
2. Define request/response models (use `@Serializable` annotation)
3. Implement repository methods that call API
4. Handle network errors appropriately
5. Update `NetworkModule.kt` with base URL and authentication headers

Example API setup (needs completion):
```kotlin
// core/network/api/AuthApi.kt
interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

// Update NetworkModule.kt with:
private const val BASE_URL = "https://your-backend.com/api/"
```

## Debugging & Development Tips

### Logging
- Debug builds have detailed logging enabled in `VoiceTaskerApp.kt`
- OkHttp logging interceptor configured in `NetworkModule.kt`
- Check Logcat (bottom of Android Studio) for log output

### Database Inspection
- Android Studio: Tools → App Inspection → Database Inspector
- View tables, rows, and execute queries in real-time
- Current DB should be empty until data is inserted

### Compose Preview
- Add `@Preview` annotation to composables to preview in IDE
- Use `@PreviewLightDark` to preview both light and dark modes
- Helpful for quick iteration on UI without running app

### Device/Emulator
- Emulator accessible via: Tools → Device Manager
- Kill running app: Ctrl+F2
- Clear app data: Settings → Apps → VoiceTasker → Storage → Clear

## Important Version Info

- **minSdk**: 31 (Android 12)
- **targetSdk/compileSdk**: 34 (Android 14)
- **Kotlin**: 1.9.20
- **Compose**: 2023.10.01 BOM
- **Hilt**: 2.48
- **Room**: 2.6.1
- **Retrofit**: 2.9.0
- **JVM Target**: 17

## Testing Strategy

### Unit Tests
- Test ViewModels with fake repositories
- Test repositories with mock API/DAO
- No Android context needed
- Located in `test/` directory

### Instrumented Tests
- Test on device/emulator (need Android context)
- Located in `androidTest/` directory
- Run via `./gradlew connectedAndroidTest`

### Example Test Structure
```kotlin
// features/auth/presentation/LoginViewModelTest.kt
@Test
fun testLoginSuccess() = runTest {
    val fakeRepository = FakeAuthRepository()
    val viewModel = LoginViewModel(fakeRepository)

    viewModel.email.value = "test@example.com"
    viewModel.password.value = "password"
    viewModel.login()

    // Assert state changes
    assertEquals(true, viewModel.isLoading.value)
}
```

## Next Implementation Priorities

### Phase 3: Backend Integration (Next)
Choose one of these backend options:
1. **Supabase** - Quick setup, PostgreSQL, built-in auth
2. **Firebase** - Google ecosystem, Firestore, easy auth
3. **Custom API** - Full control, requires server setup

**Tasks:**
- [ ] Set up backend (Supabase/Firebase/Custom)
- [ ] Create real AuthRepository replacing FakeAuthRepository
- [ ] Create real TaskRepository replacing FakeTaskRepository
- [ ] Implement JWT/token storage with EncryptedSharedPreferences
- [ ] Add network error handling and retry logic

### Phase 4: Offline-First & Local Storage
- [ ] Implement Room database persistence
- [ ] Add sync logic (local-first, sync when online)
- [ ] Handle conflict resolution
- [ ] Add loading/sync status indicators

### Phase 5: Voice Input Feature
- [ ] Implement voice recording with MediaRecorder
- [ ] Add speech-to-text (Google Speech API or on-device)
- [ ] Parse voice input to extract task details
- [ ] Add voice button to task creation screen

### Phase 6: Reminders & Notifications
- [ ] Implement AlarmManager for task reminders
- [ ] Create notification channel and display
- [ ] Add reminder time picker to task form
- [ ] Handle boot receiver for persisted alarms

### Phase 7: Polish & Testing
- [ ] Write unit tests for ViewModels
- [ ] Write integration tests
- [ ] UI polish and animations
- [ ] Error handling improvements
- [ ] Performance optimization

See `final_destination/05_DEVELOPMENT_ROADMAP.md` for full 20-week plan.

## Documentation References

| Document | Purpose |
|----------|---------|
| `README_DEVELOPMENT.md` | Detailed architecture and feature implementation guide |
| `QUICK_START_ANDROID_STUDIO.md` | Step-by-step guide to running the app |
| `final_destination/01_CONSOLIDATED_PRD.md` | Feature requirements and user stories |
| `final_destination/02_TECHNICAL_ARCHITECTURE.md` | System design and technical decisions |
| `final_destination/03_COMPLIANCE_CONSTRAINTS.md` | Android/Play Store compliance rules |
| `final_destination/05_DEVELOPMENT_ROADMAP.md` | 20-week development timeline |

