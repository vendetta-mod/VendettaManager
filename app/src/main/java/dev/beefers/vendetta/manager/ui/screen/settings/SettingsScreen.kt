package dev.beefers.vendetta.manager.ui.screen.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.ui.components.settings.SettingsButton
import dev.beefers.vendetta.manager.ui.components.settings.SettingsHeader
import dev.beefers.vendetta.manager.ui.components.settings.SettingsItemChoice
import dev.beefers.vendetta.manager.ui.components.settings.SettingsSwitch
import dev.beefers.vendetta.manager.ui.components.settings.SettingsTextField
import dev.beefers.vendetta.manager.ui.viewmodel.settings.SettingsViewModel
import dev.beefers.vendetta.manager.utils.ManagerTab
import dev.beefers.vendetta.manager.utils.TabOptions
import org.koin.androidx.compose.get

class SettingsScreen : ManagerTab {
    override val options: TabOptions
        @Composable get() = TabOptions(
            title = R.string.title_settings,
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )

    @Composable
    override fun Content() {
        val viewModel: SettingsViewModel = getScreenModel()
        val prefs: PreferenceManager = get()
        val ctx = LocalContext.current

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            SettingsHeader(stringResource(R.string.settings_appearance))
            SettingsSwitch(
                label = stringResource(R.string.settings_dynamic_color),
                secondaryLabel = stringResource(R.string.settings_dynamic_color_description),
                pref = prefs.monet,
                onPrefChange = {
                    prefs.monet = it
                },
                disabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            )
            SettingsItemChoice(
                label = stringResource(R.string.settings_theme),
                pref = prefs.theme,
                labelFactory = {
                    ctx.getString(it.labelRes)
                },
                onPrefChange = {
                    prefs.theme = it
                }
            )

            SettingsHeader(stringResource(R.string.settings_advanced))
            SettingsTextField(
                label = stringResource(R.string.settings_app_name),
                pref = prefs.appName,
                onPrefChange = {
                    prefs.appName = it
                }
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_app_icon),
                secondaryLabel = stringResource(R.string.settings_app_icon_description),
                pref = prefs.patchIcon,
                onPrefChange = {
                    prefs.patchIcon = it
                }
            )

            SettingsButton(
                label = stringResource(R.string.action_clear_cache),
                onClick = {
                    viewModel.clearCache()
                }
            )
        }
    }

    @Composable
    override fun Actions() {
    }
}