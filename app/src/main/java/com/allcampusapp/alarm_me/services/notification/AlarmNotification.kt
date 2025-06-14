package com.allcampusapp.alarm_me.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.isNullExt
import com.allcampusapp.alarm_me.model.AlarmModel
import com.allcampusapp.alarm_me.views.MainAlarmActivity
import com.allcampusapp.alarm_me.services.broadcast.AlarmReceiver

object AlarmNotification {

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val descriptionText = "Channel for Alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(KVal.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null) // Disable default sound because you play ringtone manually
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAlarmNotification(context: Context, alarmM: AlarmModel, missed: Boolean = false) {

        //  ========== stop alarm button
        val stopPendingIntent = stopPendingIntent(context, alarmM)

        //  ============    snooze alarm button
        val snoozePendingIntent = snoozePendingIntent(context, alarmM)

        //  ==========  open alarm notification onclick
        val contentPendingIntent = contentPendingIntent(context, alarmM)

        createNotificationChannel(context)

        val notification = buildNotification(context, alarmM, stopPendingIntent, snoozePendingIntent, contentPendingIntent, missed)
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.notify(KVal.ALARM_NOTIFICATION_ID + alarmM.id, notification)  // unique notification id per alarm
    }

    fun buildNotification(context: Context, alarmM: AlarmModel, stopPendingIntent : PendingIntent,
                          snoozePendingIntent: PendingIntent, contentPendingIntent : PendingIntent, missed: Boolean = false)
    : Notification {

        val title = if (missed) {
            "${context.getString(R.string.missed)} ${context.getString(R.string.alarm)} @ ${alarmM.time} ${alarmM.type.takeIf { alarmM.format == KVal.H12 } ?: ""}"
        } else {
            "${context.getString(R.string.alarm)} @ ${alarmM.time} ${alarmM.type.takeIf { alarmM.format == KVal.H12 } ?: ""}"
        }

        val stopOrCancel = context.getString(R.string.cancel).takeIf { missed } ?: context.getString(R.string.stop)

        val builder = NotificationCompat.Builder(context, KVal.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(alarmM.label.takeIf { !it.isNullExt() } ?: context.getString(R.string.alarmRinging))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.baseline_airline_stops_24, stopOrCancel, stopPendingIntent)
            .setOngoing(true)

        if (alarmM.snoozeOn) {
            builder.addAction( R.drawable.baseline_adjust_24, context.getString(R.string.snooze), snoozePendingIntent )
        }

        return builder.build()
    }

    fun stopPendingIntent(context: Context, alarmM: AlarmModel) : PendingIntent {
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = KVal.ACTION_STOP
            putExtra(KVal.ALARM_ID, alarmM.id)  // So receiver knows which alarm to stop
        }
        return PendingIntent.getBroadcast(
            context,
            alarmM.id,  // unique request code here
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun snoozePendingIntent(context: Context, alarmM: AlarmModel) : PendingIntent {
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = KVal.ACTION_SNOOZE
            putExtra(KVal.ALARM_ID, alarmM.id)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmM.id + 1000,  // unique code different from stopIntent
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun contentPendingIntent(context: Context, alarmM: AlarmModel) : PendingIntent {
        val fullScreenIntent = Intent(context, MainAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KVal.INTENT_PURPOSE, KVal.ALARM_LABEL)
            putExtra(KVal.ALARM_ID, alarmM.id)
        }
        return PendingIntent.getActivity(
            context,
            alarmM.id + 2000,  // unique code for the activity
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelNotification(context: Context, alarmId: Int) {
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.cancel(KVal.ALARM_NOTIFICATION_ID + alarmId)
    }

}