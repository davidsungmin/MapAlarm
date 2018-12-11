package com.example.mitchellpatton.mapalarm.touch

interface ItemTouchHelperAdapter {
    fun onDismissed(position: Int)
    fun onAlarmMoved(fromPosition: Int, toPosition: Int)
}