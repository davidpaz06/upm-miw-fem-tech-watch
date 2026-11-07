package com.example.project_auth.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.graphics.Color
import com.example.project_auth.R

private const val THEME_TRANSITION_MILLIS = 400

/**
 * Built from res/values/colors.xml, so that file is the one place the palette is defined:
 * purple for primary and the pills, amber and orange for the warning banner and accents.
 */
@Composable
private fun appColorScheme(darkTheme: Boolean): ColorScheme {
    val purple200 = colorResource(R.color.purple_200)
    val purple500 = colorResource(R.color.purple_500)
    val purple700 = colorResource(R.color.purple_700)
    val amber = colorResource(R.color.teal_200)
    val orange = colorResource(R.color.teal_700)
    val black = colorResource(R.color.black)
    val white = colorResource(R.color.white)
    return if (darkTheme) {
        darkColorScheme(
            primary = purple200,
            onPrimary = black,
            primaryContainer = purple700,
            onPrimaryContainer = white,
            secondary = amber,
            onSecondary = black,
            secondaryContainer = purple700,
            onSecondaryContainer = white,
            tertiary = amber,
            onTertiary = black,
            tertiaryContainer = amber,
            onTertiaryContainer = black,
            error = Red60,
            onError = white,
        )
    } else {
        lightColorScheme(
            primary = purple500,
            onPrimary = white,
            primaryContainer = purple200,
            onPrimaryContainer = black,
            secondary = orange,
            onSecondary = black,
            secondaryContainer = purple200,
            onSecondaryContainer = black,
            tertiary = amber,
            onTertiary = black,
            tertiaryContainer = amber,
            onTertiaryContainer = black,
            error = Red40,
            onError = white,
        )
    }
}

@Composable
fun ProjectauthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = appColorScheme(darkTheme).animated(),
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
