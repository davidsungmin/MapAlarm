package com.example.mitchellpatton.mapalarm

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.example.mitchellpatton.mapalarm.GeofenceTransitionsIntentService.Companion.KEY_PLAY_AUDIO
import com.example.mitchellpatton.mapalarm.adapter.AlarmAdapter
import com.example.mitchellpatton.mapalarm.adapter.GeofenceAdapter
import com.example.mitchellpatton.mapalarm.data.Alarm
import com.example.mitchellpatton.mapalarm.data.AppDatabase
import com.example.mitchellpatton.mapalarm.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_list.*
import android.media.Ringtone
import android.widget.Toast
import com.example.mitchellpatton.mapalarm.GeofenceTransitionsIntentService.Companion.KEY_ALARM


class ListActivity : AppCompatActivity() {

    private lateinit var alarmAdapter: AlarmAdapter

    private lateinit var markersToDelete: ArrayList<String>

    private lateinit var alarmList: List<Alarm>

    private lateinit var geofenceAdapter: GeofenceAdapter

    private val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    private lateinit var sound : Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        initRecyclerView()

        sound = RingtoneManager.getRingtone(applicationContext, alert)

        if (intent.hasExtra(KEY_PLAY_AUDIO) && !sound.isPlaying){
            btnStopAlarm.visibility = View.VISIBLE
            sound.play()
            Toast.makeText(this@ListActivity, getString(R.string.display_address) + intent.getStringExtra(KEY_PLAY_AUDIO), Toast.LENGTH_LONG).show()
        }

        markersToDelete = ArrayList()

        btnDeleteAll.setOnClickListener {
            alarmAdapter.deleteAllAlarms()
        }

//        btnStopAlarm.setOnClickListener {
//            btnStopAlarm.visibility = View.GONE
//            stopSound()
//            if(intent.hasExtra(KEY_ALARM)){
//                val markerId = intent.getStringExtra(KEY_ALARM)
//                alarmAdapter.deleteById(markerId)
//            }
//        }

        val returnIntent = Intent()
        returnIntent.putExtra("markersToDelete", markersToDelete)
        setResult(Activity.RESULT_OK,returnIntent)
    }

    private fun initRecyclerView() {
        Thread {
            alarmList = AppDatabase.getInstance(
                    this@ListActivity
            ).alarmDao().findAllAlarms()

            alarmAdapter = AlarmAdapter(this@ListActivity, alarmList)

           // geofenceAdapter = GeofenceAdapter(this@ListActivity, alarmList)

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

    fun addAllMarkersToDelete(markerIds: MutableList<String>) {
        markersToDelete.addAll(markerIds)
    }

//    fun deleteGeofence(markerId: String){
//        geofenceAdapter.removeGeofence(markerId)
//    }

    override fun onStop() {
        stopSound()
        super.onStop()
    }

    fun stopSound(){
        if (sound.isPlaying){
            sound.stop()
        }
    }

    //fun deleteAllGeofence(requestidList: MutableList<String>){
    //    geofenceAdapter.removeAllGeofence(requestidList)
    //}

}
