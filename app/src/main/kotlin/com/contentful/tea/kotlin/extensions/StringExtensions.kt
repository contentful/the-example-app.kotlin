package com.contentful.tea.kotlin.extensions

fun String?.or(other: String): String = if (isNullOrEmpty()) other else this!!