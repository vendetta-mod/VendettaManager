package dev.beefers.vendetta.manager.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.ui.components.SegmentedButton
import dev.beefers.vendetta.manager.ui.screen.installer.InstallerScreen
import dev.beefers.vendetta.manager.ui.viewmodel.home.HomeViewModel
import dev.beefers.vendetta.manager.ui.widgets.home.Commit
import dev.beefers.vendetta.manager.utils.DiscordVersion
import dev.beefers.vendetta.manager.utils.ManagerTab
import dev.beefers.vendetta.manager.utils.TabOptions
import dev.beefers.vendetta.manager.utils.navigate
import org.koin.androidx.compose.get

class HomeScreen : ManagerTab {
    override val options: TabOptions
        @Composable get() = TabOptions(
            title = R.string.title_home,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        )

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val prefs: PreferenceManager = get()
        val viewModel: HomeViewModel = getScreenModel()
        val iconColor = when {
            prefs.patchIcon -> Color(0xFF3AB8BA)
            prefs.channel == DiscordVersion.Type.ALPHA -> Color(0xFFFBB33C)
            else -> Color(0xFF5865F2)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_discord_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(iconColor)
            )

            Text(
                text = prefs.appName,
                style = MaterialTheme.typography.titleLarge
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedVisibility(visible = viewModel.discordVersions != null) {
                    Text(
                        text = "Latest: ${viewModel.discordVersions?.get(prefs.channel)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalContentColor.current.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }

                AnimatedVisibility(visible = viewModel.installManager.current != null) {
                    Text(
                        text = "Current: ${viewModel.installManager.current?.versionName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = LocalContentColor.current.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = {
                    val version = viewModel.discordVersions!![prefs.channel]!!
                    nav.navigate(InstallerScreen(version))
                },
                enabled = viewModel.discordVersions != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                val label = when {
                    viewModel.installManager.current == null -> R.string.action_install
                    viewModel.installManager.current?.versionName == viewModel.discordVersions?.get(
                        prefs.channel
                    ).toString() -> R.string.action_reinstall

                    else -> R.string.action_update
                }
                Text(stringResource(label))
            }

            AnimatedVisibility(visible = viewModel.installManager.current != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    SegmentedButton(
                        icon = Icons.Filled.OpenInNew,
                        text = stringResource(R.string.action_launch),
                        onClick = { viewModel.launchVendetta() }
                    )
                    SegmentedButton(
                        icon = Icons.Filled.Info,
                        text = stringResource(R.string.action_info),
                        onClick = { viewModel.launchVendettaInfo() }
                    )
                    SegmentedButton(
                        icon = Icons.Filled.Delete,
                        text = stringResource(R.string.action_uninstall),
                        onClick = { viewModel.uninstallVendetta() }
                    )
                }
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val commits = viewModel.commits.collectAsLazyPagingItems()
                val loading =
                    commits.loadState.append is LoadState.Loading || commits.loadState.refresh is LoadState.Loading
                val failed =
                    commits.loadState.append is LoadState.Error || commits.loadState.refresh is LoadState.Error

                LazyColumn {
                    itemsIndexed(
                        items = commits,
                        key = { _, commit -> commit.sha }
                    ) { i, commit ->
                        if (commit != null) {
                            Column {
                                Commit(commit = commit)
                                if (i < commits.itemSnapshotList.lastIndex) {
                                    Divider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }

                        }
                    }

                    if (loading) {
                        item {
                            Box(
                                contentAlignment = Alignment.TopCenter,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    if (failed) {
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.msg_load_fail),
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.Center
                                )

                                Button(onClick = { commits.retry() }) {
                                    Text(stringResource(R.string.action_retry))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun Actions() {
    }

}