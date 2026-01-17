# TECHNICAL ARCHITECTURE & IMPLEMENTATION GUIDE

**VoiceTasker - Technical Specification**

---

## TABLE OF CONTENTS

1. Architecture Overview
2. Technology Stack
3. System Architecture Layers
4. Data Flow & Pipelines
5. Backend Architecture (Supabase)
6. Frontend Architecture (Android)
7. Voice Processing Pipeline
8. AI Integration
9. Database Schema & Queries
10. API Specifications
11. Security & Encryption
12. Performance Optimizations
13. Testing Strategy

---

## 1. ARCHITECTURE OVERVIEW

### 1.1 Architectural Pattern

**Pattern**: Clean Architecture + MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────────────┐
│             UI Layer (Compose)                  │
│  ┌──────────────────────────────────────────┐  │
│  │ HomeScreen | TaskDetailScreen | etc.     │  │
│  └──────────────────────────────────────────┘  │
├─────────────────────────────────────────────────┤
│         Presentation Layer (ViewModel)          │
│  ┌──────────────────────────────────────────┐  │
│  │ TaskListViewModel | VoiceViewModel | ... │  │
│  │ + State Management (LiveData/Flow)       │  │
│  └──────────────────────────────────────────┘  │
├─────────────────────────────────────────────────┤
│          Domain Layer (Use Cases)               │
│  ┌──────────────────────────────────────────┐  │
│  │ CreateTaskUseCase                        │  │
│  │ ProcessVoiceUseCase                      │  │
│  │ SyncTasksUseCase                         │  │
│  │ + Domain Models (entities, interfaces)   │  │
│  └──────────────────────────────────────────┘  │
├─────────────────────────────────────────────────┤
│        Data Layer (Repository)                  │
│  ┌──────────────────────────────────────────┐  │
│  │ TaskRepository                           │  │
│  │ ReminderRepository                       │  │
│  │ + Local (Room) + Remote (Supabase)       │  │
│  └──────────────────────────────────────────┘  │
├─────────────────────────────────────────────────┤
│   Infrastructure (Services & External APIs)     │
│  ┌──────────────────────────────────────────┐  │
│  │ SpeechToTextService (Deepgram)           │  │
│  │ LLMService (OpenAI via Supabase)         │  │
│  │ AuthService (Supabase Auth)              │  │
│  │ SyncService (Supabase Realtime)          │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

### 1.2 Modular Structure

```
VoiceTasker/
├── app/                          # App-level config, MainActivity
├── core/
│   ├── common/                   # Utilities, extensions, constants
│   ├── database/                 # Room database setup, entities, DAOs
│   ├── datastore/                # Encrypted preferences
│   ├── network/                  # Supabase client, interceptors
│   └── security/                 # Encryption, keystore management
├── feature/
│   ├── auth/                     # Authentication module
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   ├── task/                     # Task management module
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   ├── voice/                    # Voice processing module
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   ├── reminder/                 # Reminder & alarm module
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   ├── sync/                     # Cloud sync module
│   │   ├── presentation/
│   │   ├── domain/
│   │   └── data/
│   └── settings/                 # Settings & preferences
├── buildSrc/                     # Build configuration, version management
└── gradle/                       # Gradle wrapper

```

---

## 2. TECHNOLOGY STACK

### 2.1 Frontend (Android)

