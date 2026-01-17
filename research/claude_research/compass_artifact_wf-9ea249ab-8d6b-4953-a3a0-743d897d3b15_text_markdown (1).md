# Jarvis for Tasks: AI-powered voice-first Android task manager

**Bottom Line Up Front**: Building a voice-first task manager requires a **streaming STT→LLM→Action pipeline** with **Deepgram Nova-3** for real-time multilingual transcription (Hindi, English, code-switching), **Google Chirp 3** as fallback for Bengali/Urdu, and **OpenAI function calling with strict mode** for reliable task extraction. The Android app should use **Clean Architecture + MVVM** with **Room database** using recursive CTEs for task hierarchies, supporting **offline-first** operation. Backend uses **Supabase** (PostgreSQL + Auth + RLS) for cloud sync and user management, with **Google Sign-In via Credential Manager** as primary auth. **Stripe** integration planned for Phase 3 subscription billing. The UX must minimize cognitive load through progressive disclosure, confidence-based confirmations, and visual feedback during voice interactions.

---

## 1. Executive summary

This PRD defines a voice-first Android task management application that enables users to create, update, and manage tasks through natural conversation in **Bangla, Hindi, Urdu, and English**. The app combines state-of-the-art speech recognition with LLM-powered natural language understanding to interpret complex requests like "remind me to call mom tomorrow afternoon" or "add a subtask to buy groceries for the weekend trip."

**Key differentiators**:
- **True voice-first design**: Voice is primary input, touch is secondary
- **South Asian language excellence**: Optimized for Hindi, Bangla, Urdu including code-switching (Hinglish)
- **Hierarchical tasks**: Support for tasks → subtasks → sub-subtasks with unlimited depth
- **Offline-first**: Full functionality without internet, background sync when connected
- **Conversational context**: "Update that task" and "add a subtask to the previous one" work naturally
- **Secure cloud sync**: User accounts with Google Sign-In, data persisted to Supabase with RLS

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
- User: "কল meeting के बाद grocery लेनी है" (Tomorrow after meeting need to get grocery)
- System handles Hinglish seamlessly
- Creates task in original language with normalized date

**UC5: Offline task capture**
- User creates task with no connectivity
- System saves locally, queues for sync
- Syncs automatically when online

**UC6: Cross-device sync**
- User creates task on phone
- Task appears on tablet within seconds
- Conflict resolution favors most recent edit

---

## 3. Functional requirements

### 3.1 User authentication & account management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-A1 | Google Sign-In via Credential Manager (primary auth method) | P0 |
| FR-A2 | Email/password registration with email verification | P0 |
| FR-A3 | Password reset via email link | P0 |
| FR-A4 | Session persistence with secure token storage | P0 |
| FR-A5 | Account deletion with data purge (GDPR/compliance) | P0 |
| FR-A6 | Profile management (display name, avatar from Google) | P1 |
| FR-A7 | Multi-device session management | P1 |

### 3.2 Voice input and processing

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-V1 | Support continuous voice input with intelligent endpointing | P0 |
| FR-V2 | Provide real-time transcription feedback (waveform + text) | P0 |
| FR-V3 | Support push-to-talk and optional wake word activation | P0 |
| FR-V4 | Handle background noise with graceful degradation | P1 |
| FR-V5 | Support voice input cancellation ("cancel", "never mind") | P0 |
| FR-V6 | Enable voice-based corrections ("no, I meant...") | P1 |

### 3.3 Task management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-T1 | Create tasks with title, due date, priority, tags, duration | P0 |
| FR-T2 | Support unlimited hierarchy depth (tasks → subtasks → sub-subtasks) | P0 |
| FR-T3 | Parse relative dates ("tomorrow", "next Friday", "in 3 days") | P0 |
| FR-T4 | Support recurring tasks (daily, weekly, monthly, custom) | P1 |
| FR-T5 | Enable task updates via voice ("change priority to high") | P0 |
| FR-T6 | Support task queries ("what's due tomorrow?", "show high priority") | P1 |
| FR-T7 | Maintain conversational context for reference resolution | P0 |
| FR-T8 | Cloud sync with conflict resolution (last-write-wins) | P0 |

