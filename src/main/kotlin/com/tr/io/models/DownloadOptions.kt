package com.tr.io.models

enum class DownloadOptions(val value: String) {
    DIVIDEND("D"),
    SAVINGS_PLAN("S"),
    INTEREST("Z"),
    ORDER("O");

    companion object {
        fun fromValue(value: String): DownloadOptions {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: throw IllegalStateException("Invalid Option")
        }

        fun isValidOption(name: String) = entries.any { it.value.equals(name, ignoreCase = true) }
    }
}
