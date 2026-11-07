package com.example.project_auth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val GainLight = Color(0xFF2E7D32)
private val LossLight = Color(0xFFC62828)

// Lighter shades: the light-mode ones lack contrast on a dark background.
private val GainDark = Color(0xFF66BB6A)
private val LossDark = Color(0xFFEF5350)

@Composable
fun changeColor(value: Double): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return when {
        value >= 0 -> if (isDark) GainDark else GainLight
        else -> if (isDark) LossDark else LossLight
    }
}
