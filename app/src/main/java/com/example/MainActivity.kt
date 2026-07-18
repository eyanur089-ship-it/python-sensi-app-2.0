package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.MobileAds

/**
 * Screen states for our application navigation stack.
 */
sealed class Screen {
    object Splash : Screen()
    object Entry : Screen()
    object Home : Screen()
    data class BrandModels(val brand: String) : Screen()
    data class ModelSettings(val brand: String, val model: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable modern Edge-to-Edge full bleed layout
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK safely
        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                // Stack-based reactive navigation history
                var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.Splash)) }
                val currentScreen = screenStack.lastOrNull() ?: Screen.Splash

                // Manage hardware back gestures safely
                // Enabled only when depth is deeper than the root (Home screen is depth 1)
                val canGoBack = screenStack.size > 1 && currentScreen != Screen.Entry && currentScreen != Screen.Splash
                BackHandler(enabled = canGoBack) {
                    screenStack = screenStack.dropLast(1)
                }

                // Smooth Crossfade routing (60 FPS)
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 300),
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        Screen.Splash -> {
                            SplashScreen(
                                onTimeout = {
                                    // Show App Open Ad if cached/loaded, then safely transition to Entry Screen
                                    AdManager.showAppOpenAdIfLoaded(this@MainActivity) {
                                        // Flush stack and transition to interactive Entry Screen
                                        screenStack = listOf(Screen.Entry)
                                    }
                                }
                            )
                        }
                        Screen.Entry -> {
                            EntryScreen(
                                onEnterClicked = {
                                    // Flush stack and transition to main HomePage (clearing back history completely)
                                    screenStack = listOf(Screen.Home)
                                }
                            )
                        }
                        Screen.Home -> {
                            HomePage(
                                onBrandClicked = { selectedBrand ->
                                    screenStack = screenStack + Screen.BrandModels(selectedBrand)
                                },
                                onModelClicked = { brand, model ->
                                    // Intercept navigation to Sensitivity screen to show Interstitial Ad
                                    AdManager.showInterstitialAd(this@MainActivity) {
                                        screenStack = screenStack + Screen.ModelSettings(brand, model)
                                    }
                                }
                            )
                        }
                        is Screen.BrandModels -> {
                            BrandModelsScreen(
                                brandName = screen.brand,
                                onBack = {
                                    screenStack = screenStack.dropLast(1)
                                },
                                onModelClicked = { brand, model ->
                                    // Intercept navigation to Sensitivity screen to show Interstitial Ad
                                    AdManager.showInterstitialAd(this@MainActivity) {
                                        screenStack = screenStack + Screen.ModelSettings(brand, model)
                                    }
                                }
                            )
                        }
                        is Screen.ModelSettings -> {
                            ModelSettingsScreen(
                                brandName = screen.brand,
                                modelName = screen.model,
                                onBack = {
                                    screenStack = screenStack.dropLast(1)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
