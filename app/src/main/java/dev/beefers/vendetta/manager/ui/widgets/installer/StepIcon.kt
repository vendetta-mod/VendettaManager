package dev.beefers.vendetta.manager.ui.widgets.installer

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.StepStatus
import dev.beefers.vendetta.manager.ui.viewmodel.installer.InstallerViewModel
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun StepIcon(
    status: StepStatus,
    size: Dp,
    progress: Float?
) {
    val strokeWidth = Dp(floor(size.value / 10) + 1)
    val context = LocalContext.current

    when (status) {
        StepStatus.ONGOING -> {
            if(progress != null) {
                CircularProgressIndicator(
                    progress = progress,
                    strokeWidth = strokeWidth,
                    modifier = Modifier
                        .size(size)
                        .semantics {
                            contentDescription = "${(progress * 100).roundToInt()}%"
                        }
                )
            } else {
                CircularProgressIndicator(
                    strokeWidth = strokeWidth,
                    modifier = Modifier
                        .size(size)
                        .semantics {
                            contentDescription = context.getString(R.string.status_ongoing)
                        }
                )
            }
        }

        StepStatus.SUCCESSFUL -> {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.status_successful),
                tint = Color(0xFF59B463),
                modifier = Modifier.size(size)
            )
        }

        StepStatus.UNSUCCESSFUL -> {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = stringResource(R.string.status_fail),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(size)
            )
        }

        StepStatus.QUEUED -> {
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = stringResource(R.string.status_queued),
                tint = LocalContentColor.current.copy(alpha = 0.4f),
                modifier = Modifier.size(size)
            )
        }
    }
}