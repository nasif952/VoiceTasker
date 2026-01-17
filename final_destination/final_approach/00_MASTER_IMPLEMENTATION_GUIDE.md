# MASTER IMPLEMENTATION GUIDE

**VoiceTasker - Complete Build, Test, Deploy Strategy**

**Document Version**: 1.0
**Last Updated**: 2026-01-17
**Status**: Ready for Implementation

---

## TABLE OF CONTENTS

1. Overview & Philosophy
2. Project Initialization Checklist
3. Development Workflow
4. Testing Strategy
5. CI/CD Pipeline
6. Build Process
7. Deployment Strategy
8. Monitoring & Maintenance
9. Team Communication
10. Success Metrics

---

## 1. OVERVIEW & PHILOSOPHY

### 1.1 Build Philosophy

**VoiceTasker is built with these principles**:

1. **Offline-First**: Local cache is source of truth
2. **Type-Safe**: Kotlin + Compose prevent runtime errors
3. **Testable**: Clean Architecture enables unit testing
4. **Observable**: Monitoring built-in from Day 1
5. **Compliant**: Privacy & Android rules enforced in code
6. **Performant**: Latency targets enforced via benchmarks

### 1.2 Project Structure (Finalized)

```
VoiceTasker/
├── app/                                  # Main app module
│   ├── src/main/
│   │   ├── java/com/voicetasker/
│   │   │   ├── MainActivity.kt
│   │   │   └── ...
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── feature/                              # Feature modules (isolated)
│   ├── auth/                            # Authentication
│   │   ├── src/main/java/presentation/
│   │   ├── src/main/java/domain/
│   │   ├── src/main/java/data/
│   │   ├── src/test/java/ (unit tests)
│   │   ├── src/androidTest/java/ (UI tests)
│   │   └── build.gradle.kts
│   ├── task/                            # Task management
│   ├── voice/                           # Voice processing
│   ├── reminder/                        # Reminders & alarms
│   ├── sync/                            # Cloud sync
│   └── settings/                        # User settings
│
├── core/                                 # Shared code
│   ├── common/
│   │   ├── extensions/
│   │   ├── utilities/
│   │   └── constants/
│   ├── database/
│   │   ├── entities/
│   │   ├── daos/
│   │   └── AppDatabase.kt
│   ├── network/
│   │   ├── client/
│   │   ├── interceptors/
│   │   └── service/
│   ├── security/
│   │   ├── encryption/
│   │   └── keystore/
│   └── build.gradle.kts
│
├── buildSrc/                             # Build configuration
│   └── src/main/kotlin/
│       ├── Config.kt
│       ├── Dependencies.kt
│       └── ...
│
├── gradle/                               # Gradle wrapper
├── settings.gradle.kts
├── build.gradle.kts
└── README.md

Feature module structure (example: feature/task/):
task/
├── src/main/java/com/voicetasker/feature/task/
│   ├── presentation/
│   │   ├── viewmodel/
│   │   │   └── TaskListViewModel.kt
│   │   ├── screen/
│   │   │   ├── HomeScreen.kt
│   │   │   └── TaskDetailScreen.kt
│   │   └── component/
│   │       ├── TaskItem.kt
│   │       └── TaskForm.kt
│   ├── domain/
│   │   ├── usecase/
│   │   │   ├── CreateTaskUseCase.kt
│   │   │   ├── GetTasksUseCase.kt
│   │   │   └── ...
│   │   ├── model/
│   │   │   └── Task.kt
│   │   └── repository/
│   │       └── TaskRepository.kt
│   └── data/
│       ├── repository/
│       │   └── TaskRepositoryImpl.kt
│       ├── datasource/
│       │   ├── LocalTaskDataSource.kt
│       │   └── RemoteTaskDataSource.kt
│       └── mapper/
│           └── TaskMapper.kt
├── src/test/java/ (unit tests)
├── src/androidTest/java/ (UI tests)
└── build.gradle.kts
```

### 1.3 Key Technologies (Finalized)

