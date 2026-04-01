package com.example.glucoselog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GlucoseEntry): Long

    @Update
    suspend fun update(entry: GlucoseEntry)

    @Delete
    suspend fun delete(entry: GlucoseEntry)

    @Query("SELECT * FROM glucose_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<GlucoseEntry>>

    @Query("SELECT * FROM glucose_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getEntriesBetween(start: Long, end: Long): List<GlucoseEntry>
}