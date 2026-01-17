Perfect — thanks for the clarification.
Below is a **FULL, LONG-FORM PRODUCT REQUIREMENTS DOCUMENT (PRD)**, written **as if this app is going to be built, reviewed, audited, and published**.

This is **not a summary**.
This is **behavioral, step-by-step, implementation-aware**, and aligned with **everything we discussed**.

You can:

* Hand this to another AI for deeper research
* Give it to a dev team
* Use it as a Play Store–safe internal spec
* Use it as a single source of truth while building

---

# 📕 PRODUCT REQUIREMENTS DOCUMENT (PRD)

## Voice-First AI Task Management Application (Android)

---

## 1. PRODUCT OVERVIEW

### 1.1 Product Name (Working)

Voice-first AI Task Manager
(Name intentionally generic for now)

---

### 1.2 Product Goal

The goal of this application is to allow users to **manage tasks, reminders, and alarms using natural speech**, while maintaining:

* **Full user control**
* **No hidden behavior**
* **Strict Android & Google Play compliance**
* **High trust and transparency**
* **Future-ready AI extensibility**

This is **not** a background assistant.
This is **not** an always-listening app.
This is a **user-initiated, voice-driven productivity tool**.

---

### 1.3 Core Value Proposition

* Users can **speak naturally**, even long and messy speech
* The app intelligently extracts tasks and reminders
* The user **reviews and confirms** before anything critical happens
* Reminders are **reliable**, **battery-safe**, and **Play Store compliant**
* AI helps interpret intent but **never acts silently**

---

## 2. TARGET USERS

### 2.1 Primary Users

* Busy professionals
* Students
* Neurodivergent users who prefer voice over typing
* Users in multilingual environments (Bangla, Hindi, Urdu, English)

### 2.2 Secondary Users

* Users migrating from manual task apps
* Users who want reminders but distrust “smart assistants”

---

## 3. PLATFORM & TECHNICAL CONSTRAINTS

### 3.1 Supported Platform

* Android **12+ (API 31+)**
* Kotlin
* Clean Architecture
* MVVM
* Modular app design

### 3.2 Hard Constraints (Non-Negotiable)

The app **must never**:

* Record audio without explicit user action
* Use hotword / always-on listening
* Use Accessibility API for automation
* Speak unexpectedly without notification context
* Run infinite or long background services
* Hide account deletion
* Bypass Google Play Billing for digital purchases

These constraints shape **every design decision**.

---

## 4. USER ACCOUNT & IDENTITY

### 4.1 Account Requirement

**User registration is mandatory.**

Rationale:

* Tasks must persist across devices
* Reminders must survive reinstall
* Pro entitlements must be enforceable
* Account deletion must be possible

---

### 4.2 Authentication Methods

#### Supported login methods:

1. **Continue with Google**
2. **Email + password**

Implementation:

* Supabase Auth
* Android Credential Manager
* Secure token storage (Android Keystore)

---

### 4.3 First Launch Behavior

**App launch → Authentication screen**

UI:

* App branding
* Short explanation:

  > “Your tasks are securely saved to your account and synced across devices.”
* Buttons:

  * Continue with Google
  * Continue with email

User **cannot proceed** without choosing one.

---

### 4.4 Session Handling

* Sessions persist securely
* Token refresh handled silently
* On token expiry:

  * User is prompted to re-authenticate
* On sign-out:

  * Local cache is cleared
  * Alarms are canceled
  * User must sign in again to continue

---

### 4.5 Account Deletion (CRITICAL)

#### In-app behavior:

1. User navigates to **Settings → Account**
2. User taps **Delete account**
3. App shows:

   * What data will be deleted
   * Recovery window (e.g., 30 days)
4. User must confirm (typed confirmation or long-press)
5. App:

   * Immediately signs user out
   * Marks account as deleted (soft delete)
   * Cancels all alarms
   * Prevents future login

#### Backend behavior:

* All user data is soft-deleted
* After retention window, data is permanently purged

#### Web deletion page:

* Required for Play Store compliance
* Allows deletion request without app access
* Behavior matches in-app deletion

---

## 5. DATA STORAGE & BACKEND

### 5.1 Backend Stack

* Supabase
* PostgreSQL
* Row Level Security (RLS)
* Edge Functions for privileged operations

---

### 5.2 Data Ownership Rules

* Every row belongs to exactly one `user_id`
* Client can only access rows where `auth.uid() = user_id`
* No shared data
* No public access

---

### 5.3 Core Entities

* Profile
* Task (nested)
* Reminder
* User Entitlement
* Voice / AI Log (minimal, optional)

---

## 6. TASK MANAGEMENT BEHAVIOR

