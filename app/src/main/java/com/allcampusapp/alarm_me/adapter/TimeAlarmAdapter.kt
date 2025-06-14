package com.allcampusapp.alarm_me.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.extension.removeAllBoldExt
import com.allcampusapp.alarm_me.extension.setBoldExt
import com.allcampusapp.alarm_me.extension.setTextColorExt

class TimeAlarmAdapter(
    private val timeList: List<String>,
    private val context: Context,
    private val onItemClick: (time: String) -> Unit
) : RecyclerView.Adapter<TimeAlarmAdapter.HourViewHolder>() {

    var highlightPosition = -1
    private val fakeSize = Int.MAX_VALUE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_time, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {

        val realPos = position % timeList.size

        // reset data
        holder.textView.setTextColorExt(R.color.textColor)
        holder.itemView.setBackgroundColor(context.getColor(R.color.cardColor)) // reset color
        holder.textView.text = ""
        holder.textView.removeAllBoldExt(timeList[realPos])

        holder.textView.text = timeList[realPos]

        if (position == highlightPosition) {
            holder.itemView.setBackgroundColor(context.getColor(R.color.appBlue)) // highlight color
            holder.textView.setTextColorExt(R.color.white)
            holder.textView.setBoldExt()
            onItemClick(holder.textView.text.toString())
        }

        holder.itemView.setOnClickListener { onItemClick(holder.textView.text.toString()) }

    }

    override fun getItemCount(): Int = fakeSize

    class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.time_TV)
    }


}
