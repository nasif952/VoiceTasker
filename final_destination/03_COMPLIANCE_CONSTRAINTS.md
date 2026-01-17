# COMPLIANCE & ANDROID CONSTRAINTS DEEP DIVE

**VoiceTasker - Android 12+ & Google Play Store Compliance**

---

## TABLE OF CONTENTS

1. Android OS Constraints (API 31+)
2. Google Play Store Requirements
3. Background Execution Rules
4. Microphone & Audio Rules
5. Notification Rules
6. Permission Declaration & Justification
7. Privacy & Data Protection
8. AI/LLM Specific Rules
9. Pre-Launch Checklist
10. Common Pitfalls & Solutions

---

## 1. ANDROID OS CONSTRAINTS (API 31+)

### 1.1 Minimum SDK & Target SDK

**Requirement**: `minSdk = 31`, `targetSdk = 34+`

**Why Android 12+ (API 31)?**
- Approximate Share Sheet (privacy)
- Hibernation of unused apps (background control)
- Microphone & camera indicators
- Enhanced security model

**Target SDK**: Always target latest (34+)
- Ensures you follow latest Android best practices
- Required by Google Play (updates every Aug)

---

### 1.2 Doze Mode & Background Execution Limits

**What is Doze?**

Doze is Android's power-saving mode that suspends apps when:
- Device is unplugged
- Screen off for 10+ minutes
- No user interaction

**What's restricted in Doze?**
- Network access (blocked)
- Wake locks (ignored)
- JobScheduler (delayed)
- Alarms (delayed, except exact alarms)

**What bypasses Doze?**
- Exact alarms (AlarmManager.setAndAllowWhileIdle())
- Foreground services
- High-priority FCM messages

**For VoiceTasker:**

Reminders must use:
```kotlin
// This works in Doze
alarmManager.setAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    reminderTime,
    pendingIntent
)

// This does NOT work in Doze (WorkManager)
WorkManager.scheduleExact(...)  // ❌ Delays in Doze
WorkManager.schedule(...)       // ❌ Delays in Doze
```

**Consequence**: Use AlarmManager for exact reminders (bypasses Doze), WorkManager for flexible reminders (deferred in Doze).

---

### 1.3 Foreground Service Rules

**When can a foreground service start?**

A foreground service can ONLY start from:
1. User taps something (button, notification action, widget)
2. System broadcasts BOOT_COMPLETED or specific system events
3. Never from background task or broadcast receiver

**For VoiceTasker:**

✅ **ALLOWED**:
- User taps mic button → start FGS for recording
- User taps notification "Reply" action → start FGS for voice response
- System calls BOOT_COMPLETED → start notification job (one-time)

❌ **NOT ALLOWED**:
- Broadcast receiver (e.g., TIMEZONE_CHANGED) starts FGS
- WorkManager task starts FGS
- AlarmManager callback starts FGS

**Implementation**:

```kotlin
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Suppress("InlinedApi")
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // Create notification (required for FGS)
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Listening...")
        .setSmallIcon(R.drawable.ic_mic)
        .build()

    // Start foreground service (Android 12+ requires type)
    startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
    )

    // Do work...
    recordAudio()

    // Stop immediately after done
    stopForeground(Service.STOP_FOREGROUND_REMOVE)
    return START_NOT_STICKY
}
```

**Declaration in AndroidManifest.xml**:

```xml
<service
    android:name=".feature.voice.data.VoiceRecordingForegroundService"
    android:foregroundServiceType="microphone"
    android:exported="false" />
```

---

### 1.4 Boot Completion & Device Startup

**Scenario**: Device reboots, app has pending reminders.

**Solution**: Listen to BOOT_COMPLETED

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<receiver
    android:name=".core.boot.BootCompletedReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.QUICKBOOT_POWERON" />
    </intent-filter>
