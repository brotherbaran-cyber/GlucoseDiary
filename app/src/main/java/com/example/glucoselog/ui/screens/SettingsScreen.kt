package com.example.glucoselog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucoselog.viewmodel.EntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel
) {
    val reminderState by viewModel.reminderState.collectAsState()
    val dailyPickerState = rememberTimePickerState(
        initialHour = reminderState.dailyHour,
        initialMinute = reminderState.dailyMinute,
        is24Hour = true
    )

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reminders & Notifications",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Clinical Schedule Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Clinical Workflow", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Enable smart tracking to automatically set a reminder 2 hours after you log a 'Before Meal' reading.",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto 2h Post-Meal", style = MaterialTheme.typography.bodyLarge)
                        Text("Highly recommended", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Switch(
                        checked = reminderState.postMealAutoEnabled,
                        onCheckedChange = viewModel::togglePostMealReminder
                    )
                }
            }
        }

        // Daily Check-in Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Static Reminders", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daily Baseline Check", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = reminderState.dailyEnabled,
                        onCheckedChange = viewModel::toggleDailyReminder
                    )
                }
                
                if (reminderState.dailyEnabled) {
                    HorizontalDivider()
                    Text("Check-in Time", style = MaterialTheme.typography.labelLarge)
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimeInput(state = dailyPickerState)
                    }
                }
            }
        }

        // Global Save Button
        Button(
            onClick = {
                viewModel.setDailyTime(dailyPickerState.hour, dailyPickerState.minute)
                viewModel.applyReminderSettings()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save All Settings")
        }

        if (reminderState.statusMessage != null) {
            Text(
                text = reminderState.statusMessage ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: androidx.compose.ui.Alignment = androidx.compose.ui.Alignment.TopStart,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    androidx.compose.foundation.layout.Box(modifier, contentAlignment, false, content)
}
