package com.respiroc.webapp.controller.response

sealed class Callout(
    open val message: String,
    open val link: String? = null
) {
    data class Success(override val message: String, override val link: String? = null) : Callout(message, link)
    data class Error(override val message: String, override val link: String? = null) : Callout(message, link)
    data class Warning(override val message: String, override val link: String? = null) : Callout(message, link)
    data class Info(override val message: String, override val link: String? = null) : Callout(message, link)

    fun icon(): String = when (this) {
        is Success -> "circle-check"
        is Error -> "circle-exclamation"
        is Warning -> "triangle-exclamation"
        is Info -> "circle-info"
    }

    fun variant(): String = when (this) {
        is Success -> "success"
        is Error -> "danger"
        is Warning -> "warning"
        is Info -> "brand"
    }
}
