package com.allcampusapp.alarm_me.utility

import android.content.Context
import android.content.SharedPreferences
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.model.AlarmModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LocalStorageUtils {

    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(KVal.APP_PREFERENCE, Context.MODE_PRIVATE)
    }

    private fun saveAllAlarm(context: Context, alarms: List<AlarmModel>) {
        val json = gson.toJson(alarms)
        getPrefs(context).edit().putString(KVal.KEY_ALARMS, json).apply()
    }

    fun getAllAlarm(context: Context): MutableList<AlarmModel> {
        val json = getPrefs(context).getString(KVal.KEY_ALARMS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<AlarmModel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addAlarm(context: Context, alarm: AlarmModel) {
        val alarms = getAllAlarm(context)
        alarms.add(0, alarm)
        saveAllAlarm(context, alarms)
    }

    fun updateAlarm(context: Context, index: Int, updatedAlarm: AlarmModel) {
        val alarms = getAllAlarm(context)
        if (index in alarms.indices) {
            alarms[index] = updatedAlarm
            saveAllAlarm(context, alarms)
        }
    }

    fun deleteAlarm(context: Context, index: Int) {
        val alarms = getAllAlarm(context)
        if (index in alarms.indices) {
            alarms.removeAt(index)
            saveAllAlarm(context, alarms)
        }
    }

    fun clearAllAlarm(context: Context) {
        getAllAlarm(context).forEach { alarmModel -> SoundUtils.cancelAlarm(context, alarmModel) }
        getPrefs(context).edit().remove(KVal.KEY_ALARMS).apply()
    }

}