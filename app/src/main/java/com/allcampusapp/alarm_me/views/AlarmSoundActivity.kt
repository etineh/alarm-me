package com.allcampusapp.alarm_me.views

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.adapter.RingtoneAdapter
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.finishActivityWithHighlight
import com.allcampusapp.alarm_me.extension.setDelay
import com.allcampusapp.alarm_me.extension.setImageDrawableExt
import com.allcampusapp.alarm_me.extension.stopVibration
import com.allcampusapp.alarm_me.extension.vibrateIndefinitely
import com.allcampusapp.alarm_me.utility.ListenerManagerUtil
import com.allcampusapp.alarm_me.utility.SoundUtils

class AlarmSoundActivity : AppCompatActivity() {

    private lateinit var soundRecycler : RecyclerView

    private var currentRingtone: Ringtone? = null


    private val ringtoneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter(
            SoundUtils.alarmSounds,
            onSelectSound = { selected ->
                ListenerManagerUtil.signalListenerBySetAlarmActivity?.onAlarmSound(selected)
            },
            onPlaySound = { selected ->
                // Stop and release previous ringtone if any
                currentRingtone?.stop()
                this.stopVibration()

                if (selected.title == KVal.VIBRATE_ONLY) {
                    if (selected.isPlaying) this.vibrateIndefinitely()
                }
                else {    // Create new ringtone
                    currentRingtone = RingtoneManager.getRingtone(this, Uri.parse(selected.uri))
                    if (selected.isPlaying) currentRingtone?.play()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_sound)

        header()

        soundRecycler = findViewById(R.id.soundRecycler)
        soundRecycler.layoutManager = LinearLayoutManager(this)

        SoundUtils.alarmSounds.forEach { model -> model.isPlaying = false } // change all to pause button

        setDelay (300) {
            soundRecycler.adapter = ringtoneAdapter

            val soundTitle = intent.getStringExtra(KVal.SOUND_TITLE)

            // assign the previous saved selected sound model
            val index = SoundUtils.alarmSounds.indexOfFirst { ringM -> ringM.title == soundTitle }
            ringtoneAdapter.previousSelectedIndex = index
            ringtoneAdapter.previousSoundSelected = SoundUtils.alarmSounds[index]

        }


    }

    private fun header() {
        val headingInclude = findViewById<RelativeLayout>(R.id.headingInclude)
        val headTitle = headingInclude.findViewById<TextView>(R.id.headingTitle)
        val arrowBack = headingInclude.findViewById<ImageView>(R.id.backArrowIV)

        headTitle.text = getString(R.string.selectSound)
        arrowBack.setImageDrawableExt(R.drawable.baseline_arrow_back_24)

        arrowBack.finishActivityWithHighlight(this)
    }


    override fun onPause() {
        super.onPause()
        currentRingtone?.stop() // Stop and release previous ringtone if any
        this.stopVibration()
//        selectedSound.isPlaying = false
    }

}