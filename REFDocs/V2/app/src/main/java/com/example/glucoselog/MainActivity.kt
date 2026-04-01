package com.example.glucoselog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glucoselog.data.AppDatabase
import com.example.glucoselog.data.EntryRepository
import com.example.glucoselog.ui.GlucoseLogApp
import com.example.glucoselog.ui.theme.GlucoseLogTheme
import com.example.glucoselog.viewmodel.EntryViewModel
import com.example.glucoselog.viewmodel.EntryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GlucoseLogTheme {
                val database = remember { AppDatabase.getDatabase(applicationContext) }
                val repository = remember { EntryRepository(database.glucoseEntryDao()) }
                val viewModel: EntryViewModel = viewModel(factory = EntryViewModelFactory(repository))
                GlucoseLogApp(viewModel = viewModel)
            }
        }
    }
}
