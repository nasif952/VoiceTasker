# Jarvis for Tasks: AI-powered voice-first Android task manager

**Bottom Line Up Front**: Building a voice-first task manager requires a **streaming STT→LLM→Action pipeline** with **Deepgram Nova-3** for real-time multilingual transcription (Hindi, English, code-switching), **Google Chirp 3** as fallback for Bengali/Urdu, and **OpenAI function calling with strict mode** for reliable task extraction. The Android app should use **Clean Architecture + MVVM** with **Room database** using recursive CTEs for task hierarchies, supporting **offline-first** operation. The UX must minimize cognitive load through progressive disclosure, confidence-based confirmations, and visual feedback during voice interactions.

---

## 1. Executive summary

This PRD defines a voice-first Android task management application that enables users to create, update, and manage tasks through natural conversation in **Bangla, Hindi, Urdu, and English**. The app combines state-of-the-art speech recognition with LLM-powered natural language understanding to interpret complex requests like "remind me to call mom tomorrow afternoon" or "add a subtask to buy groceries for the weekend trip."

**Key differentiators**:
- **True voice-first design**: Voice is primary input, touch is secondary
- **South Asian language excellence**: Optimized for Hindi, Bangla, Urdu including code-switching (Hinglish)
- **Hierarchical tasks**: Support for tasks → subtasks → sub-subtasks with unlimited depth
- **Offline-first**: Full functionality without internet, background sync when connected
- **Conversational context**: "Update that task" and "add a subtask to the previous one" work naturally

**Target latency**: Sub-2-second voice-to-confirmation for task creation.

---

## 2. User personas and use cases

### Primary personas

| Persona | Description | Key Needs |
|---------|-------------|-----------|
| **Riya, 28** | Busy professional in Dhaka, switches between Bangla and English | Voice capture during commute, code-switching support, quick task entry |
| **Amit, 35** | Small business owner in Delhi, speaks Hinglish naturally | Hands-free operation, recurring task management, subtask organization |
| **Sara, 42** | Working mother in Karachi, prefers Urdu | RTL interface, family task sharing, reminder reliability |
| **David, 31** | Remote worker, English speaker, uses multiple devices | Cross-platform sync, natural language dates, project organization |

### Core use cases

**UC1: Voice task creation with natural language**
- User: "Add buy groceries tomorrow morning, high priority"
- System extracts: title="Buy groceries", due_date=tomorrow 9:00 AM, priority=high
- Visual confirmation with 10-second undo window

**UC2: Hierarchical task management**
- User: "Add pack bags as a subtask to the weekend trip task"
- System locates "weekend trip" task, adds subtask
- Shows updated hierarchy visually

**UC3: Conversational updates**
- User: "Move that to next week"
- System resolves "that" to last-mentioned task
- Confirms: "Weekend trip moved to next Saturday"

**UC4: Multilingual code-switching**
- User: "कल meeting के बाद grocery लेनी है" (Tomorrow after meeting need to get grocery)
- System handles Hinglish seamlessly
- Creates task in original language with normalized date

**UC5: Offline task capture**
- User creates task with no connectivity
- System saves locally, queues for sync
- Syncs automatically when online

---

## 3. Functional requirements

### 3.1 Voice input and processing

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-V1 | Support continuous voice input with intelligent endpointing | P0 |
| FR-V2 | Provide real-time transcription feedback (waveform + text) | P0 |
| FR-V3 | Support push-to-talk and optional wake word activation | P0 |
| FR-V4 | Handle background noise with graceful degradation | P1 |
| FR-V5 | Support voice input cancellation ("cancel", "never mind") | P0 |
| FR-V6 | Enable voice-based corrections ("no, I meant...") | P1 |

### 3.2 Task management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-T1 | Create tasks with title, due date, priority, tags, duration | P0 |
| FR-T2 | Support unlimited hierarchy depth (tasks → subtasks → sub-subtasks) | P0 |
| FR-T3 | Parse relative dates ("tomorrow", "next Friday", "in 3 days") | P0 |
| FR-T4 | Support recurring tasks (daily, weekly, monthly, custom) | P1 |
| FR-T5 | Enable task updates via voice ("change priority to high") | P0 |
| FR-T6 | Support task queries ("what's due tomorrow?", "show high priority") | P1 |
| FR-T7 | Maintain conversational context for reference resolution | P0 |

### 3.3 Notes and attachments

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-N1 | Attach voice memos to tasks (optional audio storage) | P2 |
| FR-N2 | Add text notes to tasks via voice dictation | P1 |
| FR-N3 | Support image attachments from camera/gallery | P2 |

