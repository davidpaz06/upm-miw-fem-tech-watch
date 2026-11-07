package com.example.project_auth.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.project_auth.R

/** Back arrow used at the top of every full screen that has somewhere to go back to. */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    RoundIconButton(
        icon = R.drawable.ic_arrow_back,
        description = stringResource(R.string.back),
        onClick = onClick,
        modifier = modifier,
    )
}
