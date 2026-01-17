# TESTING EXECUTION GUIDE

**VoiceTasker - Comprehensive Testing Strategy**

---

## TESTING PYRAMID & TARGETS

```
Level 3: UI Tests (5%)        [Espresso, Compose]
         Target: 40% coverage

Level 2: Integration Tests (15%) [Real DB, APIs]
         Target: 60% coverage

Level 1: Unit Tests (80%)      [Mocks, fast]
         Target: 80%+ coverage
```

**Overall Coverage Target**: > 80% (measured by JaCoCo)

---

## UNIT TESTS

### Test Structure (Example: CreateTaskUseCase)

```kotlin
// feature/task/src/test/java/com/voicetasker/feature/task/domain/CreateTaskUseCaseTest.kt
package com.voicetasker.feature.task.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

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
            title = "Buy milk",
            description = null,
            dueDate = LocalDateTime.now().plusDays(1),
            status = TaskStatus.PLANNED,
            parentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            version = 1
        )
        val expectedId = "task-123"

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

    @Test
    fun `createTask should handle null title gracefully`() = runTest {
        // Arrange
        val task = Task(
            id = "",
            title = "", // Empty title
            description = null,
            dueDate = null,
            status = TaskStatus.PLANNED,
            parentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            version = 1
        )

        coEvery { mockTaskRepository.createTask(any()) } returns Result.failure(
            IllegalArgumentException("Title cannot be empty")
        )

        // Act
        val result = createTaskUseCase(task)

        // Assert
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }
}
```

### Running Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run single test class
./gradlew test --tests CreateTaskUseCaseTest

# Run single test method
./gradlew test --tests CreateTaskUseCaseTest.createTask_should_save_to_repository_and_return_ID

# Run with coverage report
./gradlew testDebugUnitTestCoverage

# View coverage report
open app/build/reports/coverage/index.html

# Run with detailed output
./gradlew test --info
```

### Test Utilities

```kotlin
// core/common/src/test/java/com/voicetasker/core/test/TestDispatchers.kt
package com.voicetasker.core.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MainDispatcherRule : TestWatcher() {
    private val testDispatcher = StandardTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

// Usage in test:
class MyTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun myTest() = runTest {
        // Use runTest { } which uses TestDispatcher
    }
}
```

---

## INTEGRATION TESTS

### Database Integration Test

```kotlin
// core/database/src/androidTest/java/com/voicetasker/core/database/TaskDaoIntegrationTest.kt
package com.voicetasker.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class TaskDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = db.taskDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveTask() = runTest {
        // Arrange
        val task = TaskEntity(
            id = "task-1",
            userId = "user-123",
            title = "Test task",
            description = "Description",
            status = "PLANNED",
            dueDate = LocalDateTime.now(),
            priority = "HIGH"
        )

        // Act
        taskDao.insertTask(task)
        val retrieved = taskDao.getTaskById("task-1")

        // Assert
        assertNotNull(retrieved)
        assertEquals("Test task", retrieved?.title)
        assertEquals("user-123", retrieved?.userId)
    }

    @Test
    fun observeTasksEmitsCorrectData() = runTest {
        // Arrange
        val task1 = TaskEntity(
            id = "task-1",
            userId = "user-123",
            title = "Task 1",
            status = "PLANNED"
        )
        val task2 = TaskEntity(
            id = "task-2",
            userId = "user-123",
            title = "Task 2",
            status = "PLANNED"
        )

        // Act
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        val tasks = taskDao.observeUserTasks("user-123").first()

        // Assert
        assertEquals(2, tasks.size)
        assertTrue(tasks.any { it.title == "Task 1" })
        assertTrue(tasks.any { it.title == "Task 2" })
    }

    @Test
    fun updateTaskModifiesData() = runTest {
        // Arrange
        val task = TaskEntity(
            id = "task-1",
            userId = "user-123",
            title = "Original title",
            status = "PLANNED"
        )
        taskDao.insertTask(task)

        // Act
        val updated = task.copy(title = "Updated title", status = "IN_PROGRESS")
        taskDao.updateTask(updated)
        val retrieved = taskDao.getTaskById("task-1")

        // Assert
        assertEquals("Updated title", retrieved?.title)
        assertEquals("IN_PROGRESS", retrieved?.status)
    }

    @Test
    fun deleteTaskRemovesData() = runTest {
        // Arrange
        val task = TaskEntity(
            id = "task-1",
            userId = "user-123",
            title = "To delete",
            status = "PLANNED"
        )
        taskDao.insertTask(task)

        // Act
        taskDao.deleteTask(task)
        val retrieved = taskDao.getTaskById("task-1")

        // Assert
        assertNull(retrieved)
    }
}
```

### Repository Integration Test

```kotlin
// feature/task/src/androidTest/java/com/voicetasker/feature/task/data/TaskRepositoryIntegrationTest.kt
package com.voicetasker.feature.task.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskRepositoryIntegrationTest {

    private lateinit var repository: TaskRepository
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val taskDao = db.taskDao()
        val localDataSource = LocalTaskDataSource(taskDao)
        val remoteDataSource = mockk<RemoteTaskDataSource>() // Mock remote

        repository = TaskRepositoryImpl(localDataSource, remoteDataSource)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun createTask_savesToLocalDatabase() = runTest {
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

        // Act
        val result = repository.createTask(task)

        // Assert
        assertTrue(result.isSuccess)
        val tasks = repository.getAllTasks("user-123").first()
        assertEquals(1, tasks.size)
        assertEquals("Test task", tasks[0].title)
    }
}
```

---

## UI TESTS (Compose)

### Home Screen Test

```kotlin
// feature/task/src/androidTest/java/com/voicetasker/feature/task/presentation/HomeScreenTest.kt
package com.voicetasker.feature.task.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    fun homeScreen_clickingMicButton_startsRecording() {
        composeTestRule
            .onNodeWithContentDescription("Voice task")
            .performClick()

        composeTestRule
            .onNodeWithText("Listening...")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_displayEmptyStateWhenNoTasks() {
        composeTestRule
            .onNodeWithText("No tasks yet. Tap the mic to add one!")
            .assertIsDisplayed()
    }
}
```

---

## RUNNING TESTS

### All Tests

```bash
# Run all unit tests (fast, ~1 min)
./gradlew test

