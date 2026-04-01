package com.example.glucoselog.report

import android.content.Context
import com.example.glucoselog.data.GlucoseEntry
import java.io.File
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportMonthCsv(
        context: Context,
        month: YearMonth,
        entries: List<GlucoseEntry>
    ): File {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val output = File(context.cacheDir, "glucose_report_${month}.csv")
        output.bufferedWriter().use { writer ->
            writer.appendLine("DateTime,Context,CGM,Manual,Difference,Note")
            entries.sortedBy { it.timestamp }.forEach { entry ->
                val escapedNote = entry.note.replace("\"", "\"\"")
                writer.appendLine(
                    "\"${formatter.format(Date(entry.timestamp))}\",\"${entry.contextTag}\",${entry.cgmReading ?: ""},${entry.manualReading},${entry.difference},\"${escapedNote}\""
                )
            }
        }
        return output
    }
}