### 3.4 Notes and attachments

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-N1 | Attach voice memos to tasks (optional audio storage) | P2 |
| FR-N2 | Add text notes to tasks via voice dictation | P1 |
| FR-N3 | Support image attachments from camera/gallery | P2 |

### 3.5 Notifications and reminders

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-R1 | Schedule notifications at task due time | P0 |
| FR-R2 | Support custom reminder times (15 min before, 1 hour before, etc.) | P1 |
| FR-R3 | Deliver recurring task notifications | P1 |
| FR-R4 | Support snooze from notification | P1 |

### 3.6 Subscription & billing (Phase 3)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-B1 | Free tier with usage limits (tasks, voice minutes) | P2 |
| FR-B2 | Pro subscription via Stripe PaymentSheet | P2 |
| FR-B3 | Subscription status sync with backend | P2 |
| FR-B4 | Grace period handling for failed payments | P2 |
| FR-B5 | In-app subscription management (upgrade/cancel) | P2 |

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
| **Security** | API keys protection | Android Keystore + backend proxy |
| **Security** | Auth tokens | Short-lived JWT + secure refresh |
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

**Onboarding / Sign-In Screen**
```
┌─────────────────────────────────────┐
│                                     │
│         🎤 Jarvis Tasks             │
│                                     │
│    Your voice-first task manager    │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🔵 Continue with Google     │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ ✉️  Sign up with Email      │   │
│   └─────────────────────────────┘   │
│                                     │
│   Already have an account? Log in   │
│                                     │
│   By continuing, you agree to our   │
│   Terms of Service & Privacy Policy │
└─────────────────────────────────────┘
```

**Home/Task List Screen**
```
┌─────────────────────────────────────┐
│  ☰  Jarvis Tasks        🔍  👤     │
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

**Account Settings Screen**
```
┌─────────────────────────────────────┐
│  ←  Account Settings                │
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │  👤  Riya Ahmed                 ││
│  │  riya@gmail.com                 ││
│  │  Signed in with Google          ││
│  └─────────────────────────────────┘│
│                                     │
│  Subscription                       │
│  ├─ Current Plan: Free              │
│  └─ [Upgrade to Pro]                │
│                                     │
│  Data & Privacy                     │
│  ├─ Export my data                  │
│  ├─ Delete voice recordings         │
│  └─ Delete my account (danger)      │
│                                     │
│  [Sign Out]                         │
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
│  │  ┌────────────────────────────────────────────────────┐     │  │
│  │  │              Supabase Client (Auth + Sync)          │     │  │
│  │  └────────────────────────────────────────────────────┘     │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      SUPABASE BACKEND                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  Auth       │  │  PostgreSQL │  │   Edge Functions        │  │
│  │  (GoTrue)   │  │  + RLS      │  │   (LLM Proxy, Stripe)   │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐                               │
│  │  Storage    │  │  Realtime   │                               │
│  │  (Files)    │  │  (Sync)     │                               │
│  └─────────────┘  └─────────────┘                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    EXTERNAL SERVICES                             │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  OpenAI API │  │  Deepgram   │  │   Stripe (Phase 3)      │  │
│  │  (via Edge) │  │  STT        │  │   (Subscriptions)       │  │
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
│   ├── auth/                     # Supabase Auth, Credential Manager
│   └── ui/                       # Design system, theme, components
├── feature/
│   ├── auth/                     # Login, registration, account
│   ├── tasks/                    # Task list, detail, creation
│   ├── voice/                    # Voice input, STT integration
│   ├── reminders/                # Notification scheduling
│   ├── settings/                 # User preferences, language
│   └── subscription/             # Pro upgrade, billing (Phase 3)
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
│       ↓                                                         │
│   Background Sync to Supabase (if online)                      │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

---

## 7. Authentication & user management

### 7.1 Backend recommendation: Supabase

**Why Supabase over Firebase:**

