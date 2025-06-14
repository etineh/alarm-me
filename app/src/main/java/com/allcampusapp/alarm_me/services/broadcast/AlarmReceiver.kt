package com.allcampusapp.alarm_me.services.broadcast

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.model.AlarmModel
import com.allcampusapp.alarm_me.permission.AppPermission
import com.allcampusapp.alarm_me.services.AlarmService
import com.allcampusapp.alarm_me.services.notification.AlarmNotification
import com.allcampusapp.alarm_me.utility.LocalStorageUtils
import com.allcampusapp.alarm_me.utility.SoundUtils

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(KVal.ALARM_ID, 0)
        val alarmM = LocalStorageUtils.getAllAlarm(context).find { it.id == alarmId } ?: AlarmModel()

        when (intent.action) {
            KVal.ACTION_STOP -> { // Stop alarm sound and cancel notification
                SoundUtils.cancelAutoSnooze(context, alarmM)
                SoundUtils.stopAlarmSound()
                SoundUtils.stopVibrationOnNotify(context, alarmM)
                SoundUtils.resetOneTimeAlarm(context, alarmM)
                AlarmNotification.cancelNotification(context, alarmId)
                return
            }
            KVal.ACTION_SNOOZE -> { // Stop alarm sound and reschedule alarm for snooze duration
                SoundUtils.cancelAutoSnooze(context, alarmM)
                SoundUtils.stopAlarmSound()
                SoundUtils.stopVibrationOnNotify(context, alarmM)
                SoundUtils.snoozeAlarm(context, alarmM)
                AlarmNotification.cancelNotification(context, alarmId)
                return
            }
            else -> {
                AlarmNotification.showAlarmNotification(context, alarmM)
                if ( AppPermission.isNotificationOk(context) /** && alarmNotificationIsOk **/ ) {
                    playTone(context, alarmM)
                    scheduleAutoSnooze(context, alarmM)
                }
            }
        }
    }

    private fun playTone(context: Context, alarmModel: AlarmModel) {
        if (alarmModel.sound.title == KVal.VIBRATE_ONLY) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra(KVal.INTENT_PURPOSE, KVal.PLAY_ALARM)
                putExtra(KVal.ALARM_ID, alarmModel.id)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
            return
        }

        val soundUriString = alarmModel.sound.uri
        val soundUri = if (soundUriString != null) Uri.parse(soundUriString)
        else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        // Play the ringtone
        SoundUtils.playAlarm(context, soundUri)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAutoSnooze(context: Context, alarmModel: AlarmModel) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AutoSnoozeReceiver::class.java).apply {
            putExtra(KVal.ALARM_ID, alarmModel.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmModel.id + 999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000 // stop alarm at 5min
        if (!alarmModel.snoozeOn) return

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

    }


}

