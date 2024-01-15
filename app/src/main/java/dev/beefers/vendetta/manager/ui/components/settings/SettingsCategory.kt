package dev.beefers.vendetta.manager.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import dev.beefers.vendetta.manager.utils.navigate

@Composable
fun SettingsCategory(
    icon: ImageVector,
    text: String,
    subtext: String,
    destination: (() -> Screen)? = null
) {
    val screen = destination?.invoke()
    val nav = LocalNavigator.current

    Box(
        modifier = Modifier
            .clickable {
                screen?.let { nav?.navigate(it) }
            }
    ) {
        SettingsItem(
            icon = { Icon(icon, null) },
            text = { Text(text) },
            secondaryText = { Text(subtext) }
        )
    }
}