### 6.1 Task Structure

A task may have:

* Title (required)
* Description (optional)
* Status
* Due date (optional)
* Reminders (0..n)
* Parent task (optional)

Unlimited nesting allowed.

---

### 6.2 Task Creation – Manual

User can:

* Tap “+”
* Type task title
* Optionally set due date / reminder
* Save

This works offline (cached) and syncs later.

---

### 6.3 Task Creation – Voice (Short Speech)

Example:

> “Add task submit visa documents next Tuesday at 5 PM”

Flow:

1. User taps mic
2. App listens
3. Speech → text
4. AI parses intent
5. App **shows preview**:

   * Task title
   * Due date
   * Reminder
6. User confirms
7. Task is saved
8. Reminder scheduled

---

### 6.4 Task Creation – Voice (Long Speech)

Example:

> “Okay so tomorrow I need to call the bank, also remind me to submit my visa docs by Friday, and sometime later I should book flight tickets…”

Flow:

1. User taps mic
2. App records until silence or stop
3. Speech → text
4. AI extracts **candidate tasks**
5. App shows **review screen**:

   * List of extracted tasks
   * Each task editable
6. User:

   * Edits
   * Deletes
   * Confirms
7. Only confirmed tasks are saved

❗ No task is created without review.

---

### 6.5 Task Completion

Voice:

> “Mark groceries done”

Flow:

* If exactly one match → complete
* If multiple matches → clarification
* If ambiguous → confirmation required

---

## 7. REMINDERS, TIMERS & ALARMS

### 7.1 Reminder Types

* Notification reminder
* Exact alarm (if permission granted)

---

### 7.2 Scheduling Rules

* WorkManager for flexible reminders
* AlarmManager for exact alarms
* If exact alarm permission denied:

  * Use inexact fallback
  * Inform user

---

### 7.3 Reminder Trigger Behavior

When reminder fires:

1. Notification appears
2. Optional spoken reminder (if enabled)
3. Notification actions:

   * Mark done
   * Snooze
   * Reply with voice

---

## 8. VOICE SYSTEM

### 8.1 Microphone Rules

Microphone activates **only when**:

* User taps mic
* User taps widget
* User taps notification action

❌ No background listening
❌ No hotword

---

### 8.2 Voice Reply from Notification

Flow:

1. Reminder notification shows
2. User taps “Reply with voice”
3. App starts foreground service
4. Mic opens
5. User speaks
6. AI interprets response
7. App confirms action

---

### 8.3 Text-to-Speech Behavior

* Short confirmations only
* Never long paragraphs
* Disabled during quiet hours
* User toggle available

---

## 9. AI SYSTEM BEHAVIOR

### 9.1 Role of AI

AI is **not autonomous**.

AI:

* Interprets speech
* Outputs structured commands
* Requests clarification when unsure

AI **does not**:

* Create data directly
* Schedule alarms directly
* Override user intent

---

### 9.2 AI Input Context

AI receives:

* Transcript
* Current time
* Timezone
* User preferences
* Small set of relevant tasks

AI never receives full DB.

---

### 9.3 AI Output

AI returns:

* JSON command OR
* Clarification request

No free text.

---

### 9.4 Confirmation Rules

AI must request confirmation for:

* Deletion
* Completion when ambiguous
* Quiet-hour alarms
* Vague time references

---

## 10. UX TRUST & SAFETY

### 10.1 Transparency

User always sees:

* What AI understood
* What action will happen
* How to undo

---

### 10.2 Undo

* Undo available for:

  * Task creation
  * Completion
  * Reminder changes

---

## 11. PRIVACY & DATA HANDLING

### 11.1 Voice Data

* Not stored by default
* Transcripts stored minimally
* No background capture

---

### 11.2 AI Disclosure

* Clear disclosure
* User can disable AI features
* Provider named

---

## 12. BILLING (FUTURE)

### 12.1 Free Tier

* Core task features
* Limited AI

### 12.2 Pro Tier

* Advanced AI
* Automations
* Higher limits

### 12.3 Payment Rules

* Google Play Billing for Android
* Stripe only on web / outside Play app

---

## 13. PLAY STORE COMPLIANCE

Mandatory:

* Privacy policy
* Data Safety form
* Account deletion URL
* Accurate permission usage

Prohibited:

* Hidden mic usage
* Accessibility abuse
* External billing steering

---

## 14. NON-FUNCTIONAL REQUIREMENTS

### Security

* TLS everywhere
* Encrypted local storage
* Secrets server-only

### Performance

* Voice start < 300ms
* AI response < 5s
* Reminder reliability > 99%

---

## 15. FINAL STATEMENT

This application is designed to:

