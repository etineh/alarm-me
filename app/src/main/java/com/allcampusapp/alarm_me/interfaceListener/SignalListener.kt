package com.allcampusapp.alarm_me.interfaceListener

import com.allcampusapp.alarm_me.model.RingtoneItemM

interface SignalListener {

    fun onAlarmSound(ringtoneItemM: RingtoneItemM) {}

}