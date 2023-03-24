package dev.beefers.vendetta.manager.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import dev.beefers.vendetta.manager.ui.screen.home.HomeScreen
import dev.beefers.vendetta.manager.ui.viewmodel.main.MainViewModel
import dev.beefers.vendetta.manager.ui.widgets.updater.UpdateDialog
import dev.beefers.vendetta.manager.utils.MainTab
import kotlinx.coroutines.launch

class MainScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
    @Composable
    override fun Content() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val viewModel: MainViewModel = getScreenModel()
        val pagerState = rememberPagerState()

        CompositionLocalProvider(
            LocalPagerState provides pagerState
        ) {
            Scaffold(
                bottomBar = { NavBar() },
                topBar = { TitleBar(scrollBehavior) },
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { pv ->
                if (viewModel.showUpdateDialog && viewModel.release != null) {
                    UpdateDialog(
                        release = viewModel.release!!,
                        isUpdating = viewModel.isUpdating,
                        onDismiss = { viewModel.showUpdateDialog = false },
                        onConfirm = {
                            viewModel.downloadAndInstallUpdate()
                        }
                    )
                }

                HorizontalPager(
                    count = MainTab.values().size,
                    state = pagerState,
                    contentPadding = pv
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        MainTab.values()[page].tab.Content()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
    @Composable
    private fun TitleBar(
        scrollBehavior: TopAppBarScrollBehavior
    ) {
        val pagerState = LocalPagerState.current
        val tab = MainTab.values()[pagerState.currentPage].tab

        TopAppBar(
            title = { Text(tab.options.title) },
            actions = { tab.Actions() },
            scrollBehavior = if (tab is HomeScreen) null else scrollBehavior
        )
    }

    @Composable
    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
    private fun NavBar() {
        val pagerState = LocalPagerState.current
        val scope = rememberCoroutineScope()
        val tab = MainTab.values()[pagerState.currentPage].tab

        NavigationBar {
            MainTab.values().forEach { mainTab ->
                NavigationBarItem(
                    selected = mainTab.tab === tab,
                    onClick = {
                        val page = MainTab.values().indexOf(mainTab)
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    },
                    label = { Text(mainTab.tab.options.title) },
                    alwaysShowLabel = true,
                    icon = {
                        Icon(
                            painter = mainTab.tab.options.icon!!,
                            contentDescription = mainTab.tab.options.title
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
val LocalPagerState = compositionLocalOf<PagerState> {
    error("Pager not initialized")
}