| Factor | Supabase | Firebase |
|--------|----------|----------|
| **Database** | PostgreSQL (relational, SQL) | Firestore (NoSQL) |
| **Data model fit** | ✅ Hierarchical tasks with foreign keys | ❌ Document model awkward for relations |
| **RLS Security** | ✅ SQL-based Row Level Security | JavaScript-like rules |
| **Vendor lock-in** | ✅ Open-source, can self-host | ❌ Proprietary |
| **Pricing predictability** | ✅ Fixed tiers | Pay-per-read/write |
| **Migration path** | ✅ Standard PostgreSQL | Difficult export |

**Recommendation**: Use **Supabase** for auth, database, storage, and realtime sync.

### 7.2 Authentication architecture

```kotlin
// Auth provider abstraction for testability
interface AuthProvider {
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signOut()
    suspend fun resetPassword(email: String)
    suspend fun deleteAccount()
    fun getCurrentUser(): Flow<User?>
    fun getAccessToken(): String?
}

// Supabase implementation
class SupabaseAuthProvider @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val credentialManager: CredentialManager
) : AuthProvider {
    
    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val result = supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
            AuthResult.Success(result.user.toAppUser())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed")
        }
    }
    
    override suspend fun deleteAccount() {
        // 1. Delete all user data via RLS-protected delete
        supabaseClient.from("tasks")
            .delete { filter { eq("user_id", getCurrentUserId()) } }
        
        // 2. Delete user from auth
        supabaseClient.auth.admin.deleteUser(getCurrentUserId())
        
        // 3. Sign out locally
        signOut()
    }
}
```

### 7.3 Google Sign-In with Credential Manager

```kotlin
class GoogleSignInManager @Inject constructor(
    private val context: Context,
    private val authProvider: AuthProvider
) {
    private val credentialManager = CredentialManager.create(context)
    
    suspend fun signIn(): AuthResult {
        // Build Google ID option
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .setNonce(generateNonce())
            .build()
        
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        
        return try {
            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Cancelled
        } catch (e: NoCredentialException) {
            // No saved credentials, show sign-up flow
            AuthResult.NoCredentials
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign-in failed")
        }
    }
    
    private suspend fun handleSignInResult(result: GetCredentialResponse): AuthResult {
        val credential = result.credential
        
        return when {
            credential is CustomCredential && 
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                authProvider.signInWithGoogle(googleIdToken.idToken)
            }
            else -> AuthResult.Error("Unexpected credential type")
        }
    }
    
    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        authProvider.signOut()
    }
}
```

### 7.4 Secure token storage

```kotlin
class SecureTokenStorage @Inject constructor(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("token_expiry", System.currentTimeMillis() + TOKEN_LIFETIME)
            .apply()
    }
    
    fun getAccessToken(): String? {
        val expiry = encryptedPrefs.getLong("token_expiry", 0)
        if (System.currentTimeMillis() > expiry) {
            return null // Token expired, need refresh
        }
        return encryptedPrefs.getString("access_token", null)
    }
    
    fun clearTokens() {
        encryptedPrefs.edit().clear().apply()
    }
    
    companion object {
        private const val TOKEN_LIFETIME = 3600_000L // 1 hour
    }
}
```

---

## 8. Data model & cloud sync

### 8.1 Supabase PostgreSQL schema

```sql
-- Users table (extends Supabase auth.users)
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT,
    avatar_url TEXT,
    preferred_language TEXT DEFAULT 'en',
    timezone TEXT DEFAULT 'UTC',
    subscription_tier TEXT DEFAULT 'free',
    subscription_expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Tasks table with hierarchical support
CREATE TABLE public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Content (stored in original language)
    original_title TEXT NOT NULL,
    original_language TEXT NOT NULL DEFAULT 'en',
    normalized_title TEXT,
    description TEXT,
    
    -- Hierarchy
    parent_id UUID REFERENCES public.tasks(id) ON DELETE CASCADE,
    level INTEGER DEFAULT 0,
    position INTEGER DEFAULT 0,
    
    -- Status
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    
    -- Timing
    due_date TIMESTAMPTZ,
    due_date_timezone TEXT,
    reminder_time TIMESTAMPTZ,
    duration_minutes INTEGER,
    
    -- Recurrence (RRULE format)
    recurrence_rule TEXT,
    
    -- Priority & Tags
    priority INTEGER DEFAULT 2,
    tags JSONB DEFAULT '[]',
    
    -- Attachments
    audio_file_path TEXT,
    attachment_paths JSONB DEFAULT '[]',
    
    -- Sync metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    client_updated_at TIMESTAMPTZ,
    version INTEGER DEFAULT 1
);

-- Indexes for performance
CREATE INDEX idx_tasks_user_id ON public.tasks(user_id);
CREATE INDEX idx_tasks_parent_id ON public.tasks(parent_id);
CREATE INDEX idx_tasks_due_date ON public.tasks(due_date);
CREATE INDEX idx_tasks_updated_at ON public.tasks(updated_at);

-- Trigger to auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tasks_updated_at
    BEFORE UPDATE ON public.tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();
```

