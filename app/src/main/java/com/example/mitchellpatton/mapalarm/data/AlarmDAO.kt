package com.example.mitchellpatton.mapalarm.data

import android.arch.persistence.room.*

@Dao
interface AlarmDAO {

    @Query("SELECT * FROM alarm")
    fun findAllAlarms(): List<Alarm>

    @Insert
    fun insertAlarm(alarm: Alarm) : Long

    @Delete
    fun deleteAlarm(alarm: Alarm)

    @Update
    fun updateAlarm(alarm: Alarm)

    @Query("DELETE FROM alarm")
    fun deleteAll()
}
