package dev.beefers.vendetta.manager.ui.widgets.updater

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.network.dto.Release

@Composable
fun UpdateDialog(
    release: Release,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            FilledTonalButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_start_update))
            }
        },
        title = {
            Text(stringResource(R.string.title_update))
        },
        text = {
            Text(stringResource(R.string.update_description, release.versionName))
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.SystemUpdate,
                contentDescription = null
            )
        }
    )
}