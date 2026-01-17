# CONSOLIDATED PRODUCT REQUIREMENTS DOCUMENT (PRD)

**VoiceTasker - Voice-First AI Task Management Application**

---

## TABLE OF CONTENTS

1. Product Overview
2. Target Users & Personas
3. Core Features & Functionality
4. Voice Interaction Patterns
5. Task Management Behavior
6. Reminders, Timers & Alarms
7. AI System Architecture
8. Data Models & Schema
9. Functional Requirements (Detailed)
10. Non-Functional Requirements
11. User Journey & Workflows
12. Edge Cases & Error Handling

---

## 1. PRODUCT OVERVIEW

### 1.1 Product Definition

**VoiceTasker** is a voice-first AI task management application for Android that allows users to manage personal tasks, reminders, and alarms using natural speech while maintaining full user control, transparency, and Android/Google Play compliance.

### 1.2 Product Positioning

| Aspect | Position |
|--------|----------|
| **Category** | Voice-first Productivity / Task Management |
| **Platform** | Android 12+ |
| **Language Support** | Phase 1: English (+ optional Bengali), Phase 2: Bengali, Hindi, Urdu, English |
| **Privacy Model** | User-initiated, no background listening, on-device first |
| **AI Role** | Interpreter (not autonomous) |
| **Monetization** | Freemium: Free tier (50 tasks) + Pro tier ($4.99/mo unlimited) |

### 1.3 Core Value Proposition

- **Friction-free task capture**: Speak naturally, app understands complex sentences
- **Intelligent extraction**: AI parses tasks, deadlines, reminders from messy speech
- **User verification**: Review before any critical action (no silent execution)
- **Reliable reminders**: WorkManager + AlarmManager for persistent notifications
- **Offline-ready**: Tasks work without internet; cloud sync when online
- **Trustworthy AI**: No hidden behavior, transparent decisions, easy override

---

## 2. TARGET USERS & PERSONAS

### 2.1 Primary Users

1. **Busy Professionals**
   - Use case: Quick task capture during meetings/commute
   - Pain point: Typing is slow, voice is faster
   - Expected usage: 5-10 tasks/day

2. **Students**
   - Use case: Capture assignment deadlines and project reminders
   - Pain point: Hard to organize nested tasks (project → subtasks)
   - Expected usage: 20-30 tasks/semester

3. **Neurodivergent Users**
   - Use case: Verbal task entry (ADHD, dyslexia, autism spectrum)
   - Pain point: Typing barriers; voice is more natural
   - Expected usage: 10-20 tasks/day

4. **Multilingual Users**
   - Use case: Mixed-language speech (code-switching)
   - Pain point: English-only apps force English thinking
   - Expected usage: 10+ tasks/day with language mixing

### 2.2 Secondary Users

1. **Power users migrating from other apps** (Todoist, Google Tasks)
2. **Accessibility-conscious users** (prefer voice over TalkBack navigation)
3. **Battery-conscious users** (offline-first reduces network drain)

---

## 3. CORE FEATURES & FUNCTIONALITY

### 3.1 Feature Overview

| Feature | Phase | Description |
|---------|-------|-------------|
| Voice task creation | 1 | Create task from speech |
| Manual task creation | 1 | Type task title + metadata |
| Task hierarchy | 2 | Unlimited nesting (subtasks) |
| Reminders | 1 | Notification at specific time |
| Alarms | 1 | Exact time alarms (with permission) |
| Task updates | 2 | Modify via voice or manual |
| Task completion | 1 | Mark done via voice or tap |
| Cloud sync | 2 | Sync across devices |
| Multilingual support | 2 | Bengali, Hindi, Urdu, English |
| Pro tier features | 3 | Unlimited tasks, advanced AI |
| Recurring tasks | 4 | Repeat daily/weekly/monthly |
| Calendar integration | 4 | View tasks in calendar UI |

### 3.2 Core Entities

1. **Task**
   - Title (required)
   - Description (optional)
   - Due date (optional)
   - Priority (optional, Phase 2+)
   - Status (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)
   - Reminders (0..n)
   - Parent task ID (optional, Phase 2+)
   - Created/updated timestamps
   - User ID (ownership)

2. **Reminder**
   - Task ID (foreign key)
   - Scheduled time
   - Reminder type (notification, alarm)
   - Status (PENDING, FIRED, DISMISSED, SNOOZED)

3. **User Profile**
   - Email + auth credentials
   - Timezone
   - Language preference
   - Subscription tier (Free/Pro)
   - Created/updated timestamps

