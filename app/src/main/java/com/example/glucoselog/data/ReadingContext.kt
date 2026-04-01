package com.example.glucoselog.data

enum class ReadingContext(val label: String) {
    BEFORE_MEAL("Before meal"),
    AFTER_MEAL("After meal"),
    MORNING_FASTING("First thing in the morning"),
    BEFORE_BED("Before bed"),
    BEFORE_SNACK("Before snack"),
    AFTER_SNACK("After snack"),
    BEFORE_WORKOUT("Before workout"),
    AFTER_WORKOUT("After workout")
}