| Component | Technology | Version | Rationale |
|-----------|-----------|---------|-----------|
| **Language** | Kotlin | 1.9+ | Modern, null-safe, Android-first |
| **UI Framework** | Jetpack Compose | Latest | Modern, reactive, state-driven |
| **Architecture** | Clean Arch + MVVM | N/A | Testable, modular, maintainable |
| **DI Framework** | Hilt | 2.48+ | Built by Google, integrates with Compose |
| **Local DB** | Room | 2.6+ | Type-safe, reactive, works offline |
| **State Management** | Flow + ViewModel | N/A | Coroutine-native, reactive |
| **Networking** | Retrofit + OkHttp | 4.11+ | Well-tested, interceptor support |
| **JSON Serialization** | Kotlinx Serialization | 1.6+ | Multiplatform, type-safe |
| **Async** | Kotlin Coroutines | 1.7+ | Structured concurrency |
| **Security** | Android Security Crypto | 1.1+ | Encrypted SharedPreferences |
| **Permissions** | Jetpack Core (Permissions API) | 1.14+ | Modern permission handling |
| **WorkManager** | WorkManager | 2.9+ | Flexible background scheduling |

### 2.2 Backend (Supabase)

| Component | Technology | Notes |
|-----------|-----------|-------|
| **Database** | PostgreSQL | Relational, RLS policies |
| **Auth** | Supabase Auth | Google Sign-In + Email/password |
| **Realtime** | Supabase Realtime | WebSocket subscriptions for sync |
| **Serverless** | Edge Functions | API key proxy (OpenAI) |
| **Storage** | Supabase Storage | Future: attachments (Phase 3+) |

### 2.3 AI/LLM & Voice Services

| Service | Provider | Purpose | Method |
|---------|----------|---------|--------|
| **Speech-to-Text (English/Hindi)** | Deepgram Nova-3 | Transcription | REST API via Supabase |
| **Speech-to-Text (Bengali/Urdu)** | Google Chirp 3 | Transcription | REST API via Supabase |
| **Offline STT Fallback** | Vosk / SileroVAD | On-device transcription | Offline model |
| **LLM** | OpenAI GPT-4 / GPT-5 | Intent extraction | REST API via Supabase Edge Function |
| **Text-to-Speech** | Android TextToSpeech API | TTS responses | System service (on-device) |

### 2.4 External Services

| Service | Purpose | Auth Method | Rationale |
|---------|---------|-------------|-----------|
| **Google Sign-In** | User authentication | OAuth 2.0 | Familiar, secure |
| **Stripe** | Payment processing | API key (server-side only) | Phase 3+, PCI-compliant |
| **Supabase** | Backend | JWT tokens | All-in-one backend solution |

---

## 3. SYSTEM ARCHITECTURE LAYERS

### 3.1 Presentation Layer (UI)

**Responsibility**: Render UI, handle user interactions, pass to ViewModels.

**Key Components**:

1. **Screens** (Compose):
   - `HomeScreen` - Task list + mic button
   - `TaskDetailScreen` - Task editing
   - `VoiceReviewScreen` - Review extracted tasks
   - `SettingsScreen` - User preferences
   - `AuthScreen` - Login/signup

2. **ViewModels**:
   - `TaskListViewModel` - Manage task list state
   - `VoiceViewModel` - Handle voice interactions
   - `AuthViewModel` - Auth state management
   - `ReminderViewModel` - Reminder state

3. **State Management**:
   - All state flows through ViewModel
   - UI observes via Flow/LiveData
   - No direct database access in composables

### 3.2 Domain Layer (Business Logic)

**Responsibility**: Define business rules, use cases, interfaces.

**Key Components**:

1. **Use Cases**:
   ```kotlin
   CreateTaskUseCase(taskRepository: TaskRepository) {
       suspend operator fun invoke(task: Task): Result<String>
   }

   ProcessVoiceUseCase(
       speechService: SpeechToTextService,
       llmService: LLMService,
       taskRepository: TaskRepository
   ) {
       suspend operator fun invoke(audioFile: File): Result<List<Task>>
   }

   SyncTasksUseCase(taskRepository: TaskRepository) {
       suspend operator fun invoke(): Result<Unit>
   }
   ```

