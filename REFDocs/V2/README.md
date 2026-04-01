# Glucose Pair Log - Android V2 Starter

This V2 bundle upgrades the original starter with a more practical handoff for Android Studio.

## What is new in V2
- entry editing flow
- form validation with user-facing error messages
- larger caregiver-friendly cards and buttons
- month-based summary browsing in addition to rolling 30-day metrics
- multi-page PDF report generation
- Android share sheet support for generated reports
- FileProvider configuration for safe PDF sharing

## Still worth polishing before production use
- dedicated date and time picker dialogs
- instrumentation tests and UI tests
- a true print preview flow on all devices
- backup/export to CSV or cloud storage
- reminder scheduling

## Open in Android Studio
1. Open the `glucose_android_v2` folder.
2. Let Gradle sync fully.
3. Build and run on a device or emulator.
4. Test the Add, History, and Summary tabs.

## Suggested first test pass
1. Add 3–5 entries with different contexts.
2. Edit one entry from History.
3. Verify the month summary changes.
4. Generate a PDF and share it.

## Key files
- `viewmodel/EntryViewModel.kt` - app state, editing flow, validation, summary logic
- `ui/screens/AddEntryScreen.kt` - reusable add/edit form
- `ui/screens/HistoryScreen.kt` - list with edit/delete actions
- `ui/screens/SummaryScreen.kt` - rolling and monthly summary + export
- `report/ReportExporter.kt` - multi-page PDF creation
- `report/PdfShareHelper.kt` - secure PDF sharing
