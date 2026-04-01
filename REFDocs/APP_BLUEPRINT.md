# App Blueprint

## Working Name
Glucose Pair Log

## Core Goal
Provide a fast Android app for logging and comparing paired glucose readings across a continuing timeline, while making it easy to produce a 30-day printable summary.

## Primary Users
- Patient
- Spouse/caregiver
- Family member assisting with logging

## Main Requirements
Each reading event stores:
- Date
- Time
- CGM reading
- Manual blood draw reading
- Reading context:
  - Before meal
  - After meal
  - First thing in the morning
  - Before bed

## Functional Goals
1. Add an entry quickly
2. See recent entries
3. Review/edit previous entries
4. View a rolling 30-day summary
5. Print or export a report
6. Continue seamlessly into the next month

## Screen Plan

### 1. Home Screen
Shows:
- Quick stats for last 30 days
- Recent entries
- Buttons to add entry and generate report

### 2. Add Entry Screen
Fields:
- Date
- Time
- CGM reading
- Manual reading
- Context selector
- Optional note
- Save button

### 3. History Screen
Features:
- Reverse-chronological list
- Filter by context
- Filter by date range
- Tap to edit entry
- Delete entry

### 4. Summary Screen
Displays:
- 30-day average CGM
- 30-day average manual
- Average difference
- Highest/lowest values
- Entry counts by context
- Generate PDF action

## User Experience Principles
- Large, clear text for readability
- Minimal steps for each entry
- Mostly local/offline operation
- Simple print-ready summaries
- Safe editing without losing history

## Long-Term Enhancements
- Reminders/notifications
- Trend charts
- CSV export
- Secure backup
- Doctor-specific summary layout
- Optional color alerts for large discrepancies
