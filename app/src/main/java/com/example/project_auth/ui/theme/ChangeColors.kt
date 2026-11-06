package com.example.project_auth.ui.theme

import androidx.compose.ui.graphics.Color

val GainColor = Color(0xFF2E7D32)
val LossColor = Color(0xFFC62828)

fun changeColor(value: Double): Color = if (value >= 0) GainColor else LossColor