```
Language:        Kotlin 1.9+
UI Framework:    Jetpack Compose
Architecture:    Clean Architecture + MVVM
DI Framework:    Hilt 2.48+
Local Database:  Room 2.6+
State Management: Flow + ViewModel
Networking:      Retrofit 4.11+ + OkHttp
Serialization:   Kotlinx Serialization 1.6+
Async:           Kotlin Coroutines 1.7+
Security:        Android Security Crypto 1.1+
Permissions:     Jetpack Core 1.14+
Background:      WorkManager 2.9+
```

---

## 2. PROJECT INITIALIZATION CHECKLIST

### Phase 2.1: Pre-Development Setup (Week -1)

**[ ] GitHub Repository**
```bash
# Create repo (private)
# Add .gitignore (Android template)
# Add README.md
# Add CONTRIBUTING.md
# Set up branch protection (main: require PRs)
```

**[ ] Development Environment**
```bash
# Install Android Studio Hedgehog+ (latest stable)
# Install JDK 17 (Android Studio bundles it)
# Install Gradle wrapper (in repo)
# Configure Android SDK (API 31-34)
# Configure emulator (Pixel 6, API 34)
```

**[ ] Team Credentials & Secrets**
```bash
# Google Cloud project (for Google Sign-In)
# Supabase project (PostgreSQL + Auth)
# Deepgram API key
# OpenAI API key
# Stripe account (test + production)

# Store in:
# - local.properties (git-ignored)
# - GitHub Secrets (for CI/CD)
# - 1Password / LastPass (team access)
```

**[ ] Communication Setup**
```bash
# Slack workspace (for notifications)
# Daily standup (10 AM, 15 min)
# Weekly tech sync (Friday, 2 PM)
# GitHub Issues (sprint planning)
# Figma (UI design)
```

### Phase 2.2: Project Scaffolding (Week 0, Day 1-2)

**Step 1: Create Android Project**

```bash
cd ~/AndroidStudioProjects
# Use Android Studio: File → New → New Android Project
# Project name: VoiceTasker
# Package: com.voicetasker
# Minimum SDK: API 31
# Target SDK: API 34
# Language: Kotlin
# Template: Empty Activity
```

**Step 2: Configure build.gradle.kts (root)**

```kotlin
// build.gradle.kts (Project)
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
    id("com.google.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.11" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

**Step 3: Configure app/build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 34
    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "com.voicetasker"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            debuggable(true)
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // TODO: Add release key
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Permissions
    implementation("androidx.core:core-core:1.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

**Step 4: Create Module Structure**

```bash
# Create feature modules
mkdir -p feature/{auth,task,voice,reminder,sync,settings}
mkdir -p core/{common,database,network,security}
mkdir -p buildSrc/src/main/kotlin

# Each feature module gets:
# - src/main/java
# - src/test/java
# - src/androidTest/java
# - src/main/res
# - build.gradle.kts
```

**Step 5: Configure Git**

```bash
git init
git add .
git commit -m "chore: initial project scaffold"
git branch -M main
git remote add origin https://github.com/yourorg/voicetasker.git
git push -u origin main
```

### Phase 2.3: Database Setup (Week 0, Day 3-4)

**Step 1: Create Room Database**

```kotlin
// core/database/AppDatabase.kt
@Database(
    entities = [
        TaskEntity::class,
        ReminderEntity::class,
        ProfileEntity::class,
        EntitlementEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val factory = EncryptedRoomDatabaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "voicetasker.db"
                )
                    .setEncryptionKey(masterKey)
                    .build()

                INSTANCE = factory
                factory
            }
        }
    }
}
```

**Step 2: Create Entities**

```kotlin
// core/database/entities/TaskEntity.kt
@Entity(
    tableName = "tasks",
    indices = [
        Index("user_id"),
        Index("parent_id"),
        Index("status"),
        Index("updated_at")
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String? = null,
    val status: String = "PLANNED",
    val dueDate: LocalDateTime? = null,
    val priority: String = "MEDIUM",
    val parentId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Int = 1,
    val synced: Boolean = false
)

// core/database/entities/ReminderEntity.kt, ProfileEntity.kt, EntitlementEntity.kt
// (Similar structure)
```

**Step 3: Create DAOs**

```kotlin
// core/database/daos/TaskDao.kt
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY updated_at DESC")
    fun observeUserTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}