</receiver>
```

```kotlin
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all pending reminders from database
            val context = context ?: return
            val viewModelScope = CoroutineScope(Job() + Dispatchers.Main)
            viewModelScope.launch {
                RescheduleRemindersUseCase()(context)
            }
        }
    }
}
```

---

## 2. GOOGLE PLAY STORE REQUIREMENTS

### 2.1 App Submission Requirements

**Mandatory before first submission**:

1. **Privacy Policy**
   - Clearly describe: what data is collected, how it's used, third-party sharing
   - Mention: voice transcripts, device ID, analytics
   - REQUIRED in manifest + Play Console form

2. **Data Safety Form**
   - Declare all data types collected
   - Yes/No for: analytics, crash reporting, personal info, location, etc.
   - Declare if data is encrypted, user-deletable

3. **Content Rating Questionnaire**
   - Usually "Everyone" for a task app
   - Fill accurately (Play will review)

4. **Account Deletion**
   - In-app deletion available
   - Web deletion page accessible
   - No account recovery hints after deletion

5. **Accurate Permissions**
   - Declare all permissions used
   - Provide justification for sensitive permissions

---

### 2.2 Data Safety Form (Critical)

**Google Play requires accurate completion.**

Example entries for VoiceTasker:

| Data Type | Collected? | Encrypted? | User-Deletable? | Retained? | Third-Party Sharing? |
|-----------|-----------|-----------|-----------------|-----------|----------------------|
| Audio | Yes | Yes | Yes | Until app uninstall | No (Edge Functions only) |
| Transcripts | Yes | Yes | Yes | 30 days then delete | Deepgram/Google (STT) |
| User ID | Yes | Yes | Yes | Until account deleted | Supabase only |
| Tasks | Yes | Yes | Yes | Indefinite (user choice) | Supabase only |
| Analytics | No | N/A | N/A | N/A | N/A |

**Consequence**: Any inaccuracy = rejected app or store removal.

---

### 2.3 Policy Violations (Instant Rejection)

**Deceptive Behavior Policy Violations:**

❌ **NOT ALLOWED**:
- App named "Google Task Assistant" (impersonation)
- Hidden background listening (even brief)
- "AI-powered" but only using regex
- Claiming privacy then selling data
- Recording without notification

✅ **ALLOWED**:
- "VoiceTasker" (own branding)
- Clear disclosure: "This app may record audio temporarily for transcription"
- Actually using AI (OpenAI)
- Data minimization + secure storage
- Notification during recording (visual indicator)

---

### 2.4 Audio Recording Policy

**Rule**: Transparent disclosure, no eavesdropping, user verification.

**Requirement**:
1. App MUST show visible indicator while recording
2. User MUST be able to stop recording anytime
3. Record only when user explicitly taps mic

**Implementation**:

```kotlin
// Show "Listening..." UI while recording
Scaffold {
    if (isRecording) {
        Text(
            "Listening...",
            color = Color.Red,
            fontSize = 24.sp
        )
        Icon(Icons.Default.Mic, tint = Color.Red)
    }
}

// Always provide stop button
Button(
    onClick = { stopRecording() },
    enabled = isRecording
) {
    Text("Stop")
}
```

---

## 3. BACKGROUND EXECUTION RULES

### 3.1 What can run in the background?

**Allowed**:
1. Exact alarms (AlarmManager)
2. Foreground services (if started from user action)
3. WorkManager (delayed, not guaranteed timing)
4. Firebase Cloud Messaging (FCM)

**Not allowed**:
1. Background services (Service that auto-starts)
2. Implicit broadcasts
3. Network requests from broadcast receivers
4. Indefinite PowerLocks

**For VoiceTasker reminders**:

```kotlin
// ✅ CORRECT: Exact alarm
alarmManager.setAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    triggerTime,
    pendingIntent
)

// ❌ WRONG: Background service
startService(Intent(this, MyService::class.java))

// ✅ CORRECT: WorkManager (flexible)
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_tasks",
    ExistingPeriodicWorkPolicy.KEEP,
    PeriodicWorkRequestBuilder<SyncTasksWorker>(
        1, TimeUnit.HOURS
    ).build()
)
```

---

### 3.2 WorkManager vs AlarmManager

| Aspect | WorkManager | AlarmManager |
|--------|------------|--------------|
| **Timing** | Flexible (minutes to hours) | Exact (millisecond precise) |
| **Doze Bypass** | No (delayed in Doze) | Yes (fires in Doze) |
| **Use Case** | Periodic sync, logs | Reminders, alarms, timers |
| **Battery Impact** | Low | Higher (wakes device) |
| **Guarantee** | Best-effort | High (when permission granted) |

**Decision for VoiceTasker**:
- **Exact reminders** (user-set time) → AlarmManager
- **Flexible reminders** (e.g., "sometime this morning") → WorkManager
- **Periodic sync** (every hour) → WorkManager
- **One-time tasks** (sync now) → WorkManager

---

## 4. MICROPHONE & AUDIO RULES

### 4.1 Microphone Permission

**Declaration**:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

**Runtime Permission Request** (Android 6+):

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        recordVoice()
    } else {
        showError("Microphone permission required for voice tasks")
    }
}

// Trigger on mic button tap
Button(onClick = {
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED) {
        recordVoice()
    } else {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
})
```

