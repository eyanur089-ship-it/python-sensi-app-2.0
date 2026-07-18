package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModelSettingsScreen(
    brandName: String,
    modelName: String,
    onBack: () -> Unit
) {
    val pureBlack = Color(0xFF000000)
    val cardBackground = Color(0xFF0F0F0F)
    val brightRed = Color(0xFFFF1E1E)
    val lightGrayBorder = Color(0xFF1A1A1A)

    val context = LocalContext.current

    // Load persistent, independently random settings for this specific model
    val settings = remember(context, brandName, modelName) {
        ModelRepository.getOrGenerateSettingsForModel(context, brandName, modelName)
    }

    // Keep state values initialized to the model's fixed generated settings
    var generalVal by remember(settings) { mutableStateOf(settings.general.toFloat()) }
    var redDotVal by remember(settings) { mutableStateOf(settings.redDot.toFloat()) }
    var scope2xVal by remember(settings) { mutableStateOf(settings.scope2x.toFloat()) }
    var scope4xVal by remember(settings) { mutableStateOf(settings.scope4x.toFloat()) }
    var sniperVal by remember(settings) { mutableStateOf(settings.sniper.toFloat()) }
    var freeLookVal by remember(settings) { mutableStateOf(settings.freeLook.toFloat()) }

    // Helper lambda to save settings locally
    val saveSettings = { g: Float, r: Float, s2: Float, s4: Float, sn: Float, fl: Float ->
        ModelRepository.saveSettingsForModel(
            context,
            brandName,
            modelName,
            SensitivitySettings(
                general = g.toInt(),
                redDot = r.toInt(),
                scope2x = s2.toInt(),
                scope4x = s4.toInt(),
                sniper = sn.toInt(),
                freeLook = fl.toInt(),
                recommendedDpi = settings.recommendedDpi,
                fireButtonSize = settings.fireButtonSize
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = pureBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(pureBlack)
        ) {
            // 1. Pristine Top Header Area: Back Button + Red Phone/Gear Icon + Phone Model Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Gear Settings",
                    tint = brightRed,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = modelName.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // 2. Scrollable content for the sliders and boxes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Slider Card Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBackground)
                        .border(1.dp, lightGrayBorder, RoundedCornerShape(12.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // General Slider (100 - 200)
                        SensitivitySliderItem(
                            title = "General",
                            value = generalVal,
                            valueRange = 0f..200f,
                            onValueChange = {
                                val newVal = it.coerceIn(100f, 200f)
                                generalVal = newVal
                            },
                            onValueChangeFinished = {
                                saveSettings(generalVal, redDotVal, scope2xVal, scope4xVal, sniperVal, freeLookVal)
                            },
                            brightRed = brightRed
                        )

                        // Red Dot Slider (1 - 200)
                        SensitivitySliderItem(
                            title = "Red Dot",
                            value = redDotVal,
                            valueRange = 0f..200f,
                            onValueChange = {
                                val newVal = it.coerceIn(1f, 200f)
                                redDotVal = newVal
                            },
                            onValueChangeFinished = {
                                saveSettings(generalVal, redDotVal, scope2xVal, scope4xVal, sniperVal, freeLookVal)
                            },
                            brightRed = brightRed
                        )

                        // 2x Scope Slider (100 - 200)
                        SensitivitySliderItem(
                            title = "2x Scope",
                            value = scope2xVal,
                            valueRange = 0f..200f,
                            onValueChange = {
                                val newVal = it.coerceIn(100f, 200f)
                                scope2xVal = newVal
                            },
                            onValueChangeFinished = {
                                saveSettings(generalVal, redDotVal, scope2xVal, scope4xVal, sniperVal, freeLookVal)
                            },
                            brightRed = brightRed
                        )

                        // 4x Scope Slider (100 - 200)
                        SensitivitySliderItem(
                            title = "4x Scope",
                            value = scope4xVal,
                            valueRange = 0f..200f,
                            onValueChange = {
                                val newVal = it.coerceIn(100f, 200f)
                                scope4xVal = newVal
                            },
                            onValueChangeFinished = {
                                saveSettings(generalVal, redDotVal, scope2xVal, scope4xVal, sniperVal, freeLookVal)
                            },
                            brightRed = brightRed
                        )

                        // Sniper Scope Slider (100 - 200)
                        SensitivitySliderItem(
                            title = "Sniper Scope",
                            value = sniperVal,
                            valueRange = 0f..200f,
                            onValueChange = {
                                val newVal = it.coerceIn(100f, 200f)
                                sniperVal = newVal
                            },
                            onValueChangeFinished = {
                                saveSettings(generalVal, redDotVal, scope2xVal, scope4xVal, sniperVal, freeLookVal)
                            },
                            brightRed = brightRed
                        )

                        // Free Look Slider (100 - 200)
                        SensitivitySliderItem(
                            title = "Free Look",
                            value = freeLookVal,
                            valueRange = 0f..200f,
                            onValueChange = {
                                val newVal = it.coerceIn(100f, 200f)
                                freeLookVal = newVal
                            },
                            onValueChangeFinished = {
                                saveSettings(generalVal, redDotVal, scope2xVal, scope4xVal, sniperVal, freeLookVal)
                            },
                            brightRed = brightRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Recommendations Row: Symmetrical boxes for Button Size and DPI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Box: Button Size Suggestion
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cardBackground)
                            .border(1.dp, lightGrayBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.Adjust,
                                contentDescription = "Fire Button Target",
                                tint = brightRed,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column {
                                Text(
                                    text = "BUTTON:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${settings.fireButtonSize}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Right Box: DPI Suggestion
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cardBackground)
                            .border(1.dp, lightGrayBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "Mobile DPI Icon",
                                tint = brightRed,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column {
                                Text(
                                    text = "DPI:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${settings.recommendedDpi}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensitivitySliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    brightRed: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${value.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(2.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = brightRed,
                activeTrackColor = brightRed,
                inactiveTrackColor = Color(0xFF222222),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
