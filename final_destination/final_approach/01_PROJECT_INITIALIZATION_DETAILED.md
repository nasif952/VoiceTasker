# PROJECT INITIALIZATION - DETAILED STEP-BY-STEP

**VoiceTasker - Week 0 Setup Guide**

**Duration**: 5 days
**Team**: 2 developers + 1 backend engineer
**Output**: Production-ready Android project scaffold

---

## DAY 1: ENVIRONMENT & PREREQUISITES

### Step 1.1: Developer Machine Setup (2 hours)

**Install required software**:

```bash
# macOS (using Homebrew)
brew install openjdk@17
brew install gradle
brew install git

# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-17-jdk gradle git

# Windows (using Chocolatey)
choco install openjdk17 gradle git
```

**Verify installations**:
```bash
java -version
# Expected: openjdk version "17.x.x"

gradle -v
# Expected: Gradle 8.x

git --version
# Expected: git version 2.x.x
```

**Android Studio**:
- Download Hedgehog (2023.1.1) or latest stable
- Install on Mac: `/Applications/Android\ Studio.app`
- Install on Linux: `~/android-studio`
- Install on Windows: `C:\Program Files\Android Studio`

**Android SDK Setup**:
```bash
# Open Android Studio → Settings → SDK Manager
# Install:
# - Android SDK Platform 34 (latest)
# - Android SDK Platform 31 (minimum supported)
# - Android Emulator
# - Android SDK Build-Tools (latest)
# - Android Emulator (ARM or x86_64)

# Verify SDK path
echo $ANDROID_SDK_ROOT  # Should be set

# Add to ~/.bash_profile or ~/.bashrc
export ANDROID_SDK_ROOT=~/Library/Android/sdk  # macOS
export ANDROID_SDK_ROOT=~/Android/Sdk  # Linux
export PATH=$PATH:$ANDROID_SDK_ROOT/tools:$ANDROID_SDK_ROOT/platform-tools
```

**Emulator Setup**:
```bash
# Create emulator image
$ANDROID_SDK_ROOT/emulator/emulator -avd Pixel_6_API_34 -list-avds

# If not exists, create via Android Studio:
# Tools → Device Manager → Create Device
# Select: Pixel 6
# API level: 34
# Start emulator
$ANDROID_SDK_ROOT/emulator/emulator -avd Pixel_6_API_34 &
```

### Step 1.2: Git Configuration (30 min)

```bash
# Global config
git config --global user.name "Your Name"
git config --global user.email "your.email@company.com"
git config --global core.editor "vim"

# Default branch naming
git config --global init.defaultBranch main

# Useful aliases
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.st status
git config --global alias.unstage 'reset HEAD --'

# Verify config
git config --list
```

**GitHub SSH Setup**:
```bash
# Generate SSH key
ssh-keygen -t ed25519 -C "your.email@company.com"
# Press Enter (default location)
# Enter passphrase (optional but recommended)

# Copy public key
cat ~/.ssh/id_ed25519.pub

# Add to GitHub:
# GitHub → Settings → SSH and GPG keys → New SSH key
# Paste public key

# Test connection
ssh -T git@github.com
# Expected: "Hi username! You've successfully authenticated..."
```

### Step 1.3: IDE Configuration (1 hour)

**Android Studio Preferences**:

```
File → Settings:
  ├── Editor
  │   ├── Code Style → Kotlin
  │   │   └── Set from: Predefined style → Kotlin style guide
  │   └── General
  │       └── Enable: Line numbers, Code folding
  ├── Plugins
  │   ├── Install: Detekt (static analysis)
  │   ├── Install: Ktlint (code formatting)
  │   └── Install: Spotless (code formatter)
  ├── Build, Execution, Deployment
  │   ├── Build Tools → Gradle
  │   │   └── Gradle JDK: jdk 17
  │   └── Android Studio
  │       └── Max Heap Size: 2048 MB
  └── Version Control → Git
      └── SSH executable: Native
```

**Key Bindings** (Optional but recommended):
```
Android Studio → Settings → Keymap
  └── Search for common actions and bind:
      - Run: Ctrl+R
      - Debug: Ctrl+D
      - Format Code: Ctrl+Shift+L
      - Build: Ctrl+B
```