**Pre-permission explanation** (required by Play Store):

Show dialog BEFORE requesting permission:

```kotlin
if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
    AlertDialog(
        title = "Microphone Access",
        text = "VoiceTasker uses your microphone to record voice tasks. No data is stored locally.",
        confirmButton = {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    )
} else {
    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
}
```

---

### 4.2 Microphone Indicator (Mandatory Android 12+)

**Requirement**: Visual/audio indicator while recording.

**Built-in (Android 12+)**:
- System automatically shows mic indicator in status bar
- User can see which app is using mic

**For VoiceTasker** (additional transparency):
- Show "Listening..." overlay with mic icon
- Show waveform animation
- Allow user to tap "Stop" anytime

---

### 4.3 Voice Reply via Notification (Allowed)**

**Scenario**: Reminder notification has "Reply with voice" action.

**Implementation**:

```kotlin
// Notification action for voice reply
val replyAction = NotificationCompat.Action(
    R.drawable.ic_reply,
    "Reply with voice",
    createVoiceReplyPendingIntent(taskId)
)

val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle(task.title)
    .addAction(replyAction)
    .build()

// Handle voice reply intent
fun handleVoiceReply(taskId: String) {
    // Start foreground service for recording
    startForegroundService(Intent(this, VoiceReplyService::class.java).apply {
        putExtra("task_id", taskId)
    })
}
```

---

### 4.4 Text-to-Speech Rules

**Allowed**:
- Speak confirmations ("Task created")
- Read reminders ("You have a task to do")

**Restricted**:
- Don't impersonate system voice (e.g., Google Assistant)
- Don't speak sensitive info in public (location, passwords)
- Respect silent/vibrate mode

**Implementation**:

```kotlin
// Check silent mode before speaking
val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
val isSilent = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT

if (!isSilent && ttsEnabled) {
    tts.speak(message, TextToSpeech.QUEUE_FLUSH, null)
}
```

---

## 5. NOTIFICATION RULES

### 5.1 Notification Permission (Android 13+)

**Declaration**:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Runtime Permission Request**:

```kotlin
val notificationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        sendReminder()
    } else {
        // Gracefully degrade: show in-app alert instead
        showInAppAlert("Reminders disabled (notification permission denied)")
    }
}
```

---

### 5.2 Notification Channels

**Requirement**: Organize notifications into channels (Android 8+).

```kotlin
fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channels = listOf(
            NotificationChannel(
                "reminders",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your tasks"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            },
            NotificationChannel(
                "alarms",
                "Alarms",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Exact alarms for critical tasks"
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
            },
            NotificationChannel(
                "sync",
                "Sync Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Cloud sync notifications"
            }
        )

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannels(channels)
    }
}
```

---

### 5.3 Notification Actions

**Allowed**:
- Mark done
- Snooze
- Voice reply
- Delete
- View

**Restricted**:
- Don't launch third-party apps without user knowledge
- Don't perform destructive actions silently

**Implementation**:

```kotlin
val notification = NotificationCompat.Builder(context, "reminders")
    .setContentTitle(task.title)
    .setContentText("Due now")
    .addAction(
        R.drawable.ic_check,
        "Mark done",
        createCompleteTaskIntent(taskId)
    )
    .addAction(
        R.drawable.ic_snooze,
        "Snooze",
        createSnoozeIntent(taskId, 15 * 60 * 1000) // 15 min
    )
    .addAction(
        R.drawable.ic_reply,
        "Reply",
        createVoiceReplyIntent(taskId)
    )
    .setAutoCancel(true)
    .build()
```

---

## 6. PERMISSION DECLARATION & JUSTIFICATION

### 6.1 Permissions Required

**Essential**:

```xml
<!-- Recording voice -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- Internet (API calls) -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Notifications (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Alarms (exact reminder timing) -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- Boot completion (reschedule after device reboot) -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Read/write system settings (audio mode) -->
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

**Optional** (Phase 2+):
```xml
<!-- Calendar integration (future) -->
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />

