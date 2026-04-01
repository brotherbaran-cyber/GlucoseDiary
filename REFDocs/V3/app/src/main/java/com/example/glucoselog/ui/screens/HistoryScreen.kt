package com.example.glucoselog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.glucoselog.data.GlucoseEntry
import com.example.glucoselog.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel,
    onEditRequested: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            HistoryRow(
                entry = entry,
                formatter = formatter,
                onDelete = { viewModel.deleteEntry(entry) },
                onEdit = {
                    viewModel.startEditing(entry)
                    onEditRequested()
                }
            )
        }
    }
}

@Composable
private fun HistoryRow(
    entry: GlucoseEntry,
    formatter: SimpleDateFormat,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val warn = abs(entry.difference) >= 20
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(formatter.format(Date(entry.timestamp)), style = MaterialTheme.typography.titleMedium)
            Text("Context: ${entry.contextTag.replace("_", " ")}")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("CGM: ${entry.cgmReading}")
                Text("Manual: ${entry.manualReading}")
                Text(
                    "Diff: ${entry.difference}",
                    color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            if (entry.note.isNotBlank()) {
                Text("Note: ${entry.note}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onEdit) { Text("Edit") }
                Button(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}