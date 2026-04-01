# Data Model

## Entity: GlucoseEntry

| Field | Type | Notes |
|---|---|---|
| id | Long | Primary key |
| timestamp | Long | Epoch millis for date/time |
| cgmReading | Int | CGM reading |
| manualReading | Int | Finger-stick reading |
| contextTag | String/Enum | BEFORE_MEAL, AFTER_MEAL, MORNING_FASTING, BEFORE_BED |
| note | String | Optional |
| createdAt | Long | Audit field |
| updatedAt | Long | Audit field |

## Derived Values
- difference = cgmReading - manualReading
- absoluteDifference = abs(cgmReading - manualReading)

## Reporting Window
The app stores all records, but the default summary window is the last 30 days.

## Aggregate Metrics
For a selected window, calculate:
- total entries
- average CGM
- average manual
- average difference
- average absolute difference
- highest CGM
- lowest CGM
- highest manual
- lowest manual
- counts by context

## Indexing / Query Strategy
- Fetch recent entries ordered by timestamp descending
- Filter entries by timestamp range for 30-day summaries
- Support context filter in history
