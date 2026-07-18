package com.example

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EntryScreen(onEnterClicked: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Full screen Entry background image
        Image(
            painter = painterResource(id = R.drawable.img_entry),
            contentDescription = "Entry Screen Character Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Single invisible clickable area positioned exactly over the existing "ENTER - CLICK" button in the image.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp) // Adjusted padding to cover the target area perfectly across screen aspect ratios
                .width(340.dp) // Slightly wider to ensure it encompasses the full image button width
                .height(115.dp) // Taller vertical padding to accommodate device cropping/scaling differences
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
                .background(Color.Transparent) // Critical: forces Compose to register pointer input on transparent elements
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // No default gray ripple to keep visual design completely clean
                ) {
                    coroutineScope.launch {
                        scale = 0.97f
                        delay(80)
                        scale = 1f
                        onEnterClicked()
                    }
                }
        )
    }
}
