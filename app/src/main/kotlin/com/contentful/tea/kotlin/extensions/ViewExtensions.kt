package com.contentful.tea.kotlin.extensions

import android.view.View
import android.view.ViewGroup

fun ViewGroup.forEach(action: (index: Int, view: View) -> Any?) {
    for (i in 0 until this.childCount) {
        action(i, this.getChildAt(i))
    }
}