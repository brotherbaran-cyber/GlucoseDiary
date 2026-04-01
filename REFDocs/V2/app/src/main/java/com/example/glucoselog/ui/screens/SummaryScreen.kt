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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.glucoselog.report.PdfShareHelper
import com.example.glucoselog.report.ReportExporter
import com.example.glucoselog.viewmodel.EntryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel
) {
    val rolling by viewModel.rollingSummary.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthSummary by viewModel.selectedMonthSummary.collectAsState()
    val monthEntries by viewModel.selectedMonthEntries.collectAsState()
    val context = LocalContext.current
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Rolling 30-day summary", style = MaterialTheme.typography.titleLarge)
                SummaryStatsBlock(
                    count = rolling.count,
                    avgCgm = rolling.avgCgm,
                    avgManual = rolling.avgManual,
                    avgDifference = rolling.avgDifference,
                    maxCgm = rolling.maxCgm,
                    minCgm = rolling.minCgm,
                    maxManual = rolling.maxManual,
                    minManual = rolling.minManual,
                    contextCounts = rolling.contextCounts
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = viewModel::previousMonth) { Text("Previous") }
                    Button(onClick = viewModel::nextMonth) { Text("Next") }
                }
                Text("Selected month: $selectedMonth", style = MaterialTheme.typography.titleLarge)
                SummaryStatsBlock(
                    count = monthSummary.count,
                    avgCgm = monthSummary.avgCgm,
                    avgManual = monthSummary.avgManual,
                    avgDifference = monthSummary.avgDifference,
                    maxCgm = monthSummary.maxCgm,
                    minCgm = monthSummary.minCgm,
                    maxManual = monthSummary.maxManual,
                    minManual = monthSummary.minManual,
                    contextCounts = monthSummary.contextCounts
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { viewModel.refreshAllSummaries() }) {
                        Text("Refresh")
                    }
                    Button(onClick = {
                        val file = ReportExporter.exportMonthlyReport(context, selectedMonth, monthEntries, monthSummary)
                        PdfShareHelper.sharePdf(context, file)
                    }) {
                        Text("Create & share PDF")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(monthEntries, key = { it.id }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(formatter.format(Date(entry.timestamp)), style = MaterialTheme.typography.titleMedium)
                        Text("${entry.contextTag} • CGM ${entry.cgmReading} • Manual ${entry.manualReading} • Diff ${entry.cgmReading - entry.manualReading}")
                        if (entry.note.isNotBlank()) Text(entry.note)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatsBlock(
    count: Int,
    avgCgm: Double,
    avgManual: Double,
    avgDifference: Double,
    maxCgm: Int,
    minCgm: Int,
    maxManual: Int,
    minManual: Int,
    contextCounts: Map<String, Int>
) {
    Text("Entries: $count")
    Text("Average CGM: ${"%.1f".format(avgCgm)}")
    Text("Average manual: ${"%.1f".format(avgManual)}")
    Text("Average difference: ${"%.1f".format(avgDifference)}")
    Text("Highest CGM: $maxCgm")
    Text("Lowest CGM: $minCgm")
    Text("Highest manual: $maxManual")
    Text("Lowest manual: $minManual")
    contextCounts.forEach { (key, value) -> Text("$key: $value") }
}
