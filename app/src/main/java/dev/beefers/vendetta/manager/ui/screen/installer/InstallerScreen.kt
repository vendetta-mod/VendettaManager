package dev.beefers.vendetta.manager.ui.screen.installer

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.ui.viewmodel.installer.InstallerViewModel
import dev.beefers.vendetta.manager.ui.widgets.installer.StepGroupCard
import dev.beefers.vendetta.manager.utils.DiscordVersion
import org.koin.core.parameter.parametersOf

class InstallerScreen(
    val version: DiscordVersion
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: InstallerViewModel = getScreenModel {
            parametersOf(version)
        }

        var expandedGroup by remember {
            mutableStateOf<InstallerViewModel.InstallStepGroup?>(null)
        }

        LaunchedEffect(viewModel.currentStep) {
            expandedGroup = viewModel.currentStep?.group
        }

        Scaffold(
            topBar = { TitleBar() }
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
    private fun TitleBar() {
        val nav = LocalNavigator.currentOrThrow
        TopAppBar(
            title = { Text(stringResource(R.string.title_installer)) },
            navigationIcon = {
                IconButton(onClick = { nav.pop() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        )
    }

}