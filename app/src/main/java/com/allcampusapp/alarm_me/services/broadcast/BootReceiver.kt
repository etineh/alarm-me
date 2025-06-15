package com.allcampusapp.alarm_me.services.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allcampusapp.alarm_me.extension.setDelay
import com.allcampusapp.alarm_me.utility.SoundUtils

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule them with AlarmManager
            Thread {
                SoundUtils.cancelAllAlarms(context)
                setDelay(1000) { SoundUtils.scheduleAllAlarms(context)  }
            }.start()
        }
    }
}
