# Schedule Alarm App (with CI)

Simple alarm scheduler using `AlarmManager.setAlarmClock(...)`.
CI workflow builds an installable `app-debug.apk` in GitHub Actions.

## How to get the APK (web only)
1. Upload the *contents* of this folder to a new GitHub repo (don’t upload the ZIP itself).
2. Open the repo → **Actions** tab → wait for **Android APK** workflow.
3. Download the **app-debug** artifact → copy to your phone → install.

## Local build (Android Studio)
- Build → Build APK(s) → install `app-debug.apk`.