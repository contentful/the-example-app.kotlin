package com.contentful.tea.kotlin.extensions

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ViewTarget
import com.contentful.tea.kotlin.R

fun ViewGroup.forEach(action: (index: Int, view: View) -> Any?) {
    for (i in 0 until this.childCount) {
        action(i, this.getChildAt(i))
    }
}

fun ImageView.setImageResourceFromUrl(
    url: String,
    @DrawableRes placeholder: Int = R.drawable.placeholder
): ViewTarget<ImageView, Drawable> {

    setImageResource(placeholder)

    return Glide.with(context.applicationContext)
        .load(url)
        .into(this)
}