* Respect users
* Respect Android OS
* Respect Google Play policies
* Scale safely with AI

If built according to this PRD, the app is:

* **Publishable**
* **Trustworthy**
* **Future-proof**

---

Understood.
This time I will **not list topics** and **not summarize**.
I will **describe behaviorally**, step-by-step, **what happens, why it happens, how the app behaves on screen, in background, in AI, in backend**, and **what decisions the system makes**.

Below is a **true long-form PRD / SRS**, written the way **a senior product + platform architect** would write it for a regulated, AI-assisted Android app.

Read it slowly — this is the *actual blueprint* of your product.

---

# 📕 FULL DEEP PRODUCT REQUIREMENTS DOCUMENT

## Voice-First AI Task Management Application (Android)

---

## 1. PRODUCT INTENT AND NON-NEGOTIABLE PRINCIPLES

### 1.1 What this app IS

This application is a **user-initiated voice interaction system** for managing personal tasks, reminders, and alarms.

It is designed to behave like a **trusted tool**, not like a background assistant.

The app:

* Responds **only when the user asks**
* Acts **only after the user confirms**
* Speaks **only when contextually appropriate**
* Stores data **only under an authenticated user identity**
* Uses AI **only as an interpreter**, never as an authority

---

### 1.2 What this app is NOT

This application is **not**:

* A hotword-based assistant
* An always-listening service
* A background automation engine
* A system-level assistant replacement
* An app that silently modifies user data

These exclusions are **intentional** and **architectural**, not limitations.

---

## 2. FIRST LAUNCH & ACCOUNT CREATION — ACTUAL BEHAVIOR

### 2.1 Why login is mandatory

The app **does not allow anonymous usage**.

Reasoning:

* Tasks must survive app reinstall
* Reminders must persist across devices
* AI personalization requires continuity
* Google Play requires account deletion paths if accounts exist
* Future Pro subscriptions require identity

Because of this, **authentication happens before functionality**.

---

### 2.2 First launch flow (screen-level)

**When the app opens for the first time:**

1. The user sees a clean onboarding screen:

   * App name
   * One-sentence explanation:

     > “Your tasks are securely saved to your account and synced across devices.”
2. Two primary actions are shown:

   * Continue with Google
   * Continue with email

No “skip” option exists.

---

### 2.3 Continue with Google — exact behavior

When the user taps **Continue with Google**:

1. Android Credential Manager launches Google account chooser
2. User selects a Google account
3. Supabase Auth verifies identity
4. If user exists:

   * Session is restored
   * App proceeds to main screen
5. If user is new:

   * Profile row is created
   * Default entitlement (Free) is created
   * App proceeds to main screen

No password handling happens on device.

---

### 2.4 Email + password — exact behavior

When user chooses email login:

1. User enters email + password
2. Password is never stored on device
3. Supabase handles hashing + verification
4. Email verification may be required (configurable)
5. On success, same flow as Google login

---

### 2.5 Session persistence

Once logged in:

* Tokens are stored securely
* Session auto-refreshes silently
* User does not need to log in repeatedly

If session expires:

* User is redirected to login
* Local task cache is cleared
* Alarms are canceled for safety

---

## 3. CORE HOME SCREEN — HOW USER EXPERIENCES THE APP

### 3.1 What the home screen represents

The home screen is **not a chat app**.

It is:

* A task list
* A control center
* A voice entry point

---

### 3.2 UI structure (behavioral)

The home screen contains:

* A large microphone button (primary CTA)
* A list of tasks (sorted by relevance)
* Visual indicators for:

  * Due soon
  * Has reminder
  * Overdue
* A manual “+” add button

The mic button is always visible because voice is the primary interaction.

---

## 4. VOICE INTERACTION — EXACT RULES & FLOW

### 4.1 When microphone activates

The microphone **only activates** in these situations:

1. User taps the mic button
2. User taps a widget
3. User taps a notification action (“Reply with voice”)

There is **no other code path** that activates audio.

---

### 4.2 Voice capture lifecycle

When the mic is activated:

1. App starts a **foreground service**
2. A visible indicator shows “Listening…”
3. System microphone indicator lights up
4. Audio capture begins
5. Silence detection or user tap stops recording
6. Foreground service stops immediately

At no point does the mic stay open after this.

---

### 4.3 Speech-to-text behavior

* On-device STT is preferred when available
* Cloud STT is used only if needed
* Raw audio is **not stored**
* Only the transcript is kept temporarily

---

## 5. LONG SPEECH HANDLING — WHAT ACTUALLY HAPPENS

### 5.1 Problem this solves

Users do not speak in clean commands.

They say things like:

