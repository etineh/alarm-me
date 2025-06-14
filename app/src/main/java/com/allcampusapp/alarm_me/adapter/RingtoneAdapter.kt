package com.allcampusapp.alarm_me.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.extension.setImageDrawableExt
import com.allcampusapp.alarm_me.extension.visible
import com.allcampusapp.alarm_me.model.RingtoneItemM

class RingtoneAdapter(
    private val items: List<RingtoneItemM>,
    private val onSelectSound: (RingtoneItemM) -> Unit,
    private val onPlaySound: (RingtoneItemM) -> Unit
) : RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {

    var previousSelectedIndex = -1
    var previousSoundSelected: RingtoneItemM? = null

    private var previousPlayIndex = -1
    private var previousPlayModel: RingtoneItemM? = null

    inner class RingtoneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.rankCheckBox)
        val playIcon : ImageView = view.findViewById(R.id.playIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_select_rank, parent, false)
        return RingtoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position1: Int) {

        val position = holder.adapterPosition
        val sound = items[position]

        // reset item
        holder.checkBox.isChecked = false
        holder.checkBox.text = sound.title
        holder.playIcon.visible()
        holder.playIcon.setImageDrawableExt(R.drawable.baseline_play_arrow_24)

        // set item details
        holder.checkBox.text = sound.title
        holder.checkBox.isChecked = sound.isSelected
        if (sound.isPlaying) holder.playIcon.setImageDrawableExt(R.drawable.pause_icon)

        holder.checkBox.setOnClickListener {
            // uncheck previous selected
            if (previousSoundSelected != sound) {
                previousSoundSelected?.isSelected = false
                notifyItemChanged(previousSelectedIndex)
            }

            sound.isSelected = holder.checkBox.isChecked

            onSelectSound(sound)

            previousSoundSelected = sound
            previousSelectedIndex = position

        }

        holder.playIcon.setOnClickListener {
            // change previous play icon to pause
            if (previousPlayModel != sound) previousPlayModel?.isPlaying = false
            notifyItemChanged(previousPlayIndex)

            sound.isPlaying = !sound.isPlaying
            onPlaySound(sound)
            if (sound.isPlaying) holder.playIcon.setImageDrawableExt(R.drawable.pause_icon)

            // assign the latest model and position
            previousPlayIndex = position
            previousPlayModel = sound
        }

    }

    override fun getItemCount(): Int = items.size
}








