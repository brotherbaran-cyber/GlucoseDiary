# Setup and Test Checklist

## First open
- Open the `glucose_android_v3` folder in Android Studio.
- Wait for Gradle sync to finish.
- If asked to upgrade Gradle wrappers or SDK components, let Android Studio install what it needs.

## First run
- Build and run on a device or emulator.
- Grant notification permission on Android 13+ if you want reminders.
- Add 5 entries with different contexts.
- Edit one entry.
- Delete one entry.
- Confirm the rolling 30-day summary updates.
- Switch months in Summary and confirm the month block updates.

## Export tests
- Generate a PDF and share it.
- Generate a CSV and share it.
- Open the shared file from email, Drive, or local storage target.

## Reminder tests
- Open Settings.
- Enable reminders.
- Choose a time.
- Confirm a reminder notification appears later.
- Disable reminders and confirm future reminders stop.

## If the app does not build immediately
- Re-sync Gradle.
- Check that compile SDK 35 and Java 17 are installed.
- Let Android Studio apply any import suggestions.