> “Okay so tomorrow I need to call the bank, and also remind me to submit visa docs by Friday, and oh I should probably book flights later…”

This speech contains:

* Tasks
* Dates
* Irrelevant filler
* Uncertainty

---

### 5.2 System behavior for long speech

The app does **NOT** immediately create tasks.

Instead:

1. Transcript is generated
2. AI is asked to **extract candidate tasks**
3. AI returns:

   * A list of possible tasks
   * Each with guessed metadata (optional)
4. App shows a **review screen**

---

### 5.3 Review screen behavior

The review screen:

* Lists each extracted task
* Allows editing:

  * Title
  * Due date
  * Reminder
* Allows deletion
* Allows reordering

Only when the user taps **Confirm** are tasks saved.

This prevents:

* Hallucinated tasks
* Unwanted reminders
* User confusion

---

## 6. AI SYSTEM — WHAT IT CAN AND CANNOT DO

### 6.1 AI’s responsibility

AI’s only responsibility is:

> Convert human speech into structured intent.

It does **not**:

* Decide what matters
* Override user preferences
* Schedule alarms by itself
* Modify the database directly

---

### 6.2 AI input discipline

AI is intentionally **starved of context**.

It receives:

* Current transcript
* Current time
* User timezone
* User preferences
* A small list of relevant tasks

It does NOT receive:

* Full task list
* Past history
* Hidden data

This prevents over-reach and hallucination.

---

### 6.3 AI output discipline

AI must return **only JSON**.

Two possible outputs:

1. A command (create, update, complete, etc.)
2. A clarification question

If AI is uncertain, it **must ask**.

---

## 7. TASK CREATION — EXACT DECISION LOGIC

### 7.1 Example: clear command

User says:

> “Add task submit visa documents next Tuesday at 5 PM”

System behavior:

1. AI parses intent
2. Task preview shown
3. User confirms
4. Task saved
5. Reminder scheduled

---

### 7.2 Example: ambiguous time

User says:

> “Remind me tomorrow morning”

System behavior:

1. AI detects ambiguity
2. AI returns clarification:

   * “What time tomorrow morning?”
3. App asks user
4. User selects time
5. Task created

AI **never guesses**.

---

## 8. REMINDERS & ALARMS — RELIABILITY FIRST

### 8.1 Reminder scheduling logic

When a reminder is confirmed:

1. App checks if exact alarm permission exists
2. If yes:

   * Schedule exact alarm
3. If no:

   * Schedule inexact reminder
   * Inform user of limitation

---

### 8.2 Reminder firing behavior

When reminder triggers:

1. Notification appears
2. Notification includes actions:

   * Mark done
   * Snooze
   * Reply with voice
3. Optional spoken reminder plays (if enabled)

The app **never speaks without a notification**.

---

## 9. VOICE REPLY FROM NOTIFICATION — FULL FLOW

When user taps **Reply with voice**:

1. App launches minimal UI
2. Foreground service starts
3. Mic opens
4. User says:

   > “Yes, done”
5. AI interprets response
6. App confirms action
7. Task updated
8. Reminder canceled

---

## 10. TRUST & SAFETY — HOW USER IS PROTECTED

### 10.1 Transparency guarantees

User always knows:

* What the AI heard
* What action will happen
* How to undo it

---

### 10.2 Undo behavior

Undo is available for:

* Task creation
* Completion
* Reminder changes

Undo is **time-bounded** to avoid complexity.

---

## 11. DATA, PRIVACY & DELETION — EXACT SEMANTICS

### 11.1 Voice data

* Audio is transient
* Transcripts are minimal
* No background capture
* No selling or training

---

### 11.2 Account deletion — what actually happens

When user deletes account:

1. User is logged out
2. Tokens revoked
3. Tasks marked deleted
4. Reminders canceled
5. AI logs scrubbed
6. After retention window:

   * Data permanently removed

This is consistent across app and web.

---

## 12. MONETIZATION — DESIGNED BUT DORMANT

### 12.1 Why it’s designed early

Billing affects:

* Data models
* Entitlements
* Feature flags

So it’s designed now but not activated.

---

### 12.2 Billing rules

* Android app uses **Google Play Billing**
* Stripe used only on web later
* No steering inside app

---

## 13. FAILURE MODES — WHAT IF THINGS GO WRONG

### 13.1 AI fails

* User informed
* Manual fallback shown

### 13.2 Network down

* Tasks saved locally
* Sync later

### 13.3 Permission denied

* Feature degrades gracefully
* User informed

---

## 14. FINAL GUARANTEE

This app:

* Respects Android
* Respects Google Play
* Respects users
* Uses AI responsibly

It is **safe to build**, **safe to publish**, and **safe to scale**.


