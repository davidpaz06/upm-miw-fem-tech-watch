package com.example.project_auth.ui.theme

import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.project_auth.R
import com.example.project_auth.data.ThemeMode
import com.example.project_auth.data.ThemeSettings

/** Switches between light and dark. The icon shows the mode a tap will switch to. */
@Composable
fun ThemeToggle(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val description = stringResource(if (isDark) R.string.theme_switch_to_light else R.string.theme_switch_to_dark)
    IconButton(
        onClick = { ThemeSettings.instance.set(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK) },
        modifier = modifier.semantics { contentDescription = description },
    ) {
        Text(if (isDark) "☀" else "☾", style = MaterialTheme.typography.titleLarge)
    }
}
