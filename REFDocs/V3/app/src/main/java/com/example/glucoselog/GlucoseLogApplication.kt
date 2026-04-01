package com.example.glucoselog

import android.app.Application
import com.example.glucoselog.reminder.ReminderNotificationHelper

class GlucoseLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderNotificationHelper.createChannel(this)
    }
}