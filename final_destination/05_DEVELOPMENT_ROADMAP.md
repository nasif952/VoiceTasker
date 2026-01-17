# DEVELOPMENT ROADMAP & PHASED DELIVERY PLAN

**VoiceTasker - 4-Phase Implementation Strategy**

---

## ROADMAP OVERVIEW

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 1: MVP                │ Phase 2: Multilingual          │
│ (8 weeks)                   │ (6 weeks)                      │
│ ✓ Auth                      │ ✓ Bengali, Hindi, Urdu         │
│ ✓ Voice→Task                │ ✓ Task hierarchy               │
│ ✓ Reminders (local)         │ ✓ Cloud sync                   │
│ ✓ Basic UI                  │ ✓ Task updates via voice       │
├─────────────────────────────────────────────────────────────┤
│ Phase 3: Monetization       │ Phase 4: Advanced              │
│ (6 weeks)                   │ (ongoing)                      │
│ ✓ Stripe integration        │ ✓ Offline LLM                  │
│ ✓ Pro tier features         │ ✓ Calendar integration         │
│ ✓ Usage tracking            │ ✓ Recurring tasks              │
│ ✓ Play Store submission     │ ✓ Collaboration (optional)     │
└─────────────────────────────────────────────────────────────┘

Total Duration: 20 weeks (MVP + full feature set)
Post-Launch: Ongoing improvements & Phase 4 advanced features
```

---

## PHASE 1: MINIMUM VIABLE PRODUCT (8 weeks)

**Goal**: Functional voice-first task app with local storage and basic reminders.

**Release Target**: Internal alpha testing

### 1.1 Week 1-2: Foundation & Project Setup

**Deliverables**:
- [ ] Android project scaffolding (Clean Architecture + MVVM)
- [ ] Dependency injection setup (Hilt)
- [ ] Database setup (Room with encryption)
- [ ] Secure token storage (Android Keystore + EncryptedSharedPreferences)
- [ ] Basic UI (Jetpack Compose)

**Key Tasks**:
1. Create app module structure:
   ```
   app/
   feature/
   ├─ auth/
   ├─ task/
   ├─ voice/
   ├─ reminder/
   core/
   ├─ database/
   ├─ network/
   ├─ security/
   ```
2. Configure build.gradle dependencies
3. Set up CI/CD (GitHub Actions)
4. Create database schema (Task, Reminder, Profile, Entitlements)

**Estimated Effort**: 2 developers, full-time
**Risk**: None (low-risk foundation work)

---

### 1.2 Week 3: Authentication

**Deliverables**:
- [ ] Google Sign-In integration (Android Credential Manager)
- [ ] Email + password fallback (Supabase Auth)
- [ ] Token persistence (secure storage)
- [ ] Session management (auto-refresh, logout)
- [ ] Account deletion UI (in-app)

**Key Tasks**:
1. Integrate Credential Manager
2. Configure Supabase Auth
3. Implement AuthService + AuthViewModel
4. Create login/signup screens
5. Test auth flow (happy path + edge cases)

**API Integrations Required**:
- Supabase Auth REST API
- Google OAuth (via Credential Manager)

**Testing**:
- Test Google Sign-In flow
- Test email login flow
- Test token refresh
- Test logout + cache clear

**Estimated Effort**: 1.5 developers, 1 week

---

### 1.3 Week 4-5: Voice Recording & STT

**Deliverables**:
- [ ] Microphone recording (AudioRecord API)
- [ ] Foreground service (visible indicator)
- [ ] Deepgram STT integration (cloud-based)
- [ ] Vosk STT fallback (offline)
- [ ] Transcription service

**Key Tasks**:
1. Implement VoiceRecordingForegroundService
2. Integrate Deepgram API (via Supabase Edge Function)
3. Integrate Vosk (on-device model)
4. Test STT accuracy on various accents
5. Implement fallback logic (Deepgram → Vosk)

**Audio Processing**:
- Sample rate: 16 kHz, mono, PCM 16-bit
- Record until silence (3-sec timeout)
- Audio buffering and streaming

**Permission Handling**:
- Request RECORD_AUDIO at runtime
- Show pre-permission explanation
- Handle denial gracefully

**Testing**:
- Record various voices (different accents, genders, ages)
- Test STT accuracy (target > 85%)
- Test offline fallback (Vosk accuracy)
- Test timeout behavior

**Estimated Effort**: 2 developers, 2 weeks

---

### 1.4 Week 6: LLM Integration (Intent Extraction)

**Deliverables**:
- [ ] OpenAI GPT function calling integration (via Supabase Edge Function)
- [ ] Intent extraction service
- [ ] Structured JSON output parsing
- [ ] Fallback rule-based extraction

**Key Tasks**:
1. Create Supabase Edge Function `extract_intent`
2. Implement LLMService with OpenAI client
3. Define function calling schema (extract_task)
4. Parse JSON response
5. Implement fallback (regex/rules)

**Prompt Engineering**:
- System prompt with task extraction rules
- Few-shot examples (5-10 examples)
- Few-shot format: User input → Expected JSON

**Edge Function Code**:
```typescript
import { serve } from "https://deno.land/std@0.208.0/http/server.ts";
import OpenAI from "https://esm.sh/openai@4";

