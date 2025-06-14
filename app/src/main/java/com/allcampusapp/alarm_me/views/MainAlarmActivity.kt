package com.allcampusapp.alarm_me.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.adapter.AlarmAdapter
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.rotateView360Ext
import com.allcampusapp.alarm_me.extension.setDelay
import com.allcampusapp.alarm_me.extension.setImageDrawableExt
import com.allcampusapp.alarm_me.extension.slideInFromRight
import com.allcampusapp.alarm_me.extension.slideOutToRight
import com.allcampusapp.alarm_me.extension.stopVibration
import com.allcampusapp.alarm_me.extension.vibrateIndefinitely
import com.allcampusapp.alarm_me.extension.visible
import com.allcampusapp.alarm_me.model.AlarmModel
import com.allcampusapp.alarm_me.services.notification.AlarmNotification
import com.allcampusapp.alarm_me.utility.GlobalUtils
import com.allcampusapp.alarm_me.utility.LocalStorageUtils
import com.allcampusapp.alarm_me.utility.SoundUtils

class MainAlarmActivity : AppCompatActivity() {

    private val alarmAdapter : AlarmAdapter by lazy {
        AlarmAdapter(this, LocalStorageUtils.getAllAlarm(this)) { alarmM, position ->
            val intent = Intent(this, SetAlarmActivity::class.java)
            GlobalUtils.alarmModelClick = alarmM
            intent.putExtra(KVal.KEY_POSITION, position)
            intent.putExtra(KVal.INTENT_PURPOSE, KVal.EDIT)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_alarm)

        header()

        val noAlarmYetTV = findViewById<TextView>(R.id.noAlarmYetTV)
        val recyclerView = findViewById<RecyclerView>(R.id.alarmRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (LocalStorageUtils.getAllAlarm(this).size < 1) noAlarmYetTV.visible()
        noAlarmYetTV.setOnClickListener {
            val intent = Intent(this, SetAlarmActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView.adapter = alarmAdapter

        intentNotify(intent)
    }

    private fun header() {
        val headingInclude = findViewById<RelativeLayout>(R.id.headingInclude)
        val headTitle = headingInclude.findViewById<TextView>(R.id.headingTitle)
        val logoIV = headingInclude.findViewById<ImageView>(R.id.backArrowIV)
        val iconEndIV = headingInclude.findViewById<ImageView>(R.id.iconEndIV)
        val iconMiddleIV = headingInclude.findViewById<ImageView>(R.id.iconMiddleIV)

        headTitle.text = getString(R.string.app_name)
        iconEndIV.setImageDrawableExt(R.drawable.baseline_add_circle_outline_24)
        iconEndIV.visible()
        iconMiddleIV.setImageDrawableExt(R.drawable.delete_all_icon)
        if (LocalStorageUtils.getAllAlarm(this).size > 1) iconMiddleIV.visible()

        logoIV.setImageDrawableExt(R.drawable.baseline_alarm_24)

        iconEndIV.setOnClickListener {
            it.rotateView360Ext()
            val intent = Intent(this, SetAlarmActivity::class.java)
            startActivity(intent)
            finish()
        }

        iconMiddleIV.setOnClickListener {
            noticeViewStub()
        }

    }

    private var noticeView : View? = null

    private fun noticeViewStub() {

        if (noticeView != null) {
            noticeView?.slideInFromRight(300)
            return
        }

        val viewStub = findViewById<ViewStub>(R.id.noticeViewStub)
        noticeView = viewStub.inflate()

        noticeView?.let {
            val closeIV = it.findViewById<ImageView>(R.id.closeIV)
            val yesTV = it.findViewById<TextView>(R.id.yesTV)
            val cancelTV = it.findViewById<TextView>(R.id.cancelTV)

            closeIV?.setOnClickListener { noticeView?.slideOutToRight(300) }
            yesTV?.setOnClickListener {
                noticeView?.slideOutToRight(300)
                alarmAdapter.emptyList(this)
            }
            cancelTV?.setOnClickListener { noticeView?.slideOutToRight(300) }

        }

        noticeView?.setOnClickListener { noticeView?.slideOutToRight(300) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intentNotify(intent)
    }

    private fun intentNotify(intent: Intent?) {
        if (intent == null) return
        setDelay (1000) {
            val intentPurpose = intent.getStringExtra(KVal.INTENT_PURPOSE)
            if (intentPurpose == KVal.ALARM_LABEL) {
                val alarmId = intent.getIntExtra(KVal.ALARM_ID, 0)
                val alarmM = LocalStorageUtils.getAllAlarm(this).find { it.id == alarmId } ?: AlarmModel()

                AlarmNotification.cancelNotification(this, alarmId)

                SoundUtils.resetOneTimeAlarm(this, alarmM)
                SoundUtils.cancelAutoSnooze(this, alarmM)

                alarmAdapter.resetAlarmUI()

                vibrateIndefinitely(100)
                stopVibration()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundUtils.stopAlarmSound()
        stopVibration()
    }

}