2. **Domain Models**:
   ```kotlin
   // Domain entity (pure Kotlin, no framework deps)
   data class Task(
       val id: String,
       val title: String,
       val description: String?,
       val dueDate: LocalDateTime?,
       val status: TaskStatus,
       val parentId: String?,
       val createdAt: LocalDateTime,
       val updatedAt: LocalDateTime,
       val version: Int
   )

   enum class TaskStatus { PLANNED, IN_PROGRESS, COMPLETED, CANCELLED }
   ```

3. **Repository Interfaces** (abstracted):
   ```kotlin
   interface TaskRepository {
       suspend fun createTask(task: Task): Result<String>
       suspend fun updateTask(task: Task): Result<Unit>
       suspend fun deleteTask(taskId: String): Result<Unit>
       fun observeTasks(): Flow<List<Task>>
       suspend fun syncTasks(): Result<Unit>
   }
   ```

### 3.3 Data Layer (Repositories & Data Sources)

**Responsibility**: Fetch/store data from local DB and remote API.

**Key Components**:

1. **Repositories** (implementations):
   ```kotlin
   class TaskRepositoryImpl(
       private val localDataSource: LocalTaskDataSource,
       private val remoteDataSource: RemoteTaskDataSource
   ) : TaskRepository {
       override suspend fun createTask(task: Task): Result<String> {
           // 1. Save locally (offline-first)
           val localId = localDataSource.insertTask(task)

           // 2. Sync to remote (background)
           remoteDataSource.syncTask(task)
               .onFailure { /* queue retry */ }

           return Result.success(localId)
       }

       override fun observeTasks(): Flow<List<Task>> {
           return localDataSource.observeAllTasks() // Always serve local data first
               .shareIn(scope, SharingStarted.Lazily)
       }
   }
   ```

2. **Local Data Source** (Room):
   ```kotlin
   @Dao
   interface TaskDao {
       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun insertTask(task: TaskEntity)

       @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY updated_at DESC")
       fun observeUserTasks(userId: String): Flow<List<TaskEntity>>

       @Query("""
           WITH RECURSIVE task_tree AS (
               SELECT * FROM tasks WHERE parent_id IS NULL
               UNION ALL
               SELECT t.* FROM tasks t
               JOIN task_tree ON t.parent_id = task_tree.id
           )
           SELECT * FROM task_tree WHERE user_id = :userId
       """)
       fun observeTaskHierarchy(userId: String): Flow<List<TaskEntity>>
   }
   ```

3. **Remote Data Source** (Supabase):
   ```kotlin
   class RemoteTaskDataSource(private val supabase: SupabaseClient) {
       suspend fun syncTask(task: Task): Result<Unit> {
           return try {
               supabase.from("tasks")
                   .upsert(task.toJson()) // Last-write-wins with client timestamp
               Result.success(Unit)
           } catch (e: Exception) {
               Result.failure(e)
           }
       }
   }
   ```

---

## 4. DATA FLOW & PIPELINES

### 4.1 Voice-to-Task Pipeline

```
User taps mic
    ↓
VoiceViewModel.startRecording()
    ↓
SpeechToTextService.recordAndTranscribe()
    • Foreground service starts
    • Mic opens (permission check)
    • Record until silence (3-sec timeout)
    • Send to Deepgram/Google
    ↓
TranscriptionResult (text)
    ↓
LLMService.extractIntent()
    • Send transcript + context to OpenAI
    • OpenAI function calling: extract_task_from_speech()
    ↓
StructuredIntent JSON
    {
        "action": "create_task",
        "tasks": [...],
        "confidence": 0.92
    }
    ↓
VoiceViewModel updates state
    • Emit extracted tasks
    ↓
UI shows review screen
    ↓
User confirms
    ↓
CreateTaskUseCase.invoke(tasks)
    ↓
TaskRepositoryImpl.createTask()
    • Insert into Room (local)
    • Async sync to Supabase
    ↓
Task saved ✓
```

### 4.2 Reminder Pipeline