### 8.2 Row Level Security (RLS) policies

```sql
-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

-- Profiles: users can only access their own profile
CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Tasks: users can only access their own tasks
CREATE POLICY "Users can view own tasks"
    ON public.tasks FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can create own tasks"
    ON public.tasks FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own tasks"
    ON public.tasks FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own tasks"
    ON public.tasks FOR DELETE
    USING (auth.uid() = user_id);

-- Auto-create profile on user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, display_name, avatar_url)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email),
        NEW.raw_user_meta_data->>'avatar_url'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();
```

### 8.3 Local Room database schema

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
        Index(value = ["syncStatus"]),
        Index(value = ["updatedAt"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    
    // Content
    val originalTitle: String,
    val originalLanguage: String,
    val normalizedTitle: String?,
    val description: String? = null,
    
    // Hierarchy
    val parentId: String? = null,
    val level: Int = 0,
    val position: Int = 0,
    
    // Status
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    
    // Timing
    val dueDate: Long? = null,
    val dueDateTimezone: String? = null,
    val reminderTime: Long? = null,
    val durationMinutes: Int? = null,
    
    // Recurrence
    val recurrenceRule: String? = null,
    
    // Priority & Tags
    val priority: Int = 2,
    val tags: String = "[]",
    
    // Sync metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clientUpdatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val serverVersion: Int? = null
)

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    CONFLICT
}
```

### 8.4 Offline-first sync strategy

```kotlin
class TaskSyncManager @Inject constructor(
    private val localDao: TaskDao,
    private val supabaseClient: SupabaseClient,
    private val connectivityObserver: ConnectivityObserver
) {
    // Realtime subscription for live updates
    private var realtimeChannel: RealtimeChannel? = null
    
    suspend fun startRealtimeSync(userId: String) {
        realtimeChannel = supabaseClient.channel("tasks-$userId")
        
        realtimeChannel?.postgresChangeFlow<PostgresAction>(
            schema = "public",
            table = "tasks",
            filter = "user_id=eq.$userId"
        )?.collect { change ->
            when (change) {
                is PostgresAction.Insert -> handleRemoteInsert(change.record)
                is PostgresAction.Update -> handleRemoteUpdate(change.record)
                is PostgresAction.Delete -> handleRemoteDelete(change.oldRecord)
            }
        }
        
        realtimeChannel?.subscribe()
    }
    
    suspend fun syncPendingChanges() {
        if (!connectivityObserver.isConnected()) return
        
        val pendingTasks = localDao.getPendingSync()
        
        pendingTasks.forEach { task ->
            try {
                when (task.syncStatus) {
                    SyncStatus.PENDING_CREATE -> {
                        supabaseClient.from("tasks")
                            .insert(task.toSupabaseTask())
                        localDao.updateSyncStatus(task.id, SyncStatus.SYNCED)
                    }
                    SyncStatus.PENDING_UPDATE -> {
                        // Optimistic concurrency with version check
                        val result = supabaseClient.from("tasks")
                            .update(task.toSupabaseTask()) {
                                filter { 
                                    eq("id", task.id)
                                    eq("version", task.serverVersion ?: 0)
                                }
                            }
                        if (result.data.isEmpty()) {
                            // Conflict detected, fetch server version
                            handleConflict(task)
                        } else {
                            localDao.updateSyncStatus(task.id, SyncStatus.SYNCED)
                        }
                    }
                    SyncStatus.PENDING_DELETE -> {
                        supabaseClient.from("tasks")
                            .delete { filter { eq("id", task.id) } }
                        localDao.hardDelete(task.id)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("Sync", "Failed to sync task ${task.id}", e)
            }
        }
    }
    
    private suspend fun handleConflict(localTask: TaskEntity) {
        // Fetch server version
        val serverTask = supabaseClient.from("tasks")
            .select { filter { eq("id", localTask.id) } }
            .decodeSingleOrNull<SupabaseTask>()
        
        if (serverTask == null) {
            // Deleted on server, remove locally
            localDao.hardDelete(localTask.id)
            return
        }
        
        // Last-write-wins based on clientUpdatedAt
        if (localTask.clientUpdatedAt > serverTask.clientUpdatedAt) {
            // Local wins, force update
            supabaseClient.from("tasks")
                .update(localTask.toSupabaseTask()) {
                    filter { eq("id", localTask.id) }
                }
            localDao.updateSyncStatus(localTask.id, SyncStatus.SYNCED)
        } else {
            // Server wins, update local
            localDao.upsert(serverTask.toLocalEntity())
        }
    }
}
```

---

## 9. Security architecture

### 9.1 API key protection strategy

**CRITICAL**: Never ship OpenAI/Deepgram API keys in the app. Use backend proxy.

```kotlin
// Supabase Edge Function: /functions/v1/llm-proxy
// This runs server-side, API keys never leave Supabase

// supabase/functions/llm-proxy/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const OPENAI_API_KEY = Deno.env.get('OPENAI_API_KEY')

serve(async (req) => {
    // Verify JWT from Supabase Auth
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
        return new Response('Unauthorized', { status: 401 })
    }
    
    // Parse request
    const { messages, tools } = await req.json()
    
    // Rate limiting check (from database)
    const userId = getUserIdFromJwt(authHeader)
    const canProceed = await checkRateLimit(userId)
    if (!canProceed) {
        return new Response('Rate limit exceeded', { status: 429 })
    }
    
    // Forward to OpenAI
    const response = await fetch('https://api.openai.com/v1/chat/completions', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${OPENAI_API_KEY}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            model: 'gpt-4o-mini',
            messages,
            tools,
            tool_choice: 'auto'
        })
    })
    
    return new Response(response.body, {
        headers: { 'Content-Type': 'application/json' }
    })
})
```

### 9.2 Security checklist

| Layer | Protection | Implementation |
|-------|------------|----------------|
| **Auth tokens** | Encrypted storage | EncryptedSharedPreferences with MasterKey |
| **API keys** | Backend proxy | Supabase Edge Functions |
| **Network** | TLS 1.3 | Network Security Config |
| **Data at rest** | AES-256 | Room encryption + EncryptedFile |
| **Audio data** | User control | Delete after transcription (configurable) |
| **Logs** | No PII | ProGuard strip, no sensitive logging |
| **Backups** | Encrypted | android:allowBackup with encrypted transport |

### 9.3 Network security configuration

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    
    <domain-config>
        <domain includeSubdomains="true">your-project.supabase.co</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">AAAA...</pin>
            <pin digest="SHA-256">BBBB...</pin> <!-- Backup pin -->
        </pin-set>
    </domain-config>
</network-security-config>
```