---

## DAY 2: GITHUB & PROJECT CREATION

### Step 2.1: Create GitHub Repository (30 min)

**On GitHub.com**:

1. Click "New repository"
2. **Repository name**: `voicetasker`
3. **Description**: Voice-first AI task management for Android
4. **Visibility**: Private (or Public after launch)
5. **Initialize with**:
   - [ ] Add a README file
   - [ ] Add .gitignore (select: Android)
   - [ ] Add a license (select: Apache 2.0)
6. Click "Create repository"

**Clone locally**:
```bash
cd ~/AndroidStudioProjects
git clone git@github.com:yourorg/voicetasker.git
cd voicetasker
```

### Step 2.2: Create Android Project

**Via Android Studio**:

1. **File** → **New** → **New Android Project**
2. **Phone and Tablet**
3. **Empty Activity**

**Configuration**:
```
Name: VoiceTasker
Package name: com.voicetasker
Save location: (your project directory)
Language: Kotlin
Minimum SDK: API 31
Build configuration language: Kotlin DSL
```

**Project Structure** (Android Studio generates):
```
VoiceTasker/
├── app/
├── gradle/
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew
```

### Step 2.3: Delete Default Files & Customize

```bash
cd VoiceTasker

# Remove auto-generated files we don't need
rm -rf app/src/main/java/com/voicetasker/*.java

# Rename MainActivity.kt to use package structure
mkdir -p app/src/main/java/com/voicetasker
mv app/src/main/java/com/voicetasker/MainActivity.kt \
   app/src/main/java/com/voicetasker/MainActivity.kt

# Update AndroidManifest.xml
cat > app/src/main/AndroidManifest.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.voicetasker">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.VoiceTasker"
        android:name=".VoiceTaskerApp"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.VoiceTasker">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
EOF
```

### Step 2.4: Initialize Git in Project

```bash
# Move to project root
cd VoiceTasker

# Initialize git (if not already done)
git init
git add .

# Initial commit
git commit -m "chore: initial Android project scaffold

- Android Studio Hedgehog project
- Kotlin + Jetpack Compose setup
- Gradle Kotlin DSL
- Target API 34, Min API 31"

# Add remote
git remote add origin git@github.com:yourorg/voicetasker.git

# Push to GitHub
git branch -M main
git push -u origin main
```

---

## DAY 3: BUILD CONFIGURATION & DEPENDENCIES

### Step 3.1: Update build.gradle.kts (Root)

```kotlin
// build.gradle.kts (Project)
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.0" apply false
    id("com.google.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.11" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```

### Step 3.2: Update app/build.gradle.kts

```kotlin
// app/build.gradle.kts
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.voicetasker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.voicetasker"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.9.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")

    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
}

kapt {
    correctErrorTypes = true
}
```

### Step 3.3: Create gradle.properties

```properties
# Kotlin
kotlin.jvm.target=17

# Android Gradle Plugin
android.useNewApkCreator=true
android.enableJetifier=false
android.nonTransitiveRClass=true

# Gradle Performance
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true

# Compose
compose.metrics=false
compose.reports=false
```

### Step 3.4: Sync Gradle

```bash
# In project root
./gradlew sync

# Or via Android Studio:
# File → Sync Now (or Ctrl+Shift+Y)
```

**Expected Output**:
```
BUILD SUCCESSFUL in Xs
```

---

## DAY 4: DATABASE & ARCHITECTURE SETUP

### Step 4.1: Create Core Modules

```bash
# Create module directories
mkdir -p core/common/src/main/java/com/voicetasker/core/common
mkdir -p core/database/src/main/java/com/voicetasker/core/database
mkdir -p core/network/src/main/java/com/voicetasker/core/network
mkdir -p core/security/src/main/java/com/voicetasker/core/security

# Create test directories
mkdir -p core/{common,database,network,security}/src/test/java/com/voicetasker/core/
mkdir -p core/{common,database,network,security}/src/androidTest/java/com/voicetasker/core/
```

### Step 4.2: Create Feature Modules

