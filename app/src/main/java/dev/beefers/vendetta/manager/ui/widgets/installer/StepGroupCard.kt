package dev.beefers.vendetta.manager.ui.widgets.installer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.ui.viewmodel.installer.InstallerViewModel

@Composable
fun StepGroupCard(
    name: String,
    isCurrent: Boolean,
    steps: List<InstallerViewModel.InstallStepData>,
    onClick: () -> Unit
) {
    val status = when {
        steps.all { it.status == InstallerViewModel.InstallStatus.QUEUED } -> InstallerViewModel.InstallStatus.QUEUED
        steps.all { it.status == InstallerViewModel.InstallStatus.SUCCESSFUL } -> InstallerViewModel.InstallStatus.SUCCESSFUL
        steps.any { it.status == InstallerViewModel.InstallStatus.ONGOING } -> InstallerViewModel.InstallStatus.ONGOING
        else -> InstallerViewModel.InstallStatus.UNSUCCESSFUL
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .run {
                if (isCurrent) {
                    background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                } else this
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StepIcon(status, 24.dp, progress = null)

            Text(text = name)

            Spacer(modifier = Modifier.weight(1f))

            if (status != InstallerViewModel.InstallStatus.ONGOING && status != InstallerViewModel.InstallStatus.QUEUED) {
                Text(
                    text = "%.2fs".format(steps.map { it.duration }.sum()),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            val (arrow, cd) = when {
                isCurrent -> Icons.Filled.KeyboardArrowUp to R.string.action_collapse
                else -> Icons.Filled.KeyboardArrowDown to R.string.action_expand
            }

            Icon(
                imageVector = arrow,
                contentDescription = stringResource(cd)
            )
        }

        AnimatedVisibility(visible = isCurrent) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background.copy(0.6f))
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(start = 4.dp)
            ) {
                steps.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        val progress by animateFloatAsState(it.progress ?: 0f, label = "Progress")

                        StepIcon(it.status, size = 18.dp, progress = if(it.progress == null) null else progress)

                        Text(
                            text = stringResource(it.nameRes),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, true),
                        )

                        if (it.status != InstallerViewModel.InstallStatus.ONGOING && it.status != InstallerViewModel.InstallStatus.QUEUED) {
                            if (it.cached) {
                                val style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = stringResource(R.string.installer_cached),
                                    style = style,
                                    maxLines = 1,
                                )
                            }

                            Text(
                                text = "%.2fs".format(it.duration),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}