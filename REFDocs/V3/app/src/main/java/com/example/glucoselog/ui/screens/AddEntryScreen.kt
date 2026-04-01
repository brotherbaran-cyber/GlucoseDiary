package com.example.glucoselog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.glucoselog.data.ReadingContext
import com.example.glucoselog.ui.components.DatePickerModal
import com.example.glucoselog.ui.components.TimePickerModal
import com.example.glucoselog.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel
) {
    val state by viewModel.formState.collectAsState()
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedMillis by remember {
        mutableLongStateOf(
            runCatching { formatter.parse(state.dateTimeText)?.time ?: System.currentTimeMillis() }
                .getOrElse { System.currentTimeMillis() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (state.isEditing) "Edit paired glucose entry" else "Add paired glucose entry",
                    style = MaterialTheme.typography.titleLarge
                )

                if (state.errorMessage != null) {
                    Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                }
                if (state.successMessage != null) {
                    Text(state.successMessage ?: "", color = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = state.dateTimeText,
                    onValueChange = {
                        viewModel.onDateTimeChanged(it)
                        runCatching { formatter.parse(it)?.time }.getOrNull()?.let { millis -> selectedMillis = millis }
                    },
                    label = { Text("Date & time (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showDatePicker = true }) { Text("Pick date") }
                    Button(onClick = { showTimePicker = true }) { Text("Pick time") }
                    TextButton(onClick = {
                        selectedMillis = System.currentTimeMillis()
                        viewModel.onDateTimeChanged(formatter.format(Date(selectedMillis)))
                    }) {
                        Text("Use now")
                    }
                }

                OutlinedTextField(
                    value = state.cgmText,
                    onValueChange = viewModel::onCgmChanged,
                    label = { Text("CGM reading") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.manualText,
                    onValueChange = viewModel::onManualChanged,
                    label = { Text("Manual reading") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val diff = (state.cgmText.toIntOrNull() ?: 0) - (state.manualText.toIntOrNull() ?: 0)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current difference", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.cgmText.isNotBlank() && state.manualText.isNotBlank()) "$diff mg/dL" else "Enter both readings to calculate difference",
                            color = if (kotlin.math.abs(diff) >= 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = state.contextTag.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Context") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ReadingContext.entries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.label) },
                                onClick = {
                                    viewModel.onContextChanged(item)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReadingContext.entries.forEach { context ->
                        AssistChip(
                            onClick = { viewModel.onContextChanged(context) },
                            label = { Text(context.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = state.noteText,
                    onValueChange = viewModel::onNoteChanged,
                    label = { Text("Optional note") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = viewModel::saveForm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isEditing) "Update entry" else "Save entry")
                }

                if (state.isEditing) {
                    Button(
                        onClick = viewModel::cancelEditing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel editing")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initialMillis = selectedMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                showDatePicker = false
                if (millis != null) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                    val pickedDate = Calendar.getInstance().apply { timeInMillis = millis }
                    calendar.set(Calendar.YEAR, pickedDate.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, pickedDate.get(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, pickedDate.get(Calendar.DAY_OF_MONTH))
                    selectedMillis = calendar.timeInMillis
                    viewModel.onDateTimeChanged(formatter.format(Date(selectedMillis)))
                }
            }
        )
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }
        TimePickerModal(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                selectedMillis = calendar.timeInMillis
                viewModel.onDateTimeChanged(formatter.format(Date(selectedMillis)))
            }
        )
    }
}