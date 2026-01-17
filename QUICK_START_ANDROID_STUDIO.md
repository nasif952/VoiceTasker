# VoiceTasker - Quick Start with Android Studio

## 🚀 Step-by-Step Guide to Run the App

### **Step 1: Open Project in Android Studio**

1. **Open Android Studio**
   - Launch Android Studio from your desktop or Start menu

2. **Open the VoiceTasker Project**
   - Click **File** → **Open**
   - Navigate to: `C:\Users\ROG\AndroidStudioProjects\VoiceTasker`
   - Click **OK**

3. **Wait for Gradle Sync**
   - Android Studio will automatically start syncing
   - You'll see "Gradle sync in progress..." at the bottom
   - Wait for it to complete (2-5 minutes on first sync)
   - Status changes to "Gradle sync finished"

### **Step 2: Verify Project Setup**

1. **Check for Build Errors**
   - Look at the bottom status bar
   - Should show "Build successful" (green checkmark)
   - If you see errors, see troubleshooting section below

2. **Check SDK is Installed**
   - Go to **File** → **Project Structure**
   - Check **SDK Location** section
   - Should show Android SDK location (e.g., `C:\Users\ROG\AppData\Local\Android\Sdk`)
   - If not, you need to download SDK (see below)

### **Step 3: Setup Android Emulator or Device**

#### **Option A: Using Android Emulator (Recommended for Testing)**

1. **Open AVD Manager**
   - Click **Tools** → **Device Manager**
   - Or click the device icon in top toolbar

2. **Create Virtual Device**
   - Click **Create Device**
   - Select device type (e.g., "Pixel 5")
   - Click **Next**
   - Select system image (Android 13 or 14 - API 33 or 34)
   - Click **Next**
   - Review settings, click **Finish**

3. **Start the Emulator**
   - In Device Manager, find your device
   - Click the play (▶) button next to it
   - Wait 1-2 minutes for emulator to fully boot
   - You'll see Android lock screen when ready

#### **Option B: Using Physical Device**

1. **Enable Developer Mode on Device**
   - Settings → About Phone
   - Tap "Build Number" 7 times
   - You'll see "You are a developer"

2. **Enable USB Debugging**
   - Settings → Developer Options
   - Toggle "USB Debugging" ON

3. **Connect Device via USB**
   - Plug phone into computer
   - Tap "Allow" on phone when prompted
   - Android Studio will recognize device

---

### **Step 4: Build the Project**

1. **Build Debug APK**
   - Click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
   - Or use keyboard shortcut: **Ctrl+F9**
   - Wait for build to complete (1-3 minutes)
   - You'll see notification "Build successful" at bottom right

2. **Verify Build Succeeded**
   - Check status bar at bottom: should show "Build successful"
   - No red error messages

---

### **Step 5: Run the App**

#### **Method 1: Run Button (Easiest)**

1. **Select Target Device**
   - Top toolbar has a device dropdown
   - Click on it
   - Select your emulator or connected device
   - Click **OK** if prompted

2. **Click Run Button**
   - Green play button (▶) in toolbar
   - Or press **Shift + F10**
   - Or click **Run** → **Run 'app'**

3. **Wait for App to Launch**
   - App will compile and install to device/emulator
   - Takes 30-60 seconds first time
   - Then app automatically launches

#### **Method 2: Using Logcat**

1. **Monitor App Startup**
   - At bottom, click **Logcat** tab
   - Filter by "VoiceTasker" or "MainActivity"
   - You'll see startup logs
   - Look for "Activity Started" or similar

2. **Check for Crashes**
   - Red text = errors
   - Search for "Exception" or "Error"
   - If app crashes, see troubleshooting below

---

## ✅ **What You Should See**

When the app launches successfully:

1. **Login Screen Appears**
   - Title: "Voice Tasker" at top
   - Email input field
   - Password input field (masked)
   - "Login" button
   - "Don't have an account? Register" link

2. **Screen is Interactive**
   - Can tap on email field and type
   - Can tap on password field and type
   - Login button works (not implemented yet)
   - Can click Register link

---

## 🔧 **Troubleshooting**

### **Issue 1: "Gradle sync failed"**

**Error Message**: Red text at bottom saying "Gradle sync failed"

**Solution**:
```
1. Click "Sync Now" in the notification
2. If still fails, try:
   - File → Invalidate Caches → Invalidate and Restart
   - This closes and restarts Android Studio (1-2 min)
3. If still fails:
   - File → Project Structure
   - Check SDK Location is correct
   - Download missing components if prompted
```

### **Issue 2: "No device selected"**

**Error Message**: Popup saying "No devices found" when running

**Solution**:
```
1. Start the emulator first (Device Manager → Play button)
2. Wait 2-3 minutes for it to fully boot
3. Then click Run again
4. OR connect physical device via USB
```

### **Issue 3: "Build failed - JAVA_HOME not set"**

**Error Message**: "JAVA_HOME is not set and no 'java' command could be found"

**Solution**:
```
1. Android Studio should have Java built-in
2. Try:
   - File → Project Structure
   - Check JDK Location shows a path
3. If empty, click "Download JDK" button
4. After download, restart Android Studio
```

### **Issue 4: "SDK not found"**

**Error Message**: "Could not find Android SDK" or "compileSdk not found"

**Solution**:
```
1. File → Project Structure
2. SDK Location section
3. Click "Edit" next to SDK Location
4. Point to: C:\Users\ROG\AppData\Local\Android\Sdk
5. If folder doesn't exist, download SDK:
   - Tools → SDK Manager
   - Check Android 14 (API 34)
   - Check Android 12 (API 31)
   - Click OK to download
```

### **Issue 5: App Crashes on Startup**

