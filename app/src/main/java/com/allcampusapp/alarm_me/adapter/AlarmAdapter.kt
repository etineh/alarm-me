package com.allcampusapp.alarm_me.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.extension.daysActiveIndex
import com.allcampusapp.alarm_me.extension.gone
import com.allcampusapp.alarm_me.extension.isNullExt
import com.allcampusapp.alarm_me.extension.setBackgroundDrawableExt
import com.allcampusapp.alarm_me.extension.setBoldAndColorPartAndReduceRestTextExt
import com.allcampusapp.alarm_me.extension.setBoldAndColorPartsExt
import com.allcampusapp.alarm_me.extension.setBoldExt
import com.allcampusapp.alarm_me.extension.setTextColorExt
import com.allcampusapp.alarm_me.extension.visible
import com.allcampusapp.alarm_me.model.AlarmModel
import com.allcampusapp.alarm_me.utility.LocalStorageUtils
import com.allcampusapp.alarm_me.utility.SoundUtils

class AlarmAdapter(
    private val context: Context,
    private var alarmList: MutableList<AlarmModel>,
    private var onItemClick: (AlarmModel, Int) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
//        val position = holder.adapterPosition
        val alarmM = alarmList[position]
        holder.reset(alarmM)
        holder.setDetails(alarmM)
        holder.onClicks(alarmM, position)

    }

    override fun getItemCount(): Int = alarmList.size

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val daysTV: TextView = itemView.findViewById(R.id.daysTV)
        private val timeTV: TextView = itemView.findViewById(R.id.timeTV)
        private val labelTV: TextView = itemView.findViewById(R.id.labelTV)
        private val deleteIV: ImageView = itemView.findViewById(R.id.deleteIV)
        private val alarmSwitch: Switch = itemView.findViewById(R.id.alarmSwitch)

        fun setDetails(alarmM: AlarmModel) {

            // ==========   set days    ==================
            val dayIndexes = alarmM.daysActiveIndex()

            daysTV.letterSpacing = 0.0f

            when {
                alarmM.dayModel.allDay -> {
                    daysTV.text = context.getString(R.string.everyday)
                    daysTV.setBoldExt()
                    daysTV.setTextColorExt(R.color.appBlue)
                }
                dayIndexes.isEmpty() -> {
                    daysTV.text = context.getString(R.string.one_time)
                    daysTV.setBoldExt()
                    daysTV.setTextColorExt(R.color.appBlue)
                }
                else -> {
                    daysTV.setTextColorExt(R.color.defaultColorTV)
                    daysTV.letterSpacing = 0.5f
                    daysTV.setBoldAndColorPartsExt(KVal.DAY_SHORT, dayIndexes, R.color.appBlue)
                }
            }

            // set time
            val time = "${alarmM.time} ${alarmM.type.takeIf { alarmM.format == KVal.H12 } ?: ""}"
            timeTV.text = time
            timeTV.setBoldAndColorPartAndReduceRestTextExt(time, alarmM.time)


            // set label
            if(!alarmM.label.isNullExt()) labelTV.visible()
            labelTV.text = alarmM.label

            // set radius button
            if (alarmM.alarmIsActive) {
                alarmSwitch.isChecked = true
                alarmSwitch.setBackgroundDrawableExt(R.drawable.round_radius_blue100)
                alarmSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            }


        }

        fun onClicks(alarmM: AlarmModel, position: Int) {

            alarmSwitch.setOnClickListener {
                val isChecked = alarmSwitch.isChecked
                alarmM.alarmIsActive = isChecked

                SoundUtils.toggleAlarm(context, alarmM, isChecked)

                if (isChecked) {
                    alarmSwitch.setBackgroundDrawableExt(R.drawable.round_radius_blue100)
                    alarmSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                } else {
                    alarmSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textColor))
                    alarmSwitch.setBackgroundDrawableExt(R.drawable.radius_card_color100)
                }

                LocalStorageUtils.updateAlarm(context, position, alarmM)
            }

            deleteIV.setOnClickListener {
                alarmList.removeAt(position)
                notifyItemRemoved(position) // this
                notifyItemRangeChanged(position, alarmList.size - position)

                SoundUtils.cancelAlarm(context, alarmM)
                LocalStorageUtils.deleteAlarm(context, position)
            }

            itemView.setOnClickListener { onItemClick(alarmM, position) }

        }

        fun reset(alarmM: AlarmModel) {
            daysTV.text = KVal.DAY_SHORT
            daysTV.letterSpacing = 0f
            daysTV.setBoldExt()
            daysTV.setTextColorExt(R.color.textColor)
            timeTV.text = alarmM.time
            labelTV.text = alarmM.label
            labelTV.gone()

            alarmSwitch.isChecked = false
            alarmSwitch.setBackgroundDrawableExt(R.drawable.radius_card_color100)
            alarmSwitch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textColor))

        }

    }

    fun emptyList(context: Context) {
        alarmList.clear()
        LocalStorageUtils.clearAllAlarm(context)
        notifyDataSetChanged()
    }

    fun resetAlarmUI() {
        alarmList.clear()
        alarmList.addAll(LocalStorageUtils.getAllAlarm(context))
        notifyDataSetChanged()
    }
}
