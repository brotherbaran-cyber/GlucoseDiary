package com.example.glucoselog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.glucoselog.viewmodel.EntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel
) {
    val reminderState by viewModel.reminderState.collectAsState()
    val pickerState = rememberTimePickerState(
        initialHour = reminderState.hour,
        initialMinute = reminderState.minute,
        is24Hour = true
    )

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Daily reminder", style = MaterialTheme.typography.titleLarge)
                Text("This V3 bundle includes reminder scaffolding. The default scheduler is approximate and intended as a good starter implementation.")
                Switch(
                    checked = reminderState.enabled,
                    onCheckedChange = viewModel::setReminderEnabled
                )
                TimeInput(state = pickerState)
                Button(onClick = {
                    viewModel.setReminderTime(pickerState.hour, pickerState.minute)
                    viewModel.applyReminderSettings()
                }) {
                    Text("Save reminder settings")
                }
                if (reminderState.statusMessage != null) {
                    Text(reminderState.statusMessage ?: "", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}