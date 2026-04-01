package com.example.glucoselog.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.glucoselog.ui.screens.AddEntryScreen
import com.example.glucoselog.ui.screens.HistoryScreen
import com.example.glucoselog.ui.screens.SummaryScreen
import com.example.glucoselog.ui.screens.SettingsScreen
import com.example.glucoselog.viewmodel.EntryViewModel

private enum class AppTab(val label: String) {
    ADD("Add"), 
    HISTORY("History"), 
    SUMMARY("Summary"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucoseLogApp(viewModel: EntryViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.ADD) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rosie's Glucose Diary") }) },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {},
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            AppTab.ADD -> AddEntryScreen(
                modifier = Modifier.padding(padding),
                viewModel = viewModel
            )
            AppTab.HISTORY -> HistoryScreen(
                modifier = Modifier.padding(padding), 
                viewModel = viewModel,
                onEditRequested = { selectedTab = AppTab.ADD }
            )
            AppTab.SUMMARY -> SummaryScreen(
                modifier = Modifier.padding(padding), 
                viewModel = viewModel
            )
            AppTab.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(padding),
                viewModel = viewModel
            )
        }
    }
}
