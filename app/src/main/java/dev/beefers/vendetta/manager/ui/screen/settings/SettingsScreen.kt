package dev.beefers.vendetta.manager.ui.screen.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabOptions
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import dev.beefers.vendetta.manager.domain.manager.Mirror
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.ui.components.settings.SettingsButton
import dev.beefers.vendetta.manager.ui.components.settings.SettingsHeader
import dev.beefers.vendetta.manager.ui.components.settings.SettingsItemChoice
import dev.beefers.vendetta.manager.ui.components.settings.SettingsSwitch
import dev.beefers.vendetta.manager.ui.components.settings.SettingsTextField
import dev.beefers.vendetta.manager.ui.screen.about.AboutScreen
import dev.beefers.vendetta.manager.ui.viewmodel.settings.SettingsViewModel
import dev.beefers.vendetta.manager.utils.DiscordVersion
import dev.beefers.vendetta.manager.utils.ManagerTab
import dev.beefers.vendetta.manager.utils.TabOptions
import dev.beefers.vendetta.manager.utils.navigate
import org.koin.androidx.compose.get
import java.io.File

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
        val installManager: InstallManager = get()
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
            SettingsItemChoice(
                label = stringResource(R.string.settings_check_updates),
                pref = prefs.updateDuration,
                labelFactory = {
                    ctx.getString(it.labelRes)
                },
                onPrefChange = {
                    prefs.updateDuration = it
                    viewModel.updateCheckerDuration(it)
                }
            )
            SettingsItemChoice(
                label = stringResource(R.string.settings_channel),
                pref = prefs.channel,
                labelFactory = {
                    ctx.getString(it.labelRes)
                },
                onPrefChange = {
                    prefs.channel = it
                }
            )
            SettingsItemChoice(
                label = stringResource(R.string.settings_mirror),
                pref = prefs.mirror,
                excludedOptions = listOf(Mirror.VENDETTA_ROCKS),
                labelFactory = {
                    it.baseUrl.toUri().authority ?: it.baseUrl
                },
                onPrefChange = {
                    prefs.mirror = it
                }
            )
            SettingsItemChoice(
                label = stringResource(R.string.install_method),
                pref = prefs.installMethod,
                labelFactory = {
                    ctx.getString(it.labelRes)
                },
                onPrefChange = {
                    prefs.installMethod = it
                }
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_auto_clear_cache),
                secondaryLabel = stringResource(R.string.settings_auto_clear_cache_description),
                pref = prefs.autoClearCache,
                onPrefChange = {
                    prefs.autoClearCache = it
                }
            )

            SettingsButton(
                label = stringResource(R.string.action_clear_cache),
                onClick = {
                    viewModel.clearCache()
                }
            )

            if (prefs.isDeveloper) {
                SettingsHeader(stringResource(R.string.settings_developer))
                SettingsTextField(
                    label = stringResource(R.string.settings_package_name),
                    pref = prefs.packageName,
                    onPrefChange = {
                        prefs.packageName = it
                        installManager.getInstalled()
                    }
                )
                var version by remember {
                    mutableStateOf(prefs.discordVersion)
                }
                var versionError by remember {
                    mutableStateOf(false)
                }
                val supportingText = if (versionError)
                    stringResource(R.string.msg_invalid_version)
                else if (version.isNotBlank())
                    DiscordVersion.fromVersionCode(version).toString()
                else
                    null
                SettingsTextField(
                    label = stringResource(R.string.settings_version),
                    pref = version,
                    error = versionError,
                    supportingText = supportingText,
                    onPrefChange = {
                        version = it
                        if (DiscordVersion.fromVersionCode(it) == null && it.isNotBlank()) {
                            versionError = true
                        } else {
                            versionError = false
                            prefs.discordVersion = it
                        }
                    }
                )
                SettingsSwitch(
                    label = stringResource(R.string.settings_debuggable),
                    secondaryLabel = stringResource(R.string.settings_debuggable_description),
                    pref = prefs.debuggable,
                    onPrefChange = { prefs.debuggable = it }
                )
                SettingsTextField(
                    label = stringResource(R.string.settings_module_location),
                    supportingText = stringResource(R.string.settings_module_location_description),
                    pref = prefs.moduleLocation.absolutePath,
                    onPrefChange = {
                        prefs.moduleLocation = File(it)
                    }
                )
                SettingsButton(
                    label = stringResource(R.string.settings_module_location_reset),
                    onClick = {
                        prefs.moduleLocation = prefs.DEFAULT_MODULE_LOCATION
                    }
                )
            }
        }
    }

    @Composable
    override fun Actions() {
        val navigator = LocalNavigator.currentOrThrow

        IconButton(onClick = { navigator.navigate(AboutScreen()) }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.action_open_about)
            )
        }
    }
}