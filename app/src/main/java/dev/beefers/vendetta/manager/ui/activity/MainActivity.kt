package dev.beefers.vendetta.manager.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.beefers.vendetta.manager.ui.screen.main.MainScreen
import dev.beefers.vendetta.manager.ui.theme.VendettaManagerTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VendettaManagerTheme {
                Navigator(MainScreen()) {
                    SlideTransition(it)
                }
            }
        }
    }
}