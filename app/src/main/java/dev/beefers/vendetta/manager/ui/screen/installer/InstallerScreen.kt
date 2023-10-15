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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.ui.viewmodel.installer.InstallerViewModel
import dev.beefers.vendetta.manager.ui.widgets.dialog.BackWarningDialog
import dev.beefers.vendetta.manager.ui.widgets.dialog.DownloadFailedDialog
import dev.beefers.vendetta.manager.ui.widgets.installer.StepGroupCard
import dev.beefers.vendetta.manager.utils.DiscordVersion
import kotlinx.coroutines.delay
import org.koin.core.parameter.parametersOf
import java.util.UUID

class InstallerScreen(
    val version: DiscordVersion
) : Screen {

    override val key: ScreenKey = "Installer-${UUID.randomUUID()}"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val activity = LocalContext.current as? ComponentActivity
        var timeoutDuration = remember { 90 /* seconds */ }
        val viewModel: InstallerViewModel = getScreenModel {
            parametersOf(version)
        }

        var expandedGroup by remember {
            mutableStateOf<InstallerViewModel.InstallStepGroup?>(null)
        }

        LaunchedEffect(viewModel.currentStep) {
            var timer = 0 // seconds
            expandedGroup = viewModel.currentStep?.group
            while (viewModel.currentStep?.group == InstallerViewModel.InstallStepGroup.DL) {
                if(!viewModel.failedOnDownload) {
                    if(timer > timeoutDuration) viewModel.failedOnDownload = true
                    timer += 1
                }
                delay(1000)
            }
        }

        // Listen for error messages from InstallService
        val intentListener: (Intent) -> Unit = remember {
            {
                val msg = it.getStringExtra("vendetta.extras.EXTRA_MESSAGE")
                viewModel.addLogError(msg ?: "")
            }
        }

        DisposableEffect(Unit) {
            activity?.addOnNewIntentListener(intentListener)
            onDispose {
                activity?.removeOnNewIntentListener(intentListener)
            }
        }

        BackHandler(
            enabled = !viewModel.isFinished
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
                    viewModel.failedOnDownload = false
                    viewModel.cancelInstall()
                    nav.replace(InstallerScreen(version))
                },
                onDismiss = {
                    viewModel.failedOnDownload = false
                    timeoutDuration += 30
                }
            )
        }

        Scaffold(
            topBar = {
                TitleBar(
                    onBackClick = {
                        if(!viewModel.isFinished)
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
                for (group in InstallerViewModel.InstallStepGroup.values()) {
                    StepGroupCard(
                        name = stringResource(group.nameRes),
                        isCurrent = expandedGroup == group,
                        onClick = { expandedGroup = group },
                        steps = viewModel.getSteps(group),
                    )
                }

                if (viewModel.isFinished) {
                    Spacer(modifier = Modifier.height(16.dp))

                    viewModel.installManager.current?.let {
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