```
User creates task with reminder
    ↓
ReminderRepository.scheduleReminder(reminder)
    ↓
Check SCHEDULE_EXACT_ALARM permission
    ↓
If yes: AlarmManager.setAndAllowWhileIdle()
If no: WorkManager.scheduleExactMetaData()
    ↓
Scheduled time arrives
    ↓
AlarmManager/WorkManager triggers
    ↓
ReminderBroadcastReceiver.onReceive()
    ↓
NotificationManager.notify(notificationId, notification)
    • Notification shows task title
    • Actions: Mark done, Snooze, Reply with voice
    ↓
User interacts with notification
    ↓
If "Mark done":
    • TaskRepository.completeTask()
    • Reminder marked as FIRED
If "Snooze":
    • ReminderRepository.snooze(15 min)
If "Reply with voice":
    • Start foreground service
    • Record voice response
    • AI interprets action
    • Execute action
    ↓
Notification dismissed / action completed
```

### 4.3 Sync Pipeline

```
Task created locally (online)
    ↓
TaskRepository.createTask()
    • Insert into Room (immediate)
    • Send to Supabase (async, background)
    ↓
If sync succeeds:
    • Task marked as synced (version incremented)
If sync fails:
    • Task queued for retry (WorkManager)
    • Exponential backoff: 1s, 5s, 30s, 5m, 30m
    ↓
Other device updates same task
    ↓
Supabase Realtime subscription notifies
    ↓
SyncViewModel updates local Room
    • Conflict resolution: last-write-wins (client timestamp)
    ↓
UI observes Flow<Task>
    ↓
UI updates automatically
```

---

## 5. BACKEND ARCHITECTURE (Supabase)

### 5.1 Supabase Structure

**Database (PostgreSQL)**
- Tables: tasks, reminders, profiles, entitlements, voice_logs
- RLS policies: Users can only access their own data
- Triggers: Auto-update updated_at timestamp

**Auth (Supabase Auth)**
- Providers: Google OAuth, Email/password
- Tokens: JWT (short-lived) + refresh tokens
- Keystore: Android Keystore (hardware-backed if available)

**Edge Functions**
- `process_voice` - Proxy to Deepgram/Google STT
- `extract_intent` - Proxy to OpenAI with function calling
- `sync_task` - Handle task sync with versioning logic

**Realtime (WebSocket)**
- Subscribe to `tasks` table changes
- Notify clients when task updated by other device

### 5.2 Edge Functions (Critical Security Boundary)

**Why Edge Functions?**

Never expose API keys in Android app. Use Edge Functions as proxy.

**Function: extract_intent**

```typescript
// supabase/functions/extract_intent/index.ts
import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.38.0";

const openaiKey = Deno.env.get("OPENAI_API_KEY"); // Server secret
const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
);

serve(async (req) => {
    const { transcript, timezone, language } = await req.json();

    // Call OpenAI (API key never sent to client)
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${openaiKey}`,
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            model: "gpt-4-turbo",
            functions: [{
                name: "extract_tasks",
                parameters: {
                    // Schema for structured output
                }
            }],
            messages: [{
                role: "user",
                content: `Extract tasks from: ${transcript}`
            }]
        })
    });

    return new Response(JSON.stringify(response), {
        headers: { "Content-Type": "application/json" }
    });
});
```

**Deploy**: `supabase functions deploy extract_intent`

---

## 6. FRONTEND ARCHITECTURE (Android)

### 6.1 ViewModel Pattern (MVVM)

**Example: TaskListViewModel**

```kotlin
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val tasksUseCase: GetAllTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val syncUseCase: SyncTasksUseCase,
) : ViewModel() {

    // UI State (sealed class for type-safety)
    sealed class UiState {
        object Loading : UiState()
        data class Success(val tasks: List<Task>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            tasksUseCase()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { tasks ->
                    _uiState.value = UiState.Success(tasks)
                }
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            createTaskUseCase(task)
                .onSuccess {
                    // Task created, UI will auto-update via Flow
                }
                .onFailure { e ->
                    _uiState.value = UiState.Error(e.message ?: "Create failed")
                }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
                .onFailure { e ->
                    _uiState.value = UiState.Error(e.message ?: "Delete failed")
                }
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            syncUseCase()
                .onFailure { e ->
                    _uiState.value = UiState.Error("Sync failed: ${e.message}")
                }
        }
    }
}
```

### 6.2 Compose UI

**Example: HomeScreen**

```kotlin
@Composable
fun HomeScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    onTaskClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Start voice recording */ }
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is TaskListViewModel.UiState.Loading -> {
                    CircularProgressIndicator()
                }
                is TaskListViewModel.UiState.Success -> {
                    LazyColumn {
                        items(state.tasks) { task ->
                            TaskItem(
                                task = task,
                                onClick = { onTaskClick(task.id) }
                            )
                        }
                    }
                }
                is TaskListViewModel.UiState.Error -> {
                    Text(state.message, color = Color.Red)
                }
            }
        }
    }
}
```

---

## 7. VOICE PROCESSING PIPELINE (Detailed)

### 7.1 Component: SpeechToTextService

```kotlin
interface SpeechToTextService {
    suspend fun recordAndTranscribe(
        timeoutMs: Long = 30000,
        language: String = "en"
    ): Result<String>
}

