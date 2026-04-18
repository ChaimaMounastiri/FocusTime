package com.example.studysmart.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf

@Immutable
data class ThemeController(
    val mode: String,
    val setMode: (String) -> Unit
)

val LocalThemeController = compositionLocalOf { ThemeController("system", {}) }
