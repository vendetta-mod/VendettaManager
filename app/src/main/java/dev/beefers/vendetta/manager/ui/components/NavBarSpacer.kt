package dev.beefers.vendetta.manager.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.beefers.vendetta.manager.utils.DimenUtils

@Composable
fun NavBarSpacer() {
    Spacer(
        modifier = Modifier.height(DimenUtils.navBarPadding)
    )
}