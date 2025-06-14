package com.allcampusapp.alarm_me.services.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.setDelay
import com.allcampusapp.alarm_me.services.notification.AlarmNotification
import com.allcampusapp.alarm_me.utility.LocalStorageUtils
import com.allcampusapp.alarm_me.utility.SoundUtils

class AutoSnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(KVal.ALARM_ID, 0)
        val alarmModel = LocalStorageUtils.getAllAlarm(context).find { it.id == alarmId } ?: return

        SoundUtils.stopAlarmSound()
        SoundUtils.stopVibrationOnNotify(context, alarmModel)

        setDelay (200) {
            AlarmNotification.showAlarmNotification(context, alarmModel, true)
        }

        // Get snooze count from map
        val currentCount = SoundUtils.alarmSnoozeCountMap[alarmId] ?: 0

        if (alarmModel.snoozeOn && currentCount < 5) {
            // Increment and update the map
            SoundUtils.alarmSnoozeCountMap[alarmId] = currentCount + 1

            // Snooze the alarm
            SoundUtils.snoozeAlarm(context, alarmModel)
            if (currentCount == 4) SoundUtils.resetOneTimeAlarm(context, alarmModel)

        } else {
            // Clean up
            SoundUtils.alarmSnoozeCountMap.remove(alarmId)
            SoundUtils.resetOneTimeAlarm(context, alarmModel)
            SoundUtils.cancelAutoSnooze(context, alarmModel)
        }
    }
}