const openai = new OpenAI({ apiKey: Deno.env.get("OPENAI_API_KEY") });

serve(async (req) => {
    const { transcript, timezone, language } = await req.json();

    const response = await openai.chat.completions.create({
        model: "gpt-4-turbo",
        functions: [{
            name: "extract_tasks",
            parameters: { /* schema */ }
        }],
        messages: [{ role: "user", content: transcript }]
    });

    return new Response(JSON.stringify(response));
});
```

**Testing**:
- Test intent extraction accuracy (target > 90%)
- Test with ambiguous inputs
- Test fallback extraction
- Test timeout behavior (max 5 sec)

**Estimated Effort**: 1.5 developers, 1 week

---

### 1.5 Week 7: Task CRUD & Local Storage

**Deliverables**:
- [ ] Create task (manual + voice)
- [ ] Read task list
- [ ] Update task
- [ ] Delete task
- [ ] Task hierarchy (Phase 1: simple, no nesting)
- [ ] Room DAO queries

**Key Tasks**:
1. Create TaskEntity and TaskDao
2. Implement TaskRepository (local-only)
3. Create CreateTaskUseCase, GetTasksUseCase, UpdateTaskUseCase, DeleteTaskUseCase
4. Create TaskListViewModel
5. Create task list UI (Compose)
6. Create task detail screen

**Database Queries**:
- Get all tasks for user
- Get task by ID
- Insert task
- Update task
- Delete task (soft-delete)

**UI Screens**:
- HomeScreen (task list + mic button)
- TaskDetailScreen (view/edit task)
- VoiceReviewScreen (review extracted tasks before saving)

**Testing**:
- Test CRUD operations
- Test offline persistence
- Test task list rendering (performance > 100ms for 100 tasks)
- Test voice review screen

**Estimated Effort**: 1.5 developers, 1 week

---

### 1.6 Week 8: Reminders & Alarms

**Deliverables**:
- [ ] Reminder scheduling (AlarmManager)
- [ ] Flexible reminders (WorkManager fallback)
- [ ] Reminder notification
- [ ] Snooze functionality
- [ ] Notification channels

**Key Tasks**:
1. Create ReminderEntity and ReminderDao
2. Implement ReminderRepository
3. Create ReminderScheduler (AlarmManager + WorkManager)
4. Create notification channels
5. Create BroadcastReceiver for alarm trigger
6. Create snooze logic

**Reminder Behavior**:
- If SCHEDULE_EXACT_ALARM permission: use AlarmManager (exact time)
- If not: use WorkManager (flexible, 15-30 min window)
- Show notification with task title + actions
- Actions: Mark done, Snooze, Dismiss

**Notification UI**:
- Task title
- Due time
- Action buttons
- Optional TTS (read reminder aloud)

**Testing**:
- Schedule exact alarm
- Schedule flexible reminder
- Test notification delivery (> 99% reliability)
- Test snooze (reschedule + notification)
- Test mark done (task completed, alarm canceled)

**Estimated Effort**: 1 developer, 1 week

---

### 1.7 Phase 1 Summary

**Deliverables**:
✅ User registration + auth
✅ Voice recording + STT
✅ LLM intent extraction
✅ Task CRUD (local)
✅ Reminder scheduling + notifications
✅ Basic UI (Compose)

**Metrics**:
- STT accuracy: > 85%
- LLM accuracy: > 85%
- Voice-to-confirmation latency: < 3 seconds
- App crash rate: < 1%
- Reminder delivery: > 99%

**Testing**:
- Unit tests (core services)
- Integration tests (voice pipeline)
- UI tests (compose screens)
- Manual QA (on 2-3 devices)

**Go/No-Go Decision**: Internal alpha testing decides if ready for Phase 2

---

## PHASE 2: MULTILINGUAL & HIERARCHY (6 weeks)

**Goal**: Multilingual support + task hierarchy + cloud sync

**Release Target**: Beta testing with multilingual users

### 2.1 Week 1: Cloud Sync Setup

**Deliverables**:
- [ ] Supabase Realtime subscription
- [ ] Sync service (upload + download)
- [ ] Conflict resolution (last-write-wins)
- [ ] Offline-first sync queue

**Key Tasks**:
1. Create TaskRemoteDataSource (Supabase client)
2. Update TaskRepository to support hybrid (local + remote)
3. Implement sync queue (retry logic with exponential backoff)
4. Implement versioning (for conflict detection)
5. Create SyncViewModel (show sync status)

**Sync Strategy**:
- Local Room is always source of truth
- Changes queued for remote sync
- Remote changes downloaded + merged locally
- Conflict resolution: client timestamp wins

**Testing**:
- Test sync on same device across app restart
- Test sync on multiple devices
- Test conflict resolution
- Test offline sync queue + retry

**Estimated Effort**: 1 developer, 1 week

---

### 2.2 Week 2: Task Hierarchy (Unlimited Nesting)

**Deliverables**:
- [ ] Parent-child relationships
- [ ] Recursive queries (descendants)
- [ ] Hierarchical UI (collapsible tree)
- [ ] Cascade delete (confirm dialog)

**Key Tasks**:
1. Update TaskEntity to include parentId
2. Create recursive DAO queries (CTEs)
3. Create TaskWithSubtasks relationship class
4. Update UI to show hierarchy
5. Test cascade delete behavior

**Database Schema**:
```sql
ALTER TABLE tasks ADD COLUMN parent_id UUID REFERENCES tasks(id);

