package com.example.mitchellpatton.mapalarm.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mitchellpatton.mapalarm.ListActivity
import com.example.mitchellpatton.mapalarm.R
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.example.mitchellpatton.mapalarm.data.AppDatabase
import com.example.mitchellpatton.mapalarm.touch.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.alarm_row.view.*
import java.util.*

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
        Thread {
            (context as ListActivity).addMarkerToDelete(alarms[adapterPosition].markerId)
            AppDatabase.getInstance(
                    context).alarmDao().deleteAlarm(alarms[adapterPosition])

            alarms.removeAt(adapterPosition)

            context.runOnUiThread {
                notifyItemRemoved(adapterPosition)
            }
        }.start()

    }


    fun deleteAllAlarms() {
        alarms.clear()
        notifyDataSetChanged()
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