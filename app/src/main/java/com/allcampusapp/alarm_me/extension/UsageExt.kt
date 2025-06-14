package com.allcampusapp.alarm_me.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.allcampusapp.alarm_me.R
import com.allcampusapp.alarm_me.constant.KVal
import com.allcampusapp.alarm_me.model.AlarmModel

fun AlarmModel.daysActiveIndex() : List<Int> {
    return listOfNotNull(
        if (dayModel.sun) 0 else null,
        if (dayModel.mon) 2 else null,
        if (dayModel.tue) 4 else null,
        if (dayModel.wed) 6 else null,
        if (dayModel.thur) 8 else null,
        if (dayModel.fri) 10 else null,
        if (dayModel.sat) 12 else null
    )
}

fun View.gone() {
    this.visibility = View.GONE
}

fun Any?.isNullExt(): Boolean {
    return this == null || (this is String && (this.isEmpty() || this == "null" || this == ""))
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun TextView.setTextColorExt(colorResId: Int) {
    val color = ContextCompat.getColor(context, colorResId)
    this.setTextColor(color)
}

fun TextView.setBoldAndColorPartAndReduceRestTextExt(
    fullText: String,
    partToSpan: String,
    colorResId: Int = R.color.appBlue,
    restSize: Float = 36f
) {
    val spannable = SpannableString(fullText)
    val startIndex = fullText.indexOf(partToSpan)

    if (startIndex >= 0) {
        val color = ContextCompat.getColor(context, colorResId)

        // Apply color and bold to the target part
        spannable.setSpan(
            ForegroundColorSpan(color),
            startIndex,
            startIndex + partToSpan.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex,
            startIndex + partToSpan.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply smaller size to the parts before and after the span
        val smallSizeRatio = restSize / textSize // textSize is in pixels

        if (startIndex > 0) {
            spannable.setSpan(
                RelativeSizeSpan(smallSizeRatio),
                0,
                startIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (startIndex + partToSpan.length < fullText.length) {
            spannable.setSpan(
                RelativeSizeSpan(smallSizeRatio),
                startIndex + partToSpan.length,
                fullText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    text = spannable
}




fun TextView.setBoldAndColorPartsExt(
    textString: String,
    indexesToBold: List<Int>,
    colorResId: Int
) {
    val spannable = SpannableString(textString)
    val color = ContextCompat.getColor(context, colorResId)

    for (index in indexesToBold) {
        // Apply bold style
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            index,
            index + 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply color
        spannable.setSpan(
            ForegroundColorSpan(color),
            index,
            index + 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    text = spannable
}

fun View.setBackgroundDrawableExt(drawableResId: Int) {
    val drawable = if (drawableResId != 0) {
        ContextCompat.getDrawable(context, drawableResId)
    } else {
        null
    }
    this.background = drawable
}

fun AlarmModel.convertTo24Hour(): Pair<Int, Int> {
    val (hourStr, minuteStr) = this.time.split(":")
    var hour = hourStr.toInt()
    val minute = minuteStr.toInt()

    if (format == KVal.H12) {
        if (type == KVal.PM && hour < 12) {
            hour += 12
        } else if (type == KVal.AM && hour == 12) {
            hour = 0
        }
    }
    return hour to minute
}

fun Context.vibrateIndefinitely(dur : Long = 500) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    val pattern = longArrayOf(0, dur, dur) // wait 0ms, vibrate, sleep

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createWaveform(pattern, 0) // 0 = repeat from start
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, 0)
    }
}

fun Context.stopVibration() {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    vibrator.cancel()
}

fun setDelay(duration: Long = 10_000, onComplete : () -> Unit){
    Handler(Looper.getMainLooper()).postDelayed({
        onComplete()
    }, duration)
}

fun TextView.removeAllBoldExt(textString: String? = null) {
    textString?.let { text = textString }
    paintFlags = paintFlags and Paint.FAKE_BOLD_TEXT_FLAG.inv()
}

fun TextView.setItalicExt() {
    val spannable = SpannableString(this.text)
    spannable.setSpan(StyleSpan(Typeface.ITALIC), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.text = spannable
}

fun TextView.setBoldExt() {
    val spannable = SpannableString(this.text)
    spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.text = spannable
}

fun Context.toastShort(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toastLong(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun ImageView.setImageDrawableExt(drawableResId: Int) {
    val drawable = if (drawableResId != 0) {
        ContextCompat.getDrawable(context, drawableResId)
    } else {
        null
    }
    this.setImageDrawable(drawable)
}


fun View.rotateView360Ext(duration: Long = 500) {
    ObjectAnimator.ofFloat(this, "rotation", 0f, 360f).apply {
        this.duration = duration // Default duration is 1 second
        repeatCount = 0 // No repetition
        start()
    }
}

fun onBackPress(action: () -> Unit) : OnBackPressedCallback {
    return object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            action()
        }
    }
}

fun View.slideInFromRight(duration: Long = 500) {
    this.translationX = this.width.toFloat()
    this.visibility = View.VISIBLE
    ObjectAnimator.ofFloat(this, "translationX", this.width.toFloat(), 0f).apply {
        this.duration = duration
        start()
    }
}

fun View.slideOutToRight(duration: Long = 500) {
    ObjectAnimator.ofFloat(this, "translationX", 0f, this.width.toFloat()).apply {
        this.duration = duration
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                this@slideOutToRight.visibility = View.GONE
            }
        })
        start()
    }
}

fun View.finishActivityWithHighlight(activity: Activity) {
    this.setOnClickListener {
        it.rotateView360Ext()
        activity.finish()
    }
}