package com.example.project_auth.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Colours of a [Pill]. Use the factory functions so the colours follow the active theme. */
data class PillColors(val container: Color, val content: Color, val border: BorderStroke? = null)

object PillDefaults {
    /** Quiet pill for navigation shortcuts. */
    @Composable
    fun tonal() = PillColors(
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    /** Outlined pill for an option that is not selected. */
    @Composable
    fun outlined() = PillColors(
        container = Color.Transparent,
        content = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    )

    /** Filled pill for the selected option. */
    @Composable
    fun selected() = PillColors(
        container = MaterialTheme.colorScheme.primary,
        content = MaterialTheme.colorScheme.onPrimary,
    )

    /** Solid red pill for destructive actions such as signing out. */
    @Composable
    fun destructive() = PillColors(
        container = MaterialTheme.colorScheme.error,
        content = MaterialTheme.colorScheme.onError,
    )
}

/** Fully rounded button with a label and optional icons at either end. */
@Composable
fun Pill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: PillColors = PillDefaults.tonal(),
    @DrawableRes leadingIcon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 36.dp),
        shape = CircleShape,
        color = colors.container,
        contentColor = colors.content,
        border = colors.border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let { PillIcon(it) }
            Text(text, style = MaterialTheme.typography.labelLarge)
            trailingIcon?.let { PillIcon(it) }
        }
    }
}

@Composable
private fun PillIcon(@DrawableRes icon: Int) {
    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
}

/** Circular, icon-only version of [Pill]. [description] is read by screen readers. */
@Composable
fun RoundIconButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: PillColors = PillDefaults.tonal(),
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = colors.container,
        contentColor = colors.content,
        border = colors.border,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.padding(8.dp),
        )
    }
}
