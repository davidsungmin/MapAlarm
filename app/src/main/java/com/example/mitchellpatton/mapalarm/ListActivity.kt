package com.example.mitchellpatton.mapalarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import com.example.mitchellpatton.mapalarm.adapter.AlarmAdapter
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.example.mitchellpatton.mapalarm.data.AppDatabase
import com.example.mitchellpatton.mapalarm.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_list.*


class ListActivity : AppCompatActivity() {

    private lateinit var alarmAdapter: AlarmAdapter

    private lateinit var markersToDelete: ArrayList<String>

    private lateinit var alarmList: List<Alarm>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        initRecyclerView()

        markersToDelete = ArrayList()

        btnDeleteAll.setOnClickListener {
            deleteAll()
        }

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

    fun deleteAll(){
        Thread {
            AppDatabase.getInstance(this@ListActivity).alarmDao().deleteAll()
            runOnUiThread {
                alarmAdapter.deleteAllAlarms()
            }
        }.start()
    }


}
