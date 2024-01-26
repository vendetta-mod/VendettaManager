package dev.beefers.vendetta.manager.ui.screen.installer

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.step.StepStatus
import dev.beefers.vendetta.manager.ui.viewmodel.installer.InstallerViewModel
import dev.beefers.vendetta.manager.ui.widgets.dialog.BackWarningDialog
import dev.beefers.vendetta.manager.ui.widgets.dialog.DownloadFailedDialog
import dev.beefers.vendetta.manager.ui.widgets.installer.StepGroupCard
import dev.beefers.vendetta.manager.utils.DiscordVersion
import org.koin.core.parameter.parametersOf
import java.util.UUID

class InstallerScreen(
    val version: DiscordVersion
) : Screen {

    override val key: ScreenKey = "Installer-${UUID.randomUUID()}"

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val activity = LocalContext.current as? ComponentActivity
        val viewModel: InstallerViewModel = getScreenModel {
            parametersOf(version)
        }

        LaunchedEffect(viewModel.runner.currentStep) {
            viewModel.expandGroup(viewModel.runner.currentStep?.group)
        }

        // Listen for error messages from InstallService
        val intentListener: (Intent) -> Unit = remember {
            {
                val msg = it.getStringExtra("vendetta.extras.EXTRA_MESSAGE")
                viewModel.logError(msg)
            }
        }

        DisposableEffect(Unit) {
            activity?.addOnNewIntentListener(intentListener)
            onDispose {
                activity?.removeOnNewIntentListener(intentListener)
            }
        }

        BackHandler(
            enabled = !viewModel.runner.completed
        ) {
            viewModel.openBackDialog()
        }

        if(viewModel.backDialogOpened) {
            BackWarningDialog(
                onExitClick = {
                    viewModel.closeBackDialog()
                    viewModel.cancelInstall()
                    nav.pop()
                },
                onClose = { viewModel.closeBackDialog() }
            )
        }

        if(viewModel.failedOnDownload) {
            DownloadFailedDialog(
                onTryAgainClick = {
                    viewModel.dismissDownloadFailedDialog()
                    viewModel.cancelInstall()
                    nav.replace(InstallerScreen(version))
                },
                onDismiss = {
                    viewModel.dismissDownloadFailedDialog()
                }
            )
        }

        Scaffold(
            topBar = {
                TitleBar(
                    onBackClick = {
                        if(!viewModel.runner.completed)
                            viewModel.openBackDialog()
                        else
                            nav.pop()
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                for ((group, steps) in viewModel.groupedSteps) {
                    key(group) {
                        StepGroupCard(
                            name = stringResource(group.nameRes),
                            isCurrent = viewModel.expandedGroup == group,
                            onClick = { viewModel.expandGroup(group) },
                            steps = steps,
                        )
                    }
                }

                if (viewModel.runner.completed) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Show launch only if success
                    val installSuccessful = viewModel.runner.currentStep?.status == StepStatus.SUCCESSFUL
                    if (installSuccessful) {
                        Button(
                            onClick = { viewModel.launchVendetta() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.action_launch))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.copyDebugInfo() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.action_copy_logs))
                        }
                        FilledTonalButton(
                            onClick = { viewModel.clearCache() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.action_clear_cache))
                        }
                    }
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun TitleBar(
        onBackClick: () -> Unit
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.title_installer)) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        )
    }

}