<!-- Contacts (for reference resolution) -->
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

### 6.2 Play Store Permission Justification Form

**For each permission, fill form in Play Console:**

| Permission | Justification |
|-----------|---|
| RECORD_AUDIO | Required for voice task capture. Audio is transcribed only, never stored. |
| SCHEDULE_EXACT_ALARM | Required for reliable reminder delivery at user-set times. |
| POST_NOTIFICATIONS | Required to display task reminders to users. |
| INTERNET | Required to sync tasks with server and process speech-to-text. |
| RECEIVE_BOOT_COMPLETED | Required to reschedule reminders after device restart. |
| MODIFY_AUDIO_SETTINGS | Required to manage audio focus and text-to-speech playback. |

---

## 7. PRIVACY & DATA PROTECTION

### 7.1 Privacy Policy Requirements

**Must cover**:

1. **Data Collection**
   - Voice transcripts (temporary, deleted after processing)
   - Task data (stored indefinitely until user delete)
   - Device info (UUID for sync)
   - Crash logs (optional, if enabled)

2. **Data Usage**
   - For task management and sync
   - For speech recognition (Deepgram / Google)
   - For LLM processing (OpenAI)
   - Never for advertising

3. **Third-Party Services**
   - Supabase (backend, stores tasks + profile)
   - Deepgram/Google (STT transcription)
   - OpenAI (LLM intent extraction)
   - Stripe (payment, if Pro tier enabled)

4. **Data Retention**
   - Voice transcripts: deleted immediately after processing
   - Tasks: stored until user deletes
   - Account deletion: soft-delete (30-day recovery), permanent after 30 days

5. **User Rights**
   - Right to delete account (in-app + web)
   - Right to export data (future feature)
   - Right to disable AI processing (fallback to rules)

### 7.2 Encryption Requirements

**At Rest (Local Database)**:
- AES-256-GCM (Android Security Crypto)
- Hardware-backed Keystore if available

**In Transit (Network)**:
- TLS 1.3 for all HTTPS
- Certificate pinning for Supabase

**Token Storage**:
- Android Keystore (not SharedPreferences)
- EncryptedSharedPreferences for sensitive data

**Consequence**: Failure to encrypt = Play Store rejection + security vulnerability.

---

## 8. AI/LLM SPECIFIC RULES

### 8.1 AI Disclosure

**Requirement**: Clear disclosure if AI/LLM is used.

**In-app disclosure**:

```kotlin
val privacyText = """
VoiceTasker uses artificial intelligence (OpenAI GPT-4) to understand
your voice and extract tasks. Your voice is transcribed using third-party
services (Deepgram, Google) and sent to OpenAI only for processing.

Your data is never used to train AI models.
You can disable AI and use manual task entry instead.
"""
```

**In Privacy Policy**:

> "VoiceTasker uses OpenAI GPT-4 for natural language understanding.
> Transcribed speech is sent to OpenAI for processing. OpenAI may retain
> data for 30 days per their privacy policy. User voice data is never
> used to train OpenAI models."

---

### 8.2 Content Moderation

**Requirement**: Don't let AI produce harmful content.

**Implementation**:

```kotlin
// Don't allow LLM to create certain task types
fun validateAIOutput(intent: StructuredIntent): Result<Unit> {
    // Check if any task is flagged as sensitive
    val hasSensitiveContent = intent.tasks.any { task ->
        isContentFlaggedAsHarmful(task.title)
    }

    return if (hasSensitiveContent) {
        Result.failure(Exception("Content policy violation detected"))
    } else {
        Result.success(Unit)
    }
}

// Moderation via OpenAI Moderation API
suspend fun isContentFlaggedAsHarmful(text: String): Boolean {
    return try {
        val response = openaiClient.moderations(text)
        response.results.first().flagged
    } catch (e: Exception) {
        false // Don't block on error
    }
}
```

---

### 8.3 Fallback When AI Unavailable

**Requirement**: Graceful degradation if LLM fails.

**Implementation**:

