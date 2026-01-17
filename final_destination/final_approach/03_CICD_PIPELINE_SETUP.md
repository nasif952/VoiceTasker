# CI/CD PIPELINE SETUP GUIDE

**VoiceTasker - Automated Build, Test, Deploy**

---

## PIPELINE OVERVIEW

```
Developer pushes code
    ↓
Git webhook triggers
    ↓
GitHub Actions runs:
    1. Lint & Format Check
    2. Unit Tests
    3. Integration Tests
    4. Security Scan
    5. Build APK/AAB
    6. Deploy (if main/release branch)
    ↓
Email notification sent
```

**Total Pipeline Duration**: ~15 minutes (unit tests + build)

---

## GITHUB ACTIONS SETUP

### Step 1: Create Workflow Directory

```bash
mkdir -p .github/workflows
```

### Step 2: Main CI Workflow

**File: `.github/workflows/ci.yml`**

```yaml
name: Continuous Integration

on:
  push:
    branches: [ main, develop, 'feature/**', 'bugfix/**' ]
  pull_request:
    branches: [ main, develop ]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

env:
  GRADLE_OPTS: -Xmx4096m -Dorg.gradle.daemon=false
  ANDROID_COMPILE_SDK: 34
  ANDROID_BUILD_TOOLS: 34.0.0
  ANDROID_MIN_SDK: 31

jobs:
  lint:
    name: Lint & Format Check
    runs-on: ubuntu-latest
    timeout-minutes: 10

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}
          restore-keys: ${{ runner.os }}-gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run lint
        run: ./gradlew lint

      - name: Upload lint results
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: lint-results
          path: app/build/reports/lint-results*.xml

  test:
    name: Unit Tests
    runs-on: ubuntu-latest
    timeout-minutes: 15
    needs: lint

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}
          restore-keys: ${{ runner.os }}-gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run unit tests
        run: ./gradlew test --no-daemon

      - name: Generate coverage report
        run: ./gradlew testDebugUnitTestCoverage jacocoTestReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
          flags: unittests
          fail_ci_if_error: false

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: unit-test-results
          path: app/build/reports/tests/

  security:
    name: Security Scan
    runs-on: ubuntu-latest
    timeout-minutes: 15

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run Detekt
        run: ./gradlew detekt || true

      - name: Check for secrets
        uses: trufflesecurity/trufflehog@main
        with:
          path: ./
          base: ${{ github.event.repository.default_branch }}
          head: HEAD
          extra_args: --only-verified --no-update

      - name: Upload Detekt report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: detekt-report
          path: app/build/reports/detekt/

  build:
    name: Build APK & AAB
    runs-on: ubuntu-latest
    timeout-minutes: 20
    needs: [ lint, test, security ]

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}
          restore-keys: ${{ runner.os }}-gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Build release AAB
        run: ./gradlew bundleRelease

      - name: Upload debug APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug-${{ github.run_number }}.apk
          path: app/build/outputs/apk/debug/app-debug.apk

      - name: Upload release AAB
        uses: actions/upload-artifact@v3
        with:
          name: app-release-${{ github.run_number }}.aab
          path: app/build/outputs/bundle/release/app-release.aab

  deploy-test:
    name: Deploy to Firebase (Internal Test)
    runs-on: ubuntu-latest
    timeout-minutes: 10
    needs: build
    if: github.ref == 'refs/heads/develop' && github.event_name == 'push'

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Download Firebase CLI
        run: npm install -g firebase-tools

      - name: Deploy to Firebase App Distribution
        env:
          FIREBASE_TOKEN: ${{ secrets.FIREBASE_TOKEN }}
        run: |
          firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk \
            --project voicetasker \
            --groups internal_testers \
            --release-notes "Build #${{ github.run_number }} from develop"

  notify:
    name: Notify Results
    runs-on: ubuntu-latest
    if: always()
    needs: [ lint, test, security, build, deploy-test ]

    steps:
      - name: Send Slack notification
        uses: 8398a7/action-slack@v3
        if: always()
        with:
          status: ${{ job.status }}
          text: |
            CI Pipeline: ${{ job.status }}
            Commit: ${{ github.event.head_commit.message }}
            Author: ${{ github.event.head_commit.author.name }}
            Branch: ${{ github.ref_name }}
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}

      - name: Create GitHub deployment
        if: success() && github.ref == 'refs/heads/main'
        uses: actions/github-script@v6
        with:
          script: |
            github.rest.repos.createDeployment({
              owner: context.repo.owner,
              repo: context.repo.repo,
              ref: context.ref,
              environment: 'production',
              production_environment: true,
              auto_merge: false,
              required_contexts: []
            })
```

### Step 3: Pull Request Workflow

**File: `.github/workflows/pr-checks.yml`**

