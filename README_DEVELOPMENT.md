# VoiceTasker - Development Guide

## 🚀 Quick Start

This document provides guidance for continuing development on VoiceTasker after the Phase 1 build structure has been created.

### Current Status
- **Build Date**: 2026-01-17
- **Phase**: 1 (Core Features)
- **Completion**: Infrastructure (~40%)
- **Next Focus**: Implement business logic and API integration

---

## 📋 Development Environment Setup

### Prerequisites
- Android Studio Arctic Fox (or later)
- JDK 17+
- Android SDK 31+ (minSdk), target 34
- Gradle 8.2+

### Setup Steps

1. **Clone/Open Project**
   ```bash
   cd C:\Users\ROG\AndroidStudioProjects\VoiceTasker
   ```

2. **Sync Gradle**
   - Open in Android Studio
   - File → Sync Now
   - Wait for Gradle sync to complete

3. **Verify Build**
   ```bash
   # Terminal in project root
   ./gradlew build -x test
   ```

4. **Run on Emulator/Device**
   - Press Shift+F10 in Android Studio or
   - Run → Run 'app'

---

## 🏗️ Architecture Overview

### Clean Architecture + MVVM
```
UI Layer (Composables)
    ↓
ViewModel (State Management)
    ↓
Repository (Business Logic)
    ↓
Data Sources (API + Database)
```

### Key Components

**UI Layer**: Jetpack Compose screens in `ui/screens/`
- AuthGraph: Login/Register flows
- HomeScreen: Task list view
- TaskCreateScreen: Task creation with voice

**ViewModel Layer**: State management in `features/*/presentation/`
- Manages UI state
- Handles user interactions
- Calls repository methods

**Repository Layer**: Business logic in `features/*/data/repository/`
- Coordinates between API and database
- Handles offline-first sync
- Error handling

**Data Layer**:
- **API**: Retrofit services in `core/network/api/`
- **Database**: Room entities & DAOs in `core/database/`

**DI Layer**: Hilt modules in `di/`
- Provides dependencies
- Manages singleton instances

---

## 📝 Next Implementation Tasks

### Priority 1: Core Authentication (Week 1)

**Files to Modify**:
1. `LoginViewModel.kt` - Add authentication flow
2. `RegisterViewModel.kt` - Add registration flow
3. `LoginScreen.kt` - Wire up view model
4. `RegisterScreen.kt` - Wire up view model

**Implementation Steps**:
```kotlin
// LoginViewModel example
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    fun login() {
        viewModelScope.launch {
            isLoading.value = true
            authRepository.login(LoginRequest(email.value, password.value))
                .onSuccess { response ->
                    // Save tokens, navigate to home
                }
                .onFailure { exception ->
                    error.value = exception.message
                }
            isLoading.value = false
        }
    }
}
```

### Priority 2: Task Management (Week 2)

1. Complete `TaskListViewModel` with task flow
2. Complete `TaskCreateViewModel` with save logic
3. Update `HomeScreen` to display task list
4. Update `TaskCreateScreen` form handling
5. Add task detail screen

### Priority 3: Voice Recording (Week 2-3)

1. Create `VoiceRecorder.kt` service
2. Implement audio recording
3. Add speech-to-text integration
4. Integrate with task creation

### Priority 4: Reminders (Week 3)

1. Implement `AlarmReceiver.kt`
2. Create `ReminderManager` service
3. Schedule alarms via `AlarmManager`
4. Display notifications

---

## 🧪 Testing Strategy

### Unit Tests
- ViewModels: Test state changes and repository calls
- Repositories: Test API and database interactions
- Use Cases: Test business logic

Example:
```kotlin
@Test
fun testLoginSuccess() {
    val viewModel = LoginViewModel(fakeRepository)
    viewModel.email.value = "test@example.com"
    viewModel.password.value = "password"

    viewModel.login()

    assertTrue(viewModel.isLoading.value)
    // Assert success callback
}
```

### Integration Tests
- Database tests: Verify Room operations
- API tests: Mock Retrofit responses

### UI Tests (Espresso/Compose)
- Screen navigation
- Form validation
- Button interactions

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Coverage report
./gradlew testDebugUnitTestCoverage
```

---

## 🔌 API Integration

### Supabase Setup
1. Create Supabase project
2. Set up authentication
3. Create tasks table
4. Enable RLS policies
5. Get API URL and key

### Update NetworkModule
```kotlin
// In di/NetworkModule.kt
private const val BASE_URL = "https://YOUR_SUPABASE_URL.supabase.co/rest/v1/"