---

## 10. Stripe payment integration (Phase 3)

### 10.1 Architecture overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        PAYMENT FLOW                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   User taps "Upgrade to Pro"                                    │
│           │                                                      │
│           ↓                                                      │
│   ┌───────────────────────────────────────────────────────┐     │
│   │ Android App: Request subscription setup                │     │
│   │  → POST /functions/v1/create-subscription              │     │
│   └───────────────────────────────────────────────────────┘     │
│           │                                                      │
│           ↓                                                      │
│   ┌───────────────────────────────────────────────────────┐     │
│   │ Supabase Edge Function:                                │     │
│   │  1. Verify user JWT                                    │     │
│   │  2. Create/retrieve Stripe Customer                    │     │
│   │  3. Create Subscription with PaymentIntent             │     │
│   │  4. Return client_secret                               │     │
│   └───────────────────────────────────────────────────────┘     │
│           │                                                      │
│           ↓                                                      │
│   ┌───────────────────────────────────────────────────────┐     │
│   │ Android App: Present PaymentSheet                      │     │
│   │  → User enters card / Google Pay                       │     │
│   │  → Stripe SDK handles 3DS if needed                    │     │
│   └───────────────────────────────────────────────────────┘     │
│           │                                                      │
│           ↓                                                      │
│   ┌───────────────────────────────────────────────────────┐     │
│   │ Stripe Webhook → Supabase Edge Function:               │     │
│   │  → invoice.paid                                        │     │
│   │  → customer.subscription.updated                       │     │
│   │  → Update profiles.subscription_tier                   │     │
│   └───────────────────────────────────────────────────────┘     │
│           │                                                      │
│           ↓                                                      │
│   ┌───────────────────────────────────────────────────────┐     │
│   │ Supabase Realtime:                                     │     │
│   │  → Push subscription status to app                     │     │
│   │  → App unlocks Pro features                            │     │
│   └───────────────────────────────────────────────────────┘     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 Subscription tiers