4. **Entitlement**
   - User ID (foreign key)
   - Subscription tier
   - Expiry date (if applicable)
   - Limits (task count, API usage)

5. **Voice Log** (optional, privacy-sensitive)
   - User ID (foreign key)
   - Transcript (text only)
   - Timestamp
   - Confidence score
   - Action taken

---

## 4. VOICE INTERACTION PATTERNS

### 4.1 Voice Activation Rules

The microphone **activates ONLY when**:

1. User taps the large mic button on home screen
2. User taps a widget
3. User taps "Reply with voice" on a notification

**No other activation paths exist** (no hotword, no background listening).

### 4.2 Short Command Pattern

**User says:**
> "Add task submit visa documents next Tuesday at 5 PM"

**System behavior:**
1. Start foreground service + show "Listening…"
2. Record until silence detected (configurable timeout)
3. Transcribe to text (on-device STT preferred, fallback to cloud)
4. Send to AI: "Extract task from: {transcript}"
5. AI returns: `{action: "create_task", title: "...", due: "2026-01-28T17:00", confidence: 0.95}`
6. Show preview screen (confirmation)
7. User confirms
8. Task saved, reminder scheduled

**Latency target**: < 2 seconds (transcription + AI + preview display)

### 4.3 Long Speech Pattern

**User says:**
> "Okay so tomorrow I need to call the bank, also remind me to submit visa docs by Friday, and sometime later I should book flight tickets…"

**System behavior:**
1. Foreground service starts, mic opens
2. Record until silence detected (e.g., 3 seconds of silence)
3. Transcribe entire speech
4. Send to AI: "Extract ALL tasks from: {transcript}"
5. AI returns:
   ```json
   {
     "action": "create_multiple_tasks",
     "tasks": [
       {"title": "Call the bank", "due": "2026-01-18", "confidence": 0.92},
       {"title": "Submit visa documents", "due": "2026-01-20", "confidence": 0.88},
       {"title": "Book flight tickets", "due": null, "confidence": 0.70}
     ],
     "clarification_needed": [{"task_index": 2, "reason": "No deadline mentioned"}]
   }
   ```
6. Show **review screen** with extracted tasks
7. User can:
   - Edit each task (title, date, reminder)
   - Delete any task
   - Add missing metadata
   - Confirm batch creation
8. Only confirmed tasks are saved

**Latency target**: < 3 seconds (transcription + AI extraction + review screen display)

### 4.4 Ambiguity Resolution Pattern

**User says:**
> "Remind me to call John tomorrow morning"

**System behavior:**
1. Transcription successful
2. AI processes: "Create reminder for 'call John' tomorrow morning at ?"
3. AI detects ambiguity: time not specified ("morning" is vague)
4. AI returns: `{action: "clarify", clarification: "What time tomorrow morning? (e.g., 8:00 AM, 9:00 AM)"}`
5. App shows dialog with time picker
6. User selects time
7. Task created with reminder at selected time

**Principle**: AI **never guesses**; it asks for clarification.

### 4.5 Voice Reply from Notification

When a reminder notification is shown:

**User taps "Reply with voice":**
1. Notification action triggers
2. Foreground service starts
3. Minimal UI shown ("Listening for response...")
4. Mic opens
5. User says: "Yes, done" OR "Snooze for 1 hour" OR "Mark completed"
6. Transcribe + send to AI
7. AI parses intent
8. Task updated (e.g., marked complete, snoozed)
9. Confirmation shown briefly

---

## 5. TASK MANAGEMENT BEHAVIOR

### 5.1 Task Creation — Manual

**Flow:**
1. User taps "+" button
2. Input form shown:
   - Title (required)
   - Description (optional)
   - Due date (optional, date picker)
   - Reminder (optional, time picker)
3. User taps "Save"
4. Task saved locally (Room)
5. Synced to cloud (if online)

### 5.2 Task Creation — Voice (Detailed)

**See Section 4.2 & 4.3** for voice patterns.

### 5.3 Task Hierarchy (Phase 2)

**Requirement**: Unlimited nesting depth (e.g., Project → Epic → Story → Subtask)

**Parent task behavior:**
- Parent tasks can have 0..n subtasks
- Completion of all subtasks marks parent as ready-to-complete
- Deleting parent cascades to subtasks (user confirms)
- Viewing parent shows subtask progress bar

