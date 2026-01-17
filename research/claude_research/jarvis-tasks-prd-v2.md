# Jarvis for Tasks: AI-Powered Voice-First Android Task Manager PRD

**Version**: 2.0 | **Date**: January 17, 2026  
**Status**: Implementation-Ready

---

## Executive Summary

This PRD defines a voice-first Android task management application enabling users to create, update, and manage tasks through natural conversation in **Bangla, Hindi, Urdu, and English**.

**Key Architecture Decisions:**
- **Backend**: Supabase (PostgreSQL + Auth + RLS + Realtime)
- **Auth**: Google Sign-In via Credential Manager (primary) + Email/Password
- **STT**: Deepgram Nova-3 (Hindi/English), Google Chirp 3 (Bengali/Urdu)
- **LLM**: OpenAI via Supabase Edge Functions (API keys never in app)
- **Payments**: Stripe (Phase 3) - research complete, implementation deferred
- **Architecture**: Clean Architecture + MVVM, offline-first with Room

**Target latency**: Sub-2-second voice-to-confirmation.

---

## Table of Contents

1. [User Personas & Use Cases](#1-user-personas--use-cases)
2. [Functional Requirements](#2-functional-requirements)
3. [Non-Functional Requirements](#3-non-functional-requirements)
4. [UI/UX Design](#4-uiux-design)
5. [System Architecture](#5-system-architecture)
6. [Authentication & User Management](#6-authentication--user-management)
7. [Data Model & Cloud Sync](#7-data-model--cloud-sync)
8. [Security Architecture](#8-security-architecture)
9. [STT & LLM Integration](#9-stt--llm-integration)
10. [Stripe Payment Integration (Phase 3)](#10-stripe-payment-integration-phase-3)
11. [Multilingual Strategy](#11-multilingual-strategy)
12. [MVP vs Future Phases](#12-mvp-vs-future-phases)
13. [Risks & Mitigations](#13-risks--mitigations)
14. [References](#14-references)

---

## 1. User Personas & Use Cases

### Primary Personas

| Persona | Description | Key Needs |
|---------|-------------|-----------|
| **Riya, 28** | Professional in Dhaka, Bangla/English | Voice during commute, code-switching |
| **Amit, 35** | Business owner in Delhi, Hinglish | Hands-free, subtask organization |
| **Sara, 42** | Working mother in Karachi, Urdu | RTL interface, reliable reminders |
| **David, 31** | Remote worker, English | Cross-device sync, natural dates |

### Core Use Cases

**UC1**: Voice task creation - "Add buy groceries tomorrow morning, high priority"  
**UC2**: Hierarchical tasks - "Add pack bags as a subtask to weekend trip"  
**UC3**: Conversational updates - "Move that to next week"  
**UC4**: Code-switching - "कल meeting के बाद grocery लेनी है"  
**UC5**: Offline capture with background sync  
**UC6**: Cross-device sync via Supabase Realtime

---

## 2. Functional Requirements

### 2.1 Authentication & Account Management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-A1 | Google Sign-In via Credential Manager | P0 |
| FR-A2 | Email/password registration with verification | P0 |
| FR-A3 | Password reset via email | P0 |
| FR-A4 | Session persistence with encrypted storage | P0 |
| FR-A5 | Account deletion with data purge (GDPR) | P0 |
| FR-A6 | Profile management (display name, avatar) | P1 |
| FR-A7 | Multi-device session management | P1 |

### 2.2 Voice Input

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-V1 | Continuous voice input with intelligent endpointing | P0 |
| FR-V2 | Real-time transcription feedback (waveform + text) | P0 |
| FR-V3 | Push-to-talk and optional wake word | P0 |
| FR-V4 | Background noise handling | P1 |
| FR-V5 | Voice cancellation ("cancel", "never mind") | P0 |
| FR-V6 | Voice corrections ("no, I meant...") | P1 |

### 2.3 Task Management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-T1 | Create tasks with title, due date, priority, tags | P0 |
| FR-T2 | Unlimited hierarchy (tasks → subtasks → sub-subtasks) | P0 |
| FR-T3 | Relative date parsing ("tomorrow", "next Friday") | P0 |
| FR-T4 | Recurring tasks (daily, weekly, monthly) | P1 |
| FR-T5 | Voice-based task updates | P0 |
| FR-T6 | Task queries ("what's due tomorrow?") | P1 |
| FR-T7 | Conversational context resolution | P0 |
| FR-T8 | Cloud sync with conflict resolution | P0 |

### 2.4 Subscription & Billing (Phase 3)

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-B1 | Free tier with usage limits | P2 |
| FR-B2 | Pro subscription via Stripe PaymentSheet | P2 |
| FR-B3 | Subscription status sync with backend | P2 |
| FR-B4 | Grace period for failed payments | P2 |
| FR-B5 | In-app subscription management | P2 |

---

## 3. Non-Functional Requirements

| Category | Requirement | Target |
|----------|-------------|--------|
| **Performance** | Voice-to-confirmation latency | < 2 seconds |
| **Performance** | App cold start | < 1.5 seconds |
| **Performance** | STT streaming latency | < 300ms |
| **Reliability** | Offline task creation | 100% success |
| **Reliability** | Crash rate | < 0.5% sessions |
| **Scalability** | Tasks per user | 10,000+ |
| **Security** | Encryption at rest | AES-256-GCM |
| **Security** | API communication | TLS 1.3 |
| **Security** | API keys | Backend proxy only |
| **Security** | Auth tokens | Short-lived JWT + secure refresh |
| **Accessibility** | WCAG compliance | 2.2 AA |
| **Localization** | Languages | EN, HI, BN, UR |
| **Localization** | RTL support | Full (Urdu) |

---

## 4. UI/UX Design

### 4.1 Design Principles

1. **Radical Simplicity**: Max 5-7 visible tasks, single-focus voice mode
2. **Confidence-Aware Feedback**: >90% auto-execute, 70-90% quick confirm, <70% disambiguate
3. **Voice-First, Touch-Fallback**: All actions by voice, touch for corrections
4. **Immediate Feedback**: Visual response within 100ms

### 4.2 Key Screens

**Onboarding/Sign-In**
```
┌─────────────────────────────────┐
│       🎤 Jarvis Tasks           │
│   Your voice-first task manager │
│                                 │
│  [🔵 Continue with Google]      │
│  [✉️  Sign up with Email]       │
│                                 │
│  Already have account? Log in   │
└─────────────────────────────────┘
```

**Home/Task List**
```
┌─────────────────────────────────┐
│ ☰  Jarvis Tasks       🔍  👤   │
├─────────────────────────────────┤
│ TODAY (3)                       │
│ ○ Buy groceries        🔴 2PM  │
│   └ ○ Get milk                 │
│   └ ○ Buy eggs                 │
│ ○ Call mom             🟡 5PM  │
│ ○ Review PRD           🟢 EOD  │
│                                 │
│        [🎤 Speak]               │
└─────────────────────────────────┘
```

**Voice Input Overlay**
```
┌─────────────────────────────────┐
│        ╭──────────╮             │
│        │ 🎤 )))   │  ← Pulsing  │
│        ╰──────────╯             │
│   ▁▃▅▇▅▃▁▃▅▇▅▃▁  ← Waveform    │
│ "Add buy groceries tomorrow..." │
│      [Cancel]  [Done]           │
└─────────────────────────────────┘
```

### 4.3 Microphone State Machine

| State | Visual | Audio | Haptic |
|-------|--------|-------|--------|
| Idle | Muted mic | None | None |
| Listening | Pulsing ring + waveform | 200ms tone | Light tap |
| Processing | Dots animation | None | None |
| Confirming | Result card | Chime | Success tap |
| Error | Red shake | Error tone | Strong vibration |

---

## 5. System Architecture

### 5.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    ANDROID CLIENT                        │
├─────────────────────────────────────────────────────────┤
│  UI Layer (Compose) │ Voice Layer │ Background Services │
│         ↓                  ↓               ↓            │
│  ┌─────────────────────────────────────────────────┐    │
│  │         PRESENTATION (ViewModels + UDF)          │    │
│  └─────────────────────────────────────────────────┘    │
│                          ↓                              │
│  ┌─────────────────────────────────────────────────┐    │
│  │    DOMAIN (Use Cases + Interfaces + Models)      │    │
│  └─────────────────────────────────────────────────┘    │
│                          ↓                              │
│  ┌─────────────────────────────────────────────────┐    │
│  │ DATA: Room DB │ STT Client │ LLM Client │ Auth  │    │
│  │               │ Supabase Client (Auth + Sync)    │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                   SUPABASE BACKEND                       │
├─────────────────────────────────────────────────────────┤
│ Auth (GoTrue) │ PostgreSQL + RLS │ Edge Functions       │
│ Storage       │ Realtime (Sync)  │ (LLM Proxy, Stripe)  │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│               EXTERNAL SERVICES                          │
│  OpenAI API │ Deepgram STT │ Stripe (Phase 3)           │
└─────────────────────────────────────────────────────────┘
```

### 5.2 Module Structure

```
project/
├── app/
├── core/
│   ├── common/        # Extensions, utilities
│   ├── data/          # Repository implementations
│   ├── database/      # Room database, DAOs
│   ├── domain/        # Use cases, interfaces
│   ├── network/       # Retrofit, API definitions
│   ├── auth/          # Supabase Auth, Credential Manager
│   └── ui/            # Design system, theme
├── feature/
│   ├── auth/          # Login, registration
│   ├── tasks/         # Task list, detail
│   ├── voice/         # Voice input, STT
│   ├── reminders/     # Notifications
│   ├── settings/      # Preferences
│   └── subscription/  # Pro upgrade (Phase 3)
└── buildSrc/
```

---

## 6. Authentication & User Management

### 6.1 Backend: Supabase (Recommended)

**Why Supabase over Firebase:**

| Factor | Supabase | Firebase |
|--------|----------|----------|
| Database | PostgreSQL (relational) | Firestore (NoSQL) |
| Data model fit | ✅ Foreign keys for hierarchy | ❌ Document model awkward |
| RLS Security | SQL-based policies | JavaScript-like rules |
| Vendor lock-in | ✅ Open-source, self-host | ❌ Proprietary |
| Migration path | Standard PostgreSQL | Difficult export |

### 6.2 Google Sign-In with Credential Manager

```kotlin
class GoogleSignInManager @Inject constructor(
    private val context: Context,
    private val authProvider: AuthProvider
) {
    private val credentialManager = CredentialManager.create(context)
    
    suspend fun signIn(): AuthResult {
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
}
```

### 6.3 Secure Token Storage

```kotlin
class SecureTokenStorage @Inject constructor(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context, "auth_prefs", masterKey,
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
}
```

### 6.4 Account Deletion Flow

```kotlin
suspend fun deleteAccount() {
    // 1. Delete all user data via RLS-protected delete
    supabaseClient.from("tasks")
        .delete { filter { eq("user_id", getCurrentUserId()) } }
    
    // 2. Delete user from Supabase Auth
    supabaseClient.auth.admin.deleteUser(getCurrentUserId())
    
    // 3. Clear local data
    roomDatabase.clearAllTables()
    secureTokenStorage.clearTokens()
    
    // 4. Sign out
    credentialManager.clearCredentialState(ClearCredentialStateRequest())
}
```

---

## 7. Data Model & Cloud Sync

### 7.1 Supabase PostgreSQL Schema

```sql
-- Profiles (extends auth.users)
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

-- Tasks with hierarchical support
CREATE TABLE public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Content
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
    recurrence_rule TEXT,
    
    -- Priority & Tags
    priority INTEGER DEFAULT 2,
    tags JSONB DEFAULT '[]',
    
    -- Sync metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    client_updated_at TIMESTAMPTZ,
    version INTEGER DEFAULT 1
);

-- Indexes
CREATE INDEX idx_tasks_user_id ON public.tasks(user_id);
CREATE INDEX idx_tasks_parent_id ON public.tasks(parent_id);
CREATE INDEX idx_tasks_due_date ON public.tasks(due_date);
```

### 7.2 Row Level Security (RLS)

```sql
-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

-- Profiles: users access own profile only
CREATE POLICY "Users can view own profile" ON public.profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

-- Tasks: users access own tasks only
CREATE POLICY "Users can view own tasks" ON public.tasks
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can create own tasks" ON public.tasks
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own tasks" ON public.tasks
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own tasks" ON public.tasks
    FOR DELETE USING (auth.uid() = user_id);

-- Auto-create profile on signup
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
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
```

### 7.3 Local Room Schema

```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("parentId"),
        Index("dueDate"),
        Index("syncStatus"),
        Index("updatedAt")
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val originalTitle: String,
    val originalLanguage: String,
    val normalizedTitle: String?,
    val description: String? = null,
    val parentId: String? = null,
    val level: Int = 0,
    val position: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val dueDate: Long? = null,
    val dueDateTimezone: String? = null,
    val reminderTime: Long? = null,
    val durationMinutes: Int? = null,
    val recurrenceRule: String? = null,
    val priority: Int = 2,
    val tags: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clientUpdatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    val serverVersion: Int? = null
)

enum class SyncStatus {
    SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE, CONFLICT
}
```

### 7.4 Offline-First Sync Strategy

```kotlin
class TaskSyncManager @Inject constructor(
    private val localDao: TaskDao,
    private val supabaseClient: SupabaseClient
) {
    suspend fun startRealtimeSync(userId: String) {
        val channel = supabaseClient.channel("tasks-$userId")
        channel.postgresChangeFlow<PostgresAction>(
            schema = "public", table = "tasks",
            filter = "user_id=eq.$userId"
        ).collect { change ->
            when (change) {
                is PostgresAction.Insert -> handleRemoteInsert(change.record)
                is PostgresAction.Update -> handleRemoteUpdate(change.record)
                is PostgresAction.Delete -> handleRemoteDelete(change.oldRecord)
            }
        }
        channel.subscribe()
    }
    
    suspend fun syncPendingChanges() {
        val pendingTasks = localDao.getPendingSync()
        pendingTasks.forEach { task ->
            when (task.syncStatus) {
                SyncStatus.PENDING_CREATE -> {
                    supabaseClient.from("tasks").insert(task.toSupabaseTask())
                    localDao.updateSyncStatus(task.id, SyncStatus.SYNCED)
                }
                SyncStatus.PENDING_UPDATE -> {
                    // Optimistic concurrency with version check
                    val result = supabaseClient.from("tasks")
                        .update(task.toSupabaseTask()) {
                            filter { eq("id", task.id); eq("version", task.serverVersion ?: 0) }
                        }
                    if (result.data.isEmpty()) handleConflict(task)
                    else localDao.updateSyncStatus(task.id, SyncStatus.SYNCED)
                }
                SyncStatus.PENDING_DELETE -> {
                    supabaseClient.from("tasks").delete { filter { eq("id", task.id) } }
                    localDao.hardDelete(task.id)
                }
                else -> {}
            }
        }
    }
}
```

---

## 8. Security Architecture

### 8.1 API Key Protection

**CRITICAL**: Never ship OpenAI/Deepgram API keys in the app. Use Supabase Edge Functions as proxy.

```typescript
// supabase/functions/llm-proxy/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"

const OPENAI_API_KEY = Deno.env.get('OPENAI_API_KEY')

serve(async (req) => {
    // Verify JWT from Supabase Auth
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) return new Response('Unauthorized', { status: 401 })
    
    const { messages, tools } = await req.json()
    const userId = getUserIdFromJwt(authHeader)
    
    // Rate limiting
    if (!await checkRateLimit(userId)) {
        return new Response('Rate limit exceeded', { status: 429 })
    }
    
    // Forward to OpenAI
    const response = await fetch('https://api.openai.com/v1/chat/completions', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${OPENAI_API_KEY}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ model: 'gpt-4o-mini', messages, tools, tool_choice: 'auto' })
    })
    
    return new Response(response.body, { headers: { 'Content-Type': 'application/json' } })
})
```

### 8.2 Security Checklist

| Layer | Protection | Implementation |
|-------|------------|----------------|
| Auth tokens | Encrypted storage | EncryptedSharedPreferences + MasterKey |
| API keys | Backend proxy | Supabase Edge Functions |
| Network | TLS 1.3 | Network Security Config |
| Data at rest | AES-256 | Room encryption + EncryptedFile |
| Audio data | User control | Delete after transcription (configurable) |
| Logs | No PII | ProGuard strip, no sensitive logging |

### 8.3 Network Security Config

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors><certificates src="system" /></trust-anchors>
    </base-config>
    <domain-config>
        <domain includeSubdomains="true">your-project.supabase.co</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">AAAA...</pin>
            <pin digest="SHA-256">BBBB...</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

---

## 9. STT & LLM Integration

### 9.1 STT Provider Selection

| Language | Primary | Fallback | Offline |
|----------|---------|----------|---------|
| Hindi + Hinglish | Deepgram Nova-3 | Google Chirp 3 | Vosk Hindi |
| English (Indian) | Deepgram Nova-3 | AssemblyAI | Vosk |
| Bengali | Google Chirp 3 | Azure Speech | Whisper.cpp |
| Urdu | Google Chirp 3 | Azure Speech | Whisper.cpp |

### 9.2 LLM Function Calling Schema

```json
{
  "tools": [
    {
      "type": "function",
      "function": {
        "name": "create_task",
        "strict": true,
        "parameters": {
          "type": "object",
          "properties": {
            "title": { "type": "string" },
            "due_date": { "type": ["string", "null"] },
            "priority": { "type": ["string", "null"], "enum": ["high", "medium", "low", null] },
            "parent_task_reference": { "type": ["string", "null"] }
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
        "strict": true,
        "parameters": {
          "type": "object",
          "properties": {
            "task_reference": { "type": "string" },
            "updates": {
              "type": "object",
              "properties": {
                "title": { "type": ["string", "null"] },
                "due_date": { "type": ["string", "null"] },
                "is_completed": { "type": ["boolean", "null"] }
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

## 10. Stripe Payment Integration (Phase 3)

### 10.1 Architecture

```
User taps "Upgrade"
    ↓
Android: POST /functions/v1/create-subscription
    ↓
Supabase Edge Function:
  1. Verify JWT
  2. Create/retrieve Stripe Customer
  3. Create Subscription + PaymentIntent
  4. Return client_secret
    ↓
Android: Present PaymentSheet (card / Google Pay)
    ↓
Stripe Webhook → Edge Function:
  - invoice.paid
  - customer.subscription.updated
  - Update profiles.subscription_tier
    ↓
Supabase Realtime → App unlocks Pro features
```

### 10.2 Subscription Tiers

| Tier | Price | Features |
|------|-------|----------|
| **Free** | $0 | 50 tasks, 10 voice min/month |
| **Pro** | $4.99/mo | Unlimited tasks, unlimited voice, recurring |

### 10.3 Android Integration

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.stripe:stripe-android:22.6.0")
}

class SubscriptionManager @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private lateinit var paymentSheet: PaymentSheet
    
    suspend fun startProSubscription(): Result<Unit> {
        val response = supabaseClient.functions
            .invoke("create-subscription") { body = mapOf("price_id" to "price_pro_monthly") }
        
        val setupData = response.body<SubscriptionSetupResponse>()
        
        val config = PaymentSheet.Configuration(
            merchantDisplayName = "Jarvis Tasks",
            customer = PaymentSheet.CustomerConfiguration(
                id = setupData.customerId,
                ephemeralKeySecret = setupData.ephemeralKey
            ),
            googlePay = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Production,
                countryCode = "US", currencyCode = "USD"
            ),
            primaryButtonLabel = "Subscribe for $4.99/month"
        )
        
        paymentSheet.presentWithPaymentIntent(setupData.clientSecret, config)
        return Result.success(Unit)
    }
}
```

### 10.4 Google Play Policy Note

**Important**: If selling digital content consumed in-app AND distributing via Play Store:
- In-app subscriptions **must** use Google Play Billing
- Stripe can be used for web signups, physical goods, or out-of-app consumption

**Recommendation**: Implement Stripe for web signups; evaluate Play Billing at launch.

---

## 11. Multilingual Strategy

### 11.1 Language Detection & Routing

| Detected | STT Provider | LLM Prompt | RTL |
|----------|--------------|------------|-----|
| Hindi | Deepgram Nova-3 | hi | No |
| Bengali | Google Chirp 3 | bn | No |
| Urdu | Google Chirp 3 | ur | Yes |
| English | Deepgram Nova-3 | en | No |

### 11.2 Numeral Normalization

```kotlin
object NumeralNormalizer {
    private val BANGLA = "০১২৩৪৫৬৭৮৯"
    private val HINDI = "०१२३४५६७८९"
    private val URDU = "۰۱۲۳۴۵۶۷۸۹"
    private val WESTERN = "0123456789"
    
    fun normalize(text: String): String {
        var result = text
        listOf(BANGLA, HINDI, URDU).forEach { digits ->
            digits.forEachIndexed { i, d -> result = result.replace(d, WESTERN[i]) }
        }
        return result
    }
}
```

### 11.3 Dual Storage

- **originalTitle**: User's language (displayed)
- **normalizedTitle**: English translation (search)
- **searchTokens**: Original + transliterated + translated

---

## 12. MVP vs Future Phases

### Phase 1: MVP (8 weeks)
- ✅ Google Sign-In + Email/password
- ✅ Voice task creation (English, Hindi)
- ✅ Basic hierarchy (1 level)
- ✅ Local Room + Cloud sync to Supabase
- ✅ Basic notifications
- ❌ No subscription (all free)

### Phase 2: Enhanced Multilingual (6 weeks)
- Bengali, Urdu support
- Code-switching, RTL
- Recurring tasks
- Account management (password reset, deletion)
- Data export

### Phase 3: Monetization (6 weeks)
- Stripe subscription
- Free tier limits
- Pro unlock

### Phase 4: Advanced (ongoing)
- Unlimited hierarchy
- Conversational context
- Voice attachments
- Calendar integration

---

## 13. Risks & Mitigations

| Risk | Prob | Impact | Mitigation |
|------|------|--------|------------|
| Bengali STT low accuracy | High | High | Fine-tune Whisper; Google Chirp fallback |
| LLM latency > 2s | Medium | High | Stream results; GPT-4o-mini; local fallback |
| Google Sign-In rejection | Low | High | OAuth compliance; email fallback |
| Supabase outage | Low | High | Offline-first; local Room fully functional |
| Data breach | Low | Critical | RLS; encrypted storage; no API keys in app |
| Stripe policy changes | Low | Medium | Monitor Play Billing requirements |

---

## 14. References

### Authentication
- Android Credential Manager: https://developer.android.com/identity/sign-in/credential-manager
- Supabase Auth: https://supabase.com/docs/guides/auth

### Backend
- Supabase vs Firebase: https://supabase.com/alternatives/supabase-vs-firebase
- Row Level Security: https://supabase.com/docs/guides/database/postgres/row-level-security

### Payments
- Stripe Android SDK: https://docs.stripe.com/sdks/android
- Stripe Subscriptions: https://docs.stripe.com/billing/subscriptions/build-subscriptions

### Security
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
- OWASP MASVS: https://mas.owasp.org/MASVS/

### STT
- Deepgram Nova-3: https://deepgram.com/learn/introducing-nova-3
- Google Chirp 3: https://cloud.google.com/speech-to-text/docs/models

---

*Document Version: 2.0 | Last Updated: January 17, 2026*