@Singleton
class SpeechToTextServiceImpl @Inject constructor(
    private val context: Context,
    private val deepgramClient: DeepgramClient,
    private val localStfModel: LocalSTFModel
) : SpeechToTextService {

    override suspend fun recordAndTranscribe(
        timeoutMs: Long,
        language: String
    ): Result<String> = withContext(Dispatchers.Default) {
        return@withContext try {
            // Step 1: Start foreground service (visible to user)
            val foregroundService = startForegroundService()

            // Step 2: Record audio
            val audioFile = recordAudio(timeoutMs)

            // Step 3: Transcribe (prefer local, fallback to Deepgram)
            val transcript = if (isOnlineAndQuotaAvailable()) {
                // Use Deepgram (higher accuracy)
                deepgramClient.transcribe(audioFile, language)
                    .onFailure {
                        // Fallback to local model
                        localStfModel.transcribe(audioFile)
                    }
            } else {
                // Offline mode: use local model
                localStfModel.transcribe(audioFile)
            }

            // Step 4: Stop foreground service
            stopForegroundService(foregroundService)

            transcript
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun recordAudio(timeoutMs: Long): File {
        // Use Android AudioRecord to capture raw audio
        val audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.wav")
        val audioRecord = AudioRecord(
            AudioSource.MIC,
            16000, // 16 kHz sample rate
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioRecord.getMinBufferSize(...)
        )

        audioRecord.startRecording()

        // Record until silence or timeout
        val silenceThreshold = 500 // ms
        val buffer = ShortArray(1024)
        val startTime = System.currentTimeMillis()
        var lastSoundTime = startTime

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val readSize = audioRecord.read(buffer, 0, buffer.size)
            if (readSize > 0) {
                val rms = calculateRMS(buffer)
                if (rms > SILENCE_THRESHOLD) {
                    lastSoundTime = System.currentTimeMillis()
                }

                // Write to file
                audioFile.appendAudio(buffer, readSize)
            }

            // Exit if silence detected
            if (System.currentTimeMillis() - lastSoundTime > silenceThreshold) {
                break
            }
        }

        audioRecord.stop()
        audioRecord.release()

        return audioFile
    }
}
```

### 7.2 Component: LLMService

```kotlin
interface LLMService {
    suspend fun extractIntent(
        transcript: String,
        context: LLMContext
    ): Result<StructuredIntent>
}

@Singleton
class LLMServiceImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : LLMService {

    override suspend fun extractIntent(
        transcript: String,
        context: LLMContext
    ): Result<StructuredIntent> {
        return try {
            val response = supabaseClient.functions
                .invoke(
                    functionName = "extract_intent",
                    data = mapOf(
                        "transcript" to transcript,
                        "timezone" to context.timezone,
                        "language" to context.language,
                        "recent_tasks" to context.recentTasks.map { it.toJson() }
                    )
                )

            val intent = json.decodeFromString<StructuredIntent>(response.data.toString())
            Result.success(intent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Structured output from AI
data class StructuredIntent(
    val action: String, // "create_task", "update_task", "complete_task", "clarify"
    val tasks: List<ExtractedTask> = emptyList(),
    val clarificationQuestions: List<ClarificationQuestion> = emptyList(),
    val confidence: Double,
    val explanation: String
)

data class ExtractedTask(
    val title: String,
    val dueDate: LocalDateTime?,
    val priority: String = "MEDIUM",
    val reminder: ReminderConfig? = null,
    val confidence: Double
)
```

---

## 8. AI INTEGRATION (Constraints & Best Practices)

### 8.1 Prompt Engineering

**System Prompt** (sent to OpenAI):

```
You are a task extraction AI. Your job is to parse natural language speech
and extract structured tasks.

RULES:
1. Extract task title (required)
2. Extract due date if mentioned (use provided timezone for relative dates)
3. Assign confidence score (0.0-1.0) based on clarity
4. If ambiguous, ask clarification INSTEAD of guessing
5. Return ONLY valid JSON (no free text)

TASK SCHEMA:
{
  "title": "string (required)",
  "due_date": "ISO 8601 or null",
  "priority": "LOW|MEDIUM|HIGH",
  "reminder": {
    "time": "ISO 8601",
    "type": "NOTIFICATION|ALARM|FLEXIBLE"
  },
  "confidence": 0.0-1.0
}

EXAMPLES:
User: "Add task call mom tomorrow at 5 PM"
Response:
{
  "action": "create_task",
  "tasks": [{
    "title": "Call mom",
    "due_date": "2026-01-18T17:00:00",
    "priority": "MEDIUM",
    "reminder": {"time": "2026-01-18T17:00:00", "type": "NOTIFICATION"},
    "confidence": 0.95
  }]
}

User: "Remind me to call John tomorrow morning"
Response:
{
  "action": "clarify",
  "clarification_questions": [{
    "field": "due_date",
    "question": "What time tomorrow morning?",
    "suggestions": ["8:00 AM", "9:00 AM", "10:00 AM"]
  }]
}
```

### 8.2 Fallback Strategy (No LLM)

If OpenAI API fails:

```kotlin
// Fallback to rule-based extraction
fun fallbackExtractIntent(transcript: String): StructuredIntent {
    // Simple regex/string matching
    val titleMatch = Regex("(?:add task|create|task)\\s+(.+?)(?:\\s+(?:by|at|tomorrow|today))?").find(transcript)
    val title = titleMatch?.groupValues?.get(1) ?: transcript.take(50)

    val dateMatch = Regex("(tomorrow|today|next\\s+\\w+|\\d{1,2}/\\d{1,2})").find(transcript)
    val dueDate = dateMatch?.let { parseSimpleDate(it.value) }

    return StructuredIntent(
        action = "create_task",
        tasks = listOf(ExtractedTask(title, dueDate, confidence = 0.60)),
        confidence = 0.60,
        explanation = "Fallback extraction (LLM unavailable)"
    )
}
```

---

## 9. DATABASE SCHEMA & QUERIES

### 9.1 Room Entities

```kotlin
@Entity(tableName = "tasks", indices = [
    Index("user_id"),
    Index("parent_id"),
    Index("status")
])
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String? = null,
    val status: String = "PLANNED", // PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    val dueDate: LocalDateTime? = null,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val parentId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Int = 1,
    val synced: Boolean = false
)

@Entity(tableName = "reminders", indices = [
    Index("task_id"),
    Index("user_id"),
    Index("scheduled_time")
])
data class ReminderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    @ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["task_id"],
        onDelete = ForeignKey.CASCADE
    )
    val taskId: String,
    val scheduledTime: LocalDateTime,
    val reminderType: String = "NOTIFICATION", // NOTIFICATION, ALARM, FLEXIBLE
    val status: String = "PENDING", // PENDING, FIRED, DISMISSED, SNOOZED
    val snoozedUntil: LocalDateTime? = null
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val timezone: String = "UTC",
    val language: String = "en",
    val ttsEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

### 9.2 Complex Queries

**Query: Get task hierarchy (all descendants)**

```kotlin
@Dao
interface TaskDao {
    @Query("""
        WITH RECURSIVE task_tree AS (
            SELECT * FROM tasks
            WHERE user_id = :userId AND parent_id IS NULL

            UNION ALL

            SELECT t.* FROM tasks t
            JOIN task_tree tt ON t.parent_id = tt.id
            WHERE t.user_id = :userId
        )
        SELECT * FROM task_tree ORDER BY parent_id, updated_at DESC
    """)
    fun getTaskHierarchy(userId: String): Flow<List<TaskEntity>>
}
```

**Query: Get tasks with reminders**

```kotlin
@Dao
interface TaskDao {
    @Transaction
    @Query("""
        SELECT t.*, r.* FROM tasks t
        LEFT JOIN reminders r ON t.id = r.task_id
        WHERE t.user_id = :userId
        AND t.status != 'COMPLETED'
        ORDER BY r.scheduled_time ASC
    """)
    fun getTasksWithReminders(userId: String): Flow<List<TaskWithReminders>>
}

data class TaskWithReminders(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "task_id"
    )
    val reminders: List<ReminderEntity>
)
```

---

## 10. API SPECIFICATIONS

### 10.1 Supabase Edge Function: extract_intent

**Endpoint**: `POST /functions/v1/extract_intent`

**Request**:
```json
{
  "transcript": "Call mom tomorrow at 5 PM",
  "timezone": "America/New_York",
  "language": "en",
  "recent_tasks": [
    {"id": "...", "title": "Call dad", "dueDate": "..."},
    {"id": "...", "title": "Submit report", "dueDate": "..."}
  ]
}
```

**Response** (Success):
```json
{
  "action": "create_task",
  "tasks": [{
    "title": "Call mom",
    "due_date": "2026-01-18T17:00:00Z",
    "priority": "MEDIUM",
    "reminder": {
      "time": "2026-01-18T17:00:00Z",
      "type": "NOTIFICATION"
    },
    "confidence": 0.95
  }],
  "confidence": 0.95,
  "explanation": "Parsed task with tomorrow's date"
}
```

**Response** (Clarification):
```json
{
  "action": "clarify",
  "clarification_questions": [{
    "field": "due_date",
    "question": "What time tomorrow?",
    "suggestions": ["Morning", "Afternoon", "Evening"]
  }],
  "confidence": 0.70
}
```

---

## 11. SECURITY & ENCRYPTION

### 11.1 Local Encryption (Room)

```kotlin
@Database(
    entities = [TaskEntity::class, ReminderEntity::class, ProfileEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun profileDao(): ProfileDao

    companion object {
        fun create(context: Context): AppDatabase {
            // Generate encryption key (stored in Android Keystore)
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Create encrypted database
            val supportFactory = EncryptedRoomDatabaseBuilder.Factory {
                RoomDatabase.EncryptedDatabase(it, masterKey)
            }

            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "voice_tasker.db"
            )
                .openHelperFactory(supportFactory)
                .build()
        }
    }
}
```

### 11.2 Token Storage (Secure)

```kotlin
@Singleton
class SecureTokenStorage @Inject constructor(
    context: Context
) {
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        encryptedPrefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? = encryptedPrefs.getString("auth_token", null)

    fun clearAuthToken() {
        encryptedPrefs.edit().remove("auth_token").apply()
    }
}
```

### 11.3 Network Security (TLS 1.3)

```kotlin
@Singleton
@Provides
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectionSpecs(listOf(
            ConnectionSpec.MODERN_TLS, // TLS 1.2+
            ConnectionSpec.COMPATIBLE_TLS // TLS 1.0-1.2 (minimal fallback)
        ))
        .certificatePinner(
            CertificatePinner.Builder()
                .add("api.supabase.co", "sha256/...") // Pin Supabase certificate
                .build()
        )
        .addInterceptor(AuthTokenInterceptor()) // Add JWT to all requests
        .build()
}
```

---

## 12. PERFORMANCE OPTIMIZATIONS

### 12.1 Latency Targets

| Operation | Target | Strategy |
|-----------|--------|----------|
| STT | < 300ms | On-device when possible |
| AI (LLM) | < 2s | Streaming if available, timeout 5s |
| Voice → Confirmation | < 2s | Parallel processing |
| Task list render | < 100ms | Room Flow + Compose lazy loading |
| DB query (10K tasks) | < 50ms | Proper indexing, pagination |

### 12.2 Database Optimization

```kotlin
// Index on frequently queried columns
@Entity(
    tableName = "tasks",
    indices = [
        Index("user_id"),
        Index("parent_id"),
        Index("status"),
        Index("updated_at"),
        Index(value = ["user_id", "status"], unique = false)
    ]
)
data class TaskEntity(...)

// Pagination for large lists
@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId AND status = :status
        ORDER BY updated_at DESC
        LIMIT :pageSize OFFSET :offset
    """)
    suspend fun getTasksPaginated(
        userId: String,
        status: String,
        pageSize: Int = 20,
        offset: Int = 0
    ): List<TaskEntity>
}
```

### 12.3 Memory Optimization

```kotlin
// Use Flow instead of LiveData for lower memory overhead
val tasks: Flow<List<Task>> = taskRepository.observeTasks()
    .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

// Cache recent queries (improves repeated access)
private val queryCache = LruCache<String, List<Task>>(maxSize = 100)
```

---

## 13. TESTING STRATEGY

### 13.1 Unit Tests

```kotlin
// Test: CreateTaskUseCase
@Test
fun `createTask should save to repository and return ID`() = runTest {
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

    coEvery { mockTaskRepository.createTask(any()) } returns Result.success("task-id-123")

    val result = createTaskUseCase(task)

    assertTrue(result.isSuccess)
    assertEquals("task-id-123", result.getOrNull())
    coVerify { mockTaskRepository.createTask(task) }
}
```

### 13.2 Integration Tests

```kotlin
// Test: Voice → Task extraction end-to-end
@Test
fun `processVoiceUseCase should extract task from speech`() = runTest {
    val audioFile = File("assets/test_audio.wav")

    val result = processVoiceUseCase(audioFile)

    assertTrue(result.isSuccess)
    val tasks = result.getOrNull()
    assertEquals(1, tasks?.size)
    assertEquals("Call mom", tasks?.first()?.title)
}
```

### 13.3 UI Tests

```kotlin
// Test: Home screen shows task list
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun homeScreen_displaysTaskList() {
    composeTestRule.setContent {
        HomeScreen()
    }

    composeTestRule
        .onNodeWithTag("task_list")
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithText("Call mom")
        .assertExists()
}
```

---

## SUMMARY

This technical architecture is:
- **Modular** (Clean Architecture + MVVM)
- **Offline-first** (Room + Supabase sync)
- **Secure** (Encrypted tokens, API key proxy)
- **Scalable** (Indexed queries, pagination)
- **Testable** (Dependency injection, clear interfaces)
- **High-performance** (< 2s voice-to-confirmation)

Ready for development.