**UI pattern:**
- Collapsible list (expand/collapse subtasks)
- Indentation shows hierarchy level
- Visual indicators (progress bars, completion %)

### 5.4 Task Completion

**Manual completion:**
1. User taps task
2. User taps "Mark complete"
3. Task status → COMPLETED
4. Reminder canceled

**Voice completion:**

User says: "Mark groceries done"

1. Transcribe: "Mark groceries done"
2. AI: "Find task matching 'groceries'"
3. Search local tasks: fuzzy match on "groceries"
4. If exact match (1 result) → complete immediately
5. If multiple matches → show disambiguation ("Did you mean 'Grocery shopping' or 'Buy groceries'?")
6. If no match → clarify ("No task matching 'groceries' found. Create it?")

### 5.5 Task Status Lifecycle

```
PLANNED
  ↓
IN_PROGRESS (manual or voice: "I'm working on...")
  ↓
COMPLETED (voice: "Done" or manual tap)

PLANNED → CANCELLED (manual or voice: "Cancel...")
```

---

## 6. REMINDERS, TIMERS & ALARMS

### 6.1 Reminder Types

| Type | Trigger | Behavior | Permission Needed |
|------|---------|----------|------------------|
| **Notification reminder** | WorkManager at scheduled time | Notification appears, optional TTS | POST_NOTIFICATIONS (Android 13+) |
| **Exact alarm** | AlarmManager at exact time | Alarm triggers, TTS optional | SCHEDULE_EXACT_ALARM or USE_EXACT_ALARM |
| **Flexible reminder** | WorkManager (15-30 min window) | Notification appears | POST_NOTIFICATIONS (Android 13+) |

### 6.2 Scheduling Logic

**When user confirms a reminder:**

1. App checks if SCHEDULE_EXACT_ALARM permission exists
2. If yes:
   - Schedule using AlarmManager for exact time
   - Inform user: "Alarm set for 5:00 PM"
3. If no:
   - Schedule using WorkManager for flexible time
   - Inform user: "Reminder set for ~5:00 PM (flexible)"

### 6.3 Reminder Firing Behavior

When reminder fires:

1. Notification appears with:
   - Task title
   - Due time (if applicable)
   - Actions:
     - "Mark done" (completes task)
     - "Snooze" (15 min default, user-configurable)
     - "Reply with voice" (capture voice response)
2. Optional: TTS plays spoken reminder
   - Only if TTS enabled in settings
   - Respects silent/vibrate mode
3. System sets audio focus (music pauses if playing)

### 6.4 Snooze Behavior

Default snooze duration: 15 minutes (user-configurable in settings)

User can snooze via:
- Notification action
- Voice reply ("Snooze 1 hour")
- Quick settings

---

## 7. AI SYSTEM ARCHITECTURE

### 7.1 AI Role & Constraints

**What AI does:**
- Interprets speech into structured intent
- Extracts entities (title, date, time, priority, etc.)
- Requests clarification when uncertain
- Suggests next actions (Phase 2+)

**What AI does NOT do:**
- Create data directly (always needs user confirmation)
- Schedule alarms autonomously
- Delete tasks without user approval
- Speak without notification context
- Access user data beyond current request

### 7.2 AI Input Context

AI receives:
- **Current transcript** (speech converted to text)
- **Current time** (for date relative to now)
- **User timezone**
- **User language preference**
- **Recent task list** (last 10-20 tasks, for reference resolution)
- **User settings** (time format, language, etc.)

AI does NOT receive:
- Full historical task list
- User email or credentials
- Payment/subscription data
- Any data not needed for current request

### 7.3 AI Output Format

AI returns **structured JSON only**:

**Success case:**
```json
{
  "action": "create_task",
  "task": {
    "title": "Submit visa documents",
    "due_date": "2026-01-20T00:00:00Z",
    "priority": "HIGH",
    "reminder": {
      "time": "2026-01-19T18:00:00Z",
      "type": "notification"
    }
  },
  "confidence": 0.92,
  "explanation": "Parsed: 'submit visa docs' (deadline Friday from context = Jan 20)"
}
```

**Clarification case:**
```json
{
  "action": "clarify",
  "clarification_questions": [
    {
      "field": "due_date",
      "question": "When should 'book flight tickets' be done? (You mentioned 'later')",
      "suggestions": ["Tomorrow", "Next week", "Next month"]
    }
  ]
}
```

**Error case:**
```json
{
  "action": "error",
  "message": "Unable to parse intent. Could you rephrase?",
  "reason": "Speech too ambiguous (no task action detected)"
}
```