### 3.4 Notifications and reminders

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-R1 | Schedule notifications at task due time | P0 |
| FR-R2 | Support custom reminder times (15 min before, 1 hour before, etc.) | P1 |
| FR-R3 | Deliver recurring task notifications | P1 |
| FR-R4 | Support snooze from notification | P1 |

---

## 4. Non-functional requirements

| Category | Requirement | Target |
|----------|-------------|--------|
| **Performance** | Voice-to-confirmation latency | < 2 seconds |
| **Performance** | App cold start time | < 1.5 seconds |
| **Performance** | STT streaming latency | < 300ms |
| **Reliability** | Offline task creation success rate | 100% |
| **Reliability** | App crash rate | < 0.5% sessions |
| **Scalability** | Tasks per user | 10,000+ |
| **Security** | Data encryption at rest | AES-256-GCM |
| **Security** | API communication | TLS 1.3 |
| **Security** | Audio data retention | User-controlled, default delete |
| **Accessibility** | WCAG compliance | 2.2 AA |
| **Accessibility** | Screen reader support | Full TalkBack/VoiceOver |
| **Localization** | Supported languages | EN, HI, BN, UR |
| **Localization** | RTL support | Full (Urdu) |

---

## 5. UI/UX design principles and flows

### 5.1 Core design principles

Research indicates that **cognitive load is the primary enemy** of voice-first productivity apps. Users experience "task paralysis, fatigue, and prioritization confusion" with complex interfaces. The design must embrace:

**Principle 1: Radical simplicity**
- Maximum **5-7 visible tasks** in primary view
- Single-focus mode during voice interaction
- Progressive disclosure for all advanced features

**Principle 2: Confidence-aware feedback**
- High confidence (>90%): Auto-execute with undo option
- Medium confidence (70-90%): Highlight uncertain elements, quick confirm
- Low confidence (<70%): Present disambiguation options

**Principle 3: Voice-first, touch-fallback**
- All actions achievable by voice
- Touch provides quick corrections and navigation
- Never require touch during voice flow

**Principle 4: Immediate feedback**
- Visual feedback within **100ms** of voice activation
- Continuous waveform animation during listening
- Real-time transcription display

### 5.2 Key screens

**Home/Task List Screen**
```
┌─────────────────────────────────────┐
│  ☰  Jarvis Tasks        🔍  ⋮      │
├─────────────────────────────────────┤
│  TODAY (3)                          │
│  ┌─────────────────────────────────┐│
│  │ ○ Buy groceries         🔴 2PM ││
│  │   └ ○ Get milk                 ││
│  │   └ ○ Buy eggs                 ││
│  ├─────────────────────────────────┤│
│  │ ○ Call mom              🟡 5PM ││
│  ├─────────────────────────────────┤│
│  │ ○ Review PRD            🟢 EOD ││
│  └─────────────────────────────────┘│
│                                     │
│  TOMORROW (2)                       │
│  ...                                │
│                                     │
│         ┌─────────────┐             │
│         │  🎤  Speak  │             │
│         └─────────────┘             │
└─────────────────────────────────────┘
```

**Voice Input Overlay**
```
┌─────────────────────────────────────┐
│                                     │
│         ╭──────────────╮            │
│         │  🎤 )))      │            │ ← Pulsing animation
│         ╰──────────────╯            │
│                                     │
│    ▁▃▅▇▅▃▁▃▅▇▅▃▁                   │ ← Waveform
│                                     │
│  "Add buy groceries tomorrow..."    │ ← Real-time transcript
│                                     │
│         [Cancel]  [Done]            │
└─────────────────────────────────────┘
```

**Confirmation Card (Medium Confidence)**
```
┌─────────────────────────────────────┐
│  ✨ I understood:                   │
│  ┌─────────────────────────────────┐│
│  │ Task: [Buy groceries      ] ✏️ ││
│  │ Due:  [Tomorrow, 9:00 AM  ] ✏️ ││
│  │ Priority: 🔴 High           ✏️ ││
│  └─────────────────────────────────┘│
│                                     │
│    [Add Task]      [Cancel]         │
│                                     │
│  💡 Say "change to low priority"   │
│     or tap to edit                  │
└─────────────────────────────────────┘
```

### 5.3 Microphone state machine

```
┌─────────┐  tap/wake  ┌───────────┐
│  IDLE   │ ─────────→ │ LISTENING │
└─────────┘            └───────────┘
     ↑                      │
     │ timeout/cancel       │ silence detected
     │                      ↓
     │              ┌───────────────┐
     │              │  PROCESSING   │
     │              └───────────────┘
     │                      │
     │ dismiss              │ result ready
     │                      ↓
     │              ┌───────────────┐
     └────────────  │  CONFIRMING   │
                    └───────────────┘
```

