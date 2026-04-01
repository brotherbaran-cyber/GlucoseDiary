package com.example.glucoselog.data

import kotlinx.coroutines.flow.Flow

class EntryRepository(private val dao: GlucoseEntryDao) {
    fun getAllEntries(): Flow<List<GlucoseEntry>> = dao.getAllEntries()

    suspend fun insert(entry: GlucoseEntry): Long = dao.insert(entry)
    suspend fun update(entry: GlucoseEntry) = dao.update(entry)
    suspend fun delete(entry: GlucoseEntry) = dao.delete(entry)
    suspend fun getEntriesBetween(start: Long, end: Long): List<GlucoseEntry> = dao.getEntriesBetween(start, end)
}