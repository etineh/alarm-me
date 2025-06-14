package com.allcampusapp.alarm_me.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.stopVibration
import com.allcampusapp.alarm_me.extension.vibrateIndefinitely
import com.allcampusapp.alarm_me.model.AlarmModel
import com.allcampusapp.alarm_me.utility.LocalStorageUtils
import com.allcampusapp.alarm_me.services.notification.AlarmNotification

class AlarmService : Service() {

    // enable background vibration sound

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(KVal.ALARM_ID, 0)
        val playOrPause = intent?.getStringExtra(KVal.INTENT_PURPOSE) ?: KVal.STOP_ALARM
        val alarmM = LocalStorageUtils.getAllAlarm(this).find { it.id == alarmId } ?: AlarmModel()

        //  ========== stop alarm button
        val stopPendingIntent = AlarmNotification.stopPendingIntent(this, alarmM)

        //  ============    snooze alarm button
        val snoozePendingIntent = AlarmNotification.snoozePendingIntent(this, alarmM)

        //  ==========  open alarm notification onclick
        val contentPendingIntent = AlarmNotification.contentPendingIntent(this, alarmM)

        val notification = AlarmNotification.buildNotification(
            this, alarmM, stopPendingIntent, snoozePendingIntent, contentPendingIntent
        )
        startForeground(KVal.ALARM_NOTIFICATION_ID + alarmM.id, notification)

        vibrateIndefinitely()
        if (playOrPause == KVal.STOP_ALARM) {
            stopVibration()
            stopAlarmService()
        }

        return START_STICKY
    }

    // Call this when you want to stop vibration and notification
    private fun stopAlarmService() {
        stopForeground(true) // removes the notification
        stopSelf() // stops the service
    }

    override fun onBind(intent: Intent?): IBinder? = null

}
