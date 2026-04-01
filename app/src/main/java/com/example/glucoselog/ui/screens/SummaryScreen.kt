package com.example.glucoselog.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucoselog.report.CsvExporter
import com.example.glucoselog.report.FileShareHelper
import com.example.glucoselog.report.ReportExporter
import com.example.glucoselog.viewmodel.EntryViewModel
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: EntryViewModel
) {
    val rollingSummary by viewModel.rollingSummary.collectAsState()
    val monthSummary by viewModel.selectedMonthSummary.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedMonthEntries by viewModel.selectedMonthEntries.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = viewModel::previousMonth) {
                Text("<")
            }
            Text(
                text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = viewModel::nextMonth) {
                Text(">")
            }
        }

        // Calendar Month Card
        SummaryCard(title = "Calendar Month Metrics") {
            StatRow("Entries", "${monthSummary.count}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            StatRow("Avg CGM", "%.1f".format(monthSummary.avgCgm))
            StatRow("Avg Manual", "%.1f".format(monthSummary.avgManual))
            val diffColor = if (abs(monthSummary.avgDifference) > 20) Color.Red else Color.Unspecified
            StatRow("Avg Difference", "%.1f".format(monthSummary.avgDifference), valueColor = diffColor)
        }

        // Rolling 30 Day Card
        SummaryCard(title = "Rolling 30-Day Metrics", containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            StatRow("Total Entries", "${rollingSummary.count}")
            StatRow("Avg Difference", "%.1f".format(rollingSummary.avgDifference))
        }

        // Context Breakdown for selected month
        if (monthSummary.contextCounts.isNotEmpty()) {
            SummaryCard(title = "Context Breakdown") {
                monthSummary.contextCounts.forEach { (tag, count) ->
                    StatRow(tag.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, "$count")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { 
                    try {
                        val pdfFile = ReportExporter.exportMonthReport(context, selectedMonth, selectedMonthEntries, monthSummary)
                        FileShareHelper.shareFile(context, pdfFile, "application/pdf", "Share PDF Report")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export PDF Report")
            }

            Button(
                onClick = { 
                    try {
                        val csvFile = CsvExporter.exportMonthCsv(context, selectedMonth, selectedMonthEntries)
                        FileShareHelper.shareFile(context, csvFile, "text/csv", "Share CSV Report")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error generating CSV", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export CSV Data")
            }

            OutlinedButton(
                onClick = { viewModel.refreshAllSummaries() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh All Data")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryCard(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

private typealias ColumnScope = androidx.compose.foundation.layout.ColumnScope
