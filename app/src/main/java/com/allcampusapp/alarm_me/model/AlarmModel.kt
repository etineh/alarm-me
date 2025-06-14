package com.allcampusapp.alarm_me.model

import com.allcampusapp.alarm_me.constant.KVal


data class AlarmModel(
    val id: Int = generateAlarmId(),  // Unique per alarm
    var alarmIsActive: Boolean = true,
    var sound: RingtoneItemM = RingtoneItemM(),
    var dayModel: DayModel = DayModel(),
    var snoozeOn: Boolean = true,
    var snoozeDur: Int = 5,
    var label: String? = null,
    var type: String = KVal.AM, // AM or PM
    var format: String = KVal.H12,  // 24H or 12H
    var time: String = "01:00"
)

data class DayModel(
    var mon: Boolean = false,
    var tue: Boolean = false,
    var wed: Boolean = false,
    var thur: Boolean = false,
    var fri: Boolean = false,
    var sat: Boolean = false,
    var sun: Boolean = false,
    var allDay: Boolean = false
)

data class RingtoneItemM(
    val title: String = "",
    val uri: String? = null,
    var isSelected : Boolean = false,
    var isPlaying : Boolean = false
)

fun generateAlarmId(): Int {
    // You can generate a random ID or hash on creation
    return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
}

