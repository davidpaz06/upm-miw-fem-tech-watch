package com.example.project_auth.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private const val THEME_TRANSITION_MILLIS = 400

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    secondary = BlueGrey80,
    tertiary = Amber80,
    error = Red60,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    secondary = BlueGrey40,
    tertiary = Amber40,
    error = Red40,
    onError = Color.White,
)

@Composable
fun ProjectauthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = (if (darkTheme) DarkColorScheme else LightColorScheme).animated(),
        typography = Typography,
        content = content,
    )
}

@Composable
private fun animate(color: Color): Color = animateColorAsState(
    targetValue = color,
    animationSpec = tween(THEME_TRANSITION_MILLIS),
    label = "themeColor",
).value

/**
 * The same scheme with every colour animated towards its target, so switching between
 * light and dark fades instead of jumping. The first composition shows the target directly.
 */
@Composable
private fun ColorScheme.animated(): ColorScheme = copy(
    primary = animate(primary),
    onPrimary = animate(onPrimary),
    primaryContainer = animate(primaryContainer),
    onPrimaryContainer = animate(onPrimaryContainer),
    inversePrimary = animate(inversePrimary),
    secondary = animate(secondary),
    onSecondary = animate(onSecondary),
    secondaryContainer = animate(secondaryContainer),
    onSecondaryContainer = animate(onSecondaryContainer),
    tertiary = animate(tertiary),
    onTertiary = animate(onTertiary),
    tertiaryContainer = animate(tertiaryContainer),
    onTertiaryContainer = animate(onTertiaryContainer),
    background = animate(background),
    onBackground = animate(onBackground),
    surface = animate(surface),
    onSurface = animate(onSurface),
    surfaceVariant = animate(surfaceVariant),
    onSurfaceVariant = animate(onSurfaceVariant),
    surfaceTint = animate(surfaceTint),
    inverseSurface = animate(inverseSurface),
    inverseOnSurface = animate(inverseOnSurface),
    error = animate(error),
    onError = animate(onError),
    errorContainer = animate(errorContainer),
    onErrorContainer = animate(onErrorContainer),
    outline = animate(outline),
    outlineVariant = animate(outlineVariant),
    scrim = animate(scrim),
    surfaceBright = animate(surfaceBright),
    surfaceDim = animate(surfaceDim),
    surfaceContainer = animate(surfaceContainer),
    surfaceContainerHigh = animate(surfaceContainerHigh),
    surfaceContainerHighest = animate(surfaceContainerHighest),
    surfaceContainerLow = animate(surfaceContainerLow),
    surfaceContainerLowest = animate(surfaceContainerLowest),
)
