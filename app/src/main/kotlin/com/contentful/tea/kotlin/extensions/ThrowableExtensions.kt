package com.contentful.tea.kotlin.extensions

import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable?.isNetworkError(): Boolean =
    (this is RuntimeException &&
        (cause is UnknownHostException || cause is SocketTimeoutException)) ||
        this is UnknownHostException ||
        this is SocketTimeoutException