```kotlin
// Primary: Use OpenAI
val result = llmService.extractIntent(transcript)
    .recoverCatching {
        // Fallback: Use rule-based extraction
        fallbackExtractIntent(transcript)
    }

fun fallbackExtractIntent(transcript: String): Result<StructuredIntent> {
    return try {
        val title = transcript.take(100) // Simple extraction
        val confidence = 0.50 // Low confidence
        Result.success(
            StructuredIntent(
                action = "create_task",
                tasks = listOf(ExtractedTask(title, null, confidence = confidence)),
                confidence = confidence,
                explanation = "Fallback extraction (LLM unavailable)"
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 9. PRE-LAUNCH CHECKLIST

### 9.1 Before First Submission

- [ ] Privacy Policy written + linked in manifest + Play Console
- [ ] Data Safety Form filled accurately
- [ ] Account deletion available in-app
- [ ] Web deletion page created (MUST be accessible without app)
- [ ] All permissions justified in Play Console form
- [ ] RECORD_AUDIO permission justification filled
- [ ] SCHEDULE_EXACT_ALARM permission justification filled
- [ ] No API keys hardcoded in app (use Edge Functions proxy)
- [ ] Audio indicator shown while recording ("Listening...")
- [ ] User can stop recording anytime
- [ ] Notification permission requested (Android 13+)
- [ ] Privacy policy mentions: Deepgram, Google, OpenAI, Supabase
- [ ] AI/LLM usage disclosed clearly in-app
- [ ] Content moderation implemented
- [ ] Crash rate < 0.5%
- [ ] Tested on Android 12, 13, 14, 15
- [ ] All target regions tested (if multilingual)

### 9.2 Before Each Update

- [ ] Check for new Android guidelines (Q1, Q3)
- [ ] Verify all third-party service terms haven't changed
- [ ] Update Privacy Policy if any service added/removed
- [ ] Re-verify Data Safety Form accuracy
- [ ] Test account deletion flow
- [ ] Verify no new permissions accidentally added

---

## 10. COMMON PITFALLS & SOLUTIONS

### 10.1 Pitfall: Background Recording

**Wrong**:
```kotlin
// ❌ DON'T: Record in broadcast receiver
class NotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        startRecording() // WRONG! Background recording
    }
}
```

**Right**:
```kotlin
// ✅ DO: Start foreground service from UI
Button(onClick = {
    startForegroundService(Intent(this, VoiceRecordingService::class.java))
})
```

### 10.2 Pitfall: Exact Alarms Without Permission

**Wrong**:
```kotlin
// ❌ DON'T: Use exact alarm without checking permission
alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
```

**Right**:
```kotlin
// ✅ DO: Check permission first
if (canScheduleExactAlarms()) {
    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
} else {
    // Fallback to WorkManager (flexible)
    scheduleFlexibleReminder(time)
}

private fun canScheduleExactAlarms(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SCHEDULE_EXACT_ALARM
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Always true for Android 12
    }
}
```

### 10.3 Pitfall: API Keys in Code

**Wrong**:
```kotlin
// ❌ DON'T: Hardcode API keys
const val OPENAI_API_KEY = "sk-..."
```

**Right**:
```kotlin
// ✅ DO: Use Edge Functions proxy
suspend fun extractIntent(transcript: String): Result<StructuredIntent> {
    return supabaseClient.functions.invoke("extract_intent", data)
}
```

### 10.4 Pitfall: No Permission Pre-Explanation

**Wrong**:
```kotlin
// ❌ DON'T: Request permission without explanation
permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
```

**Right**:
```kotlin
// ✅ DO: Show explanation first
if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
    showExplanationDialog(
        title = "Microphone Access",
        message = "VoiceTasker needs microphone to record your voice tasks"
    ) { proceed ->
        if (proceed) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}
```

### 10.5 Pitfall: Not Requesting Notification Permission

**Wrong**:
```kotlin
// ❌ DON'T: Assume notification permission granted (Android 13+)
notificationManager.notify(id, notification)
```

**Right**:
```kotlin
// ✅ DO: Check permission first
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED) {
        notificationManager.notify(id, notification)
    }
}
```

---

## SUMMARY

**Compliance = Architecture Constraint**

- Background execution limits → Use AlarmManager (exact) + WorkManager (flexible)
- Microphone rules → Show "Listening..." UI, allow stop, user-initiated only
- Google Play strictness → Transparency, no deception, accurate data safety form
- Privacy → Encrypt data, minimize collection, disclose third-parties
- AI usage → Disclose, implement fallbacks, moderate output

**Failure to comply → Rejection or store removal.**

Build compliance in from Day 1, not as an afterthought.
