package com.allcampusapp.alarm_me.utility

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.convertTo24Hour
import com.allcampusapp.alarm_me.extension.daysActiveIndex
import com.allcampusapp.alarm_me.extension.stopVibration
import com.allcampusapp.alarm_me.extension.vibrateIndefinitely
import com.allcampusapp.alarm_me.model.AlarmModel
import com.allcampusapp.alarm_me.model.RingtoneItemM
import com.allcampusapp.alarm_me.permission.AppPermission
import com.allcampusapp.alarm_me.services.broadcast.AlarmReceiver
import com.allcampusapp.alarm_me.services.AlarmService
import com.allcampusapp.alarm_me.services.broadcast.AutoSnoozeReceiver
import java.util.Calendar

object SoundUtils {

    var alarmSounds = mutableListOf<RingtoneItemM>()

    fun getAlarmSounds(context: Context): MutableList<RingtoneItemM> {
        val ringtoneList = mutableListOf<RingtoneItemM>()

        // Add the "Vibrate only" option at the top
        ringtoneList.add(
            RingtoneItemM(
                title = KVal.VIBRATE_ONLY,
                uri = null, // or Uri.EMPTY
            )
        )

        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_ALARM) // You can also try TYPE_RINGTONE or TYPE_NOTIFICATION
        val cursor = manager.cursor

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = manager.getRingtoneUri(cursor.position)
            ringtoneList.add(RingtoneItemM(title, uri.toString()))
        }

        return ringtoneList
    }

    fun scheduleAllAlarms(context: Context, alarms: List<AlarmModel>) {
        for (alarm in alarms) {
            if (alarm.alarmIsActive) {
                scheduleAlarm(context, alarm)
            } else {
//                cancelAlarm(context, alarm)
            }
        }
    }


    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(context: Context, alarmModel: AlarmModel) {
        if (!alarmModel.alarmIsActive) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(KVal.ALARM_ID, alarmModel.id)
        }

        val requestCode = alarmModel.id  // Use your unique alarm id here

        val pendingIntent = PendingIntent.getBroadcast( context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE )

        val calendar = Calendar.getInstance()

        val (hour24, minute) = alarmModel.convertTo24Hour()

        // Set time first
        calendar.set(Calendar.HOUR_OF_DAY, hour24)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Days of week in Calendar constants
        val daysOfWeek = listOf(
            Calendar.MONDAY to alarmModel.dayModel.mon,
            Calendar.TUESDAY to alarmModel.dayModel.tue,
            Calendar.WEDNESDAY to alarmModel.dayModel.wed,
            Calendar.THURSDAY to alarmModel.dayModel.thur,
            Calendar.FRIDAY to alarmModel.dayModel.fri,
            Calendar.SATURDAY to alarmModel.dayModel.sat,
            Calendar.SUNDAY to alarmModel.dayModel.sun
        )

        val today = calendar.get(Calendar.DAY_OF_WEEK)

        // Find the soonest next day alarm should trigger
        val daysFromToday = (0 until 7).map { offset ->
            val day = (today + offset - 1) % 7 + 1 // map to 1-7 for Calendar
            Pair(offset, daysOfWeek.firstOrNull { it.first == day }?.second ?: false)
        }

        // Find the first day enabled in next 7 days
        val nextDayOffset = daysFromToday.firstOrNull { it.second }?.first ?: -1

        if (nextDayOffset == -1) {
            // No days selected - schedule once for today if time not passed, else ignore or schedule for next day
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, nextDayOffset)
            // If nextDayOffset == 0 (today), but time already passed, find next enabled day after today
            if (nextDayOffset == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
                val nextAfterToday = daysFromToday.drop(1).firstOrNull { it.second }
                if (nextAfterToday != null) {
                    calendar.add(Calendar.DAY_OF_YEAR, nextAfterToday.first)
                } else {
                    // No other days after today - schedule for next week same day
                    calendar.add(Calendar.DAY_OF_YEAR, 7)
                }
            }
        }

        if (AppPermission.ensureExactAlarmPermission(context)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun toggleAlarm(context: Context, alarmModel: AlarmModel, activate: Boolean) {
        if (activate) {
            scheduleAlarm(context, alarmModel)
        } else {
            cancelAlarm(context, alarmModel)
        }
    }

    fun cancelAlarm(context: Context, alarmModel: AlarmModel) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(KVal.ALARM_ID, alarmModel.id)
        }

        val requestCode = alarmModel.id

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        stopAlarmSound()
        context.vibrateIndefinitely(100)
        context.stopVibration()

        // reschedule all alarm again
        Thread { scheduleAllAlarms(context, LocalStorageUtils.getAllAlarm(context)) }.start()
    }

    fun cancelAutoSnooze(context: Context, alarmModel: AlarmModel) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AutoSnoozeReceiver::class.java).apply {
            putExtra(KVal.ALARM_ID, alarmModel.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmModel.id + 999,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        // Only cancel if pendingIntent exists
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private var alarmRingtone: Ringtone? = null

    fun playAlarm(context: Context, soundUri: Uri) {
        stopAlarmSound()
        alarmRingtone = RingtoneManager.getRingtone(context, soundUri)
        alarmRingtone?.play()
    }

    fun stopAlarmSound() {
        alarmRingtone?.stop()
        alarmRingtone = null
    }

    fun resetOneTimeAlarm(context: Context, alarmM: AlarmModel) {
        val dayIndexes = alarmM.daysActiveIndex()
        val indexOnStorage = LocalStorageUtils.getAllAlarm(context).indexOf(alarmM)

        if (dayIndexes.isEmpty()) {
            alarmM.alarmIsActive = false
            LocalStorageUtils.updateAlarm(context, indexOnStorage, alarmM)
            cancelAlarm(context, alarmM)
        }
        // reschedule all alarm again
        Thread { scheduleAllAlarms(context, LocalStorageUtils.getAllAlarm(context)) }.start()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun snoozeAlarm(context: Context, alarmModel: AlarmModel) {
        val snoozeTimeInMillis = System.currentTimeMillis() + alarmModel.snoozeDur * 60 * 1000

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(KVal.ALARM_ID, alarmModel.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeInMillis, pendingIntent)
    }

    fun stopVibrationOnNotify(context: Context, alarmModel: AlarmModel) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(KVal.INTENT_PURPOSE, KVal.STOP_ALARM)
            putExtra(KVal.ALARM_ID, alarmModel.id)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
        return
    }

    val alarmSnoozeCountMap = mutableMapOf<Int, Int>()

}