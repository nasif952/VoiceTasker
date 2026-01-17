# DEPLOYMENT PROCEDURES GUIDE

**VoiceTasker - Staging, Testing, Production Release**

---

## DEPLOYMENT STRATEGY

```
Phase 1-2 (MVP Development)
├── Internal Testing (team + close contacts)
└── 1-2 weeks

Phase 3 (Pre-Launch)
├── Firebase App Distribution (100-500 beta testers)
├── Google Play Beta (if submitted)
└── 2-3 weeks

Phase 4 (Production)
├── Play Store: Internal Testing track (staff + testers)
├── Play Store: Closed Beta track (100-500 users)
├── Play Store: Production track (phased rollout)
│   ├── Day 1: 10%
│   ├── Day 3: 50%
│   └── Day 7: 100%
└── Continuous monitoring
```

---

## INTERNAL TESTING (Phase 1-2)

### Prerequisites

- [ ] Project compiled successfully
- [ ] All tests passing
- [ ] No critical bugs
- [ ] Baseline performance acceptable

### Build for Internal Testing

```bash
# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# Share with team:
# - Email APK to testers
# - Or upload to file sharing service
# - Or use Firebase App Distribution
```

### Collect Feedback

**Tester Feedback Form** (Google Form):
```
1. Did the app crash? (Yes/No)
   - If yes, describe what you were doing
2. Was voice recording clear?
3. Did STT transcription work?
4. Rate performance (1-5)
5. Any bugs or issues?
6. General feedback
```

### Bug Triage

**Priority Levels**:
- **Critical**: App crashes, core feature broken (fix immediately)
- **High**: Feature doesn't work but app doesn't crash (fix this week)
- **Medium**: UI issue, performance issue (fix next week)
- **Low**: Polish, minor UI tweaks (backlog)

**Create GitHub Issues** for each bug:
```markdown
## Issue: App crashes on voice recording

**Severity**: Critical

**Steps to reproduce**:
1. Tap mic button
2. Record 5 seconds
3. App crashes

**Expected**: Transcript shown
**Actual**: Crash dialog

**Device**: Pixel 6, Android 14

**Logs**:
```
E/AndroidRuntime: java.lang.NullPointerException...
```

**Assignee**: @backend-lead
```

---

## FIREBASE APP DISTRIBUTION (Phase 3 Beta)

### Prerequisites

- [ ] Firebase project created
- [ ] Firebase App Distribution enabled
- [ ] Testers added to distribution list
- [ ] Build APK ready

### Upload APK to Firebase

**Manual Upload**:

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Deploy APK
firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk \
  --project voicetasker \
  --groups beta_testers \
  --release-notes "Version 0.2.0 - Beta Release

  - Multilingual support (Bengali, Hindi, Urdu)
  - Task hierarchy
  - Cloud sync

  Known issues:
  - Offline LLM not implemented yet"
```

**Automated (via CI/CD)**:

See `.github/workflows/ci.yml` `deploy-test` job

### Tester Invitation

**Firebase automatically sends email** to testers with:
- Download link
- Install instructions
- Release notes

**Testers receive notifications** when new builds available

### Gather Feedback

**In-App Feedback** (optional):
```
User → Settings → Send Feedback
└── Opens pre-filled email with device info
```

**External Tracking**:
- Google Form (structured)
- GitHub Discussions (ongoing conversations)
- Slack channel (#beta-testing)

---

## GOOGLE PLAY STORE SUBMISSION

### Phase 1: Internal Testing Track

**Prerequisites**:
- [ ] Google Play Developer account ($25)
- [ ] Release APK/AAB signed
- [ ] Privacy policy published
- [ ] Data Safety form filled
- [ ] Content rating completed

### Phase 2: Upload to Play Store

**Via Google Play Console**:

1. **Go to**: https://play.google.com/console
2. **Select app**: VoiceTasker
3. **Navigate to**: Release → Internal testing
4. **Click**: Create new release
5. **Upload AAB**: app/build/outputs/bundle/release/app-release.aab

### Phase 3: Fill Release Details

**Release notes** (what changed):
```
Version 0.2.0 - Multilingual & Cloud Sync

New:
- Bengali, Hindi, Urdu support
- Unlimited task hierarchy
- Cloud sync across devices
- LLM-powered task extraction