```

**Step 4: Initial Migration**

```bash
# Room auto-generates schema on first build
# Verify schema is created in:
# app/schemas/com.voicetasker.db/1.json
```

### Phase 2.4: Dependency Injection Setup (Week 0, Day 5)

**Step 1: Configure Hilt**

```kotlin
// app/src/main/java/com/voicetasker/VoiceTaskerApp.kt
@HiltAndroidApp
class VoiceTaskerApp : Application()

// app/src/main/java/com/voicetasker/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoiceTaskerTheme {
                Surface {
                    HomeScreen()
                }
            }
        }
    }
}
```

**Step 2: Create Module Providers**

```kotlin
// core/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()
}

// core/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(LoggingInterceptor().apply {
                level = HttpLoggingLevel.BODY
            })
            .addInterceptor(AuthTokenInterceptor())
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.supabase.co")
            .client(okHttpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
```

### Phase 2.5: Initial Git Commit

```bash
git add -A
git commit -m "feat: project scaffolding with Room, Hilt, Compose setup"
git push origin main
```

---

## 3. DEVELOPMENT WORKFLOW

### 3.1 Feature Branch Workflow

**All development happens on feature branches, never directly on main.**

```
Workflow:
main (production-ready)
  ├── release/v0.2.0 (staging)
  ├── develop (integration branch)
  │   ├── feature/auth-google-signin (dev)
  │   ├── feature/voice-recording (dev)
  │   ├── feature/task-creation (dev)
  │   └── feature/reminders (dev)
  └── bugfix/crash-on-login (dev)
```

### 3.2 Creating a Feature Branch

```bash
# Always branch off develop
git checkout develop
git pull origin develop
git checkout -b feature/auth-google-signin

# Naming convention:
# - feature/description (new feature)
# - bugfix/description (bug fix)
# - hotfix/description (production fix)
# - docs/description (documentation)
```

### 3.3 Committing Code

**Commit Message Format**:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
**Scope**: `auth`, `task`, `voice`, `reminder`, `sync`, `db`, `ui`

**Examples**:
```
feat(auth): implement Google Sign-In with Credential Manager

- Integrate Android Credential Manager
- Handle OAuth token exchange
- Store JWT securely in Android Keystore
- Add unit tests for auth flow

Fixes: #123
```

```
fix(voice): handle STT timeout gracefully

- Add 5-second timeout for Deepgram API
- Fallback to Vosk on timeout
- Show "Couldn't hear clearly" message
- Allow user to retry

Fixes: #456
```

### 3.4 Pull Request Workflow

**1. Push to GitHub**
```bash
git push origin feature/auth-google-signin
```

**2. Create Pull Request (via GitHub UI)**
- Title: `feat(auth): implement Google Sign-In`
- Description: Detailed explanation of changes
- Linked Issues: `Fixes #123`
- Reviewers: 1-2 team members

**3. PR Requirements (Enforced by GitHub)**
- [ ] Tests pass (CI/CD)
- [ ] Code review approved (≥1 reviewer)
- [ ] No conflicts with main
- [ ] All conversations resolved

**4. Code Review Checklist**

Reviewer should verify:
- [ ] Code follows Clean Architecture pattern
- [ ] No API keys hardcoded
- [ ] Tests included (>80% coverage)
- [ ] No crashes on edge cases
- [ ] Performance acceptable (< latency targets)
- [ ] Privacy/compliance respected
- [ ] Documentation updated (if needed)

**5. Merge & Deploy**
```bash
# Squash & merge on GitHub (keep history clean)
# Delete feature branch after merge
```

### 3.5 Daily Development Process

**Morning (9:00 AM)**
```
1. Pull latest from develop
   git checkout develop && git pull origin develop

2. Create/switch to feature branch
   git checkout -b feature/xyz

3. Read PRD section for context

4. Implement feature (following architecture)
   - Create use case
   - Create repository
   - Create ViewModel
   - Create UI (Compose)

5. Write tests (unit + UI)

6. Test locally
   - Run on emulator (Pixel 6, API 34)
   - Test on physical device (if available)
   - Verify edge cases
```

**Afternoon (2:00 PM)**
```
1. Code review your own code (pre-check)

2. Push to GitHub

3. Create Pull Request

4. Respond to review feedback

5. Commit fixes (don't squash yet)

6. Request re-review
```

**End of Day (5:00 PM)**
```
1. Check if PR approved

2. If approved:
   - Squash & merge
   - Delete branch
   - Update CHANGELOG

3. If feedback pending:
   - Note for tomorrow

4. Update task status (GitHub Issues)
```

---

## 4. TESTING STRATEGY

### 4.1 Testing Pyramid

```
           /\
          /UI Tests (5%)
         /  - Compose screens
        /   - Navigation
       /    - User interactions
      /
     /______
    /  Integration Tests (15%)
   /   - API calls
  /    - Database operations
 /     - Full use cases
/____________________
Local Tests (80%)
- Unit tests (services, use cases)
- ViewModel tests
- Repository tests
- Mapper tests
```

### 4.2 Unit Tests

**Example: Task Creation Use Case**

```kotlin
// feature/task/src/test/java/com/voicetasker/feature/task/domain/CreateTaskUseCaseTest.kt
@ExtendWith(InstantExecutorExtension::class)
class CreateTaskUseCaseTest {

    private lateinit var createTaskUseCase: CreateTaskUseCase
    private val mockTaskRepository: TaskRepository = mockk()

    @Before
    fun setup() {
        createTaskUseCase = CreateTaskUseCase(mockTaskRepository)
    }

    @Test
    fun `createTask should save to repository and return ID`() = runTest {
        // Arrange
        val task = Task(
            id = "",
            title = "Test task",
            description = null,
            dueDate = null,
            status = TaskStatus.PLANNED,
            parentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            version = 1
        )
        val expectedId = "task-id-123"

        coEvery { mockTaskRepository.createTask(any()) } returns Result.success(expectedId)

        // Act
        val result = createTaskUseCase(task)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { mockTaskRepository.createTask(task) }
    }

    @Test
    fun `createTask should return failure on repository error`() = runTest {
        // Arrange
        val task = Task(/* ... */)
        val error = Exception("Database error")

        coEvery { mockTaskRepository.createTask(any()) } returns Result.failure(error)

        // Act
        val result = createTaskUseCase(task)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }
}
```

### 4.3 Integration Tests

**Example: Voice Recording + Transcription**

```kotlin
// feature/voice/src/androidTest/java/com/voicetasker/feature/voice/ProcessVoiceIntegrationTest.kt
@RunWith(AndroidJUnit4::class)
class ProcessVoiceIntegrationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var processVoiceUseCase: ProcessVoiceUseCase
    private lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Initialize real dependencies
        taskRepository = TaskRepositoryImpl(LocalTaskDataSource(...), RemoteTaskDataSource(...))
        processVoiceUseCase = ProcessVoiceUseCase(...)
    }

    @Test
    fun `processVoice should extract task and save to database`() = runTest {
        // Arrange
        val audioFile = File("assets/test_audio.wav")

        // Act
        val result = processVoiceUseCase(audioFile)

        // Assert
        assertTrue(result.isSuccess)
        val tasks = result.getOrNull()
        assertEquals(1, tasks?.size)
        assertEquals("Call mom", tasks?.first()?.title)

        // Verify saved to DB
        val savedTasks = taskRepository.getAllTasks("user-123").first()
        assertEquals(1, savedTasks.size)
    }
}
```

### 4.4 UI Tests (Compose)

**Example: Home Screen**

```kotlin
// feature/task/src/androidTest/java/com/voicetasker/feature/task/presentation/HomeScreenTest.kt
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {
            VoiceTaskerTheme {
                HomeScreen(
                    viewModel = mockk(relaxed = true)
                )
            }
        }
    }

    @Test
    fun homeScreen_displaysMicButton() {
        composeTestRule
            .onNodeWithContentDescription("Voice task")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysTaskList() {
        composeTestRule
            .onNodeWithTag("task_list")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_tappingMicButton_startsRecording() {
        composeTestRule
            .onNodeWithContentDescription("Voice task")
            .performClick()

        composeTestRule
            .onNodeWithText("Listening...")
            .assertIsDisplayed()
    }
}
```

### 4.5 Testing Commands

```bash
# Run all unit tests
./gradlew test

# Run all UI tests (Android device required)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests CreateTaskUseCaseTest

# Run with coverage
./gradlew testDebugUnitTestCoverage

# View coverage report
open app/build/reports/coverage/index.html
```

---

## 5. CI/CD PIPELINE

### 5.1 GitHub Actions Workflow

**File: `.github/workflows/ci.yml`**

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle') }}

      - name: Run lint
        run: ./gradlew lint

      - name: Run unit tests
        run: ./gradlew test

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/tests/

  build:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build APK
        run: ./gradlew assembleDebug

      - name: Build AAB
        run: ./gradlew bundleDebugRelease

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug.apk
          path: app/build/outputs/apk/debug/app-debug.apk

  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Run Detekt (static analysis)
        run: ./gradlew detekt

      - name: Check for secrets
        uses: trufflesecurity/trufflehog@main
        with:
          path: ./
          base: ${{ github.event.repository.default_branch }}
          head: HEAD

      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          path: '.'
          format: 'JSON'

  deploy-internal:
    runs-on: ubuntu-latest
    needs: [ test, build, security ]
    if: github.ref == 'refs/heads/develop'
    steps:
      - uses: actions/checkout@v3

      - name: Deploy to Firebase App Distribution
        run: |
          echo "Deploying to internal testers..."
          # Configure Firebase CLI
          # Upload APK to internal test track
```

### 5.2 Local Pre-Commit Checks

**File: `.husky/pre-commit`**

```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

echo "Running pre-commit checks..."

# Format code
echo "Formatting code..."
./gradlew spotlessApply

# Lint
echo "Running lint..."
./gradlew lintDebug --continue

# Static analysis
echo "Running detekt..."
./gradlew detekt

# Unit tests
echo "Running unit tests..."
./gradlew testDebugUnitTest

echo "✓ Pre-commit checks passed"
```

### 5.3 Monitoring CI/CD

```
Dashboard: GitHub Actions (github.com/yourorg/voicetasker/actions)

Track:
- Build status (pass/fail)
- Test coverage (target: 80%+)
- Code quality (Detekt issues)
- Security (dependency vulnerabilities)
- Deployment status
```

---

## 6. BUILD PROCESS

### 6.1 Debug Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on emulator/device
./gradlew installDebug

# Run app
./gradlew runDebug

# All in one
./gradlew installDebugAndRun
```

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

### 6.2 Release Build

```bash
# 1. Update version
# In build.gradle.kts:
versionCode = 2  # Increment for each release
versionName = "0.2.0"

# 2. Update CHANGELOG
# In CHANGELOG.md: Add release notes

# 3. Build release bundle
./gradlew bundleRelease

# 4. Sign APK (for testing)
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release.keystore \
  app/build/outputs/bundle/release/app-release.aab \
  release_key

# 5. Verify
zipalign -v 4 app-release.apk app-release-aligned.apk
```

**Output**:
- `app/build/outputs/bundle/release/app-release.aab` (for Play Store)
- `app/build/outputs/apk/release/app-release.apk` (for direct distribution)

### 6.3 Build Configuration

**File: `gradle.properties`**

```properties
# Kotlin
kotlin.jvm.target=17

# Android Gradle Plugin
android.useNewApkCreator=true
android.enableJetifier=false

# Gradle
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true

# Compose
compose.metrics=false
compose.reports=false
```

---

## 7. DEPLOYMENT STRATEGY

### 7.1 Release Channels

**Phase 1-2 (MVP)**:
- Internal Testing (20-30 team + friends)
- 1 week → fix critical bugs

**Phase 3 (Pre-Launch)**:
- Closed Beta (100-500 testers)
- 2 weeks → gather feedback

**Phase 4 (Public)**:
- Open Release (Play Store)
- Phased rollout (10% → 50% → 100%)

### 7.2 Firebase App Distribution

**Setup**:
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Authenticate
firebase login

# Create app distribution project
firebase apps:create android --project=voicetasker
```

**Deploy APK**:
```bash
# Build APK
./gradlew assembleDebug

# Deploy to testers
firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk \
  --project=voicetasker \
  --groups="testers" \
  --release-notes="Fixed STT timeout bug"
```

**Testers receive email** with download link.

### 7.3 Google Play Store Upload

**Prerequisites**:
- [ ] Google Play Developer account ($25 one-time)
- [ ] App signed with release keystore
- [ ] Privacy policy (online)
- [ ] Data Safety form (completed)
- [ ] Store listing (screenshots, description)

**Steps**:

1. **Create Release**
   - Go to Google Play Console
   - Create Release in "Internal Testing" track

2. **Upload AAB**
   ```bash
   ./gradlew bundleRelease
   # Upload app/build/outputs/bundle/release/app-release.aab
   ```

3. **Add Release Notes**
   ```
   Version 0.1.0 (MVP Release)
   - Voice task creation
   - Local task storage
   - Basic reminders
   - Google Sign-In
   ```

4. **Review & Submit**
   - Google reviews (24-48 hours)
   - Approved → available on Play Store

### 7.4 Phased Rollout

**Day 1**: 10% of users (catch critical issues)
```
Play Console → Release → Configure Rollout → 10%
```

**Day 3**: 50% (if no critical issues)
**Day 7**: 100% (full rollout)

**Monitor**: Crash rate, star rating, user reviews

---

## 8. MONITORING & MAINTENANCE

### 8.1 Monitoring Stack

**Tools**:
- Firebase Crashlytics (crash reporting)
- Firebase Analytics (user behavior)
- Firebase Performance (latency)
- Sentry (error tracking, alternative)

### 8.2 Dashboard Setup

**Firebase Console**:
```
Dashboard:
├── Crashlytics
│   └── Daily crash rate (target: < 0.5%)
├── Analytics
│   ├── Daily active users
│   ├── Voice task creation rate
│   └── Feature adoption
└── Performance
    ├── App start time
    ├── STT latency
    └── LLM latency
```

### 8.3 Alerts & On-Call

**Alert Thresholds**:
| Metric | Alert Threshold | Owner |
|--------|-----------------|-------|
| Crash rate | > 0.5% | Android Lead |
| STT latency | > 2 sec (p95) | Voice Lead |
| LLM latency | > 3 sec (p95) | Backend Lead |
| API error rate | > 5% | Backend Lead |
| Play Store rating | < 3.5 stars | Product Lead |

**On-Call Rotation**:
- Week 1: Developer A
- Week 2: Developer B
- Week 3: Developer A
- ...

**On-Call Responsibilities**:
- Monitor dashboards (morning/evening)
- Respond to alerts within 1 hour
- Create incident ticket if needed
- Escalate to Lead if unsure

### 8.4 Post-Launch Maintenance

**Daily** (5 min):
- Check Crashlytics (new crashes?)
- Check Analytics (any anomalies?)
- Review Play Store reviews (critical feedback?)

**Weekly** (30 min):
- Review KPIs (DAU, feature adoption, crash rate)
- Check infrastructure costs (LLM, Supabase)
- Prioritize bugs from user feedback

**Monthly** (2 hours):
- Retrospective on incidents
- Update runbook
- Plan for next release

---

## 9. TEAM COMMUNICATION

### 9.1 Daily Standup (10 AM, 15 min)

**Agenda**:
1. What did I do yesterday?
2. What am I doing today?
3. Any blockers?

**Format**: Zoom call + Slack summary

**Participants**: All developers + PM + QA

### 9.2 Weekly Tech Sync (Friday, 2 PM, 60 min)

**Agenda**:
1. Review completed features (5 min)
2. Architecture decisions (15 min)
3. Technical blockers (10 min)
4. Performance review (15 min)
5. Next week planning (15 min)

**Participants**: Dev leads + Architects + PM

### 9.3 GitHub Issues (Sprint Planning)

**Template**:
```markdown
# Feature: Google Sign-In

## Description
Implement Google Sign-In using Android Credential Manager

## Acceptance Criteria
- [ ] User can tap "Continue with Google"
- [ ] Credential Manager launches
- [ ] JWT token stored securely
- [ ] Session auto-refreshes
- [ ] > 90% test coverage

## Effort Estimate
3-5 days (1 developer)

## Blocks
- Supabase Auth must be configured first

## Labels
- feature, high-priority, phase-1
```

### 9.4 Code Review Guidelines

**Reviewer Checklist**:
- [ ] Follows Clean Architecture
- [ ] Tests included + passing
- [ ] No hardcoded secrets
- [ ] Performance acceptable
- [ ] Accessibility considered
- [ ] Documentation updated

**Reviewer Comment Template**:
```
@author - Great implementation! A few thoughts:

1. **Concern**: Performance - is the list rendering on scroll optimal?
   **Suggestion**: Consider pagination for lists > 50 items

2. **Question**: Why use coroutines.cancel() instead of coroutineScope.cancel()?

Looks good overall, approve with minor changes.
```

---

## 10. SUCCESS METRICS

### 10.1 Build Metrics

| Metric | Target | Owner |
|--------|--------|-------|
| Build time (debug) | < 2 min | Android Lead |
| Build time (release) | < 5 min | Android Lead |
| APK size | < 50 MB | Android Lead |
| Cold start time | < 3 sec | Android Lead |

### 10.2 Quality Metrics

| Metric | Target | Owner |
|--------|--------|-------|
| Unit test coverage | > 80% | QA Lead |
| UI test coverage | > 40% | QA Lead |
| Crash rate | < 0.5% | QA Lead |
| ANR rate | < 0.1% | Android Lead |
| Memory leak score | 0 | Android Lead |

### 10.3 Performance Metrics

| Metric | Target | Owner |
|--------|--------|-------|
| Voice-to-confirmation latency | < 3 sec (p95) | Voice Lead |
| STT accuracy | > 85% | Voice Lead |
| LLM accuracy | > 85% | Backend Lead |
| Task list render | < 100 ms | UI Lead |
| DB query @ 10K tasks | < 50 ms | Backend Lead |

### 10.4 Release Metrics

| Metric | Target | Owner |
|--------|--------|-------|
| Release frequency | 1/month | PM |
| Time to fix critical bugs | < 24 hours | DevOps |
| Rollback success rate | 100% | DevOps |
| Play Store rating | > 4.0 stars | PM |
| User churn | < 5% /month | PM |

---

## SUMMARY

**This Master Implementation Guide provides**:

✅ Complete project scaffolding checklist
✅ Feature branch workflow (Git)
✅ Comprehensive testing strategy (Unit + Integration + UI)
✅ CI/CD pipeline (GitHub Actions)
✅ Build & deployment process
✅ Monitoring & on-call rotation
✅ Team communication structure
✅ Success metrics & KPIs

**Next Document**: `01_PROJECT_INITIALIZATION.md` (detailed setup)

---

**Status**: Ready for Phase 1 kickoff
**Duration**: 8 weeks to MVP launch
**Team Size**: 3 developers + 1 backend + 1 QA
