# BeastE Macro — Android prototype

Fresh Kotlin/Jetpack Compose project for a physical-keyboard repeat experiment.

## Target
- Android 8.0+ (API 26+)
- Tested target intended: OPPO K13 5G / ColorOS 16 / Android 16
- Shizuku started through Wireless Debugging; no root required for Shizuku's shell identity.

## Build
Open this folder in Android Studio with JDK 17 and Android SDK Platform 35, or run:
```bash
gradle assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds the same debug APK on pushes to `main`/`master` and on manual dispatch.

## Setup
1. Install and start Shizuku using Wireless Debugging.
2. Install BeastE.
3. Grant BeastE Shizuku permission and connect its UserService.
4. In Android Accessibility settings, enable "BeastE Keyboard Listener".
5. Connect a physical keyboard.
6. Select one or more keys, choose an interval, enable macro, and test in a harmless text field first.

## Critical limitations — read before testing
This is a **prototype, not a verified game macro**.

- Android accessibility key filtering is subject to system arbitration. Another accessibility service or OEM behavior can prevent BeastE from receiving physical key events.
- The service returns `false` to preserve the original physical event.
- Shizuku's UserService runs with shell (ADB) identity when Shizuku is started through Wireless Debugging. ADB permissions are limited and differ by Android/system version.
- The UserService calls `input keyevent KEYCODE_*` for discrete synthetic key presses. These are taps, not a true held-key down/up pair. The original physical key remains held separately.
- Apps/games can ignore, filter, or handle injected input differently. Roblox/Blox Fruits compatibility is not guaranteed.
- Very short intervals may be throttled by Android, OEM scheduling, or the target app. 10 ms is a requested interval, not a guaranteed delivery rate.
- This project has not been built or tested on the target OPPO device in this environment. GitHub Actions build success only confirms compilation, not input delivery.
- Keep accessibility and Shizuku permissions limited to the intended device/user. Disable the macro before disconnecting the keyboard.

## Why UserService
The project uses Shizuku `bindUserService` rather than the deprecated `newProcess` API. Shizuku documents UserService as the preferred route for more complex remote work. See:
- https://github.com/RikkaApps/Shizuku-API
- https://github.com/RikkaApps/Shizuku-API/blob/master/demo/src/main/java/rikka/shizuku/demo/service/UserService.java

## Next validation steps
1. Confirm Gradle build passes.
2. Confirm Shizuku UserService reports connected.
3. Confirm accessibility status is enabled.
4. Test selected keys in a plain text editor with a physical keyboard.
5. Only then test in the intended game and record exact device/OS/game behavior.
