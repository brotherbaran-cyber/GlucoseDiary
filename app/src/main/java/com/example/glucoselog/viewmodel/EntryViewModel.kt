package com.example.glucoselog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glucoselog.data.EntryRepository
import com.example.glucoselog.data.GlucoseEntry
import com.example.glucoselog.data.GlucoseFormState
import com.example.glucoselog.data.ReadingContext
import com.example.glucoselog.data.ReminderState
import com.example.glucoselog.data.SummaryStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class EntryViewModel(private val repository: EntryRepository) : ViewModel() {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    val entries = repository.getAllEntries().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(
        GlucoseFormState(
            dateTimeText = formatter.format(Date()),
            contextTag = ReadingContext.MORNING_FASTING
        )
    )
    val formState: StateFlow<GlucoseFormState> = _formState.asStateFlow()

    private val _rollingSummary = MutableStateFlow(SummaryStats())
    val rollingSummary: StateFlow<SummaryStats> = _rollingSummary.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val selectedMonthEntries = combine(entries, _selectedMonth) { allEntries, month ->
        val zone = ZoneId.systemDefault()
        allEntries.filter {
            val localDate = Date(it.timestamp).toInstant().atZone(zone).toLocalDate()
            YearMonth.from(localDate) == month
        }.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _selectedMonthSummary = MutableStateFlow(SummaryStats())
    val selectedMonthSummary: StateFlow<SummaryStats> = _selectedMonthSummary.asStateFlow()

    private val _reminderState = MutableStateFlow(ReminderState())
    val reminderState: StateFlow<ReminderState> = _reminderState.asStateFlow()

    init {
        refreshAllSummaries()
    }

    // Form logic
    fun onDateTimeChanged(value: String) = updateForm { copy(dateTimeText = value, errorMessage = null, successMessage = null) }
    fun onCgmChanged(value: String) = updateForm { copy(cgmText = value.filter(Char::isDigit), errorMessage = null, successMessage = null) }
    fun onManualChanged(value: String) = updateForm { copy(manualText = value.filter(Char::isDigit), errorMessage = null, successMessage = null) }
    fun onContextChanged(value: ReadingContext) = updateForm { copy(contextTag = value, errorMessage = null, successMessage = null) }
    fun onNoteChanged(value: String) = updateForm { copy(noteText = value, errorMessage = null, successMessage = null) }

    fun saveForm() {
        val state = _formState.value
        val parsedTime = runCatching { formatter.parse(state.dateTimeText)?.time }.getOrNull()
        val cgm = state.cgmText.toIntOrNull()
        val manual = state.manualText.toIntOrNull()

        when {
            parsedTime == null -> updateForm { copy(errorMessage = "Enter date and time as yyyy-MM-dd HH:mm") }
            cgm == null && manual == null -> updateForm { copy(errorMessage = "Enter at least one reading") }
            cgm != null && cgm !in 20..600 -> updateForm { copy(errorMessage = "CGM reading should be between 20 and 600") }
            manual != null && manual !in 20..600 -> updateForm { copy(errorMessage = "Manual reading should be between 20 and 600") }
            else -> {
                viewModelScope.launch {
                    val entry = if (state.isEditing && state.entryId != null) {
                        GlucoseEntry(
                            id = state.entryId,
                            timestamp = parsedTime,
                            cgmReading = cgm,
                            manualReading = manual ?: 0,
                            contextTag = state.contextTag.name,
                            note = state.noteText
                        )
                    } else {
                        GlucoseEntry(
                            timestamp = parsedTime,
                            cgmReading = cgm,
                            manualReading = manual ?: 0,
                            contextTag = state.contextTag.name,
                            note = state.noteText
                        )
                    }

                    if (state.isEditing && state.entryId != null) {
                        repository.update(entry)
                        resetForm("Entry updated")
                    } else {
                        repository.insert(entry)
                        
                        // Check if we should trigger a 2-hour reminder
                        if (_reminderState.value.postMealAutoEnabled && state.contextTag == ReadingContext.BEFORE_MEAL) {
                            scheduleTwoHourReminder()
                        }
                        
                        resetForm("Entry saved")
                    }
                    refreshAllSummaries()
                }
            }
        }
    }

    private fun scheduleTwoHourReminder() {
        val status = "2-hour post-meal reminder scheduled"
        _reminderState.value = _reminderState.value.copy(statusMessage = status)
        // In a real implementation, this would trigger AlarmManager for 2 hours from now
    }

    fun startEditing(entry: GlucoseEntry) {
        val context = ReadingContext.entries.firstOrNull { it.name == entry.contextTag } ?: ReadingContext.BEFORE_MEAL
        _formState.value = GlucoseFormState(
            entryId = entry.id,
            dateTimeText = formatter.format(Date(entry.timestamp)),
            cgmText = entry.cgmReading?.toString() ?: "",
            manualText = entry.manualReading.toString(),
            contextTag = context,
            noteText = entry.note,
            isEditing = true
        )
    }

    fun cancelEditing() = resetForm()

    fun deleteEntry(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            refreshAllSummaries()
        }
    }

    // Reminder logic
    fun toggleDailyReminder(enabled: Boolean) {
        _reminderState.value = _reminderState.value.copy(dailyEnabled = enabled, statusMessage = null)
    }

    fun setDailyTime(hour: Int, minute: Int) {
        _reminderState.value = _reminderState.value.copy(dailyHour = hour, dailyMinute = minute, statusMessage = null)
    }

    fun togglePostMealReminder(enabled: Boolean) {
        _reminderState.value = _reminderState.value.copy(postMealAutoEnabled = enabled, statusMessage = null)
    }

    fun applyReminderSettings() {
        val rs = _reminderState.value
        val message = buildString {
            append("Settings saved. ")
            if (rs.dailyEnabled) append("Daily check at ${rs.dailyHour}:${rs.dailyMinute.toString().padStart(2, '0')}. ")
            if (rs.postMealAutoEnabled) append("Auto 2h post-meal active.")
        }
        _reminderState.value = rs.copy(statusMessage = message)
    }

    // Summary logic
    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
        refreshSelectedMonthSummary()
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
        refreshSelectedMonthSummary()
    }

    fun refreshAllSummaries() {
        refresh30DaySummary()
        refreshSelectedMonthSummary()
    }

    private fun refresh30DaySummary() {
        viewModelScope.launch {
            val end = System.currentTimeMillis()
            val start = end - TimeUnit.DAYS.toMillis(30)
            val recentEntries = repository.getEntriesBetween(start, end)
            _rollingSummary.value = calculateSummary(recentEntries)
        }
    }

    private fun refreshSelectedMonthSummary() {
        val month = _selectedMonth.value
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        viewModelScope.launch {
            val monthEntries = repository.getEntriesBetween(start, end)
            _selectedMonthSummary.value = calculateSummary(monthEntries)
        }
    }

    private fun resetForm(successMessage: String? = null) {
        _formState.value = GlucoseFormState(
            dateTimeText = formatter.format(Date()),
            contextTag = ReadingContext.MORNING_FASTING,
            successMessage = successMessage
        )
    }

    private fun updateForm(transform: GlucoseFormState.() -> GlucoseFormState) {
        _formState.value = _formState.value.transform()
    }

    private fun calculateSummary(entries: List<GlucoseEntry>): SummaryStats {
        if (entries.isEmpty()) return SummaryStats()
        return SummaryStats(
            count = entries.size,
            avgCgm = entries.mapNotNull { it.cgmReading }.let { if (it.isEmpty()) 0.0 else it.average() },
            avgManual = entries.map { it.manualReading }.average(),
            avgDifference = entries.mapNotNull { e -> e.cgmReading?.let { it - e.manualReading } }.let { if (it.isEmpty()) 0.0 else it.average() },
            maxCgm = entries.mapNotNull { it.cgmReading }.maxOrNull() ?: 0,
            minCgm = entries.mapNotNull { it.cgmReading }.minOrNull() ?: 0,
            maxManual = entries.maxOf { it.manualReading },
            minManual = entries.minOf { it.manualReading },
            contextCounts = entries.groupingBy { it.contextTag }.eachCount().toSortedMap()
        )
    }
}

class EntryViewModelFactory(private val repository: EntryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