| Tier | Price | Features |
|------|-------|----------|
| **Free** | $0 | 50 tasks, 10 voice minutes/month, basic reminders |
| **Pro** | $4.99/mo | Unlimited tasks, unlimited voice, recurring tasks, priority support |
| **Team** (future) | $9.99/user/mo | Shared workspaces, collaboration, admin controls |

### 10.3 Android Stripe integration

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.stripe:stripe-android:22.6.0")
}

// SubscriptionManager.kt
class SubscriptionManager @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val context: Context
) {
    private lateinit var paymentSheet: PaymentSheet
    
    fun initialize(activity: ComponentActivity) {
        paymentSheet = PaymentSheet(activity, ::onPaymentSheetResult)
    }
    
    suspend fun startProSubscription(): Result<Unit> {
        // 1. Request subscription setup from backend
        val response = supabaseClient.functions
            .invoke("create-subscription") {
                body = mapOf("price_id" to "price_pro_monthly")
            }
        
        val setupData = response.body<SubscriptionSetupResponse>()
        
        // 2. Configure and present PaymentSheet
        val config = PaymentSheet.Configuration(
            merchantDisplayName = "Jarvis Tasks",
            customer = PaymentSheet.CustomerConfiguration(
                id = setupData.customerId,
                ephemeralKeySecret = setupData.ephemeralKey
            ),
            googlePay = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Production,
                countryCode = "US",
                currencyCode = "USD"
            ),
            primaryButtonLabel = "Subscribe for $4.99/month"
        )
        
        paymentSheet.presentWithPaymentIntent(
            setupData.clientSecret,
            config
        )
        
        return Result.success(Unit)
    }
    
    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Webhook will update subscription status
                // Listen via Supabase Realtime for confirmation
            }
            is PaymentSheetResult.Canceled -> {
                // User canceled
            }
            is PaymentSheetResult.Failed -> {
                // Handle error
            }
        }
    }
}
```

### 10.4 Important: Google Play billing policy

**Note**: If selling digital content consumed within the app AND distributing via Google Play Store, you **must** use Google Play Billing instead of Stripe for:
- In-app subscriptions
- In-game currencies
- Premium content unlocks

Stripe can be used for:
- Physical goods/services
- Out-of-app consumption
- Web-based subscriptions (user signs up on website, uses in app)

**Recommendation**: For Phase 3, implement Stripe for web signups and evaluate Google Play Billing for in-app purchases based on Google's policies at launch time.

---

## 11. LLM and STT integration recommendations

### 11.1 STT provider selection

| Language | Primary Provider | Fallback | Offline |
|----------|-----------------|----------|---------|
| **Hindi + Hinglish** | Deepgram Nova-3 | Google Chirp 3 | Vosk Hindi |
| **English (Indian)** | Deepgram Nova-3 | AssemblyAI | Vosk Indian English |
| **Bengali** | Google Chirp 3 | Azure Speech | Whisper.cpp |
| **Urdu** | Google Chirp 3 | Azure Speech | Whisper.cpp |

### 11.2 LLM function calling schema

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
              "description": "ISO 8601 date/datetime"
            },
            "priority": {
              "type": ["string", "null"],
              "enum": ["high", "medium", "low", null]
            },
            "parent_task_reference": {
              "type": ["string", "null"],
              "description": "Reference to parent task if subtask"
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
        "description": "Update an existing task",
        "strict": true,
        "parameters": {
          "type": "object",
          "properties": {
            "task_reference": { "type": "string" },
            "updates": {
              "type": "object",
              "properties": {
                "title": {"type": ["string", "null"]},
                "due_date": {"type": ["string", "null"]},
                "priority": {"type": ["string", "null"]},
                "is_completed": {"type": ["boolean", "null"]}
              }
            }
          },
          "required": ["task_reference", "updates"],
          "additionalProperties": false
        }
      }
    }
  ]
}
```

