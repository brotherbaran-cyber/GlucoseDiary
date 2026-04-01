package com.example.glucoselog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.glucoselog.data.ReadingContext
import com.example.glucoselog.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
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
                    onValueChange = viewModel::onDateTimeChanged,
                    label = { Text("Date & time (yyyy-MM-dd HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(onClick = { viewModel.onDateTimeChanged(formatter.format(Date())) }) {
                    Text("Use current date/time")
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
}