### 7.4 Confidence Scoring

AI assigns confidence to each extraction:

- **HIGH (> 0.90)**: Auto-confirm, show preview
- **MEDIUM (0.70-0.90)**: Show preview, user must confirm
- **LOW (< 0.70)**: Request clarification, don't auto-proceed

---

## 8. DATA MODELS & SCHEMA

### 8.1 Core Tables (Room + Supabase)

**Task**
```sql
CREATE TABLE tasks (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES auth.users(id),
  title TEXT NOT NULL,
  description TEXT,
  status ENUM('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'),
  due_date TIMESTAMP,
  priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
  parent_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  version INT DEFAULT 1,
  CHECK (user_id IS NOT NULL)
);
```

**Reminder**
```sql
CREATE TABLE reminders (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES auth.users(id),
  task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
  scheduled_time TIMESTAMP NOT NULL,
  reminder_type ENUM('NOTIFICATION', 'ALARM', 'FLEXIBLE'),
  status ENUM('PENDING', 'FIRED', 'DISMISSED', 'SNOOZED'),
  snoozed_until TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

**User Profile**
```sql
CREATE TABLE profiles (
  user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  email TEXT NOT NULL UNIQUE,
  timezone TEXT DEFAULT 'UTC',
  language TEXT DEFAULT 'en',
  tts_enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

**Entitlements**
```sql
CREATE TABLE entitlements (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  tier ENUM('FREE', 'PRO') DEFAULT 'FREE',
  task_limit INT DEFAULT 50,
  voice_minutes_limit INT DEFAULT 10,
  stripe_subscription_id TEXT,
  expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

### 8.2 Row Level Security (RLS) Policy

**Principle**: Users can only access their own data.

```sql
-- Task access
CREATE POLICY task_access ON tasks
  USING (user_id = auth.uid())
  WITH CHECK (user_id = auth.uid());

-- Reminder access
CREATE POLICY reminder_access ON reminders
  USING (user_id = auth.uid())
  WITH CHECK (user_id = auth.uid());
```

---

## 9. FUNCTIONAL REQUIREMENTS (DETAILED)

### 9.1 User Registration & Authentication

**FR-AUTH-001: Google Sign-In**
- Use Android Credential Manager to request Google account
- Verify with Supabase Auth
- Auto-create profile on first login
- Store auth token securely (Android Keystore)

**FR-AUTH-002: Email + Password Auth**
- User enters email + password
- Supabase handles validation
- Email verification optional (configurable)
- Token stored securely

**FR-AUTH-003: Session Persistence**
- Session auto-restores on app launch
- Tokens refreshed silently when near expiry
- Logout clears tokens + local cache + cancels alarms

**FR-AUTH-004: Account Deletion**
- In-app deletion option in Settings
- Soft-delete on backend (recoverable for 30 days)
- Hard-delete after retention window
- Web deletion page required (Google Play compliance)

### 9.2 Task Management

**FR-TASK-001: Create Task (Manual)**
- User taps "+" → form shown
- User enters title (required) + optional metadata
- Save creates local task (Room) + syncs to cloud

**FR-TASK-002: Create Task (Voice)**
- User taps mic → records speech
- Transcription + AI extraction
- Review screen shown
- User confirms → task saved

**FR-TASK-003: Read Task**
- Task displayed with all metadata
- Subtasks shown if expanded
- Task history/changelog optional (Phase 2+)

**FR-TASK-004: Update Task (Manual)**
- User taps task → edit form
- Modify title, due date, priority, description
- Save updates local + cloud

**FR-TASK-005: Update Task (Voice)**
- User says: "Change deadline to Friday"
- AI: find task in context, parse new deadline
- Confirm change
- Task updated

**FR-TASK-006: Delete Task**
- User taps task → tap delete
- Confirmation dialog shown
- Task marked as CANCELLED (soft delete)
- Reminders canceled

**FR-TASK-007: Complete Task**
- User marks task COMPLETED (manual or voice)
- Reminders associated with task canceled
- Task moves to completed section

**FR-TASK-008: Task Hierarchy (Phase 2)**
- Parent tasks can have subtasks
- Unlimited nesting allowed
- Deleting parent cascades to subtasks (user confirms)

### 9.3 Reminders & Alarms

**FR-REMINDER-001: Schedule Reminder**
- User sets reminder time for task
- App checks permission availability
- Use exact alarm (if permitted) or flexible reminder
- Inform user of actual reminder behavior

**FR-REMINDER-002: Reminder Notification**
- At scheduled time, notification appears
- Notification shows task title + actions
- User can: Mark done, Snooze, Reply with voice

**FR-REMINDER-003: Snooze**
- User taps "Snooze" → task re-notified in 15 min (configurable)
- Snoozed_until timestamp updated

**FR-REMINDER-004: Silent/Vibrate Mode**
- System respects device silent mode
- TTS skipped if in silent mode (unless alarm)
- Notification still shown (visual reminder)

### 9.4 Voice System

**FR-VOICE-001: Microphone Activation**
- Microphone activates ONLY on:
  - User taps mic button
  - User taps widget
  - User taps notification "Reply" action

**FR-VOICE-002: Recording & Transcription**
- Foreground service started (visible indicator)
- Record until silence detected (3 sec timeout)
- Transcribe to text (on-device STT preferred)
- Foreground service stopped immediately after transcription

**FR-VOICE-003: AI Processing**
- Transcript sent to AI (via Supabase Edge Function)
- AI extracts structured intent
- Response returned as JSON

**FR-VOICE-004: User Confirmation**
- Preview/review screen shown before saving
- User approves/edits before commit
- No data saved without user confirmation

**FR-VOICE-005: TTS Response**
- Optional spoken confirmation after action
- Brief message only (e.g., "Task created")
- Respects silent mode
- User can disable in settings

### 9.5 Cloud Sync & Offline

**FR-SYNC-001: Offline-First Storage**
- All tasks stored locally (Room) first
- Cloud sync happens asynchronously
- UI always shows local data (no waiting for cloud)

**FR-SYNC-002: Cloud Sync**
- When online, changes synced to Supabase
- Realtime subscriptions for other devices
- Conflict resolution: last-write-wins (client timestamp)

**FR-SYNC-003: Sync Failures**
- Network error → queue change for retry
- Max retries: 3 (exponential backoff)
- User notified of sync failures
- Manual "Sync now" button available

---

## 10. NON-FUNCTIONAL REQUIREMENTS

### 10.1 Performance

| Metric | Target | Rationale |
|--------|--------|-----------|
| STT latency | < 300ms | Perception of responsiveness |
| AI response latency | < 2s | Conversational UX |
| Voice-to-confirmation | < 2s | User doesn't wait |
| End-to-end (long speech) | < 3s | Acceptable dialog latency |
| Task list render | < 100ms | Smooth UI |
| Database query (task fetch) | < 50ms | 10,000+ tasks |
| Cloud sync initial | < 5s | Background, user tolerant |

### 10.2 Reliability

- **Reminder delivery**: > 99% (exact alarms work reliably)
- **Crash rate**: < 0.5% of sessions
- **Data loss**: 0% (encryption + backup)
- **Offline mode**: 100% functional (no internet)

### 10.3 Security

- **Encryption**: AES-256-GCM for local storage
- **TLS**: 1.3+ for all network traffic
- **Token storage**: Android Keystore (Hardware-backed if available)
- **API keys**: Never in app (Supabase Edge Functions proxy)

### 10.4 Accessibility

- **WCAG 2.2 Level AA** compliance
- Screen reader support (TalkBack)
- Captions for TTS
- High contrast mode support
- Minimum font size: 14sp

### 10.5 Localization

**Phase 1**: English (+ optional Bengali)
**Phase 2**: Bengali, Hindi, Urdu, English

- RTL support for Urdu
- Unicode normalization
- Numeral conversion (0-9 → Bengali/Hindi numerals)
- Language detection (first 500ms of speech)

---

## 11. USER JOURNEY & WORKFLOWS

### 11.1 First-Time User

1. Download app from Play Store
2. Launch app → auth screen shown
3. Tap "Continue with Google"
4. Select Google account
5. Profile created automatically
6. Redirect to home screen (empty task list)
7. Intro modal shown:
   - "Try tapping the microphone button and say a task"
   - "Example: Add task call mom tomorrow at 5 PM"
8. User taps mic → guided voice entry
9. Task preview shown
10. User confirms → task saved
11. Intro modal closes, task appears in list
12. Success! User familiar with core flow.

### 11.2 Quick Task Capture

1. User at work, has urgent task
2. Tap mic button (always visible)
3. Say: "Add task send report to boss by 3 PM"
4. Preview shown (1 sec)
5. User confirms (tap "Create")
6. Task saved (1 sec)
7. Reminder set (if specified)
8. Total time: ~3 seconds

### 11.3 Complex Task Entry

1. User has multiple tasks to capture
2. Tap mic
3. Say: "Okay so tomorrow call the bank, also by Friday I need to submit my visa docs, and sometime soon I should book flights"
4. Transcription complete (1-2 sec)
5. AI extracts 3 tasks (0.5 sec)
6. Review screen shown:
   - Task 1: "Call the bank" (Tomorrow)
   - Task 2: "Submit visa documents" (Friday)
   - Task 3: "Book flights" (No deadline)
7. User edits Task 3: adds deadline "Next Monday"
8. User confirms all
9. 3 tasks saved
10. Total time: ~5 seconds

### 11.4 Reminder Notification

1. 5:00 PM, task due
2. Notification appears: "Submit visa documents"
   - Actions: "Mark done", "Snooze", "Reply with voice"
3. User taps "Reply with voice"
4. Foreground service starts
5. App listens
6. User says: "Done"
7. AI: parse "Done" → complete task
8. Task marked completed
9. Notification dismissed
10. Confirmation: "Task completed ✓"

---

## 12. EDGE CASES & ERROR HANDLING

### 12.1 Speech Recognition Errors

**Case**: STT fails to transcribe (background noise, unclear speech)

**Behavior**:
1. User tapped mic, recorded speech
2. STT error occurs
3. App shows: "Sorry, couldn't hear you clearly. Try again?"
4. User can:
   - Tap mic again (retry)
   - Type task manually
5. No task created

### 12.2 Ambiguous Task Reference

**Case**: User says "Complete that task" but multiple tasks are recent

**Behavior**:
1. AI detects ambiguity: which task?
2. App shows: "Which task? Did you mean:"
   - Option 1: "Call the bank"
   - Option 2: "Submit visa documents"
3. User selects
4. Task marked complete

### 12.3 Unclear Deadline

**Case**: User says "Remind me to book flights later"

**Behavior**:
1. AI: "Book flights" task created
2. AI: deadline is ambiguous ("later" = vague)
3. App asks: "When would you like to book flights?"
   - Suggestions: Tomorrow, Next week, Later this month
4. User selects
5. Reminder scheduled

### 12.4 Network Failure During Sync

**Case**: Task created offline, network lost before sync

**Behavior**:
1. Task saved locally (Room)
2. Sync attempt fails (no network)
3. App queues sync for retry
4. UI shows sync indicator: "Syncing..." (badge on task)
5. Network restored → auto-sync happens
6. Sync complete → indicator removed

### 12.5 Permission Denied (Microphone)

**Case**: User denies microphone permission

**Behavior**:
1. User taps mic button
2. System prompts for microphone permission
3. User denies
4. App shows: "Microphone permission required for voice tasks"
   - Button: "Enable in Settings"
   - Button: "Add task manually"
5. User can add task via manual form

### 12.6 Permission Denied (Exact Alarm)

**Case**: SCHEDULE_EXACT_ALARM permission not available

**Behavior**:
1. User confirms reminder
2. App checks SCHEDULE_EXACT_ALARM
3. Permission not available
4. App uses WorkManager (flexible reminder)
5. User informed: "Reminder set for ~5:00 PM (flexible)"
6. Reminder fires within 15-30 min window

### 12.7 API Error (Supabase / OpenAI)

**Case**: OpenAI API returns error (rate limit, outage, etc.)

**Behavior**:
1. User taps mic, records speech
2. AI processing fails
3. Fallback: Use on-device rules engine
   - Simple date parsing
   - Template-based extraction
4. User shown preview (lower confidence noted)
5. User confirms or rejects
6. If rejected: "Try typing the task instead?"

### 12.8 Storage Quota Exceeded

**Case**: Device storage full, cannot save task

**Behavior**:
1. Task creation succeeds (in memory)
2. Save to Room fails (storage full)
3. App shows: "Storage full. Please delete some tasks or files."
4. Task not saved locally
5. User prompted to delete old tasks or clear cache

---

## SUMMARY

This PRD covers all functional and non-functional requirements for VoiceTasker Phase 1 & 2. The document prioritizes:

1. **User control** (always confirm)
2. **Latency** (< 2 seconds for core flows)
3. **Reliability** (offline-first)
4. **Privacy** (minimal data, encrypted)
5. **Accessibility** (WCAG 2.2 AA)

Implementation should follow this PRD verbatim; deviations require stakeholder approval.
