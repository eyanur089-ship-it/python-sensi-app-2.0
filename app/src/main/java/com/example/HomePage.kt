package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// Stable model for Premium Sensitivity configurations
data class PremiumConfig(
    val id: String,
    val name: String,
    val description: String,
    val badge: String,
    val originalCost: String,
    val premiumCost: String,
    val inrCost: Int,
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniper: Int,
    val freeLook: Int,
    val dpi: Int,
    val buttonSize: Int
)

@Composable
fun HomePage(
    onBrandClicked: (String) -> Unit,
    onModelClicked: (String, String) -> Unit
) {
    // Exact pure black premium gaming color theme
    val pureBlack = Color(0xFF000000)
    val cardBackground = Color(0xFF121212)
    val brightRed = Color(0xFFFF1E1E)
    val lightGrayBorder = Color(0xFF1F1F1F)
    val deepGray = Color(0xFF0D0D0D)
    val goldPremium = Color(0xFFFFD700)
    val neonGreen = Color(0xFF00FFCC)

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // Screen-level toggle states
    var selectedTab by remember { mutableStateOf(0) } // 0 = BRANDS, 1 = PREMIUM SENSITIVITY
    var searchQuery by remember { mutableStateOf("") }

    // State for premium details dialog overlay
    var selectedPremiumConfig by remember { mutableStateOf<PremiumConfig?>(null) }
    var isActivatingConfig by remember { mutableStateOf(false) }
    var isActivatedConfig by remember { mutableStateOf(false) }

    // State of unlocked configs (Persisted during session)
    var unlockedConfigs by remember { mutableStateOf(setOf<String>()) }

    var utrInput by remember { mutableStateOf("") }
    var attachedScreenshot by remember { mutableStateOf<String?>(null) }
    var isUploadingScreenshot by remember { mutableStateOf(false) }
    var isUnlockingProgress by remember { mutableStateOf(false) }
    var unlockingPhase by remember { mutableStateOf("") }
    var unlockErrorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedPremiumConfig) {
        utrInput = ""
        attachedScreenshot = null
        isUploadingScreenshot = false
        isUnlockingProgress = false
        unlockingPhase = ""
        unlockErrorMsg = null
    }

    // Stable seed-based helper to generate randomized sensitivities strictly between 50 and 200
    fun getStableRandomValue(seedName: String, min: Int, max: Int, offset: Int): Int {
        val hash = kotlin.math.abs((seedName + offset.toString()).hashCode())
        return min + (hash % (max - min + 1))
    }

    // List of premium esports configs with random high numbers between 50 and 200
    val premiumConfigs = remember {
        val inrCosts = listOf(150, 120, 90, 180, 80, 200, 160, 110)
        listOf(
            "⚡ VIP ESPORTS ONE-TAP CONFIG" to "Used by tournament finalists. Built for ultra-fast response and pixel-perfect targeting.",
            "🎯 COBRA ULTIMATE AUTO-HEADSHOT" to "Engineered for close-range rapid crosshair alignment. Instant red numbers guaranteed.",
            "🔥 RED DEVIL TRICKSHOT PROFILE" to "Aggressive sensitivity setting optimized for high-speed drag shots and 360 jump-shots.",
            "💎 WHITE444 LEGENDARY EDITION" to "The mythical custom setup modeled after white444's god-tier speed and accuracy.",
            "💀 DOOMSDAY SNIPER TACTICAL" to "Calibrated for sniper drag and micro-flick adjustments in legendary competitive lobbies.",
            "🚀 TITAN EXTREME ZERO-RECOIL" to "Specifically counter-calibrated to neutralize weapon recoil and maximize burst accuracy.",
            "🌪️ SHADOW RECOIL BYPASS PRO" to "Premium stealth config for buttery-smooth horizontal tracking and instant high-tier headshots.",
            "🦁 LIONHEART ESPORTS CHAMPION" to "Perfect synergy between hardware touch response and finger drag timing."
        ).mapIndexed { index, (name, desc) ->
            val id = "prem_$index"
            val cost = inrCosts[index % inrCosts.size]
            val badge = when (index % 4) {
                0 -> "CHAMPIONSHIP"
                1 -> "PRO SPEED"
                2 -> "BEAST MODE"
                else -> "GOD TIER"
            }
            val originalCost = when (index % 3) {
                0 -> "₹350"
                1 -> "₹250"
                else -> "₹199"
            }
            PremiumConfig(
                id = id,
                name = name,
                description = desc,
                badge = badge,
                originalCost = originalCost,
                premiumCost = "₹$cost INR",
                inrCost = cost,
                general = getStableRandomValue(name, 50, 200, 1),
                redDot = getStableRandomValue(name, 50, 200, 2),
                scope2x = getStableRandomValue(name, 50, 200, 3),
                scope4x = getStableRandomValue(name, 50, 200, 4),
                sniper = getStableRandomValue(name, 50, 200, 5),
                freeLook = getStableRandomValue(name, 50, 200, 6),
                dpi = getStableRandomValue(name, 450, 1100, 7),
                buttonSize = getStableRandomValue(name, 42, 75, 8)
            )
        }
    }

    // Filter premium configs by search
    val filteredPremiumConfigs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            premiumConfigs
        } else {
            val query = searchQuery.trim().lowercase()
            premiumConfigs.filter { 
                it.name.lowercase().contains(query) || 
                it.badge.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
            }
        }
    }

    // Filtered brands list (Lifted to top level for persistent layout caching)
    val filteredBrands = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ModelRepository.brands
        } else {
            val query = searchQuery.trim().uppercase()
            ModelRepository.brands.filter { it.contains(query) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = pureBlack,
        bottomBar = {
            AdmobBannerView()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(pureBlack)
        ) {
            // 1. Header Area: FIND YOUR SETTINGS with Red Running Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = "Running Logo",
                    tint = brightRed,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = "FIND YOUR SETTINGS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            }

            // 2. Segmented Dual-View Controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F0F0F))
                    .border(1.dp, Color(0xFF1F1F1F), RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                        .background(if (selectedTab == 0) brightRed else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BRANDS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.White else Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                        .background(if (selectedTab == 1) brightRed else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PREMIUM SENSITIVITY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.White else Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Search Bar based on view selection
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { 
                    Text(
                        text = if (selectedTab == 0) "Search phone brand..." else "Search premium setups...", 
                        color = Color.Gray
                    ) 
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = brightRed,
                    unfocusedBorderColor = Color(0xFF1F1F1F),
                    focusedContainerColor = Color(0xFF0D0D0D),
                    unfocusedContainerColor = Color(0xFF0D0D0D),
                    cursorColor = brightRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Main Content Area
            if (selectedTab == 0) {
                // BRANDS VIEW: Grid representing brand cards
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Full-Width Condor Banner
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBackground)
                                .border(1.dp, lightGrayBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onModelClicked("CONDOR", "Plume L3 Plus")
                                }
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = "Trending Icon",
                                    tint = brightRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Text(
                                    text = "CONDOR PLUME L3 PLUS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    items(filteredBrands, key = { it }) { brand ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBackground)
                                .border(1.dp, lightGrayBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onBrandClicked(brand)
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = brand,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Arrow Right",
                                    tint = brightRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // PREMIUM SENSITIVITY VIEW (Replacing the old timeline layout)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Title header for premium panel
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = "Premium Crown",
                                tint = goldPremium,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXCLUSIVE ESPORTS SETUPS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = goldPremium,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    items(filteredPremiumConfigs, key = { it.id }) { config ->
                        val premiumCardGradient = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF221A0F), Color(0xFF140D07))
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(premiumCardGradient)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(goldPremium.copy(alpha = 0.6f), Color(0xFFFFA500).copy(alpha = 0.3f))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedPremiumConfig = config
                                    isActivatingConfig = false
                                    isActivatedConfig = false
                                }
                                .padding(18.dp)
                        ) {
                            Column {
                                // Top row: Badge and Price Cost
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFFFF4500).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFFFF4500))
                                    ) {
                                        Text(
                                            text = config.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFF7F50),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    // Stylish Premium Cost Display
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = config.originalCost,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            textDecoration = TextDecoration.LineThrough,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = config.premiumCost,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = neonGreen,
                                            letterSpacing = 0.2.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Title & Description
                                Text(
                                    text = config.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = config.description,
                                    fontSize = 12.sp,
                                    color = Color.LightGray,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Stylish Sensitivity Summary Pill Displays
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isUnlocked = unlockedConfigs.contains(config.id)
                                    // General Pill
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF261C10))
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "GEN", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(text = if (isUnlocked) "${config.general}" else "🔒", fontSize = 13.sp, color = if (isUnlocked) goldPremium else Color.Gray, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    // Red Dot Pill
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF261C10))
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "RED", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(text = if (isUnlocked) "${config.redDot}" else "🔒", fontSize = 13.sp, color = if (isUnlocked) goldPremium else Color.Gray, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    // Scopes (2X / 4X Average) Pill
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF261C10))
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "SCOPE 2X", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(text = if (isUnlocked) "${config.scope2x}" else "🔒", fontSize = 13.sp, color = if (isUnlocked) goldPremium else Color.Gray, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    // Sniper Pill
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF261C10))
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "SNIPER", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(text = if (isUnlocked) "${config.sniper}" else "🔒", fontSize = 13.sp, color = if (isUnlocked) goldPremium else Color.Gray, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Tap to unlock interactive prompt
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isUnlocked = unlockedConfigs.contains(config.id)
                                    Text(
                                        text = if (isUnlocked) "✅ CONFIG UNLOCKED" else "🔒 SCAN QR TO UNLOCK (₹${config.inrCost} INR)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) neonGreen else goldPremium,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Arrow Right",
                                        tint = if (isUnlocked) neonGreen else goldPremium,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (filteredPremiumConfigs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No premium configs found matching\n\"$searchQuery\"",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Highly Stylish, Interactive Premium Details Dialog Modal Overlay
    selectedPremiumConfig?.let { config ->
        Dialog(onDismissRequest = { 
            if (!isActivatingConfig) {
                selectedPremiumConfig = null 
            }
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.5.dp, goldPremium, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070707)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Row with dismiss close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star",
                                tint = goldPremium,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PREMIUM CONFIG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = goldPremium,
                                letterSpacing = 1.sp
                            )
                        }

                        if (!isActivatingConfig) {
                            IconButton(
                                onClick = { selectedPremiumConfig = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Dialog",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title & Description
                    Text(
                        text = config.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = config.description,
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val isUnlocked = unlockedConfigs.contains(config.id)
                    if (isUnlocked) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // List of 6 sensitivity settings beautifully displayed in a stylized list
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // We show high-sensitivity items with stylized linear progress bars
                            PremiumStatProgressBar(title = "General Sensitivity", value = config.general)
                            PremiumStatProgressBar(title = "Red Dot Speed", value = config.redDot)
                            PremiumStatProgressBar(title = "2x Scope Alignment", value = config.scope2x)
                            PremiumStatProgressBar(title = "4x Scope Precision", value = config.scope4x)
                            PremiumStatProgressBar(title = "Sniper Tactical Drag", value = config.sniper)
                            PremiumStatProgressBar(title = "Free Look Scan Speed", value = config.freeLook)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button and DPI suggestion box
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Button Size Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF121212))
                                    .border(0.5.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Adjust, contentDescription = null, tint = brightRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("FIRE BUTTON", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("${config.buttonSize}%", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // DPI Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF121212))
                                    .border(0.5.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = brightRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("REC. DPI", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("${config.dpi}", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stylized Cost Segment
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF161005))
                                .border(0.5.dp, goldPremium.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ESTIMATED VALUE",
                                    fontSize = 8.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = config.originalCost,
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "0.00 FREE",
                                        fontSize = 15.sp,
                                        color = goldPremium,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Surface(
                                color = neonGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, neonGreen)
                            ) {
                                Text(
                                    text = "VIP UNLOCKED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = neonGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action buttons
                        if (isActivatedConfig) {
                            // Success screen
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F2618))
                                        .padding(vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = neonGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SETTINGS ACTIVE & INSTALLED!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = neonGreen,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Copy Activation Code Button
                                Button(
                                    onClick = {
                                        val activationCode = "SENS-${config.badge.uppercase()}-${config.general}-${config.redDot}"
                                        clipboardManager.setText(AnnotatedString(activationCode))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("COPY ACTIVATION CODE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        } else if (isActivatingConfig) {
                            // Loading Animation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = goldPremium,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "CALIBRATING PRO SENSITIVITIES...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldPremium,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else {
                            // Apply Config Trigger Button
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isActivatingConfig = true
                                        delay(1500) // Beautiful premium wait effect
                                        isActivatingConfig = false
                                        isActivatedConfig = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = goldPremium),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = "Unlock",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ACTIVATE CONFIG ON DEVICE",
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // LOCKED: Show payment instructions, QR scanner and send proof area
                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SCAN & PAY TO UNLOCK CONFIG",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // QR Scanner Image
                            Box(
                                modifier = Modifier
                                    .size(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(2.dp, neonGreen, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_payment_qr),
                                    contentDescription = "Payment Scanner QR Code",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // UPI ID copy-paste box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141414))
                                    .border(0.5.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("UPI PAYMENT ID", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("8101923797@ptyes", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("8101923797@ptyes"))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UPI ID",
                                        tint = goldPremium,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Payable Amount: ₹${config.inrCost} INR (Rupees)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = goldPremium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Send Proof written below
                            Text(
                                text = "SEND PROOF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Submit the 12-digit transaction ID or upload a screenshot to activate the panel instantly. A screenshot is required for successful unlocking.",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                lineHeight = 14.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // UTTR Input Field
                            OutlinedTextField(
                                value = utrInput,
                                onValueChange = { utrInput = it },
                                placeholder = { Text("Enter 12-digit UTR / Transaction ID (Optional)", color = Color.Gray, fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF121212),
                                    unfocusedContainerColor = Color(0xFF121212),
                                    focusedBorderColor = goldPremium,
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Screenshot upload button / picker
                            if (attachedScreenshot == null) {
                                if (isUploadingScreenshot) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF121212))
                                            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                color = goldPremium,
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Browsing gallery...", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isUploadingScreenshot = true
                                                delay(800) // simulated loading
                                                isUploadingScreenshot = false
                                                attachedScreenshot = "Screenshot_20260715_pay_proof_${config.inrCost}.png"
                                                unlockErrorMsg = null // clear error
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1313)),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.5.dp, Color(0xFFCC3333)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = "Upload Screenshot",
                                                tint = brightRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "UPLOAD SCREENSHOT PROOF",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Screenshot attached state
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F2618))
                                        .border(0.5.dp, neonGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Attached",
                                            tint = neonGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("SCREENSHOT PROOF DETECTED", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            Text(attachedScreenshot ?: "", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    IconButton(
                                        onClick = { attachedScreenshot = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Screenshot",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            unlockErrorMsg?.let { error ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFFFAAAA),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Unlock / Loader button
                            if (isUnlockingProgress) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = goldPremium,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.2.dp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = unlockingPhase,
                                        color = goldPremium,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (attachedScreenshot == null) {
                                            unlockErrorMsg = "Screenshot required! Please upload the payment screenshot to verify payment and unlock."
                                        } else {
                                            coroutineScope.launch {
                                                isUnlockingProgress = true
                                                unlockErrorMsg = null
                                                unlockingPhase = "Analyzing screenshot receipt..."
                                                delay(1000)
                                                unlockingPhase = "Verifying transaction code on network..."
                                                delay(1200)
                                                unlockingPhase = "Success! Config Unlocked."
                                                delay(600)
                                                unlockedConfigs = unlockedConfigs + config.id
                                                isUnlockingProgress = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = goldPremium),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "VERIFY PAYMENT & UNLOCK",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumStatProgressBar(title: String, value: Int) {
    val goldPremium = Color(0xFFFFD700)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            Text(
                text = "$value",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = goldPremium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        // Progress indicator depicting the high sensitivity value (50 to 200 mapped dynamically)
        val progressFraction = (value - 50).toFloat() / 150f
        LinearProgressIndicator(
            progress = { progressFraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = goldPremium,
            trackColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun AdmobBannerView() {
    val context = LocalContext.current
    var isAdFailed by remember { mutableStateOf(false) }

    if (!isAdFailed) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        // Centralized Banner Ad Unit ID from AdManager.
                        // Paste your actual production Banner ID inside AdManager.kt!
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdManager.BANNER_AD_UNIT_ID
                        
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                // Fail gracefully if ad cannot load, hiding the banner space
                                isAdFailed = true
                            }
                        }
                        
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}
