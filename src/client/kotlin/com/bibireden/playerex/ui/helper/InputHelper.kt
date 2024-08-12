package com.bibireden.playerex.ui.helper

object InputHelper {
    fun isUIntInput(str: String) = str.isEmpty() || str.toUIntOrNull() != null
}