Improved:
- Performance optimizations
- UI/UX refinements
- Crash fixes

Known issues:
- Offline LLM not available (Phase 4)
```

**Privacy policy**: Link to your privacy policy
**Contact email**: privacy@voicetasker.com

### Phase 4: Review & Testing

**Google's review process** (24-48 hours):

1. Automated checks (APK size, permissions, etc.)
2. Human review (content, compliance)
3. Approved or Rejected

**If rejected**:
- Read feedback carefully
- Fix issues
- Resubmit

**If approved**:
- App ready for beta/production rollout

---

## BETA TESTING ON PLAY STORE

### Open Closed Beta

**Steps**:

1. **Create Beta Release**
   - Play Console → Release → Closed beta
   - Upload same AAB as internal testing
   - Add release notes

2. **Add Testers**
   - Play Console → Testers → Closed beta
   - Add Google Group or individuals
   - Send invitation email

3. **Monitor Feedback**
   - Play Console → Release → Reviews
   - Filter by "Beta" rating
   - Respond to reviews

4. **Collect Crash Data**
   - Firebase Crashlytics → Crashes
   - Fix critical issues
   - Deploy hotfix if needed

---

## PRODUCTION ROLLOUT

### Pre-Release Checklist

- [ ] All critical bugs fixed
- [ ] Crash rate < 0.5%
- [ ] Performance acceptable (latency < 3 sec)
- [ ] Accessibility tested (WCAG 2.2 AA)
- [ ] Privacy policy up-to-date
- [ ] Data Safety form accurate
- [ ] Beta testing: 50+ users, 1+ week feedback
- [ ] Release notes prepared
- [ ] Monitoring dashboards ready

### Step 1: Create Production Release

**Play Console**:

1. Navigate to: Release → Production
2. Click: Create new release
3. Upload AAB: app/build/outputs/bundle/release/app-release.aab

### Step 2: Add Release Notes

```
VoiceTasker 1.0.0 - Official Launch

Welcome to VoiceTasker!

Features:
- Voice-first task management
- Multilingual support (Bengali, Hindi, Urdu, English)
- Cloud sync across devices
- AI-powered task extraction
- Reliable reminders & alarms
- Offline-first capability

What's New in 1.0.0:
- Production-ready release
- Supabase backend integration
- Google Sign-In authentication
- Full task hierarchy support
- Cloud synchronization

Privacy:
Your data is yours. End-to-end encrypted sync.
See our privacy policy: https://voicetasker.com/privacy

Support:
Email: support@voicetasker.com
Community: https://github.com/yourorg/voicetasker
```

### Step 3: Configure Phased Rollout

**Play Console → Release → Production**:

```
Rollout Strategy: Phased

Day 1: 10% of users
├── Monitor crash rate
├── Monitor star rating
└── Check reviews for critical issues

Day 3: 50% (if Day 1 stable)
├── Verify crash rate still < 0.5%
├── Check performance metrics
└── Monitor user feedback

Day 7: 100% (if Day 3 stable)
└── Full production release
```

**Or**: Maximize immediately (fast rollout, higher risk)

### Step 4: Submit for Review

**Play Console → Release → Production → Review**:

1. Click: Review and roll out
2. Agree to compliance statements
3. Click: Roll out to production

**Expected time**: 24-48 hours for approval

### Step 5: Monitor Rollout

**Real-Time Dashboard**:

```
Play Console → Release → Production
├── Rollout percentage
├── Install errors
├── Uninstall errors
└── Crash rate (real-time)

Firebase Console:
├── Crashlytics (crash rate trend)
├── Analytics (user counts)
└── Performance (latency trends)
```

**Alert Thresholds**:
| Metric | Critical Threshold |
|--------|-------------------|
| Crash rate | > 1% |
| Install errors | > 5% |
| Star rating | < 3.0 |
| Uninstall rate | > 50% |
| Latency (p95) | > 5 sec |

---

## HOTFIX DEPLOYMENT

### If Critical Bug Found in Production

**Step 1: Triage**
```
1. Confirm reproducibility
2. Assess impact (% users affected)
3. Decide: Hotfix or wait for next release
```

**Step 2: Create Hotfix Branch**
```bash
git checkout -b hotfix/crash-on-login
# Fix bug
# Commit
git push origin hotfix/crash-on-login
```

**Step 3: Expedited Testing**
```
- Unit tests
- Manual QA (critical path only)
- Skip beta, go direct to production
```

**Step 4: Build & Deploy**
```bash
# Build release
./gradlew bundleRelease

