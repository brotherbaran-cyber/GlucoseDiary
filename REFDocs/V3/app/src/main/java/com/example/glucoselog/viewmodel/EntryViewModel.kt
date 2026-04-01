package com.example.glucoselog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glucoselog.data.EntryRepository
import com.example.glucoselog.data.GlucoseEntry
import com.example.glucoselog.data.GlucoseFormState
import com.example.glucoselog.data.ReadingContext
import com.example.glucoselog.data.ReminderUiState
import com.example.glucoselog.data.SummaryStats
import com.example.glucoselog.reminder.ReminderScheduler
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

class EntryViewModel(
    private val repository: EntryRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    val entries = repository.getAllEntries().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _formState = MutableStateFlow(
        GlucoseFormState(dateTimeText = formatter.format(Date()))
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

    private val _reminderState = MutableStateFlow(ReminderUiState())
    val reminderState: StateFlow<ReminderUiState> = _reminderState.asStateFlow()

    init {
        refreshAllSummaries()
    }

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
            cgm == null -> updateForm { copy(errorMessage = "Enter a valid CGM reading") }
            manual == null -> updateForm { copy(errorMessage = "Enter a valid manual reading") }
            cgm !in 20..600 -> updateForm { copy(errorMessage = "CGM reading should be between 20 and 600") }
            manual !in 20..600 -> updateForm { copy(errorMessage = "Manual reading should be between 20 and 600") }
            else -> {
                viewModelScope.launch {
                    if (state.isEditing && state.entryId != null) {
                        repository.update(
                            GlucoseEntry(
                                id = state.entryId,
                                timestamp = parsedTime,
                                cgmReading = cgm,
                                manualReading = manual,
                                contextTag = state.contextTag.name,
                                note = state.noteText,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        resetForm("Entry updated")
                    } else {
                        repository.insert(
                            GlucoseEntry(
                                timestamp = parsedTime,
                                cgmReading = cgm,
                                manualReading = manual,
                                contextTag = state.contextTag.name,
                                note = state.noteText
                            )
                        )
                        resetForm("Entry saved")
                    }
                    refreshAllSummaries()
                }
            }
        }
    }

    fun startEditing(entry: GlucoseEntry) {
        val context = ReadingContext.entries.firstOrNull { it.name == entry.contextTag } ?: ReadingContext.BEFORE_MEAL
        _formState.value = GlucoseFormState(
            entryId = entry.id,
            dateTimeText = formatter.format(Date(entry.timestamp)),
            cgmText = entry.cgmReading.toString(),
            manualText = entry.manualReading.toString(),
            contextTag = context,
            noteText = entry.note,
            isEditing = true,
            errorMessage = null,
            successMessage = null
        )
    }

    fun cancelEditing() {
        resetForm()
    }

    fun deleteEntry(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            refreshAllSummaries()
            updateForm { copy(successMessage = "Entry deleted") }
        }
    }

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

    fun refresh30DaySummary() {
        viewModelScope.launch {
            val end = System.currentTimeMillis()
            val start = end - TimeUnit.DAYS.toMillis(30)
            val recentEntries = repository.getEntriesBetween(start, end)
            _rollingSummary.value = calculateSummary(recentEntries)
        }
    }

    fun refreshSelectedMonthSummary() {
        val month = _selectedMonth.value
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        viewModelScope.launch {
            val monthEntries = repository.getEntriesBetween(start, end)
            _selectedMonthSummary.value = calculateSummary(monthEntries)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        _reminderState.value = _reminderState.value.copy(enabled = enabled, statusMessage = null)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminderState.value = _reminderState.value.copy(hour = hour, minute = minute, statusMessage = null)
    }

    fun applyReminderSettings() {
        val state = _reminderState.value
        if (state.enabled) {
            reminderScheduler.scheduleDailyReminder()
            _reminderState.value = state.copy(statusMessage = "Daily reminder enabled")
        } else {
            reminderScheduler.cancelDailyReminder()
            _reminderState.value = state.copy(statusMessage = "Daily reminder disabled")
        }
    }

    private fun resetForm(successMessage: String? = null) {
        _formState.value = GlucoseFormState(
            dateTimeText = formatter.format(Date()),
            contextTag = ReadingContext.BEFORE_MEAL,
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
            avgCgm = entries.map { it.cgmReading }.average(),
            avgManual = entries.map { it.manualReading }.average(),
            avgDifference = entries.map { it.difference }.average(),
            maxCgm = entries.maxOf { it.cgmReading },
            minCgm = entries.minOf { it.cgmReading },
            maxManual = entries.maxOf { it.manualReading },
            minManual = entries.minOf { it.manualReading },
            contextCounts = entries.groupingBy { it.contextTag }.eachCount().toSortedMap()
        )
    }
}

class EntryViewModelFactory(
    private val repository: EntryRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryViewModel(repository, reminderScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}