# Run all integration/UI tests (slow, ~5-10 min)
./gradlew connectedAndroidTest

# Run all tests (unit + integration)
./gradlew test connectedAndroidTest
```

### Specific Tests

```bash
# Run specific test class
./gradlew test --tests CreateTaskUseCaseTest

# Run specific test method
./gradlew test --tests CreateTaskUseCaseTest.createTask_should_save_to_repository_and_return_ID

# Run tests matching pattern
./gradlew test --tests "*UseCase*"
```

### Coverage Reports

```bash
# Generate coverage report (JaCoCo)
./gradlew testDebugUnitTestCoverage jacocoTestReport

# View HTML report
open app/build/reports/jacoco/index.html

# Check coverage threshold (fail if < 80%)
./gradlew check -x lint

# View XML report
cat app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
```

---

## CONTINUOUS TESTING (CI/CD)

### GitHub Actions Workflow

**File: `.github/workflows/test.yml`**

```yaml
name: Test Suite

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}

      - name: Run unit tests
        run: ./gradlew test

      - name: Generate coverage report
        run: ./gradlew testDebugUnitTestCoverage jacocoTestReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
          fail_ci_if_error: false

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/tests/

  integration-tests:
    runs-on: macos-latest  # macOS supports emulator
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Gradle cache
        uses: gradle/gradle-build-action@v2

      - name: Run espresso tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          script: ./gradlew connectedAndroidTest

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: integration-test-results
          path: app/build/reports/androidTests/connected/
```

---

## TEST CHECKLIST (Before Release)

- [ ] Unit tests: 80%+ coverage
- [ ] Integration tests: All critical flows
- [ ] UI tests: All screens
- [ ] Manual QA: Device testing (2+ devices)
- [ ] Performance tests: Latency < targets
- [ ] Crash tests: Edge cases
- [ ] Security tests: Input validation
- [ ] Accessibility tests: TalkBack, color contrast
- [ ] CI/CD pipeline: All tests passing
- [ ] Regression tests: Previous fixes still working

---

**Status**: Ready for Phase 1 development
