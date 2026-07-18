package com.example

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Enable true full-screen immersive mode by hiding the system bars
    DisposableEffect(activity) {
        activity?.window?.let { window ->
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            // Restore standard status and navigation bars on exit
            activity?.window?.let { window ->
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Logo entry fade-in state
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = EaseInOutSine),
        label = "LogoFadeIn"
    )

    // Smooth zoom transition from initial state (0.95f) into dynamic breathing
    val entryScaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.95f,
        animationSpec = tween(durationMillis = 1800, easing = EaseOutCubic),
        label = "EntryScale"
    )

    // Infinite transitions for dynamic background glow effects (60 FPS)
    val infiniteTransition = rememberInfiniteTransition(label = "SplashEffects")

    // Ambient purple glow breathing scale
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    // Ambient glow alpha breathing
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    // Dual-frequency chaotic flicker to simulate an organic lightning energy theme
    val flickerWave1 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Flicker1"
    )

    val flickerWave2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Flicker2"
    )

    val combinedLightningGlow = (flickerWave1 * 0.6f + flickerWave2 * 0.4f)

    // Logo continuous breathing animation
    val logoBreathScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoBreath"
    )

    // Combined scale calculation
    val finalLogoScale = entryScaleAnim * logoBreathScale

    // Timer to handle animation start and navigation timeout (Exactly 5 seconds)
    LaunchedEffect(Unit) {
        startAnimation = true
        // Preload App Open Ad immediately so it's fully cached during the 5s splash screen
        AdManager.loadAppOpenAd(context)
        delay(5000)
        onTimeout()
    }

    // Pure black immersive container with premium canvas layers
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .drawBehind {
                val center = size.center

                // 1. Soft Ambient Purple Glow Layer
                val baseGlowRadius = size.minDimension * 0.45f * glowScale
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7A2BFF).copy(alpha = glowAlpha * alphaAnim),
                            Color(0xFF2E0C60).copy(alpha = (glowAlpha * 0.35f) * alphaAnim),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseGlowRadius
                    ),
                    center = center,
                    radius = baseGlowRadius
                )

                // 2. Subtle lightning aura glow layer matching logo's electric thunderbolts
                val electricGlowRadius = size.minDimension * 0.48f * (1.0f + combinedLightningGlow * 0.05f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFCEACFF).copy(alpha = (combinedLightningGlow * 0.35f) * alphaAnim),
                            Color(0xFF6C1CEB).copy(alpha = (combinedLightningGlow * 0.12f) * alphaAnim),
                            Color.Transparent
                        ),
                        center = center,
                        radius = electricGlowRadius
                    ),
                    center = center,
                    radius = electricGlowRadius
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash),
            contentDescription = "Premium Shield Crest",
            modifier = Modifier
                .fillMaxSize(0.85f)
                .alpha(alphaAnim)
                .scale(finalLogoScale),
            contentScale = ContentScale.Fit
        )
    }
}