# Create release via Play Console (same process)
# But with expedited review (mention it's a hotfix)
```

**Step 5: Phased Rollout (Aggressive)**
```
Day 0: 10% immediately
Day 1: 50% (if stable)
Day 2: 100% (full rollout)
```

---

## ROLLBACK PROCEDURES

### If Unacceptable Issue Discovered

**Step 1: Pause Rollout**
```
Play Console → Release → Production → Pause rollout
```

**Step 2: Assess Options**

```
A) Rollback to Previous Version
   - Play Console → Production Release (previous)
   - Rollback percentage (pause new version)

B) Hotfix Current Version
   - Fix issue
   - Deploy new version
   - Resume rollout at 10%
```

**Step 3: Communicate**

```
- GitHub Issue: Post-mortem
- Slack: #incidents channel
- Play Store: Respond to reviews
```

---

## VERSION MANAGEMENT

### Semantic Versioning

**Format**: MAJOR.MINOR.PATCH

- **MAJOR** (1.0.0): Major feature release or breaking changes
- **MINOR** (0.2.0): New features, backward compatible
- **PATCH** (0.1.1): Bug fixes only

**Examples**:
- v0.1.0: MVP release
- v0.2.0: Multilingual + cloud sync
- v1.0.0: Official launch
- v1.1.0: New AI features
- v1.1.1: Crash fix

### Update Checklist

**Before each release**:

1. **Update version**:
   ```kotlin
   // app/build.gradle.kts
   versionCode = 2  // Increment each release
   versionName = "0.2.0"
   ```

2. **Update CHANGELOG**:
   ```markdown
   ## [0.2.0] - 2026-02-15

   ### Added
   - Multilingual support (Bengali, Hindi, Urdu)
   - Task hierarchy (unlimited nesting)
   - Cloud sync with Supabase

   ### Fixed
   - STT timeout handling
   - Memory leak in task list

   ### Changed
   - Improved reminder accuracy
   ```

3. **Create Git Tag**:
   ```bash
   git tag -a v0.2.0 -m "Release version 0.2.0"
   git push origin v0.2.0
   ```

4. **Create GitHub Release**:
   ```
   GitHub → Releases → Create new release
   - Tag: v0.2.0
   - Title: VoiceTasker 0.2.0
   - Description: (from CHANGELOG)
   ```

---

## POST-RELEASE MONITORING

### First 48 Hours (Critical Monitoring)

**Dashboard**: Firebase Console + Play Console

```
Every 4 hours:
├── Crash rate (target: < 0.5%)
├── ANR rate (target: < 0.1%)
├── Install errors (target: < 5%)
├── Play Store rating (target: > 4.0)
└── User feedback (reviews)

If ANY metric exceeds threshold:
  └─ Pause rollout + investigate
```

### First Week

**Daily Metrics Review**:
- Crash trend (improving or worsening?)
- User retention (week 1 DAU)
- Feature adoption (usage metrics)
- Top issues (user feedback)

**Action Items**:
- If crash rate increasing: Deploy hotfix
- If feature unused: Check UI clarity
- If performance issues: Optimize

### Ongoing (Post-Release)

**Weekly Review**:
- Play Store rating trend
- User reviews (positive/negative ratio)
- Crash rate (should be stable)
- Feature adoption (expected vs actual)

---

## SUCCESS CRITERIA

**Phase Completion**:
- [ ] Successful Play Store submission (no rejections)
- [ ] Crash rate < 0.5% (production)
- [ ] Play Store rating ≥ 4.0 stars
- [ ] 100% rollout achieved without issues
- [ ] User retention > 70% (week 2)
- [ ] Positive feedback from testers

**Metrics Dashboard**:
```
Play Store Console:
├── Active installs: 1,000+
├── Rating: 4.5+ stars
├── Reviews: Mostly positive
└── Crashes: < 0.5%

Firebase Console:
├── Daily active users: 200+
├── STT success rate: > 90%
├── LLM success rate: > 90%
└── Avg session duration: > 5 min
```

---

**Status**: Ready for Phase 3 deployment
