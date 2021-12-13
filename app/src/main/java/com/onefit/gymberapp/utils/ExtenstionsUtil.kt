package com.onefit.gymberapp.utils

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import java.text.DecimalFormat

/**
 * Following three are utility functions to change visibility of View.
 */
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun TextView.setTextValueOrHideView(fieldValue: String?) {

    if (!TextUtils.isEmpty(fieldValue)) {
        visible()
        text = fieldValue
    } else {
        gone()
    }
}

fun Float?.toDecimalFormat(): Float? {
    if (this == null) return null

    val decimalFormat = DecimalFormat("#.##")
    return decimalFormat.format(this).toFloat()
}