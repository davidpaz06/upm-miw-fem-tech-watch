package com.example.project_auth.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.project_auth.R
import java.text.DateFormat
import java.util.Date

/** Tells the user the quotes below were saved earlier because fresh ones could not be fetched. */
@Composable
fun StaleDataBanner(savedAt: Long, modifier: Modifier = Modifier) {
    val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(savedAt))
    Text(
        text = stringResource(R.string.stale_data_banner, date),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
