package dev.beefers.vendetta.manager.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import dev.beefers.vendetta.manager.ui.screen.home.HomeScreen
import dev.beefers.vendetta.manager.ui.screen.main.LocalPagerState
import dev.beefers.vendetta.manager.ui.screen.settings.SettingsScreen
import dev.beefers.vendetta.manager.ui.viewmodel.main.MainViewModel

enum class MainTab(val tab: ManagerTab) {
    HOME(HomeScreen()),
    SETTINGS(SettingsScreen())
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tab.TabOptions(
    @StringRes title: Int,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector
): TabOptions {
    val pagerState = LocalPagerState.current
    val selected = MainTab.values()[pagerState.currentPage].tab == this
    val selectedIconPainter = rememberVectorPainter(
        image = selectedIcon
    )
    val unelectedIconPainter = rememberVectorPainter(
        image = unselectedIcon
    )

    return TabOptions(
        0u,
        stringResource(title),
        if (selected) selectedIconPainter else unelectedIconPainter
    )
}

tailrec fun Navigator.navigate(screen: Screen) {
    if (level == 0)
        push(screen)
    else
        this.parent!!.navigate(screen)
}

interface ManagerTab : Tab {

    @Composable
    fun Actions()

}