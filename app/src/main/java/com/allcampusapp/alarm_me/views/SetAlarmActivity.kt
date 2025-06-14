package com.allcampusapp.alarm_me.views

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewStub
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.adapter.TimeAlarmAdapter
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.onBackPress
import com.allcampusapp.alarm_me.extension.rotateView360Ext
import com.allcampusapp.alarm_me.extension.setBackgroundDrawableExt
import com.allcampusapp.alarm_me.extension.setDelay
import com.allcampusapp.alarm_me.extension.setImageDrawableExt
import com.allcampusapp.alarm_me.extension.setTextColorExt
import com.allcampusapp.alarm_me.extension.slideInFromRight
import com.allcampusapp.alarm_me.extension.slideOutToRight
import com.allcampusapp.alarm_me.extension.toastLong
import com.allcampusapp.alarm_me.extension.visible
import com.allcampusapp.alarm_me.interfaceListener.SignalListener
import com.allcampusapp.alarm_me.model.AlarmModel
import com.allcampusapp.alarm_me.model.RingtoneItemM
import com.allcampusapp.alarm_me.permission.AppPermission
import com.allcampusapp.alarm_me.utility.GlobalUtils
import com.allcampusapp.alarm_me.utility.ListenerManagerUtil
import com.allcampusapp.alarm_me.utility.LocalStorageUtils
import com.allcampusapp.alarm_me.utility.SoundUtils


class SetAlarmActivity : AppCompatActivity(), SignalListener {

    private lateinit var hourRecycler : RecyclerView
    private lateinit var minRecycler : RecyclerView
    private lateinit var hourAdapter : TimeAlarmAdapter
    private lateinit var minAdapter : TimeAlarmAdapter

    private lateinit var hr12TV : TextView
    private lateinit var hr24TV : TextView
    private lateinit var pmTV : TextView
    private lateinit var amTV : TextView
    private lateinit var timeTypeLinearLayout: LinearLayout
    private lateinit var hourTV: TextView
    private lateinit var minTV: TextView
    private lateinit var typeTV: TextView

    private var optionView: View? = null

    private val middle = Int.MAX_VALUE / 2

    private var selectedHour = "01"
    private var selectedMin = "00"
    private var alarmModel = GlobalUtils.alarmModelClick

    private var soundTitle: String? = null

