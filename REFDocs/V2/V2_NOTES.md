# V2 Notes

This bundle is intended as a practical second-stage foundation for the glucose log app requested for paired CGM and manual readings.

## Functional goals covered
- paired readings per entry
- date and time capture
- context classification
- rolling 30-day reporting
- continued month-to-month operation
- monthly report generation

## Major V2 additions
### 1. Edit existing entries
History items can now be opened in edit mode and saved back to the local database.

### 2. Validation
The add/edit form now rejects missing or invalid inputs and shows a clear message instead of silently failing.

### 3. Month navigation
Summary can move backward and forward by calendar month while still preserving the rolling 30-day summary section.

### 4. Report generation
PDF export now paginates entries and adds summary statistics.

### 5. Sharing
The generated PDF can be shared through Android's native share sheet.
