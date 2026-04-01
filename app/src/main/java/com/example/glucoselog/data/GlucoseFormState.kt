package com.example.glucoselog.data

data class GlucoseFormState(
    val entryId: Long? = null,
    val dateTimeText: String = "",
    val cgmText: String = "",
    val manualText: String = "",
    val contextTag: ReadingContext = ReadingContext.MORNING_FASTING,
    val noteText: String = "",
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