    private var editPosition : Int = -1
    private var intentPurpose : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alarm)

        ids()

        assignIntents()

        header()

        if (alarmModel.format == KVal.H12) setFormat12H()
        else setFormat24H()

        minListAdapterAndRecycler()

        if (alarmModel.type == KVal.AM) setTypeAM()
        else setTypePM()

        setDelay(200) { optionViewStub() }

        // ====     onClicks

        hr12TV.setOnClickListener { setFormat12H() }

        hr24TV.setOnClickListener { setFormat24H() }

        amTV.setOnClickListener { setTypeAM() }

        pmTV.setOnClickListener { setTypePM() }

        ListenerManagerUtil.signalListenerBySetAlarmActivity = this

        checkIfNotificationIsON()
    }

    private fun checkIfNotificationIsON() {
        if ( !AppPermission.isNotificationOk(this) ) AppPermission.requestNotification(this)
    }

    private fun ensureExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required if calling from non-activity context
                }
                startActivity(intent)
                return false // Permission not yet granted
            }
        }
        return true // Permission already granted or not needed
    }

    private fun assignIntents() {
        intentPurpose = intent.getStringExtra(KVal.INTENT_PURPOSE)
        editPosition = intent.getIntExtra(KVal.KEY_POSITION, -1)

        val (hour, min) = alarmModel.time.split(":")
        selectedHour = hour
        selectedMin = min
        hourTV.text = hour
        minTV.text = min
    }

    private fun header() {
        val headingInclude = findViewById<RelativeLayout>(R.id.headingInclude)
        val headTitle = headingInclude.findViewById<TextView>(R.id.headingTitle)
        val arrowBack = headingInclude.findViewById<ImageView>(R.id.backArrowIV)
        val iconEndIV = headingInclude.findViewById<ImageView>(R.id.iconEndIV)

        headTitle.text = getString(R.string.editAlarm).takeIf { intentPurpose == KVal.EDIT } ?: getString(R.string.setAlarm)
        arrowBack.setImageDrawableExt(R.drawable.baseline_cancel_24)
        iconEndIV.setImageDrawableExt(R.drawable.baseline_done_outline_24)
        iconEndIV.visible()

        iconEndIV.setOnClickListener {
            it.rotateView360Ext()
            if (!ensureExactAlarmPermission()) {
                toastLong(getString(R.string.failed_permit))
                return@setOnClickListener
            }

            val getLabel = labelET.text.toString()
            alarmModel.label = getLabel

            val selectedTime = "$selectedHour:$selectedMin"
            alarmModel.time = selectedTime

            if (intentPurpose == KVal.EDIT && editPosition != -1) {
                LocalStorageUtils.updateAlarm(this, editPosition, alarmModel)
                SoundUtils.cancelAlarm(this, alarmModel)
            }
            else LocalStorageUtils.addAlarm(this, alarmModel)

            SoundUtils.scheduleAlarm(this, alarmModel)  // for notification

            startActivity(Intent(this, MainAlarmActivity::class.java))
            finish()

            GlobalUtils.alarmModelClick = AlarmModel()
        }

        onBackPressedDispatcher.addCallback( onBackPress { backPress(arrowBack) } )

        arrowBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun ids() {
        hr12TV = findViewById(R.id.hr12TV)
        hr24TV = findViewById(R.id.hr24TV)
        pmTV = findViewById(R.id.pmTV)
        amTV = findViewById(R.id.amTV)
        timeTypeLinearLayout = findViewById(R.id.timeTypeLinearLayout)

        hourRecycler = findViewById(R.id.hourRecycler)
        minRecycler = findViewById(R.id.minRecycler)

        hourTV = findViewById(R.id.hourTV)
        minTV = findViewById(R.id.minTV)
        typeTV = findViewById(R.id.typeTV)
//        amTV = findViewById(R.id.amTV)
    }

    private lateinit var sunTV : TextView
    private lateinit var monTV : TextView
    private lateinit var tueTV : TextView
    private lateinit var wedTV : TextView
    private lateinit var thurTV : TextView
    private lateinit var friTV : TextView
    private lateinit var satTV : TextView
    private lateinit var dailyCheckBox : CheckBox
    private lateinit var snoozeSwitch : Switch
    private lateinit var durationSpinner : Spinner
    private lateinit var labelET : EditText
    private lateinit var soundTypeTV : TextView


    private fun optionViewStub() {
        if (optionView != null) {
            optionView?.visible()
            return
        }

        val viewStub = findViewById<ViewStub>(R.id.optionVS)
        optionView = viewStub.inflate()

        optionView?.let {
            dailyCheckBox = it.findViewById(R.id.dailyCheckBox)
            snoozeSwitch = it.findViewById(R.id.snoozeSwitch)
            durationSpinner = it.findViewById(R.id.durationSpinner)
            labelET = it.findViewById(R.id.labelET)
            sunTV = it.findViewById(R.id.sunTV)
            monTV = it.findViewById(R.id.monTV)
            tueTV = it.findViewById(R.id.tueTV)
            wedTV = it.findViewById(R.id.wedTV)
            thurTV = it.findViewById(R.id.thurTV)
            friTV = it.findViewById(R.id.friTV)
            satTV = it.findViewById(R.id.satTV)
            val soundConst = it.findViewById<ConstraintLayout>(R.id.soundConstr)
            soundTypeTV = it.findViewById(R.id.soundTypeTV)

            setDetails()

            soundConst.setOnClickListener {
                moveSelectedSoundTo2nd()
                val intent = Intent(this, AlarmSoundActivity::class.java)
                intent.putExtra(KVal.SOUND_TITLE, soundTitle)
                startActivity(intent)
            }

            setupDurationSpinner(durationSpinner) { selectedMinutes ->
                alarmModel.snoozeDur = selectedMinutes
            }

            dailyCheckBox.setOnClickListener{ checkAllDays() }

            selectDayOptionOnclick()

            activateSnoozeOnclick()

        }

    }

    private fun moveSelectedSoundTo2nd() {
        val selectedModelIndex = SoundUtils.alarmSounds.indexOfFirst { it.isSelected }

        if (selectedModelIndex != -1) {
            val selectedModel = SoundUtils.alarmSounds.removeAt(selectedModelIndex)
            SoundUtils.alarmSounds.add(1, selectedModel)
        }
    }

    private fun setDetails(){
        // set daily check box
        if (alarmModel.dayModel.allDay)  dailyCheckBox.isChecked = true

        // set alarm snooze on or off
        if (alarmModel.snoozeOn) {
            snoozeSwitch.isChecked = true
            snoozeSwitch.setBackgroundDrawableExt(R.drawable.round_radius_blue100)
            snoozeSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        }

        highlightAllActiveDays()

        // set the sound alarm
        SoundUtils.alarmSounds = SoundUtils.getAlarmSounds(this)

        val index = SoundUtils.alarmSounds.indexOfFirst { ringM ->
            ringM.title == alarmModel.sound.title && ringM.uri == alarmModel.sound.uri
        }

        val selectedItem = if (index != -1) {
            SoundUtils.alarmSounds[index].apply { isSelected = true }
        } else {
            SoundUtils.alarmSounds.getOrNull(3)?.apply { isSelected = true }
        }

        selectedItem?.let { ringM ->
            soundTypeTV.text = ringM.title
            soundTitle = ringM.title
        }

        // set label
        labelET.setText(alarmModel.label)
    }

    private val dayViewMap: Map<String, TextView> by lazy {
        mapOf(
            KVal.SUN to sunTV,
            KVal.MON to monTV,
            KVal.TUE to tueTV,
            KVal.WED to wedTV,
            KVal.THUR to thurTV,
            KVal.FRI to friTV,
            KVal.SAT to satTV
        )
    }

    private fun setupDurationSpinner(spinner: Spinner, onDurationSelected: (Int) -> Unit) {
        val durations = (5..30 step 5).toList() // [5, 10, 15, 20, 25, 30]
        val adapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item,
            durations.map { "$it ${getString(R.string.mins)}" })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDuration = durations[position]
                onDurationSelected(selectedDuration) // Call your logic with the selected value
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // fetch previous duration and set the spinner with it
        val previousDur = alarmModel.snoozeDur
        val index = durations.indexOf(previousDur)
        if (index >= 0) {
            durationSpinner.setSelection(index)
        }
    }

    private fun activateSnoozeOnclick() {
        snoozeSwitch.setOnClickListener {
            val isChecked = snoozeSwitch.isChecked
            alarmModel.snoozeOn = isChecked

            if (isChecked) {
                snoozeSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                snoozeSwitch.setBackgroundDrawableExt(R.drawable.round_radius_blue100)
            } else {
                snoozeSwitch.setBackgroundDrawableExt(R.drawable.radius_card_color100)
                snoozeSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.textColor))
            }
        }
    }

    private fun selectDayOptionOnclick() {
        monTV.setOnClickListener { v-> actionOnClick(v, KVal.MON, true) }
        tueTV.setOnClickListener { v-> actionOnClick(v, KVal.TUE, true) }
        wedTV.setOnClickListener { v-> actionOnClick(v, KVal.WED, true) }
        thurTV.setOnClickListener { v-> actionOnClick(v, KVal.THUR, true) }
        friTV.setOnClickListener { v-> actionOnClick(v, KVal.FRI, true) }
        satTV.setOnClickListener { v-> actionOnClick(v, KVal.SAT, true) }
        sunTV.setOnClickListener { v-> actionOnClick(v, KVal.SUN, true) }
    }

    private fun actionOnClick(view: View, day: String, uncheckBox: Boolean = false, toggle: Boolean = true) {
        alarmModel.dayModel.let { model ->
            val dayMap = mapOf(
                KVal.MON to model::mon,
                KVal.TUE to model::tue,
                KVal.WED to model::wed,
                KVal.THUR to model::thur,
                KVal.FRI to model::fri,
                KVal.SAT to model::sat,
                KVal.SUN to model::sun,
                KVal.ALL_DAY to model::allDay
            )

            dayMap[day]?.apply {
                if (toggle) set(!get()) // toggle the boolean
                toggleHighLightDay(view, get())
            }

            if (uncheckBox) {
                dailyCheckBox.isChecked = false
                alarmModel.dayModel.allDay = false
            }
        }
    }

    private fun checkAllDays() {
        val isChecked = dailyCheckBox.isChecked

        alarmModel.dayModel.apply {
            mon = isChecked; tue = isChecked; wed = isChecked; thur = isChecked
            fri = isChecked; sat = isChecked; sun = isChecked; allDay = isChecked
        }

        dayViewMap.forEach { (day, view) -> actionOnClick(view, day, toggle = false) }
    }

    private fun highlightAllActiveDays() {
        toggleHighLightDay(sunTV, alarmModel.dayModel.sun)
        toggleHighLightDay(monTV, alarmModel.dayModel.mon)
        toggleHighLightDay(tueTV, alarmModel.dayModel.tue)
        toggleHighLightDay(wedTV, alarmModel.dayModel.wed)
        toggleHighLightDay(thurTV, alarmModel.dayModel.thur)
        toggleHighLightDay(friTV, alarmModel.dayModel.fri)
        toggleHighLightDay(satTV, alarmModel.dayModel.sat)
    }

    private fun toggleHighLightDay(view: View, alarmDay: Boolean) {
        view.setBackgroundDrawableExt(R.drawable.radius_card_color100)
        (view as TextView).setTextColorExt(R.color.textColor)
        if (alarmDay) {
            view.setBackgroundDrawableExt(R.drawable.round_radius_blue100)
            view.setTextColorExt(R.color.white)
        }
    }

    private fun setFormat12H() {
            alarmModel.format = KVal.H12
            hr12TV.setBackgroundDrawableExt(R.drawable.round_radius_blue10)
            hr12TV.setTextColorExt(R.color.white)
            hr24TV.setBackgroundDrawableExt(0)
            hr24TV.setTextColorExt(R.color.textColor)
            timeTypeLinearLayout.slideInFromRight()
            typeTV.slideInFromRight(300)

            hourRecycler.adapter = null
            hourListAdapterAndRecycler(get12HourList())
    }

    private fun setFormat24H() {
        alarmModel.format = KVal.H24
        hr24TV.setBackgroundDrawableExt(R.drawable.round_radius_blue10)
        hr24TV.setTextColorExt(R.color.white)
        hr12TV.setBackgroundDrawableExt(0)
        hr12TV.setTextColorExt(R.color.textColor)
        timeTypeLinearLayout.slideOutToRight(300)
        typeTV.slideOutToRight(300)

        hourRecycler.adapter = null
        hourListAdapterAndRecycler(get24HourList())
    }

    private fun setTypeAM() {
        if (alarmModel.format == KVal.H12) typeTV.text = KVal.AM
        alarmModel.type = KVal.AM
        amTV.setBackgroundDrawableExt(R.drawable.round_radius_blue10)
        pmTV.setBackgroundDrawableExt(0)
        amTV.setTextColorExt(R.color.white)
        pmTV.setTextColorExt(R.color.textColor)
    }

    private fun setTypePM() {
        if (alarmModel.format == KVal.H12) typeTV.text = KVal.PM
        alarmModel.type = KVal.PM
        pmTV.setBackgroundDrawableExt(R.drawable.round_radius_blue10)
        amTV.setBackgroundDrawableExt(0)
        pmTV.setTextColorExt(R.color.white)
        amTV.setTextColorExt(R.color.textColor)
    }

    private fun hourListAdapterAndRecycler(hourList: List<String>) {
        hourRecycler.layoutManager = LinearLayoutManager(this)

        hourAdapter = TimeAlarmAdapter(hourList, this) { timeHighlighted ->
            scrollToPreviousTime(timeHighlighted, hourList, hourRecycler)
            selectedHour = timeHighlighted
            hourTV.text = timeHighlighted
        }
        hourRecycler.adapter = hourAdapter

        val baseSize = hourList.size
        val startAt = middle - (middle % baseSize)  // ensure aligned
        hourRecycler.scrollToPosition(startAt)

        scrollToPreviousTime(selectedHour, hourList, hourRecycler)
        highlight3rdItem(hourRecycler, hourAdapter)

    }

    private fun minListAdapterAndRecycler() {
        minRecycler.layoutManager = LinearLayoutManager(this)
        val minList = (0..59).map { String.format("%02d", it) }

        minAdapter = TimeAlarmAdapter(minList, this) { timeHighlighted ->
            scrollToPreviousTime(timeHighlighted, minList, minRecycler)
            selectedMin = timeHighlighted
            minTV.text = timeHighlighted
        }
        minRecycler.adapter = minAdapter

        val baseSizeMin = minList.size
        val startAtMin = middle - (middle % baseSizeMin)
        minRecycler.scrollToPosition(startAtMin)

        scrollToPreviousTime(selectedMin, minList, minRecycler)

        highlight3rdItem(minRecycler, minAdapter)
    }

    private fun get12HourList(): List<String> = (1..12).map { String.format("%02d", it) }

    private fun get24HourList(): List<String> = (0..23).map { String.format("%02d", it) }

    private fun highlight3rdItem(recyclerView: RecyclerView, adapter: TimeAlarmAdapter) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                val thirdVisible = layoutManager.findFirstVisibleItemPosition() + 2
                if (thirdVisible != adapter.highlightPosition) {
                    val previous = adapter.highlightPosition
                    adapter.highlightPosition = thirdVisible

                    recyclerView.post {
                        if (previous != -1) adapter.notifyItemChanged(previous)
                        adapter.notifyItemChanged(thirdVisible)
                    }
                }
            }
        })
    }

    private fun scrollToPreviousTime(previousTime: String, timeList: List<String>, recyclerView: RecyclerView) {
        val baseSize = timeList.size
        val index = timeList.indexOf(previousTime)
        if (index == -1) return

        val middle = Int.MAX_VALUE / 2
        val alignedMiddle = middle - (middle % baseSize)
        val position = alignedMiddle + index

        (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position - 2, 0)
    }

    private fun backPress(view: View) {
        view.rotateView360Ext()

        startActivity(Intent(this, MainAlarmActivity::class.java))
        finish()

        GlobalUtils.alarmModelClick = AlarmModel()
    }

    // interface
    override fun onAlarmSound(ringtoneItemM: RingtoneItemM) {
        super.onAlarmSound(ringtoneItemM)
        soundTypeTV.text = ringtoneItemM.title
        soundTitle = ringtoneItemM.title
        alarmModel.sound = ringtoneItemM
    }


}