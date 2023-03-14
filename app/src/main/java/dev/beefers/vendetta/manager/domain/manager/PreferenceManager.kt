package dev.beefers.vendetta.manager.domain.manager

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.base.BasePreferenceManager

class PreferenceManager(private val context: Context) :
    BasePreferenceManager(context.getSharedPreferences("prefs", Context.MODE_PRIVATE)) {

    var packageName by stringPreference("package_name", "dev.beefers.vendetta")

    var appName by stringPreference("app_name", "Vendetta")

    var discordVersion by stringPreference("discord_version", "")

    var patchIcon by booleanPreference("patch_icon", true)

    var debuggable by booleanPreference("debuggable", false)

    var monet by booleanPreference("monet", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    var isDeveloper by booleanPreference("is_developer", true)

    var theme by enumPreference("theme", Theme.SYSTEM)

}

enum class Theme(@StringRes val labelRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}