| State | Visual | Audio | Haptic |
|-------|--------|-------|--------|
| Idle | Muted mic icon | None | None |
| Listening | Pulsing ring, waveform | Activation tone (200ms) | Light tap |
| Processing | Dots animation | None | None |
| Confirming | Result card | Confirmation chime | Success tap |
| Error | Red shake | Error tone | Strong vibration |

### 5.4 Voice correction flow

```
User: "Add meeting tomorrow at 3"
System: "Added 'Meeting' for tomorrow at 3 PM"

User: "No, I meant 3 AM"
System: "Updated to 3 AM"

User: "Actually, cancel that"
System: "Cancelled. The meeting task has been removed."
```

### 5.5 Disambiguation patterns

When confidence is low or multiple interpretations exist:

```
You said: "Add the meeting"

Which meeting?
┌─────────────────────────────────┐
│ 📅 Team standup (mentioned yesterday) │
├─────────────────────────────────┤
│ 📅 Client call (from calendar)        │
├─────────────────────────────────┤
│ ✏️ Create new "The meeting"           │
└─────────────────────────────────┘
```

**Rule**: Maximum 3 options. If more possibilities, ask clarifying question instead.

---

## 6. System architecture

### 6.1 High-level architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        ANDROID CLIENT                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │   UI Layer  │  │  Voice Layer │  │  Background Services   │  │
│  │  (Compose)  │  │   (Audio)    │  │  (WorkManager)         │  │
│  └──────┬──────┘  └──────┬──────┘  └───────────┬─────────────┘  │
│         │                │                      │                │
│  ┌──────┴────────────────┴──────────────────────┴─────────────┐  │
│  │                    PRESENTATION LAYER                       │  │
│  │              ViewModels + UI State (MVVM/UDF)               │  │
│  └──────────────────────────┬──────────────────────────────────┘  │
│                             │                                    │
│  ┌──────────────────────────┴──────────────────────────────────┐  │
│  │                      DOMAIN LAYER                            │  │
│  │        Use Cases + Domain Models + Repository Interfaces     │  │
│  └──────────────────────────┬──────────────────────────────────┘  │
│                             │                                    │
│  ┌──────────────────────────┴──────────────────────────────────┐  │
│  │                       DATA LAYER                             │  │
│  │  ┌────────────┐  ┌─────────────┐  ┌────────────────────┐    │  │
│  │  │  Room DB   │  │ STT Client  │  │   LLM Client       │    │  │
│  │  │  (Local)   │  │ (Deepgram)  │  │   (OpenAI/Claude)  │    │  │
│  │  └────────────┘  └─────────────┘  └────────────────────┘    │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      BACKEND SERVICES                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │ Auth/Token  │  │  Sync API   │  │   API Proxy (LLM keys)  │  │
│  │   Service   │  │   Service   │  │                         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Module structure

```
project/
├── app/                          # Main application module
├── core/
│   ├── common/                   # Extensions, utilities
│   ├── data/                     # Repository implementations
│   ├── database/                 # Room database, DAOs, entities
│   ├── domain/                   # Use cases, interfaces, models
│   ├── network/                  # Retrofit, API definitions
│   └── ui/                       # Design system, theme, components
├── feature/
│   ├── tasks/                    # Task list, detail, creation
│   ├── voice/                    # Voice input, STT integration
│   ├── reminders/                # Notification scheduling
│   └── settings/                 # User preferences, language
└── buildSrc/                     # Gradle dependency management
```

### 6.3 Voice processing pipeline

