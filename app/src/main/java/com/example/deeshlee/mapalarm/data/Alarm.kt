package com.example.deeshlee.mapalarm.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.location.Address
import java.io.Serializable

@Entity(tableName = "alarm")
data class Alarm(
        @PrimaryKey(autoGenerate = true) var alarmId: Long?,
        @ColumnInfo(name = "latitude") var alarmLat: Float,
        @ColumnInfo(name = "longitude") var alarmLong: Float,
        @ColumnInfo(name = "address") var alarmAddress: Address,
        @ColumnInfo(name = "note") var alarmNote: String
) : Serializable
