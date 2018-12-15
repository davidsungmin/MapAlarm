package com.example.mitchellpatton.mapalarm

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mitchellpatton.mapalarm.data.Alarm
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.dialog_alarm.view.*
import java.lang.RuntimeException

class AlarmDialog : DialogFragment() {

    interface AlarmHandler {
        fun alarmStopped(alarm: Alarm)
    }
    private val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    private lateinit var sound: Ringtone

    private lateinit var alarmHandler: AlarmHandler

    private var markerId = ""


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is AlarmHandler) {
            alarmHandler = context
        } else {
            throw RuntimeException(
                    "This class does not implement AlarmHandler")
        }
        sound = RingtoneManager.getRingtone(context, alert)
    }

    private lateinit var tvAlarm: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Alarm")

        val rootView = requireActivity().layoutInflater.inflate(
                R.layout.dialog_alarm, null
        )
        tvAlarm = rootView.tvAlarm

        builder.setPositiveButton("Stop Alarm"){
            builder, which ->DialogInterface.BUTTON_POSITIVE
        }
        builder.setView(rootView)

        val arguments = this.arguments

        buildDialog(arguments, builder)

        return builder.create()
    }


    override fun onResume() {
        super.onResume()

        val positiveButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
           onPositiveButtonClick(markerId)
        }
        sound.play()
    }

    fun buildDialog(arguments: Bundle?, builder: AlertDialog.Builder) {
        val alarm = arguments!!.getSerializable(
                    "Alarm"
            ) as Alarm
        tvAlarm.text = getString(R.string.display_address) + alarm.alarmAddress
        markerId = alarm.markerId
        builder.setTitle("Alarm")
    }

    private fun onPositiveButtonClick(markerId: String){
        stopSound()
        dialog.dismiss()
       // (context as MainActivity).delete_alarm(markerId)
    }

    fun stopSound(){
        if (sound.isPlaying){
            sound.stop()
        }
    }

//    fun onPositiveButtonClick() {
//        (context as ListActivity).delete
//    }
}