**Error Message**: App opens then immediately closes, red errors in Logcat

**Solution**:
```
1. Check Logcat for specific error:
   - Bottom tab: Logcat
   - Filter: VoiceTasker
   - Red text shows the issue
2. Common causes:
   - Missing dependency in build.gradle
   - Wrong package name in code
   - Database initialization error
3. To recover:
   - Check TROUBLESHOOTING section below
   - Or clean build: Build → Clean Project
   - Then Build → Rebuild Project
```

### **Issue 6: "Duplicate class" error**

**Error Message**: "Program type already present: com.google...."

**Solution**:
```
1. Build → Clean Project
2. File → Invalidate Caches / Restart
3. Build → Rebuild Project
```

### **Issue 7: Emulator Won't Start**

**Error Message**: Emulator starts then closes

**Solution**:
```
1. Check if it's a RAM issue
   - Device Manager → Your Device → ⋮ (three dots)
   - Edit → Advanced Settings
   - RAM: Reduce to 2048 MB
   - Click Finish
2. Start emulator again
3. Or use physical device instead
```

---

## 📊 **Build Process Timeline**

```
Opening Android Studio
     ↓ (1 min)
Gradle Sync in Progress
     ↓ (2-5 min) [First time longer]
Gradle Sync Finished ✓
     ↓
Ready to Build & Run
     ↓ (Click Run button)
Building APK
     ↓ (1-3 min) [First time longer]
Build Successful ✓
     ↓
Installing App
     ↓ (30 seconds)
App Launches
     ↓
Login Screen Appears ✓
```

---

## 💡 **Pro Tips**

### **Faster Development**
1. Keep emulator running between builds
   - Closing/opening takes 2 min each time
2. Use "Run" (Shift+F10) instead of "Debug" for faster builds
3. Turn on "Instant Run" in settings for instant changes

### **Better Debugging**
1. **Logcat Filtering**:
   - At bottom: Click filter icon
   - Type: `tag:VoiceTasker`
   - Shows only your app's logs

2. **Breakpoints**:
   - Click line number in code
   - Run in Debug mode (Shift+F9)
   - App pauses when breakpoint hit
   - Step through code with F10/F11

3. **Inspect Database**:
   - View → Tool Windows → Database Inspector
   - Left panel: App Inspection
   - Right panel: Database tables and rows

### **Performance**
1. Use latest emulator system image (Android 14)
2. Allocate 4+ GB RAM to emulator
3. Enable hardware acceleration if available

---

## 🎯 **First Run Checklist**

- [ ] Android Studio is open
- [ ] Project is synced (no Gradle errors)
- [ ] Emulator is running OR device is connected
- [ ] Click Run button (Shift+F10)
- [ ] App builds successfully
- [ ] Login screen appears
- [ ] Can type in email/password fields
- [ ] App doesn't crash

---

## 📱 **Testing the App UI**

Once app is running:

1. **Test Login Screen**
   ```
   - Type in email field
   - Type in password field
   - Click "Login" button (won't work yet - API not implemented)
   - Click "Register" link (should navigate to Register screen)
   ```

2. **Test Register Screen**
   ```
   - Should show 4 input fields (name, email, password, confirm)
   - Click "Login" link to go back
   ```

3. **Test Home Screen** (After auth is implemented)
   ```
   - Login with test account
   - Should show "Tasks" title
   - Should show "No tasks yet" message
   - Click blue + button to create task
   ```

---

## 🔗 **Android Studio Keyboard Shortcuts**

| Action | Shortcut |
|--------|----------|
| Build Project | Ctrl+F9 |
| Run App | Shift+F10 |
| Debug App | Shift+F9 |
| Stop App | Ctrl+F2 |
| Rerun Last Build | Ctrl+F5 |
| Sync Gradle | Ctrl+Shift+O |
| Search File | Ctrl+Shift+O |
| Find in File | Ctrl+F |
| Replace | Ctrl+H |
| Format Code | Ctrl+Alt+L |
| Organize Imports | Ctrl+Alt+O |

---

## 📚 **Next Steps After First Run**

1. **Verify App Works**
   - Check login screen appears
   - Try typing in fields
   - Check no crashes in Logcat

2. **Read Development Guide**
   - Open `README_DEVELOPMENT.md` in project
   - Understand architecture
   - Learn how to implement features

3. **Implement Authentication**
   - Follow `PHASE_1_BUILD_STATUS.md`
   - Implement LoginViewModel
   - Connect to API (when ready)

4. **Write Tests**
   - Create test files in `src/test/`
   - Run tests: Build → Build Modules → Run Tests

---

## 🚨 **Common Questions**

**Q: The app builds but won't install on device?**
A: Check USB Debugging is enabled. Unplug and re-plug USB cable. Try in terminal:
```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Q: Emulator is super slow?**
A:
- Use x86_64 system image (faster than ARM)
- Allocate 4-6 GB RAM
- Enable hardware acceleration in settings
- Or use physical device instead

**Q: How do I see what's wrong if app crashes?**
A: Check Logcat (bottom panel):
- Red text = errors
- Look for "Exception"
- Search for "Fatal" or "Crash"
- Full stack trace shows problem line

**Q: Can I run on multiple devices?**
A: Yes! Click device dropdown and select different device. App will install on all selected devices.

---

## ✨ **Success Indicators**

✅ Android Studio opens without errors
✅ Gradle sync completes successfully
✅ Build succeeds (no red errors)
✅ Emulator or device shows app
✅ Login screen appears
✅ No crash messages in Logcat
✅ Can interact with UI (type, click buttons)

---

**Last Updated**: 2026-01-17
**Time to First Run**: 10-15 minutes (first time)
**Time to Second Run**: 2-3 minutes (subsequent)