```bash
# Create feature module directories
for feature in auth task voice reminder sync settings; do
  mkdir -p feature/$feature/src/main/java/com/voicetasker/feature/$feature
  mkdir -p feature/$feature/src/test/java/com/voicetasker/feature/$feature
  mkdir -p feature/$feature/src/androidTest/java/com/voicetasker/feature/$feature
done
```

### Step 4.3: Create build.gradle.kts for Core Module (Database Example)

```kotlin
// core/database/build.gradle.kts
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.hilt.android")
}

android {
    namespace = "com.voicetasker.core.database"
    compileSdk = 34

    defaultConfig {
        minSdk = 31
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    testImplementation("junit:junit:4.13.2")
}
```

### Step 4.4: Create Room Database

```kotlin
// core/database/src/main/java/com/voicetasker/core/database/AppDatabase.kt
package com.voicetasker.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Database(
    entities = [
        TaskEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val db = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "voicetasker.db"
                )
                    .addMigrations() // Empty for now
                    .build()

                INSTANCE = db
                db
            }
        }
    }
}
```

### Step 4.5: Create Entities

```kotlin
// core/database/src/main/java/com/voicetasker/core/database/TaskEntity.kt
package com.voicetasker.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "tasks",
    indices = [
        Index("user_id"),
        Index("status")
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val description: String? = null,
    val status: String = "PLANNED", // PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    val dueDate: LocalDateTime? = null,
    val priority: String = "MEDIUM",
    val parentId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

### Step 4.6: Create DAOs

```kotlin
// core/database/src/main/java/com/voicetasker/core/database/TaskDao.kt
package com.voicetasker.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY updated_at DESC")
    fun observeUserTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}
```

---

## DAY 5: DI SETUP & FIRST BUILD

### Step 5.1: Create Hilt Application Class

```kotlin
// app/src/main/java/com/voicetasker/VoiceTaskerApp.kt
package com.voicetasker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoiceTaskerApp : Application()
```

### Step 5.2: Update MainActivity

```kotlin
// app/src/main/java/com/voicetasker/MainActivity.kt
package com.voicetasker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import com.voicetasker.ui.theme.VoiceTaskerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoiceTaskerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("VoiceTasker")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VoiceTaskerTheme {
        Greeting("VoiceTasker")
    }
}
```

### Step 5.3: Update AndroidManifest.xml

```xml
<!-- app/src/main/AndroidManifest.xml -->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.voicetasker">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.VoiceTasker"
        android:name=".VoiceTaskerApp"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.VoiceTasker">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

### Step 5.4: First Build & Test

```bash
# Sync gradle
./gradlew sync

# Build debug APK
./gradlew assembleDebug

# Install on emulator
./gradlew installDebug

# Run app
./gradlew runDebug

# Or in Android Studio:
# Run → Run 'app' (or Shift+F10)
```

**Expected**: App launches successfully showing "Hello VoiceTasker!"

### Step 5.5: Git Commit

```bash
git add -A
git commit -m "feat: project scaffold with Hilt, Room, Compose setup

- Android project with Kotlin + Jetpack Compose
- Hilt dependency injection configured
- Room database scaffold
- Database entities (Task, Reminder)
- Initial MainActivity with Compose UI
- First successful build and run"

git push origin main
```

---

## VERIFICATION CHECKLIST

### Week 0 Completion Checklist

- [ ] GitHub repository created and cloned
- [ ] Android Studio installed and configured
- [ ] Project structure created (app, core, feature modules)
- [ ] build.gradle.kts configured with all dependencies
- [ ] Room database initialized with entities
- [ ] Hilt dependency injection set up
- [ ] First build successful (APK generated)
- [ ] App runs on emulator without crashes
- [ ] Git initialized and first commit pushed
- [ ] CI/CD webhook configured (if applicable)
- [ ] Team members have repository access
- [ ] Local development environment documented (README.md)

### Performance Baseline (Week 0)

| Metric | Value |
|--------|-------|
| Build time (debug) | ~120 sec (first build) |
| APK size | ~25 MB |
| Cold start | ~3 sec |
| Memory usage | ~80 MB |

---

## NEXT STEPS

**Week 1**: Feature module setup (Auth, Task, Voice)
**Week 2**: Database queries and repositories
**Week 3**: UI screens and ViewModels

See document: `02_FEATURE_DEVELOPMENT_TEMPLATE.md`
