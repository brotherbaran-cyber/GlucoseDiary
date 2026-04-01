package com.example.glucoselog.data

data class ReminderState(
    val dailyEnabled: Boolean = false,
    val dailyHour: Int = 8,
    val dailyMinute: Int = 0,
    
    val postMealAutoEnabled: Boolean = false, // Auto-schedule 2h after "Before Meal" logs
    
    val mealRemindersEnabled: Boolean = false,
    val breakfastHour: Int = 7,
    val lunchHour: Int = 12,
    val dinnerHour: Int = 18,

    val statusMessage: String? = null
)
