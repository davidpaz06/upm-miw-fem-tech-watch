package com.example.project_auth.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Opaque background for a full-screen destination. During a transition the navigation host
 * draws two screens at once, so a screen with no background lets the other one show through.
 * The surface also swallows touches so they cannot reach the screen underneath.
 */
@Composable
fun ScreenSurface(
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = shadowElevation,
        content = content,
    )
}
