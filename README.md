# Glucose Pair Log - Android Starter

This is a starter Android app for recording paired glucose readings.

## Current Capabilities
- Add paired CGM and manual readings
- Timestamped entries
- Context selection
- Local Room database storage
- History list
- Rolling 30-day summary calculations
- Basic PDF report generation scaffold

## Notes
This starter is intended as a clean foundation. It is close to practical use, but still needs polish in these areas:
- edit-entry flow
- safer form validation and error messaging
- Android share/print handoff for generated PDF
- multi-page PDF support
- month picker / custom date range reports
- visual formatting polish

## Open in Android Studio
1. Open the `glucose_android_starter` folder in Android Studio.
2. Let Gradle sync.
3. Build and run on an Android device or emulator.

## File Highlights
- `MainActivity.kt` - app entry point
- `data/` - Room entities, DAO, database, repository
- `ui/screens/` - Add, History, Summary screens
- `viewmodel/` - summary and entry logic
- `report/ReportExporter.kt` - basic PDF export scaffold
