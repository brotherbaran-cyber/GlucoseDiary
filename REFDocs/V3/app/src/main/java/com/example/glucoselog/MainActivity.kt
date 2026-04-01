package com.example.glucoselog

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glucoselog.data.AppDatabase
import com.example.glucoselog.data.EntryRepository
import com.example.glucoselog.reminder.ReminderScheduler
import com.example.glucoselog.ui.GlucoseLogApp
import com.example.glucoselog.ui.theme.GlucoseLogTheme
import com.example.glucoselog.viewmodel.EntryViewModel
import com.example.glucoselog.viewmodel.EntryViewModelFactory

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            GlucoseLogTheme {
                val database = remember { AppDatabase.getDatabase(applicationContext) }
                val repository = remember { EntryRepository(database.glucoseEntryDao()) }
                val reminderScheduler = remember { ReminderScheduler(applicationContext) }
                val viewModel: EntryViewModel = viewModel(
                    factory = EntryViewModelFactory(repository, reminderScheduler)
                )
                GlucoseLogApp(viewModel = viewModel)
            }
        }
    }
}