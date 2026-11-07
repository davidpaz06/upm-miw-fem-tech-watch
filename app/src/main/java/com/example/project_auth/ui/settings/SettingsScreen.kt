package com.example.project_auth.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.project_auth.R
import com.example.project_auth.ui.common.BackButton
import com.example.project_auth.data.AuthRepository
import com.example.project_auth.data.ThemeMode
import com.example.project_auth.data.ThemeSettings
import com.example.project_auth.ui.common.Pill
import com.example.project_auth.ui.common.PillDefaults
import com.example.project_auth.ui.theme.isDark

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = AuthRepository(),
) {
    val themeSettings = ThemeSettings.instance
    val mode by themeSettings.mode.collectAsState()
    val isDark = mode.isDark()

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = onBack)
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(R.string.settings_account)
            Text(authRepository.currentUser?.email.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider()

            SectionTitle(R.string.settings_appearance)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isDark,
                        role = Role.Switch,
                        onValueChange = { themeSettings.set(if (it) ThemeMode.DARK else ThemeMode.LIGHT) },
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(if (isDark) R.drawable.ic_dark_mode else R.drawable.ic_light_mode),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    stringResource(R.string.settings_dark_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                // The Row handles the click and the semantics, so the Switch itself is not interactive.
                Switch(checked = isDark, onCheckedChange = null)
            }
            HorizontalDivider()

            Pill(
                text = stringResource(R.string.auth_logout),
                onClick = authRepository::signOut,
                colors = PillDefaults.destructive(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: Int) {
    Text(
        stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}
