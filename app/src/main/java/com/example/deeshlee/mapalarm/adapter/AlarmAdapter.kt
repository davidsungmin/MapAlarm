package com.example.deeshlee.mapalarm.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.deeshlee.mapalarm.ListActivity
import com.example.deeshlee.mapalarm.MainActivity
import java.util.*
import com.example.deeshlee.mapalarm.R
import com.example.deeshlee.mapalarm.data.AppDatabase
import com.example.deeshlee.mapalarm.data.Alarm
import com.example.deeshlee.mapalarm.touch.ItemTouchHelperAdapter
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.alarm_row.view.*

class AlarmAdapter(val context: Context, val alarmList: List<Alarm>):
        RecyclerView.Adapter<AlarmAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    var alarms = mutableListOf<Alarm>()

    init {
        this.alarms.addAll(alarmList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
                R.layout.alarm_row, parent, false
        )
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.tvAddress.text = alarm.alarmAddress
        holder.tvNotes.text = alarm.alarmNote

        holder.fabDelete.setOnClickListener {
            deleteAlarm(holder.adapterPosition)
        }

    }

    inner class ViewHolder(alarmView: View) : RecyclerView.ViewHolder(alarmView)
    {
        val tvAddress = alarmView.tvAddress
        val tvNotes = alarmView.tvNotes
        val fabDelete = alarmView.fabDelete

    }



    private fun deleteAlarm(adapterPosition: Int) {
        (context as ListActivity).addMarkerToDelete(alarms[adapterPosition].markerId)
        Thread {
            AppDatabase.getInstance(
                    context).alarmDao().deleteAlarm(alarms[adapterPosition])

            alarms.removeAt(adapterPosition)

            context.runOnUiThread {
                notifyItemRemoved(adapterPosition)
            }
        }.start()

    }


    fun deleteAllAlarms() {
        val size = alarms.size - 1
        Thread {
            for (i in 0..size){
                AppDatabase.getInstance(
                        context).alarmDao().deleteAlarm(alarms[0])

                alarms.removeAt(0)

                (context as MainActivity).runOnUiThread {
                    notifyItemRemoved(0)
                }
            }
        }.start()

    }

    fun addAlarm(alarm: Alarm) {
        alarms.add(0, alarm)
        notifyDataSetChanged()
        notifyItemInserted(0)
    }

    override fun onDismissed(position: Int) {
        deleteAlarm(position)
    }

    override fun onAlarmMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(alarms, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
}