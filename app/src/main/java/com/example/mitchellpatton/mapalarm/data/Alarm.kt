package com.example.mitchellpatton.mapalarm.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "alarm")
data class Alarm(
        @PrimaryKey(autoGenerate = true) var alarmId: Long?,
        @ColumnInfo(name = "latitude") var alarmLat: Double,
        @ColumnInfo(name = "longitude") var alarmLong: Double,
        @ColumnInfo(name = "address") var alarmAddress: String,
        @ColumnInfo(name = "note") var alarmNote: String
) : Serializable