```
┌────────────────────────────────────────────────────────────────┐
│                    VOICE PROCESSING PIPELINE                    │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Audio Input                                                   │
│       ↓                                                         │
│   ┌───────────────────────────────────────────────────────┐    │
│   │ LANGUAGE DETECTION (first 500ms)                       │    │
│   │  → Route: Hindi/English → Deepgram Nova-3             │    │
│   │           Bengali → Google Chirp 3                     │    │
│   │           Urdu → Google Chirp 3                        │    │
│   │           Offline → Vosk (commands) / Whisper.cpp      │    │
│   └───────────────────────────────────────────────────────┘    │
│       ↓                                                         │
│   ┌───────────────────────────────────────────────────────┐    │
│   │ STT (Streaming transcription)                          │    │
│   │  → Real-time partial results → UI waveform/transcript  │    │
│   │  → Final transcript on silence detection               │    │
│   └───────────────────────────────────────────────────────┘    │
│       ↓                                                         │
│   ┌───────────────────────────────────────────────────────┐    │
│   │ TEXT PREPROCESSING                                      │    │
│   │  → Unicode normalization (NFKC + language-specific)    │    │
│   │  → Numeral normalization (Bangla/Hindi/Urdu → Western) │    │
│   └───────────────────────────────────────────────────────┘    │
│       ↓                                                         │
│   ┌───────────────────────────────────────────────────────┐    │
│   │ LLM FUNCTION CALLING (strict mode)                     │    │
│   │  → Intent classification (create/update/query/delete)  │    │
│   │  → Entity extraction (title, date, priority, etc.)     │    │
│   │  → Reference resolution (context memory)               │    │
│   └───────────────────────────────────────────────────────┘    │
│       ↓                                                         │
│   ┌───────────────────────────────────────────────────────┐    │
│   │ VALIDATION LAYER                                        │    │
│   │  Layer 1: Schema validation (Pydantic/data class)      │    │
│   │  Layer 2: Sanity checks (date not in past, etc.)       │    │
│   │  Layer 3: Confidence scoring                           │    │
│   └───────────────────────────────────────────────────────┘    │
│       ↓                                                         │
│   ┌───────────────────────────────────────────────────────┐    │
│   │ ACTION ROUTER                                           │    │
│   │  High confidence → Execute + undo option               │    │
│   │  Medium confidence → Confirm before execute            │    │
│   │  Low confidence → Disambiguate/clarify                 │    │
│   └───────────────────────────────────────────────────────┘    │
│       ↓                                                         │
│   Database Write + UI Update + Audio/Haptic Feedback           │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

---

## 7. Data model and API design

### 7.1 Room database schema

```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parentId"]),
        Index(value = ["dueDate"]),
        Index(value = ["isSynced"]),
        Index(value = ["createdAt"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Content (stored in original language)
    val originalTitle: String,
    val originalLanguage: String,  // "bn", "hi", "ur", "en"
    val normalizedTitle: String?,  // English normalized for search
    val description: String? = null,
    
    // Hierarchy
    val parentId: String? = null,
    val level: Int = 0,  // Depth: 0=root, 1=subtask, 2=sub-subtask
    val position: Int = 0,  // Ordering within siblings
    
    // Status
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    
    // Timing
    val dueDate: Long? = null,  // UTC timestamp
    val dueDateTimezone: String? = null,  // e.g., "Asia/Dhaka"
    val reminderTime: Long? = null,
    val duration: Int? = null,  // Estimated duration in minutes
    
    // Recurrence (RRULE format)
    val recurrenceRule: String? = null,
    
    // Priority & Tags
    val priority: Int = 2,  // 1=high, 2=medium, 3=low
    val tags: String = "[]",  // JSON array
    
    // Attachments
    val audioFilePath: String? = null,
    val attachmentPaths: String = "[]",  // JSON array
    
    // Sync metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val pendingAction: String? = null,  // "CREATE", "UPDATE", "DELETE"
    val serverVersion: Int = 0
)
```

### 7.2 DAO with hierarchy queries

```kotlin
@Dao
interface TaskDao {
    // Root tasks with immediate children
    @Transaction
    @Query("SELECT * FROM tasks WHERE parentId IS NULL ORDER BY position")
    fun getRootTasksWithSubtasks(): Flow<List<TaskWithSubtasks>>
    
    // Recursive query for all descendants
    @Query("""
        WITH RECURSIVE descendants AS (
            SELECT * FROM tasks WHERE id = :taskId
            UNION ALL
            SELECT t.* FROM tasks t
            INNER JOIN descendants d ON t.parentId = d.id
        )
        SELECT * FROM descendants ORDER BY level, position
    """)
    suspend fun getAllDescendants(taskId: String): List<TaskEntity>
    
    // Tasks due today (timezone aware)
    @Query("""
        SELECT * FROM tasks 
        WHERE dueDate BETWEEN :startOfDay AND :endOfDay
        AND isCompleted = 0
        ORDER BY dueDate, priority
    """)
    fun getTasksDueToday(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>
    
    // Full-text search (normalized title)
    @Query("""
        SELECT * FROM tasks 
        WHERE normalizedTitle LIKE '%' || :query || '%'
        OR originalTitle LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    
    // Unsynced tasks for background sync
    @Query("SELECT * FROM tasks WHERE isSynced = 0 OR pendingAction IS NOT NULL")
    suspend fun getUnsyncedTasks(): List<TaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)
    
    @Query("UPDATE tasks SET isSynced = 1, pendingAction = NULL WHERE id = :taskId")
    suspend fun markAsSynced(taskId: String)
}
```

### 7.3 Relationship classes

```kotlin
data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val subtasks: List<TaskEntity>
)

// For deeper nesting (3 levels)
data class TaskWithNestedSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        entity = TaskEntity::class,
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val subtasks: List<TaskWithSubtasks>
)
```

### 7.4 LLM function calling schema

```json
{
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "create_task",
        "description": "Create a new task with optional due date, priority, and parent task",
        "strict": true,
        "parameters": {
          "type": "object",
          "properties": {
            "title": {
              "type": "string",
              "description": "Task title in user's original language"
            },
            "due_date": {
              "type": ["string", "null"],
              "description": "ISO 8601 date/datetime (e.g., 2026-01-18T15:00:00)"
            },
            "priority": {
              "type": ["string", "null"],
              "enum": ["high", "medium", "low", null]
            },
            "tags": {
              "type": "array",
              "items": {"type": "string"},
              "default": []
            },
            "parent_task_reference": {
              "type": ["string", "null"],
              "description": "Reference to parent task if this is a subtask"
            },
            "duration_minutes": {
              "type": ["integer", "null"],
              "description": "Estimated task duration"
            },
            "recurrence": {
              "type": ["object", "null"],
              "properties": {
                "frequency": {"type": "string", "enum": ["daily", "weekly", "monthly"]},
                "interval": {"type": "integer", "default": 1},
                "days_of_week": {"type": ["array", "null"], "items": {"type": "integer"}}
              }
            }
          },
          "required": ["title"],
          "additionalProperties": false
        }
      }
    },
    {
      "type": "function",
      "function": {
        "name": "update_task",
        "description": "Update an existing task's properties",
        "strict": true,
        "parameters": {
          "type": "object",
          "properties": {
            "task_reference": {
              "type": "string",
              "description": "Task ID, title fragment, or contextual reference like 'that task'"
            },
            "updates": {
              "type": "object",
              "properties": {
                "title": {"type": ["string", "null"]},
                "due_date": {"type": ["string", "null"]},
                "priority": {"type": ["string", "null"], "enum": ["high", "medium", "low", null]},
                "is_completed": {"type": ["boolean", "null"]}
              }
            }
          },
          "required": ["task_reference", "updates"],
          "additionalProperties": false
        }
      }
    },
    {
      "type": "function",
      "function": {
        "name": "query_tasks",
        "description": "Search or filter tasks",
        "strict": true,
        "parameters": {
          "type": "object",
          "properties": {
            "query": {"type": ["string", "null"]},
            "due_before": {"type": ["string", "null"]},
            "due_after": {"type": ["string", "null"]},
            "priority": {"type": ["string", "null"], "enum": ["high", "medium", "low", null]},
            "is_completed": {"type": ["boolean", "null"]},
            "limit": {"type": "integer", "default": 10}
          },
          "required": [],
          "additionalProperties": false
        }
      }
    },
    {
      "type": "function",
      "function": {
        "name": "delete_task",
        "description": "Delete a task (requires explicit confirmation)",
        "strict": true,
        "parameters": {
          "type": "object",
          "properties": {
            "task_reference": {"type": "string"},
            "include_subtasks": {"type": "boolean", "default": true}
          },
          "required": ["task_reference"],
          "additionalProperties": false
        }
      }
    }
  ]
}
```

---

## 8. LLM and STT integration recommendations

### 8.1 STT provider selection

| Language | Primary Provider | Fallback | Offline |
|----------|-----------------|----------|---------|
| **Hindi + Hinglish** | Deepgram Nova-3 | Google Chirp 3 | Vosk Hindi |
| **English (Indian)** | Deepgram Nova-3 | AssemblyAI | Vosk Indian English |
| **Bengali** | Google Chirp 3 | Azure Speech | Whisper.cpp |
| **Urdu** | Google Chirp 3 | Azure Speech | Whisper.cpp |

**Deepgram Nova-3 advantages**:
- **Best-in-class Hindi code-switching**: First commercial model with real-time Hinglish support
- **Sub-300ms latency**: Meets real-time threshold
- **Cost-effective**: $0.0077/minute streaming

**Google Chirp 3 advantages**:
- **Broadest South Asian coverage**: Bengali, Hindi, Urdu all supported
- **Language-agnostic detection**: Auto-detects language without configuration

### 8.2 STT integration architecture

```kotlin
interface SttProvider {
    suspend fun transcribe(audioStream: Flow<ByteArray>): Flow<TranscriptionResult>
    fun getSupportedLanguages(): List<LanguageCode>
}

class SttRouter @Inject constructor(
    private val deepgram: DeepgramProvider,
    private val googleChirp: GoogleChirpProvider,
    private val vosk: VoskProvider,
    private val connectivityObserver: ConnectivityObserver
) {
    fun getProvider(detectedLanguage: LanguageCode?): SttProvider {
        return when {
            !connectivityObserver.isConnected() -> vosk
            detectedLanguage in listOf(LanguageCode.HI, LanguageCode.EN) -> deepgram
            detectedLanguage in listOf(LanguageCode.BN, LanguageCode.UR) -> googleChirp
            else -> deepgram // Default
        }
    }
}

data class TranscriptionResult(
    val text: String,
    val isFinal: Boolean,
    val confidence: Float,
    val detectedLanguage: LanguageCode?
)
```

### 8.3 LLM provider abstraction

```kotlin
interface LlmProvider {
    suspend fun complete(
        messages: List<ChatMessage>,
        tools: List<Tool>? = null,
        responseFormat: ResponseFormat? = null
    ): LlmResponse
}

class LlmRouter @Inject constructor(
    private val openai: OpenAiProvider,
    private val anthropic: AnthropicProvider,
    private val localLlm: OllamaProvider
) {
    private val providers = listOf(openai, anthropic, localLlm)
    
    suspend fun complete(request: LlmRequest): LlmResponse {
        for (provider in providers) {
            try {
                if (provider.isHealthy()) {
                    return provider.complete(
                        messages = request.messages,
                        tools = request.tools
                    )
                }
            } catch (e: Exception) {
                logFailure(provider, e)
                continue
            }
        }
        return emergencyResponse()
    }
}
```

### 8.4 System prompt for task extraction

```markdown
You are a voice-first task management assistant.

## Current Context
- Date: {current_date} ({day_of_week})
- Time: {current_time}
- Timezone: {user_timezone}
- User language: {detected_language}

## Recent Tasks (for reference resolution)
{recent_tasks_json}

## Instructions
1. Extract task information from conversational speech
2. For dates, interpret relative expressions based on current date
3. "कल" (tomorrow/yesterday) - determine from context (usually tomorrow for tasks)
4. Preserve original language for task titles
5. If ambiguous, include confidence_score < 0.8 in response
6. For references like "that task" or "the previous one", resolve using recent tasks

## Reference Resolution Rules
- "that task" / "that" → last_mentioned_task
- "the previous one" → task before last_mentioned
- "the [descriptor] task" → fuzzy match by title/tags

## Output
Always use the provided tools. Output dates in ISO 8601 format.
Include "confidence_score" (0-1) when uncertain.
```

### 8.5 Temporal reasoning: hybrid approach

Research strongly recommends **LLM for semantic extraction + deterministic library for date resolution**:

```kotlin
class TemporalResolver(
    private val dateParser: Dateparser  // or Chrono library
) {
    fun resolve(
        llmOutput: TemporalExpression,
        currentDateTime: ZonedDateTime,
        userTimezone: ZoneId
    ): ZonedDateTime? {
        return when (llmOutput.type) {
            "absolute" -> parseAbsolute(llmOutput.value)
            "relative" -> resolveRelative(llmOutput, currentDateTime)
            "recurring" -> buildRrule(llmOutput)
            "vague" -> applySmartDefault(llmOutput, currentDateTime)
            else -> null
        }
    }
    
    private fun resolveRelative(
        expr: TemporalExpression,
        now: ZonedDateTime
    ): ZonedDateTime {
        // Deterministic resolution: "next Friday" from Jan 17, 2026 → Jan 24, 2026
        return dateParser.parse(expr.rawText, now)
    }
}
```

---

## 9. Reasoning and decision pipelines

### 9.1 Validation layer implementation

```kotlin
class OutputValidator {
    fun validate(output: TaskAction): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Layer 1: Schema validation (handled by strict mode)
        
        // Layer 2: Sanity checks
        output.dueDate?.let { dueDate ->
            if (dueDate.isBefore(ZonedDateTime.now().minusHours(1))) {
                errors.add(ValidationError.DATE_IN_PAST)
            }
        }
        
        output.durationMinutes?.let { duration ->
            if (duration < 1 || duration > 1440) {
                errors.add(ValidationError.INVALID_DURATION)
            }
        }
        
        if (output.title.length > 500) {
            errors.add(ValidationError.TITLE_TOO_LONG)
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            confidence = calculateConfidence(output, errors)
        )
    }
}
```

### 9.2 Confidence-based action routing

```kotlin
class ActionRouter {
    private val thresholds = mapOf(
        ActionType.CREATE to 0.75f,
        ActionType.UPDATE to 0.80f,
        ActionType.DELETE to 0.95f,
        ActionType.QUERY to 0.60f
    )
    
    fun route(
        action: TaskAction,
        confidence: Float
    ): RoutingDecision {
        val threshold = thresholds[action.type] ?: 0.80f
        
        return when {
            confidence >= threshold -> RoutingDecision.ExecuteWithUndo
            confidence >= threshold - 0.15f -> RoutingDecision.ConfirmBeforeExecute
            else -> RoutingDecision.RequestClarification
        }
    }
}

sealed class RoutingDecision {
    object ExecuteWithUndo : RoutingDecision()
    object ConfirmBeforeExecute : RoutingDecision()
    data class RequestClarification(
        val options: List<ClarificationOption>
    ) : RoutingDecision()
}
```

### 9.3 Error recovery pipeline

```kotlin
class VoiceErrorHandler {
    suspend fun handleSttFailure(
        audio: ByteArray,
        attempt: Int = 1
    ): RecoveryResult {
        if (attempt > 3) {
            return RecoveryResult.AskToRepeat(
                message = "I didn't catch that. Could you say it again?"
            )
        }
        
        // Try fallback providers
        val fallbacks = listOf(googleChirp, assemblyAi, whisperLocal)
        for (provider in fallbacks) {
            try {
                val result = provider.transcribe(audio)
                if (result.confidence > 0.7f) {
                    return RecoveryResult.Success(result)
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        return RecoveryResult.AskToRepeat(
            message = "Sorry, there was some background noise. Please say that again."
        )
    }
    
    suspend fun handleLlmTimeout(): RecoveryResult {
        // Try backup model
        return try {
            val result = backupLlm.complete(lastRequest)
            RecoveryResult.Success(result)
        } catch (e: Exception) {
            RecoveryResult.GracefulDegradeToManual
        }
    }
}
```

---

## 10. Multilingual strategy

### 10.1 Language detection and routing

```kotlin
class LanguageRouter {
    fun detectAndRoute(audio: ByteArray): LanguageRoute {
        // Use first 500ms of audio for quick detection
        val detected = languageDetector.detect(audio.take(DETECTION_WINDOW))
        
        return when (detected.primaryLanguage) {
            LanguageCode.HI -> LanguageRoute(
                sttProvider = SttProvider.DEEPGRAM,
                llmPromptLanguage = "hi",
                supportsCodeSwitching = true
            )
            LanguageCode.BN -> LanguageRoute(
                sttProvider = SttProvider.GOOGLE_CHIRP,
                llmPromptLanguage = "bn",
                supportsCodeSwitching = false
            )
            LanguageCode.UR -> LanguageRoute(
                sttProvider = SttProvider.GOOGLE_CHIRP,
                llmPromptLanguage = "ur",
                supportsCodeSwitching = false,
                isRtl = true
            )
            else -> LanguageRoute(
                sttProvider = SttProvider.DEEPGRAM,
                llmPromptLanguage = "en",
                supportsCodeSwitching = false
            )
        }
    }
}
```

### 10.2 Numeral normalization

```kotlin
object NumeralNormalizer {
    private val BANGLA_DIGITS = "০১২৩৪৫৬৭৮৯"
    private val HINDI_DIGITS = "०१२३४५६७८९"
    private val URDU_DIGITS = "۰۱۲۳۴۵۶۷۸۹"
    private val WESTERN_DIGITS = "0123456789"
    
    fun normalize(text: String): String {
        var result = text
        listOf(BANGLA_DIGITS, HINDI_DIGITS, URDU_DIGITS).forEach { digits ->
            digits.forEachIndexed { index, digit ->
                result = result.replace(digit, WESTERN_DIGITS[index])
            }
        }
        return result
    }
}
```

### 10.3 Dual storage architecture

```kotlin
data class TaskContent(
    val originalTitle: String,        // "আগামীকাল ডাক্তার দেখাতে হবে"
    val originalLanguage: String,     // "bn"
    val normalizedTitle: String?,     // "doctor appointment tomorrow"
    val searchTokens: List<String>    // ["doctor", "appointment", "ডাক্তার"]
)

class TaskContentProcessor {
    suspend fun process(
        voiceInput: String,
        detectedLanguage: LanguageCode
    ): TaskContent {
        // Normalize Unicode
        val normalized = unicodeNormalizer.normalize(voiceInput)
        
        // Generate English normalized title for search
        val englishTitle = when (detectedLanguage) {
            LanguageCode.EN -> normalized
            else -> translator.toEnglish(normalized).takeIf { 
                shouldTranslate(detectedLanguage) 
            }
        }
        
        // Generate search tokens (original + transliterated + translated)
        val tokens = buildSearchTokens(normalized, englishTitle, detectedLanguage)
        
        return TaskContent(
            originalTitle = voiceInput,
            originalLanguage = detectedLanguage.code,
            normalizedTitle = englishTitle,
            searchTokens = tokens
        )
    }
}
```

### 10.4 RTL support for Urdu

```kotlin
// Compose UI with RTL support
@Composable
fun TaskItemCard(
    task: Task,
    modifier: Modifier = Modifier
) {
    val isRtl = task.originalLanguage == "ur"
    
    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) 
            LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { /* ... */ }
            )
            Text(
                text = task.originalTitle,
                modifier = Modifier.weight(1f),
                textDirection = if (isRtl) TextDirection.Rtl else TextDirection.Ltr
            )
            PriorityIndicator(priority = task.priority)
        }
    }
}
```

---

## 11. MVP vs future phases

### Phase 1: MVP (8 weeks)

**Scope**:
- Voice task creation (English, Hindi)
- Basic hierarchy (1 level of subtasks)
- Due date parsing (today, tomorrow, specific dates)
- Priority levels (high, medium, low)
- Local storage only (no sync)
- Push-to-talk voice activation
- Basic notifications

**Success metrics**:
- Voice task creation success rate > 80%
- Time-to-task < 5 seconds
- App crash rate < 1%

### Phase 2: Enhanced multilingual (6 weeks)

**Scope**:
- Bengali and Urdu support
- Code-switching (Hinglish, Banglish)
- RTL interface for Urdu
- Recurring tasks
- Cloud sync with offline-first
- Voice corrections ("no, I meant...")

### Phase 3: Advanced features (8 weeks)

**Scope**:
- Unlimited hierarchy depth
- Conversational context ("update that task")
- Natural language queries ("what's due this week?")
- Voice attachments
- Wake word activation (optional)
- Smart suggestions based on patterns

### Phase 4: Intelligence (ongoing)

**Scope**:
- Learning user patterns
- Proactive reminders
- Calendar integration
- Cross-device sync
- Collaboration features
- Custom wake word

---

## 12. Risks and mitigations

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| **Bengali STT accuracy too low** | High | High | Fine-tune Whisper on Bengali data; fall back to Google Chirp; collect user corrections for improvement |
| **LLM latency exceeds 2s target** | Medium | High | Stream partial results; use faster models (GPT-4o-mini); implement local LLM fallback |
| **Code-switching fails frequently** | Medium | Medium | Route clearly monolingual to standard models; Deepgram Nova-3 for Hinglish; collect failure cases |
| **User trust issues with AI corrections** | Medium | High | Always show parsed result before execution; easy correction flow; undo available for 30s |
| **Battery drain from voice processing** | Medium | Medium | Vosk for short commands; push-to-talk by default; optimize audio pipeline |
| **Date parsing ambiguity ("कल" = tomorrow/yesterday)** | High | Medium | Default to future for task context; ask if truly ambiguous; learn user patterns |
| **Notification delivery on Android 14+** | Medium | High | Request exact alarm permission; use foreground service for critical reminders; test across OEMs |
| **API cost at scale** | Low | High | Tiered STT routing (on-device first); batch processing where possible; usage-based pricing tiers |

---

## Appendix A: Key sources and references

### Speech-to-Text
- Deepgram Nova-3: https://deepgram.com/learn/introducing-nova-3-speech-to-text-api
- Google Chirp 3: https://cloud.google.com/speech-to-text/docs/models
- Bengali ASR Research (2025): https://arxiv.org/abs/2601.09710
- HiACC Hinglish Corpus: https://pmc.ncbi.nlm.nih.gov/articles/PMC12329218/

### LLM Integration
- OpenAI Function Calling: https://platform.openai.com/docs/guides/function-calling
- OpenAI Structured Outputs: https://platform.openai.com/docs/guides/structured-outputs
- Anthropic Context Engineering: https://www.anthropic.com/engineering/effective-context-engineering

### Android Architecture
- Official Architecture Guide: https://developer.android.com/topic/architecture
- Offline-First Pattern: https://developer.android.com/topic/architecture/data-layer/offline-first
- Room Relationships: https://developer.android.com/training/data-storage/room/relationships

### Voice UX
- Voice AI Stack (AssemblyAI): https://www.assemblyai.com/blog/the-voice-ai-stack
- Guardrails Best Practices: https://www.datadoghq.com/blog/llm-guardrails-best-practices/
- ECLAIR Disambiguation: https://arxiv.org/html/2503.15739v1

### Multilingual NLP
- Google Pre-translation vs Direct Inference: https://research.google/blog/pre-translation-vs-direct-inference
- IndicNLP Catalog: https://github.com/AI4Bharat/indicnlp_catalog
- bnUnicodeNormalizer: https://github.com/mnansary/bnUnicodeNormalizer

---

*Document Version: 1.0 | Last Updated: January 17, 2026*