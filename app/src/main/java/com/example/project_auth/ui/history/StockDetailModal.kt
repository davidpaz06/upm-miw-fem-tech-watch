package com.example.project_auth.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val SHEET_HEIGHT_FRACTION = 0.9f
private const val ENTER_MILLIS = 300
private const val EXIT_MILLIS = 220
private const val SCRIM_MILLIS = 250

/**
 * Detail of one stock shown as a sheet that slides up from the bottom (ease-out) and covers
 * 90% of the screen, over a dimmed overlay that dismisses it when tapped.
 * Pass null as [symbol] to hide it.
 */
@Composable
fun StockDetailModal(symbol: String?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val visible = symbol != null

    // Keeps the last symbol while the sheet slides out, or its content would vanish mid-animation.
    var lastSymbol by remember { mutableStateOf(symbol.orEmpty()) }
    LaunchedEffect(symbol) { if (symbol != null) lastSymbol = symbol }

    BackHandler(enabled = visible, onBack = onDismiss)

    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(SCRIM_MILLIS)),
            exit = fadeOut(tween(EXIT_MILLIS)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(SHEET_HEIGHT_FRACTION),
            enter = slideInVertically(tween(ENTER_MILLIS, easing = EaseOut)) { it },
            exit = slideOutVertically(tween(EXIT_MILLIS, easing = EaseIn)) { it },
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                HistoryScreen(
                    symbol = symbol ?: lastSymbol,
                    onBack = onDismiss,
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        }
    }
}
