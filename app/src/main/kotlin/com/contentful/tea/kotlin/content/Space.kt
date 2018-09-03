package com.contentful.tea.kotlin.content

data class Space(val id: String, val name: String) {
    companion object
}

data class Locale(val id: String, val code: String, val name: String) {
    companion object
}