// Add to OkHttpClient builder
.addInterceptor { chain ->
    val request = chain.request().newBuilder()
        .addHeader("apikey", "YOUR_SUPABASE_KEY")
        .addHeader("Authorization", "Bearer $authToken")
        .build()
    chain.proceed(request)
}
```

### Test API Calls
- Use Postman to verify endpoints
- Check API responses match models
- Verify error handling

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  auth_token TEXT,
  refresh_token TEXT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

### Tasks Table
```sql
CREATE TABLE tasks (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  title TEXT NOT NULL,
  description TEXT,
  status TEXT DEFAULT 'TODO',
  priority TEXT DEFAULT 'MEDIUM',
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  due_date TIMESTAMP,
  reminder_enabled BOOLEAN DEFAULT false,
  reminder_time TIMESTAMP,
  voice_transcription TEXT,
  ai_extracted_intent TEXT,
  sync_status TEXT DEFAULT 'PENDING',
  is_deleted BOOLEAN DEFAULT false
);
```

---

## 🔐 Security Checklist

- [ ] Never hardcode API keys (use BuildConfig or secure storage)
- [ ] Encrypt tokens in SharedPreferences (use EncryptedSharedPreferences)
- [ ] Validate all user input
- [ ] Use HTTPS for all API calls
- [ ] Implement token refresh mechanism
- [ ] Add certificate pinning for production
- [ ] Never log sensitive data in production
- [ ] Use ProGuard/R8 for release builds

---

## 🐛 Debugging Tips

### Enable Logging
```kotlin
// Set in BuildConfig.DEBUG
if (BuildConfig.DEBUG) {
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
}
```

### Database Inspection
- Use Android Studio Database Inspector
- Navigate to Database tab in IDE
- View tables and rows in real-time

### Network Monitoring
- Use Charles Proxy or Wireshark
- Monitor API calls and responses
- Check request/response headers

### Compose Preview
- Use `@Preview` annotation
- Preview individual screens
- Test different screen sizes

---

## 📦 Dependency Updates

### Check for Updates
```bash
./gradlew dependencyUpdates
```

### Update Gradle
```bash
# In gradle/libs.versions.toml
version = "new-version"
```

---

## 🚢 Build & Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Generate Signed APK
1. Build → Generate Signed Bundle/APK
2. Select Android App Bundle for Play Store
3. Sign with keystore
4. Upload to Play Store

---

## 📚 Documentation Structure

```
final_destination/
├── 01_CONSOLIDATED_PRD.md           # Feature requirements
├── 02_TECHNICAL_ARCHITECTURE.md     # System design
├── 03_COMPLIANCE_CONSTRAINTS.md     # Android/Play Store rules
├── 05_DEVELOPMENT_ROADMAP.md        # 20-week timeline
└── final_approach/
    ├── 01_PROJECT_INITIALIZATION_DETAILED.md  # Setup guide
    ├── 02_TESTING_EXECUTION_GUIDE.md          # Testing strategy
    ├── 03_CICD_PIPELINE_SETUP.md              # GitHub Actions
    └── 04_DEPLOYMENT_PROCEDURES.md            # Release process
```

---

## 🎯 Coding Standards

### Kotlin Style
- Use val over var when possible
- Avoid nested lambda with deep indentation
- Use scope functions (let, apply, run) appropriately
- Prefer immutable data structures

### Naming Conventions
- Classes: PascalCase (LoginViewModel)
- Functions: camelCase (loginUser)
- Constants: UPPER_SNAKE_CASE (AUTH_TOKEN_KEY)
- Private members: _camelCase

### Compose Best Practices
- Keep composables small and focused
- Use @Composable only for composables
- Pass lambdas for callbacks instead of callbacks in state
- Avoid large Column/Row with many children

---

## ✅ Weekly Checklist

### Every Monday
- [ ] Review pull requests
- [ ] Check test coverage
- [ ] Plan week's work

### Every Day
- [ ] Run tests locally
- [ ] Check compilation warnings
- [ ] Review code for TODOs

### Every Friday
- [ ] Team sync meeting
- [ ] Review week's progress
- [ ] Plan next week

---

## 🆘 Troubleshooting

### Build Fails
```bash
# Clean and rebuild
./gradlew clean build --no-daemon
```

### Gradle Sync Issues
- File → Invalidate Caches / Restart
- Delete .gradle folder
- Re-sync Gradle

### App Crashes
- Check Logcat for stack traces
- Enable debug logging
- Use debugger to step through code

### Database Issues
- Delete app data: Settings → Apps → VoiceTasker → Storage → Clear
- Drop and recreate database: Set version to 1 with destructive migration
- Use Database Inspector in Android Studio

---

## 📞 Getting Help

### Documentation
- Android Docs: https://developer.android.com/docs
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Hilt: https://developer.android.com/training/dependency-injection/hilt-android
- Room: https://developer.android.com/training/data-storage/room

### Community
- Stack Overflow: Tag with `android` and `kotlin`
- Android Slack communities
- GitHub discussions

---

## 🎉 Success Criteria

Phase 1 (Week 8) deliverables:
- ✅ User authentication working
- ✅ Task CRUD operations implemented
- ✅ Voice recording integrated
- ✅ Reminders functional
- ✅ 80%+ test coverage
- ✅ < 0.5% crash rate

---

**Last Updated**: 2026-01-17
**Next Review**: End of Week 1
**Maintained By**: Development Team
