# Android Build Notes

## Recommended Stack
- Kotlin
- Jetpack Compose
- Room database
- ViewModel + StateFlow
- Local PDF generation with Android printing APIs

## Why This Stack
- Compose is the current Android UI toolkit
- Room is reliable for local structured data
- Local persistence avoids internet dependence
- PDF export supports clean printable summaries

## Suggested Milestones
1. Entry capture and storage
2. History and editing
3. 30-day summary calculations
4. PDF report generation
5. Export/share polish

## Testing Priorities
- Validate paired readings save correctly
- Verify date/time handling
- Confirm 30-day rolling calculations
- Confirm month-to-month continuity
- Confirm PDF report content and formatting
