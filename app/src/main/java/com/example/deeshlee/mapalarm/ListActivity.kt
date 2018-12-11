package com.example.deeshlee.mapalarm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import com.example.deeshlee.mapalarm.adapter.AlarmAdapter
import com.example.deeshlee.mapalarm.R
import com.example.deeshlee.mapalarm.data.Alarm
import com.example.deeshlee.mapalarm.data.AppDatabase
import com.example.deeshlee.mapalarm.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_list.*


class ListActivity : AppCompatActivity() {

    private lateinit var alarmAdapter: AlarmAdapter

    private lateinit var markersToDelete: ArrayList<String>

    private lateinit var alarmList: List<Alarm>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        fabDeleteAll.setOnClickListener {
            alarmAdapter.deleteAllAlarms()
        }
        initRecyclerView()

        markersToDelete = ArrayList()

        val returnIntent = Intent()
        returnIntent.putExtra("markersToDelete", markersToDelete);
        setResult(Activity.RESULT_OK,returnIntent)
    }

    private fun initRecyclerView() {
        Thread {
            alarmList = AppDatabase.getInstance(
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

    fun addMarkerToDelete(markerId: String){
        markersToDelete.add(markerId)
    }


}
