# Session Inspector (Android starter)

A local-only Android JSON/JWT viewer built with Kotlin and Jetpack Compose.

## Features
- Paste JSON or a three-part JWT.
- View JWT header and payload.
- Mask values with sensitive-looking field names (token, session, cookie, tid, sid, password, OTP).
- Parsing happens locally; pasted text is not uploaded.

## Limitations
This app does not inspect another app's private storage, extract Instagram or Swiggy sessions, inject credentials, decrypt third-party app traffic, or bypass certificate pinning. Android's app sandbox prevents an ordinary app from reading another app's private session store. Cloning an app does not remove that isolation.

## Build
1. Install Android Studio with Android SDK 35 and JDK 17.
2. Open this folder in Android Studio.
3. Wait for Gradle sync.
4. Connect an Android phone with USB debugging enabled or use an emulator.
5. Press Run.

Use dummy data or credentials from systems you own and are authorized to test. Never paste live session tokens, cookies, passwords, or OTPs into unknown apps.
