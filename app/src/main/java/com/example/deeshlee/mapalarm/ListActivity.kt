package com.example.deeshlee.mapalarm

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import com.example.deeshlee.mapalarm.adapter.AlarmAdapter
import com.example.deeshlee.mapalarm.data.AppDatabase
import com.example.deeshlee.mapalarm.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {

    private lateinit var alarmAdapter: AlarmAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        Thread {
            val alarmList = AppDatabase.getInstance(
                    this@ListActivity
            ).alarmDao().findAllAlarms()

            alarmAdapter = AlarmAdapter(this@ListActivity, alarmList)

            val layoutManager = LinearLayoutManager(this)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true

            runOnUiThread {
                recyclerAlarms.adapter = alarmAdapter
                recyclerAlarms.layoutManager = layoutManager

                val callback = ItemTouchHelperCallback(alarmAdapter)
                val touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(recyclerAlarms)
            }
        }.start()
    }
}

