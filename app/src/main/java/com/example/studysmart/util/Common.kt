package com.example.studysmart.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.example.studysmart.presentation.theme.Green
import com.example.studysmart.presentation.theme.Orange
import com.example.studysmart.presentation.theme.Red
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Priority(val title: String, val color: Color, val value: Int, val titleResId: Int) {
    LOW(title = "Low", color = Green, value = 0, titleResId = com.example.studysmart.R.string.priority_low),
    MEDIUM(title = "Medium", color = Orange, value = 1, titleResId = com.example.studysmart.R.string.priority_medium),
    HIGH(title = "High", color = Red, value = 2, titleResId = com.example.studysmart.R.string.priority_high);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.value == value } ?: MEDIUM
    }
}

fun Long?.changeMillisToDateString(): String {
    val date: LocalDate = this?.let {
        Instant
            .ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } ?: LocalDate.now()
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

fun Long.toHours(): Float {
    val hours = this.toFloat() / 3600f
    return "%.2f".format(hours).toFloat()
}

sealed class SnackbarEvent {
    data class ShowSnackbar(
        val message: String? = null,
        val messageResId: Int? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ) : SnackbarEvent()

    data object NavigateUp: SnackbarEvent()
}

fun Int.pad(): String {
    return this.toString().padStart(length = 2, padChar = '0')
}