-- Recursive query: get all descendants
WITH RECURSIVE task_tree AS (
    SELECT * FROM tasks WHERE parent_id IS NULL
    UNION ALL
    SELECT t.* FROM tasks t JOIN task_tree ON t.parent_id = task_tree.id
)
SELECT * FROM task_tree WHERE user_id = ?;
```

**UI Pattern**:
- Collapsible tree structure
- Indent for each level
- Show subtask count
- Progress bar for parent (% subtasks completed)

**Testing**:
- Test recursive queries (100+ nested tasks)
- Test UI performance (render < 100ms)
- Test cascade delete

**Estimated Effort**: 1.5 developers, 1 week

---

### 2.3 Week 3: Multilingual Support

**Deliverables**:
- [ ] Language detection (first 500ms of speech)
- [ ] Deepgram Nova-3 (English/Hindi)
- [ ] Google Chirp 3 (Bengali/Urdu)
- [ ] Prompt templating (per-language)
- [ ] RTL support (Urdu)

**Key Tasks**:
1. Implement language detection (from first few phonemes)
2. Create multilingual STT router (Deepgram vs Google)
3. Create language-specific prompts (OpenAI)
4. Add RTL support to UI (Compose modifiers)
5. Create language preference setting

**Language Support**:
- English → Deepgram Nova-3
- Hindi → Deepgram Nova-3
- Bengali → Google Chirp 3
- Urdu → Google Chirp 3 (with RTL UI)

**Prompt Engineering** (per-language):
- Bengali prompt: Include Bengali numerals, context
- Hindi prompt: Include Hindi script normalization
- Urdu prompt: Include RTL markers

**Testing**:
- Test language detection accuracy (first 500ms)
- Test STT accuracy per language (target > 85%)
- Test RTL rendering (Urdu)
- Test code-switching (mix of languages)

**Estimated Effort**: 2 developers, 1 week

---

### 2.4 Week 4: Task Updates via Voice

**Deliverables**:
- [ ] Voice update commands ("Change deadline...", "Update...")
- [ ] Task reference resolution ("that task", "the visa one")
- [ ] Confirmation before changes
- [ ] Update preview screen

**Key Tasks**:
1. Extend LLM schema for update commands
2. Implement task reference resolution (fuzzy matching)
3. Create UpdateTaskUseCase
4. Create confirmation screen

**Voice Patterns**:
- "Change deadline to Friday"
- "Move to tomorrow"
- "Mark as urgent"
- "Add note: ..."

**Update Types**:
- Title, description, due date, priority, status, parent task

**Testing**:
- Test reference resolution accuracy
- Test update accuracy (> 90%)
- Test confirmation flow

**Estimated Effort**: 1 developer, 1 week

---

### 2.5 Week 5-6: Testing & Refinement

**Deliverables**:
- [ ] Integration tests (sync + hierarchy + multilingual)
- [ ] UI tests (all screens)
- [ ] Manual QA on 5+ devices (different OS versions)
- [ ] Multilingual beta testing

**Key Tasks**:
1. Write integration tests (voice → task creation with sync)
2. Create beta testing program
3. Gather feedback from multilingual users
4. Fix critical bugs
5. Performance optimization

**Beta Testing**:
- Recruit 20-30 multilingual users
- 2-week testing period
- Daily standup with QA lead
- Bug prioritization

**Go/No-Go Decision**: Beta testing feedback determines readiness for Phase 3 (monetization)

---

## PHASE 3: MONETIZATION & PLAY STORE (6 weeks)

**Goal**: Prepare for public Play Store launch with payment processing

**Release Target**: Public beta on Play Store

### 3.1 Week 1: Stripe Integration

**Deliverables**:
- [ ] Stripe PaymentSheet integration
- [ ] Subscription creation (Pro tier)
- [ ] Webhook handling (server-side)
- [ ] Entitlements tracking (user tier)

**Key Tasks**:
1. Configure Stripe account
2. Integrate Stripe PaymentSheet (Android)
3. Create subscription product (Pro: $4.99/month)
4. Implement Entitlements table + logic
5. Create webhook handler (Supabase Edge Function)

**Payment Flow**:
- User taps "Upgrade to Pro"
- PaymentSheet shown
- User enters card details
- Stripe processes payment
- Webhook confirms → Entitlements updated
- App shows "Pro unlocked"

**Estimated Effort**: 1.5 developers, 1 week

---

### 3.2 Week 2: Feature Flags & Tier Limits

**Deliverables**:
- [ ] Feature flags (per-tier)
- [ ] Task limits (Free: 50, Pro: unlimited)
- [ ] Voice minute limits (Free: 10/month, Pro: unlimited)
- [ ] UI enforcement (show upgrade prompt)

**Key Tasks**:
1. Create FeatureFlagsService
2. Implement task limit checking
3. Implement voice minute tracking
4. Create upgrade UI prompts
5. Test limit enforcement

**Limits**:
- Free: 50 tasks, 10 voice min/month
- Pro: unlimited tasks, unlimited voice

**Testing**:
- Test task limit enforcement
- Test voice minute tracking
- Test upgrade prompt UX

**Estimated Effort**: 1 developer, 1 week

---

### 3.3 Week 3: Compliance & Data Safety

**Deliverables**:
- [ ] Privacy policy (website)
- [ ] Data Safety form (Play Store)
- [ ] Web account deletion page
- [ ] GDPR compliance documentation

**Key Tasks**:
1. Write comprehensive privacy policy
2. Create account deletion web page (standalone, no app needed)
3. Fill Data Safety form (accurate + complete)
4. Prepare GDPR deletion responses
5. Create compliance review document

**Privacy Policy Topics**:
- Data collection (voice, tasks, device ID)
- Third-party services (Supabase, Deepgram, OpenAI, Stripe)
- Data retention (transcripts: immediate delete, tasks: indefinite, account delete: 30 days soft, permanent after)
- User rights (delete account, export data, opt-out AI)

**Data Safety Form**:
- Audio data: Yes, encrypted, user-deletable
- Transcripts: Yes, encrypted, 30-day retention
- User ID: Yes, encrypted, user-deletable
- Tasks: Yes, encrypted, user-deletable
- Analytics: No
- Crash reporting: No

**Estimated Effort**: 1 developer + legal review, 1 week

---

### 3.4 Week 4: Play Store Submission

**Deliverables**:
- [ ] Release build (signed)
- [ ] Store listing (screenshots, description, icon)
- [ ] Content rating (IARC form)
- [ ] Submission to Play Store

**Key Tasks**:
1. Create signed APK/AAB
2. Prepare store listing (screenshots × 5, feature graphic, description)
3. Create app icon (512×512 PNG)
4. Fill content rating questionnaire (usually "Everyone")
5. Submit to Play Store

**Store Listing**:
- **Title**: "VoiceTasker - Voice Task Manager"
- **Description**: 4,000 char explaining voice-first, multilingual, privacy-focused
- **Short Description**: 80 char tagline
- **Category**: Productivity

**Screenshots** (5 recommended):
1. Home screen (task list + mic button)
2. Voice recording screen ("Listening...")
3. Task review screen (extracted tasks)
4. Task detail screen
5. Reminder notification

**Estimated Effort**: 1 developer + designer, 1 week

---

### 3.5 Week 5-6: Play Store Review & Launch

**Deliverables**:
- [ ] Address Play Store review feedback (if any)
- [ ] Public launch
- [ ] Monitoring (crash rate, user feedback)

**Key Tasks**:
1. Monitor Play Store review status
2. Respond to reviewer feedback (if any)
3. Launch publicly when approved
4. Monitor crash rate (target < 0.5%)
5. Monitor user ratings (target > 4.0)

**Post-Launch Monitoring**:
- Crash analytics (Firebase Crashlytics)
- User feedback (Play Store reviews)
- ANR (Application Not Responding) rates
- Memory usage

**Go/No-Go Decision**: Review feedback determines if launch proceeds

---

## PHASE 4: ADVANCED FEATURES (Ongoing)

**Goal**: Enhance app with advanced AI, integrations, collaboration

**Timeline**: 3-6 months after Phase 3 launch

### 4.1 Offline LLM Fallback

**Motivation**: Enable voice when internet unavailable

**Options**:
- Llama 2 (7B or 13B param)
- Mistral 7B
- Phi 2 (smaller, faster)

**Implementation**:
- Download model (200-500 MB) in-app
- Store on device
- Use for fallback when Deepgram/OpenAI unavailable

**Trade-off**: Lower accuracy, higher latency (on-device), but fully offline

---

### 4.2 Calendar Integration

**Goal**: Show tasks in calendar view

**Implementation**:
- Query calendar events (Android Calendar API)
- Show tasks alongside events
- Create task from calendar event

---

### 4.3 Recurring Tasks

**Goal**: Support daily/weekly/monthly repetition

**Implementation**:
- Add recurrence rule to Task model
- Auto-create next occurrence when completed
- Show recurrence pattern in UI

---

### 4.4 Collaboration (Optional)

**Goal**: Share tasks with others

**Implementation**:
- Add shared_with field to tasks
- Implement RLS policies for shared access
- Create share sheet UI

---

## RESOURCE PLANNING

### Team Composition

**Phase 1-3** (20 weeks):
- 2-3 Android developers (full-time)
- 1 backend engineer (part-time, Supabase + Edge Functions)
- 1 QA engineer (part-time → full-time at Phase 2)
- 1 product manager (oversight)
- 1 designer (UI/UX, play store assets)

**Phase 4** (ongoing):
- 1-2 Android developers (feature development)
- 1 backend engineer (maintenance)
- 1 QA engineer (testing)

### Budget Estimation

**Phase 1-3** (20 weeks, ~$500K-700K):
- Developer salaries: $400K
- Infrastructure (Supabase, Deepgram, OpenAI, Stripe): $50K
- QA/Testing: $30K
- Design/PM: $40K
- Contingency (10%): $50K

**Phase 4** (ongoing, ~$30K/month):
- Development: $20K
- Infrastructure: $5K
- Maintenance/Support: $5K

---

## RISK MITIGATION & DECISION GATES

### Gate 1: End of Phase 1 (Week 8)

**Decision**: Is MVP feature-complete and stable?

**Criteria**:
- [ ] STT accuracy > 85%
- [ ] LLM accuracy > 85%
- [ ] Voice-to-confirmation < 3 sec
- [ ] Crash rate < 1%
- [ ] Reminder delivery > 99%

**If YES**: Proceed to Phase 2 beta
**If NO**: Extend Phase 1 by 2 weeks

---

### Gate 2: End of Phase 2 (Week 14)

**Decision**: Is multilingual + sync ready for monetization?

**Criteria**:
- [ ] Beta test: 30 users, 2 weeks, 95%+ approval
- [ ] Multilingual STT accuracy > 85% per language
- [ ] Sync conflict resolution < 1 conflict per 1000 ops
- [ ] UI performance > 100ms render
- [ ] Crash rate < 0.5%

**If YES**: Proceed to Phase 3 monetization
**If NO**: Extend Phase 2 by 2 weeks

---

### Gate 3: End of Phase 3 (Week 20)

**Decision**: Is app ready for Play Store launch?

**Criteria**:
- [ ] Play Store review: approved
- [ ] Crash rate < 0.5%
- [ ] Compliance checklist: 100% complete
- [ ] Privacy policy + Data Safety form: reviewed
- [ ] Store listing: screenshots + description + icon complete

**If YES**: Launch to public Play Store
**If NO**: Address reviewer feedback + resubmit

---

## SUCCESS METRICS

### Usage Metrics

- Daily active users (DAU)
- Monthly active users (MAU)
- Task creation rate (tasks/user/day)
- Voice usage (% of tasks created via voice)
- Reminder delivery success (% reminders received)

### Quality Metrics

- Crash rate (target < 0.5%)
- ANR rate (target < 0.1%)
- User rating (target > 4.0 stars)
- Review sentiment (target > 80% positive)

### Business Metrics

- Sign-ups (daily, cumulative)
- Pro subscription adoption rate
- Subscription churn rate (target < 5%/month)
- Revenue (MRR = Monthly Recurring Revenue)

---

## SUMMARY

This roadmap provides:
- ✅ Clear 4-phase delivery plan (20 weeks to launch)
- ✅ Weekly milestones and deliverables
- ✅ Resource and budget estimates
- ✅ Risk mitigation gates
- ✅ Success metrics

**Ready to execute.**
