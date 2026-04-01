package com.example.glucoselog.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glucoselog.data.GlucoseEntry
import com.example.glucoselog.viewmodel.EntryViewModel
import kotlinx.coroutines.launch
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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary

    val isScrollable by remember {
        derivedStateOf {
            listState.canScrollForward || listState.canScrollBackward
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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

        if (isScrollable && entries.isNotEmpty()) {
            // Interactive Scrollbar
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(30.dp)
                    .padding(vertical = 4.dp)
                    .pointerInput(entries.size) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val totalItems = entries.size
                            val dragY = change.position.y
                            val height = size.height.toFloat()
                            val targetIndex = ((dragY / height) * totalItems).toInt().coerceIn(0, totalItems - 1)
                            coroutineScope.launch {
                                listState.scrollToItem(targetIndex)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(end = 4.dp)) {
                    val layoutInfo = listState.layoutInfo
                    val totalItemsCount = layoutInfo.totalItemsCount
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo
                    
                    if (totalItemsCount > 0 && visibleItemsInfo.isNotEmpty()) {
                        val viewportHeight = size.height
                        val firstItem = visibleItemsInfo.first()
                        val averageSize = visibleItemsInfo.map { it.size }.average().toFloat()
                        val estimatedTotalHeight = averageSize * totalItemsCount
                        val scrollOffset = (firstItem.index * averageSize) - firstItem.offset
                        val visiblePercent = (viewportHeight / estimatedTotalHeight).coerceIn(0.1f, 1f)
                        val thumbHeight = viewportHeight * visiblePercent
                        val thumbOffset = (scrollOffset / (estimatedTotalHeight - viewportHeight)) * (viewportHeight - thumbHeight)

                        drawRoundRect(
                            color = Color.Gray.copy(alpha = 0.05f),
                            topLeft = Offset(size.width - 8.dp.toPx(), 0f),
                            size = Size(6.dp.toPx(), viewportHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )

                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.8f),
                            topLeft = Offset(size.width - 8.dp.toPx(), thumbOffset.coerceIn(0f, viewportHeight - thumbHeight)),
                            size = Size(6.dp.toPx(), thumbHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatter.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.contextTag.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("CGM", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = entry.cgmReading?.toString() ?: "N/A",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Column {
                    Text("Manual", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = entry.manualReading.toString(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                if (entry.cgmReading != null) {
                    val diff = entry.difference
                    Column {
                        Text("Diff", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (diff > 0) "+$diff" else diff.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (abs(diff) >= 20) MaterialTheme.colorScheme.error else Color.Unspecified
                        )
                    }
                }
            }
            
            if (entry.note.isNotBlank()) {
                Text(
                    text = "Note: ${entry.note}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = onEdit) {
                    Text("Edit")
                }
            }
        }
    }
}
