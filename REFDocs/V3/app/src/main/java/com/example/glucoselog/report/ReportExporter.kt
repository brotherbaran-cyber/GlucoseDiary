package com.example.glucoselog.report

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.glucoselog.data.GlucoseEntry
import com.example.glucoselog.data.SummaryStats
import java.io.File
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

object ReportExporter {
    fun exportMonthlyReport(
        context: Context,
        month: YearMonth,
        entries: List<GlucoseEntry>,
        summary: SummaryStats
    ): File {
        val document = PdfDocument()
        val pageWidth = 1200
        val pageHeight = 1600
        val pageInfoFactory = { pageNumber: Int ->
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        }

        val bodyPaint = Paint().apply { textSize = 22f }
        val headerPaint = Paint().apply { textSize = 30f; isFakeBoldText = true }
        val smallPaint = Paint().apply { textSize = 18f }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val sorted = entries.sortedBy { it.timestamp }
        val rowsPerPage = 32
        val pages = sorted.chunked(rowsPerPage).ifEmpty { listOf(emptyList()) }

        pages.forEachIndexed { index, pageEntries ->
            val page = document.startPage(pageInfoFactory(index + 1))
            val canvas = page.canvas
            var y = 60

            canvas.drawText("Glucose Pair Log - ${month}", 40f, y.toFloat(), headerPaint)
            y += 40
            canvas.drawText("Generated: ${formatter.format(Date())}", 40f, y.toFloat(), bodyPaint)
            y += 36

            if (index == 0) {
                canvas.drawText("Entries: ${summary.count}", 40f, y.toFloat(), bodyPaint)
                y += 28
                canvas.drawText(
                    "Avg CGM: ${"%.1f".format(summary.avgCgm)}   Avg Manual: ${"%.1f".format(summary.avgManual)}   Avg Diff: ${"%.1f".format(summary.avgDifference)}",
                    40f,
                    y.toFloat(),
                    bodyPaint
                )
                y += 28
                canvas.drawText(
                    "High/Low CGM: ${summary.maxCgm}/${summary.minCgm}   High/Low Manual: ${summary.maxManual}/${summary.minManual}",
                    40f,
                    y.toFloat(),
                    bodyPaint
                )
                y += 40
            } else {
                y += 16
            }

            canvas.drawText("Date/Time", 40f, y.toFloat(), headerPaint)
            canvas.drawText("Context", 340f, y.toFloat(), headerPaint)
            canvas.drawText("CGM", 650f, y.toFloat(), headerPaint)
            canvas.drawText("Manual", 780f, y.toFloat(), headerPaint)
            canvas.drawText("Diff", 950f, y.toFloat(), headerPaint)
            y += 34

            pageEntries.forEach { entry ->
                canvas.drawText(formatter.format(Date(entry.timestamp)), 40f, y.toFloat(), bodyPaint)
                canvas.drawText(entry.contextTag.replace("_", " "), 340f, y.toFloat(), smallPaint)
                canvas.drawText(entry.cgmReading.toString(), 650f, y.toFloat(), bodyPaint)
                canvas.drawText(entry.manualReading.toString(), 780f, y.toFloat(), bodyPaint)
                canvas.drawText(entry.difference.toString(), 950f, y.toFloat(), bodyPaint)
                y += 30
                if (entry.note.isNotBlank()) {
                    canvas.drawText("Note: ${entry.note.take(90)}", 60f, y.toFloat(), smallPaint)
                    y += 24
                }
            }

            canvas.drawText("Page ${index + 1} of ${pages.size}", 980f, (pageHeight - 40).toFloat(), smallPaint)
            document.finishPage(page)
        }

        val output = File(context.cacheDir, "glucose_report_${month}.pdf")
        output.outputStream().use { document.writeTo(it) }
        document.close()
        return output
    }
}