---

## 12. MVP vs future phases

### Phase 1: MVP (8 weeks)

**Scope**:
- ✅ Google Sign-In via Credential Manager
- ✅ Email/password auth with Supabase
- ✅ Voice task creation (English, Hindi)
- ✅ Basic hierarchy (1 level of subtasks)
- ✅ Due date parsing (today, tomorrow, specific dates)
- ✅ Local storage with Room
- ✅ Cloud sync to Supabase
- ✅ Basic notifications
- ❌ No subscription (all features free)

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
- Voice corrections ("no, I meant...")
- Account management (password reset, deletion)
- Data export

### Phase 3: Monetization (6 weeks)

**Scope**:
- Stripe subscription integration
- Free tier limits enforcement
- Pro tier unlock
- Usage analytics
- Payment history

### Phase 4: Advanced features (ongoing)

**Scope**:
- Unlimited hierarchy depth
- Conversational context ("update that task")
- Natural language queries ("what's due this week?")
- Voice attachments
- Wake word activation
- Smart suggestions
- Calendar integration
- Cross-device sync improvements

---

## 13. Risks and mitigations

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| **Bengali STT accuracy too low** | High | High | Fine-tune Whisper on Bengali data; fall back to Google Chirp; collect user corrections |
| **LLM latency exceeds 2s target** | Medium | High | Stream partial results; use faster models (GPT-4o-mini); local LLM fallback |
| **Google Sign-In rejection** | Low | High | Ensure OAuth consent screen compliance; test on multiple devices; have email fallback |
| **Supabase outage** | Low | High | Offline-first design; local Room DB fully functional; background sync queue |
| **API cost at scale** | Medium | High | Tiered STT routing; batch processing; usage-based pricing tiers |
| **User trust issues with AI** | Medium | High | Always show parsed result; easy correction flow; 30s undo |
| **Stripe policy changes** | Low | Medium | Monitor Google Play billing requirements; evaluate hybrid approach |
| **Data breach** | Low | Critical | RLS policies; encrypted storage; regular security audits; no API keys in app |

---

## Appendix A: Key sources and references

### Authentication
- Android Credential Manager: https://developer.android.com/identity/sign-in/credential-manager
- Supabase Auth: https://supabase.com/docs/guides/auth
- Google Sign-In Migration: https://developer.android.com/identity/sign-in/legacy-gsi-migration

### Backend & Database
- Supabase vs Firebase: https://supabase.com/alternatives/supabase-vs-firebase
- Row Level Security: https://supabase.com/docs/guides/database/postgres/row-level-security
- Supabase Realtime: https://supabase.com/docs/guides/realtime

### Payments
- Stripe Android SDK: https://docs.stripe.com/sdks/android
- Stripe Subscriptions: https://docs.stripe.com/billing/subscriptions/build-subscriptions

### Security
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
- OWASP MASVS: https://mas.owasp.org/MASVS/
- EncryptedSharedPreferences: https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences

### Speech-to-Text
- Deepgram Nova-3: https://deepgram.com/learn/introducing-nova-3-speech-to-text-api
- Google Chirp 3: https://cloud.google.com/speech-to-text/docs/models

---

*Document Version: 2.0 | Last Updated: January 17, 2026*
*Added: Authentication, Cloud Sync, Security, Stripe Integration*