```yaml
name: Pull Request Checks

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  pr-title:
    name: Check PR Title Format
    runs-on: ubuntu-latest
    steps:
      - name: Validate PR title
        uses: deepakputra/action-pr-title@v1.0.2
        with:
          regex: '^(feat|fix|docs|style|refactor|test|chore)(\(.+\))?:.*'
          allowed_prefixes: 'feat,fix,docs,style,refactor,test,chore'
          disallowed_prefixes: 'wip,work in progress'

  size:
    name: Check PR Size
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Check PR size
        uses: actions/github-script@v6
        with:
          script: |
            const { data } = await github.rest.pulls.get({
              owner: context.repo.owner,
              repo: context.repo.repo,
              pull_number: context.issue.number
            });

            const additions = data.additions;
            const deletions = data.deletions;
            const changes = additions + deletions;

            if (changes > 500) {
              core.setFailed(`PR too large: ${changes} changes. Target: < 500 changes.`);
            } else {
              console.log(`PR size OK: ${changes} changes`);
            }
```

### Step 4: Release Workflow

**File: `.github/workflows/release.yml`**

```yaml
name: Release to Play Store

on:
  push:
    tags:
      - 'v*'  # v1.0.0, v0.2.0, etc.

jobs:
  release:
    name: Build & Release
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build release bundle
        run: ./gradlew bundleRelease

      - name: Sign AAB
        uses: r0adkll/sign-android-release@v1
        with:
          releaseDirectory: app/build/outputs/bundle/release/
          signingKeyBase64: ${{ secrets.ANDROID_SIGNING_KEY }}
          alias: ${{ secrets.ANDROID_KEY_ALIAS }}
          keyStorePassword: ${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
          keyPassword: ${{ secrets.ANDROID_KEY_PASSWORD }}

      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.GOOGLE_PLAY_SERVICE_ACCOUNT_JSON }}
          packageName: com.voicetasker
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal
          inAppUpdatePriority: 5
          status: completed
          mappingFile: app/build/outputs/mapping/release/mapping.txt

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v1
        with:
          draft: false
          prerelease: false
          body: "See [CHANGELOG](./CHANGELOG.md) for changes"
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## SECRETS CONFIGURATION

### Add GitHub Secrets

**Settings → Secrets and variables → Actions**

```
ANDROID_SIGNING_KEY           (base64 of keystore file)
ANDROID_KEY_ALIAS             (alias used in signing)
ANDROID_KEYSTORE_PASSWORD     (keystore password)
ANDROID_KEY_PASSWORD          (key password)
GOOGLE_PLAY_SERVICE_ACCOUNT_JSON (JSON for Play Store API)
FIREBASE_TOKEN                (Firebase authentication token)
SLACK_WEBHOOK                 (Slack integration webhook)
```

### Create Signing Key

```bash
# Generate keystore (one-time)
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias release_key \
  -storepass $KEYSTORE_PASSWORD \
  -keypass $KEY_PASSWORD

# Convert to base64
base64 -i release.keystore | tr -d '\n' > release.keystore.b64

# Copy content and add as ANDROID_SIGNING_KEY secret
cat release.keystore.b64
```

---

## LOCAL PRE-COMMIT HOOKS

### Setup Husky

```bash
# Install husky
npm install husky --save-dev
npx husky install

# Create pre-commit hook
cat > .husky/pre-commit <<EOF
#!/bin/sh
. "\$(dirname "\$0")/_/husky.sh"

echo "Running pre-commit checks..."

# Format code
echo "Formatting code..."
./gradlew spotlessApply

# Lint
echo "Running lint..."
./gradlew lintDebug --continue

# Unit tests
echo "Running unit tests..."
./gradlew testDebugUnitTest

# Static analysis
echo "Running detekt..."
./gradlew detekt || true

echo "✓ Pre-commit checks passed"
EOF

chmod +x .husky/pre-commit
```

### Make Executable

```bash
chmod +x .husky/pre-commit
git add .husky/pre-commit
git commit -m "chore: add pre-commit hooks"
```

---

## MONITORING DASHBOARD

### View CI/CD Status

**GitHub**: Actions tab
```
https://github.com/yourorg/voicetasker/actions
```

**Key Metrics to Track**:
- Build success rate (target: 100%)
- Average pipeline duration (target: < 15 min)
- Test pass rate (target: 100%)
- Code coverage (target: > 80%)
- Deployment success (target: 100%)

### Slack Integration

**Set up notification channel**:
```bash
1. Create Slack webhook
2. Add SLACK_WEBHOOK secret to GitHub
3. Notifications auto-send on build status
```

---

## TROUBLESHOOTING

### Build Failure: Gradle Timeout

```bash
# Increase timeout in build.gradle.kts
gradle {
    org.gradle.jvmargs=-Xmx4096m -Dorg.gradle.daemon=false
}

# Or increase in CI job timeout-minutes
```

### Test Failure: Flaky Tests

```bash
# Add retry logic in CI
- name: Run tests with retry
  run: |
    for i in {1..3}; do
      ./gradlew test && break || echo "Retry $i"
    done
```

### Secrets Not Found

```bash
# Verify secrets in Settings
# Ensure secret names match exactly in workflow file
# Check that token hasn't expired
```

---

## SUCCESS CRITERIA

- [ ] GitHub Actions workflow running successfully
- [ ] All checks (lint, test, security, build) passing
- [ ] Code coverage > 80%
- [ ] Pipeline duration < 15 minutes
- [ ] Artifacts uploaded successfully
- [ ] Slack notifications received
- [ ] Pre-commit hooks working locally
- [ ] Release workflow tested

---

**Status**: Ready for Phase 1 development
