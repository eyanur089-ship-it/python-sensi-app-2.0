package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class SensitivitySettings(
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniper: Int,
    val freeLook: Int,
    val recommendedDpi: Int,
    val fireButtonSize: Int
)

data class PhoneModel(
    val name: String,
    val settings: SensitivitySettings,
    val launchYear: Int
)

val PhoneModel.durationSinceLaunch: String
    get() {
        val currentYear = 2026
        val years = currentYear - launchYear
        return when {
            years < 0 -> "Upcoming"
            years == 0 -> "Launched this year"
            years == 1 -> "Launched 1 year ago"
            else -> "Launched $years years ago"
        }
    }

data class PhoneWithBrand(
    val brand: String,
    val model: PhoneModel
)

object ModelRepository {
    // List of all popular brands requested by the user, ordered elegantly
    val brands = listOf(
        "XIAOMI", "SAMSUNG", "HUAWEI", "MOTOROLA", "REALME", "INFINIX", 
        "TECNO", "OPPO", "VIVO", "HONOR", "ONEPLUS", "LG", 
        "ALCATEL", "ZTE", "BLU", "NOKIA", "TCL", "ASUS", 
        "LENOVO", "APPLE", "REDMI", "POCO", "IQOO", "SONY", 
        "GOOGLE PIXEL", "NOTHING", "MEIZU", "BLACK SHARK", "REDMAGIC", 
        "ROG PHONE", "LAVA", "ITEL", "MICROMAX", "SHARP", "COOLPAD"
    )

    // Helper to generate realistic high-performance gaming/sensitivity settings for any phone model
    fun getSettingsForModel(brand: String, model: String): SensitivitySettings {
        val hash = Math.abs((brand + model).hashCode())
        val general = 100 + (hash % 101)
        val redDot = 1 + (hash % 200)
        val scope2x = 100 + (hash % 101)
        val scope4x = 100 + (hash % 101)
        val sniper = 100 + (hash % 101)
        val freeLook = 100 + (hash % 101)
        val recommendedDpi = 300 + (hash % 201)
        val fireButtonSize = 30 + (hash % 41)

        return SensitivitySettings(
            general = general,
            redDot = redDot,
            scope2x = scope2x,
            scope4x = scope4x,
            sniper = sniper,
            freeLook = freeLook,
            recommendedDpi = recommendedDpi,
            fireButtonSize = fireButtonSize
        )
    }

    // Load or generate unique persistent sensitivity values for a specific phone model
    fun getOrGenerateSettingsForModel(context: Context, brand: String, model: String): SensitivitySettings {
        val prefs = context.getSharedPreferences("sensitivity_settings_prefs", Context.MODE_PRIVATE)
        val keyPrefix = "${brand.uppercase()}_${model.uppercase().replace(" ", "_")}_"
        
        val generalKey = "${keyPrefix}general"
        val redDotKey = "${keyPrefix}red_dot"
        val scope2xKey = "${keyPrefix}scope_2x"
        val scope4xKey = "${keyPrefix}scope_4x"
        val sniperKey = "${keyPrefix}sniper"
        val freeLookKey = "${keyPrefix}free_look"
        val dpiKey = "${keyPrefix}dpi"
        val buttonSizeKey = "${keyPrefix}button_size"

        if (prefs.contains(generalKey)) {
            return SensitivitySettings(
                general = prefs.getInt(generalKey, 100),
                redDot = prefs.getInt(redDotKey, 100),
                scope2x = prefs.getInt(scope2xKey, 100),
                scope4x = prefs.getInt(scope4xKey, 100),
                sniper = prefs.getInt(sniperKey, 100),
                freeLook = prefs.getInt(freeLookKey, 100),
                recommendedDpi = prefs.getInt(dpiKey, 400),
                fireButtonSize = prefs.getInt(buttonSizeKey, 50)
            )
        } else {
            // Generate truly independent random values using Kotlin's Random
            val general = Random.nextInt(100, 201)
            val redDot = Random.nextInt(1, 201)
            val scope2x = Random.nextInt(100, 201)
            val scope4x = Random.nextInt(100, 201)
            val sniper = Random.nextInt(100, 201)
            val freeLook = Random.nextInt(100, 201)
            val buttonSize = Random.nextInt(30, 71)
            val dpi = Random.nextInt(300, 501)

            val settings = SensitivitySettings(
                general = general,
                redDot = redDot,
                scope2x = scope2x,
                scope4x = scope4x,
                sniper = sniper,
                freeLook = freeLook,
                recommendedDpi = dpi,
                fireButtonSize = buttonSize
            )

            // Save immediately
            prefs.edit().apply {
                putInt(generalKey, general)
                putInt(redDotKey, redDot)
                putInt(scope2xKey, scope2x)
                putInt(scope4xKey, scope4x)
                putInt(sniperKey, sniper)
                putInt(freeLookKey, freeLook)
                putInt(dpiKey, dpi)
                putInt(buttonSizeKey, buttonSize)
                apply()
            }

            return settings
        }
    }

    // Save customized sensitivity values back to SharedPreferences for this specific model
    fun saveSettingsForModel(context: Context, brand: String, model: String, settings: SensitivitySettings) {
        val prefs = context.getSharedPreferences("sensitivity_settings_prefs", Context.MODE_PRIVATE)
        val keyPrefix = "${brand.uppercase()}_${model.uppercase().replace(" ", "_")}_"
        
        prefs.edit().apply {
            putInt("${keyPrefix}general", settings.general)
            putInt("${keyPrefix}red_dot", settings.redDot)
            putInt("${keyPrefix}scope_2x", settings.scope2x)
            putInt("${keyPrefix}scope_4x", settings.scope4x)
            putInt("${keyPrefix}sniper", settings.sniper)
            putInt("${keyPrefix}free_look", settings.freeLook)
            putInt("${keyPrefix}dpi", settings.recommendedDpi)
            putInt("${keyPrefix}button_size", settings.fireButtonSize)
            apply()
        }
    }

    private val modelsCache = java.util.concurrent.ConcurrentHashMap<String, List<PhoneModel>>()

    private fun extractYear(modelName: String): Int {
        val regex = "\\((\\d{4})\\)".toRegex()
        val matchResult = regex.find(modelName)
        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 2019
    }

    private fun cleanModelName(modelName: String): String {
        return modelName.replace("\\s*\\(\\d{4}\\)".toRegex(), "").trim()
    }

    // Get phone models for a given brand
    fun getModelsForBrand(brand: String): List<PhoneModel> {
        val uppercaseBrand = brand.uppercase()
        modelsCache[uppercaseBrand]?.let { return it }

        val baseModels = when (uppercaseBrand) {
            "XIAOMI" -> listOf(
                // --- Xiaomi Number Series ---
                "Mi 1 (2011)", "Mi 2 (2012)", "Mi 2S (2013)", "Mi 3 (2013)", 
                "Mi 4 (2014)", "Mi 4i (2015)", "Mi 4c (2015)", "Mi 5 (2016)", 
                "Mi 5s (2016)", "Mi 5s Plus (2016)", "Mi 6 (2017)", "Mi 8 (2018)", 
                "Mi 8 Pro (2018)", "Mi 9 (2019)", "Mi 9 Pro (2019)", "Mi 10 (2020)", 
                "Mi 10 Pro (2020)", "Mi 10 Ultra (2020)", "Mi 11 (2021)", "Mi 11 Pro (2021)", 
                "Mi 11 Ultra (2021)", "Xiaomi 12 (2022)", "Xiaomi 12 Pro (2022)", "Xiaomi 12s Ultra (2022)", 
                "Xiaomi 13 (2023)", "Xiaomi 13 Pro (2023)", "Xiaomi 13 Ultra (2023)", "Xiaomi 14 (2024)", 
                "Xiaomi 14 Pro (2024)", "Xiaomi 14 Ultra (2024)", "Xiaomi 15 (2025)", "Xiaomi 15 Pro (2025)", 
                "Xiaomi 15 Ultra (2025)", "Xiaomi 16 (2026)", "Xiaomi 16 Pro (2026)", "Xiaomi 16 Ultra (2026)",

                // --- Xiaomi T Series ---
                "Mi 9T (2019)", "Mi 9T Pro (2019)", "Mi 10T (2020)", "Mi 10T Pro (2020)", 
                "Xiaomi 11T (2021)", "Xiaomi 11T Pro (2021)", "Xiaomi 12T (2022)", "Xiaomi 12T Pro (2022)", 
                "Xiaomi 13T (2023)", "Xiaomi 13T Pro (2023)", "Xiaomi 14T (2024)", "Xiaomi 14T Pro (2024)", 
                "Xiaomi 15T (2025)", "Xiaomi 15T Pro (2025)",

                // --- Xiaomi Lite Series ---
                "Mi 8 Lite (2018)", "Mi 9 Lite (2019)", "Mi 10 Lite (2020)", "Mi 10i (2021)", 
                "Xiaomi 11 Lite 5G NE (2021)", "Xiaomi 12 Lite (2022)", "Xiaomi 13 Lite (2023)", "Xiaomi 14 Lite (2024)",

                // --- Xiaomi Civi Series ---
                "Xiaomi Civi (2021)", "Xiaomi Civi 1S (2022)", "Xiaomi Civi 2 (2022)", "Xiaomi Civi 3 (2023)", 
                "Xiaomi Civi 4 Pro (2024)",

                // --- Xiaomi Mix & Mix Fold Series ---
                "Mi Mix (2016)", "Mi Mix 2 (2017)", "Mi Mix 2S (2018)", "Mi Mix 3 (2018)", 
                "Mix 4 (2021)", "Mix Fold (2021)", "Mix Fold 2 (2022)", "Mix Fold 3 (2023)", 
                "Mix Fold 4 (2024)", "Mix Flip (2024)", "Mix Fold 5 (2025)",

                // --- Xiaomi Max Series ---
                "Mi Max (2016)", "Mi Max 2 (2017)", "Mi Max 3 (2018)",

                // --- Xiaomi Pad Series ---
                "Mi Pad (2014)", "Mi Pad 2 (2015)", "Mi Pad 3 (2017)", "Mi Pad 4 (2018)", 
                "Xiaomi Pad 5 (2021)", "Xiaomi Pad 5 Pro (2021)", "Xiaomi Pad 6 (2023)", "Xiaomi Pad 6 Pro (2023)", 
                "Xiaomi Pad 6S Pro (2024)", "Xiaomi Pad 7 (2024)", "Xiaomi Pad 7 Pro (2024)"
            )
            "SAMSUNG" -> listOf(
                // --- Galaxy S Series ---
                "Galaxy S (2010)", "Galaxy S Plus (2011)", "Galaxy S II (2011)", "Galaxy S III (2012)", 
                "Galaxy S III Mini (2012)", "Galaxy S4 (2013)", "Galaxy S4 Mini (2013)", "Galaxy S4 Active (2013)", 
                "Galaxy S5 (2014)", "Galaxy S5 Mini (2014)", "Galaxy S5 Active (2014)", "Galaxy S6 (2015)", 
                "Galaxy S6 Edge (2015)", "Galaxy S6 Edge+ (2015)", "Galaxy S6 Active (2015)", "Galaxy S7 (2016)", 
                "Galaxy S7 Edge (2016)", "Galaxy S7 Active (2016)", "Galaxy S8 (2017)", "Galaxy S8+ (2017)", 
                "Galaxy S8 Active (2017)", "Galaxy S9 (2018)", "Galaxy S9+ (2018)", "Galaxy S10 (2019)", 
                "Galaxy S10+ (2019)", "Galaxy S10e (2019)", "Galaxy S10 Lite (2020)", "Galaxy S20 (2020)", 
                "Galaxy S20+ (2020)", "Galaxy S20 Ultra (2020)", "Galaxy S20 FE (2020)", "Galaxy S21 (2021)", 
                "Galaxy S21+ (2021)", "Galaxy S21 Ultra (2021)", "Galaxy S21 FE (2022)", "Galaxy S22 (2022)", 
                "Galaxy S22+ (2022)", "Galaxy S22 Ultra (2022)", "Galaxy S23 (2023)", "Galaxy S23+ (2023)", 
                "Galaxy S23 Ultra (2023)", "Galaxy S23 FE (2023)", "Galaxy S24 (2024)", "Galaxy S24+ (2024)", 
                "Galaxy S24 Ultra (2024)", "Galaxy S24 FE (2024)", "Galaxy S25 (2025)", "Galaxy S25+ (2025)", 
                "Galaxy S25 Ultra (2025)", "Galaxy S25 Slim (2025)", "Galaxy S26 (2026)", "Galaxy S26+ (2026)", 
                "Galaxy S26 Ultra (2026)",

                // --- Galaxy Note Series ---
                "Galaxy Note (2011)", "Galaxy Note II (2012)", "Galaxy Note 3 (2013)", "Galaxy Note 3 Neo (2014)", 
                "Galaxy Note 4 (2014)", "Galaxy Note Edge (2014)", "Galaxy Note 5 (2015)", "Galaxy Note FE (2017)", 
                "Galaxy Note 8 (2017)", "Galaxy Note 9 (2018)", "Galaxy Note 10 (2019)", "Galaxy Note 10+ (2019)", 
                "Galaxy Note 10 Lite (2020)", "Galaxy Note 20 (2020)", "Galaxy Note 20 Ultra (2020)",

                // --- Galaxy Z (Fold/Flip) Series ---
                "Galaxy Fold (2019)", "Galaxy Z Flip (2020)", "Galaxy Z Fold 2 (2020)", "Galaxy Z Flip 5G (2020)", 
                "Galaxy Z Fold 3 (2021)", "Galaxy Z Flip 3 (2021)", "Galaxy Z Fold 4 (2022)", "Galaxy Z Flip 4 (2022)", 
                "Galaxy Z Fold 5 (2023)", "Galaxy Z Flip 5 (2023)", "Galaxy Z Fold 6 (2024)", "Galaxy Z Flip 6 (2024)", 
                "Galaxy Z Fold Special Edition (2024)", "Galaxy Z Fold 7 (2025)", "Galaxy Z Flip 7 (2025)",

                // --- Galaxy A Series ---
                "Galaxy Alpha (2014)", "Galaxy A3 (2015)", "Galaxy A5 (2015)", "Galaxy A7 (2015)", 
                "Galaxy A8 (2015)", "Galaxy A3 (2016)", "Galaxy A5 (2016)", "Galaxy A7 (2016)", 
                "Galaxy A8 (2016)", "Galaxy A9 (2016)", "Galaxy A9 Pro (2016)", "Galaxy A3 (2017)", 
                "Galaxy A5 (2017)", "Galaxy A7 (2017)", "Galaxy A8 (2018)", "Galaxy A8+ (2018)", 
                "Galaxy A6 (2018)", "Galaxy A6+ (2018)", "Galaxy A7 (2018)", "Galaxy A9 (2018)", 
                "Galaxy A8 Star (2018)", "Galaxy A8s (2018)", "Galaxy A10 (2019)", "Galaxy A10s (2019)", 
                "Galaxy A10e (2019)", "Galaxy A20 (2019)", "Galaxy A20s (2019)", "Galaxy A20e (2019)", 
                "Galaxy A30 (2019)", "Galaxy A30s (2019)", "Galaxy A40 (2019)", "Galaxy A50 (2019)", 
                "Galaxy A50s (2019)", "Galaxy A60 (2019)", "Galaxy A70 (2019)", "Galaxy A70s (2019)", 
                "Galaxy A80 (2019)", "Galaxy A90 5G (2019)", "Galaxy A01 (2020)", "Galaxy A11 (2020)", 
                "Galaxy A21 (2020)", "Galaxy A21s (2020)", "Galaxy A31 (2020)", "Galaxy A41 (2020)", 
                "Galaxy A51 (2020)", "Galaxy A51 5G (2020)", "Galaxy A71 (2020)", "Galaxy A71 5G (2020)", 
                "Galaxy A02 (2021)", "Galaxy A02s (2021)", "Galaxy A12 (2020)", "Galaxy A22 (2021)", 
                "Galaxy A22 5G (2021)", "Galaxy A32 (2021)", "Galaxy A32 5G (2021)", "Galaxy A42 5G (2020)", 
                "Galaxy A52 (2021)", "Galaxy A52 5G (2021)", "Galaxy A52s 5G (2021)", "Galaxy A72 (2021)", 
                "Galaxy A03 (2021)", "Galaxy A03s (2021)", "Galaxy A03 Core (2021)", "Galaxy A13 (2022)", 
                "Galaxy A13 5G (2021)", "Galaxy A23 (2022)", "Galaxy A23 5G (2022)", "Galaxy A33 5G (2022)", 
                "Galaxy A53 5G (2022)", "Galaxy A73 5G (2022)", "Galaxy A04 (2022)", "Galaxy A04s (2022)", 
                "Galaxy A04e (2022)", "Galaxy A14 (2023)", "Galaxy A14 5G (2023)", "Galaxy A24 (2023)", 
                "Galaxy A34 5G (2023)", "Galaxy A54 5G (2023)", "Galaxy A05 (2023)", "Galaxy A05s (2023)", 
                "Galaxy A15 (2024)", "Galaxy A15 5G (2024)", "Galaxy A25 5G (2024)", "Galaxy A35 5G (2024)", 
                "Galaxy A55 5G (2024)", "Galaxy A16 5G (2024)", "Galaxy A26 (2025)", "Galaxy A36 (2025)", 
                "Galaxy A56 (2025)",

                // --- Galaxy J Series ---
                "Galaxy J1 (2015)", "Galaxy J1 Ace (2015)", "Galaxy J1 Nxt (2016)", "Galaxy J1 Mini Prime (2016)", 
                "Galaxy J2 (2015)", "Galaxy J2 (2016)", "Galaxy J2 Prime (2016)", "Galaxy J2 Pro (2018)", 
                "Galaxy J2 Core (2018)", "Galaxy J3 (2016)", "Galaxy J3 Pro (2016)", "Galaxy J3 Prime (2017)", 
                "Galaxy J4 (2018)", "Galaxy J4+ (2018)", "Galaxy J4 Core (2018)", "Galaxy J5 (2015)", 
                "Galaxy J5 (2016)", "Galaxy J5 Prime (2016)", "Galaxy J5 Pro (2017)", "Galaxy J6 (2018)", 
                "Galaxy J6+ (2018)", "Galaxy J7 (2015)", "Galaxy J7 (2016)", "Galaxy J7 Prime (2016)", 
                "Galaxy J7 Prime 2 (2018)", "Galaxy J7 Pro (2017)", "Galaxy J7 Max (2017)", "Galaxy J7 Nxt (2017)", 
                "Galaxy J7 Duo (2018)", "Galaxy J8 (2018)",

                // --- Galaxy M Series ---
                "Galaxy M10 (2019)", "Galaxy M10s (2019)", "Galaxy M20 (2019)", "Galaxy M30 (2019)", 
                "Galaxy M30s (2019)", "Galaxy M40 (2019)", "Galaxy M11 (2020)", "Galaxy M21 (2020)", 
                "Galaxy M21s (2020)", "Galaxy M31 (2020)", "Galaxy M31s (2020)", "Galaxy M51 (2020)", 
                "Galaxy M02 (2021)", "Galaxy M02s (2021)", "Galaxy M12 (2021)", "Galaxy M22 (2021)", 
                "Galaxy M32 (2021)", "Galaxy M32 5G (2021)", "Galaxy M42 5G (2021)", "Galaxy M52 5G (2021)", 
                "Galaxy M62 (2021)", "Galaxy M13 (2022)", "Galaxy M13 5G (2022)", "Galaxy M23 (2022)", 
                "Galaxy M33 5G (2022)", "Galaxy M53 5G (2022)", "Galaxy M04 (2022)", "Galaxy M14 (2023)", 
                "Galaxy M14 5G (2023)", "Galaxy M34 5G (2023)", "Galaxy M44 (2023)", "Galaxy M54 5G (2023)", 
                "Galaxy M15 5G (2024)", "Galaxy M35 5G (2024)", "Galaxy M55 5G (2024)",

                // --- Galaxy F Series ---
                "Galaxy F41 (2020)", "Galaxy F02s (2021)", "Galaxy F12 (2021)", "Galaxy F22 (2021)", 
                "Galaxy F42 5G (2021)", "Galaxy F52 5G (2021)", "Galaxy F62 (2021)", "Galaxy F23 5G (2022)", 
                "Galaxy F13 (2022)", "Galaxy F04 (2023)", "Galaxy F14 5G (2023)", "Galaxy F34 5G (2023)", 
                "Galaxy F54 5G (2023)", "Galaxy F15 5G (2024)", "Galaxy F55 5G (2024)"
            )
            "HUAWEI" -> listOf(
                "Ascend P1 (2012)", "Ascend P6 (2013)", "Ascend Mate 7 (2014)", "P8 (2015)", 
                "P9 (2016)", "P10 (2017)", "Mate 10 Pro (2017)", "P20 Pro (2018)", 
                "Mate 20 Pro (2018)", "P30 Pro (2019)", "Mate 30 Pro (2019)", "P40 Pro (2020)", 
                "Mate 40 Pro (2020)", "P50 Pro (2021)", "Mate 50 Pro (2022)", "Nova 11 (2023)", 
                "P60 Pro (2023)", "Mate 60 Pro (2023)", "Pura 70 Ultra (2024)", "Nova 12 Ultra (2024)", 
                "Mate XT Ultimate (2024)", "Mate 70 Pro (2025)"
            )
            "MOTOROLA" -> listOf(
                // --- Moto G Series ---
                "Moto G (2013)", "Moto G2 (2014)", "Moto G3 (2015)", "Moto G4 (2016)", 
                "Moto G5 (2017)", "Moto G5 Plus (2017)", "Moto G6 (2018)", "Moto G7 (2019)", 
                "Moto G8 (2020)", "Moto G9 (2020)", "Moto G10 (2021)", "Moto G30 (2021)", 
                "Moto G50 (2021)", "Moto G100 (2021)", "Moto G22 (2022)", "Moto G52 (2022)", 
                "Moto G62 (2022)", "Moto G72 (2022)", "Moto G82 (2022)", "Moto G13 (2023)", 
                "Moto G23 (2023)", "Moto G54 (2023)", "Moto G84 (2023)", "Moto G24 (2024)", 
                "Moto G64 (2024)", "Moto G85 (2024)", "Moto G55 (2025)", "Moto G95 (2025)",

                // --- Moto E Series ---
                "Moto E (2014)", "Moto E2 (2015)", "Moto E3 (2016)", "Moto E4 (2017)", 
                "Moto E5 (2018)", "Moto E6 (2019)", "Moto E7 (2020)", "Moto E20 (2021)", 
                "Moto E32 (2022)", "Moto E13 (2023)", "Moto E22 (2023)", "Moto E40 (2023)",

                // --- Moto X & Moto Z Series ---
                "Moto X (2013)", "Moto X2 (2014)", "Moto X Style (2015)", "Moto X Play (2015)", 
                "Moto X4 (2017)", "Moto Z (2016)", "Moto Z Play (2016)", "Moto Z2 Play (2017)", 
                "Moto Z3 (2018)", "Moto Z4 (2019)",

                // --- Moto Edge Series ---
                "Edge (2020)", "Edge+ (2020)", "Edge 20 (2021)", "Edge 20 Pro (2021)", 
                "Edge 30 (2022)", "Edge 30 Pro (2022)", "Edge 30 Fusion (2022)", "Edge 30 Ultra (2022)", 
                "Edge 40 (2023)", "Edge 40 Neo (2023)", "Edge 40 Pro (2023)", "Edge 50 Pro (2024)", 
                "Edge 50 Fusion (2024)", "Edge 50 Ultra (2024)", "Edge 50 Neo (2024)", "Edge 60 Pro (2025)",

                // --- Moto Razr Series ---
                "Razr (2019)", "Razr 5G (2020)", "Razr 2022 (2022)", "Razr 40 (2023)", 
                "Razr 40 Ultra (2023)", "Razr 50 (2024)", "Razr 50 Ultra (2024)", "Razr 60 Ultra (2025)",

                // --- Moto One Series ---
                "Motorola One (2018)", "One Power (2018)", "One Vision (2019)", "One Action (2019)", 
                "One Zoom (2019)", "One Hyper (2020)"
            )
            "REALME" -> listOf(
                // --- Realme C Series ---
                "Realme C1 (2018)", "Realme C2 (2019)", "Realme C3 (2020)", "Realme C11 (2020)", 
                "Realme C12 (2020)", "Realme C15 (2020)", "Realme C21 (2021)", "Realme C25 (2021)", 
                "Realme C30 (2022)", "Realme C31 (2022)", "Realme C33 (2022)", "Realme C35 (2022)", 
                "Realme C51 (2023)", "Realme C53 (2023)", "Realme C55 (2023)", "Realme C67 (2023)", 
                "Realme C65 (2024)", "Realme C63 (2024)", "Realme C61 (2024)",

                // --- Realme Number Series (1-14) ---
                "Realme 1 (2018)", "Realme 2 (2018)", "Realme 2 Pro (2018)", "Realme 3 (2019)", 
                "Realme 3 Pro (2019)", "Realme 5 (2019)", "Realme 5 Pro (2019)", "Realme 6 (2020)", 
                "Realme 6 Pro (2020)", "Realme 7 (2020)", "Realme 7 Pro (2020)", "Realme 8 (2021)", 
                "Realme 8 Pro (2021)", "Realme 9 (2022)", "Realme 9 Pro (2022)", "Realme 9 Pro+ (2022)", 
                "Realme 10 (2022)", "Realme 10 Pro (2022)", "Realme 10 Pro+ (2022)", "Realme 11 (2023)", 
                "Realme 11 Pro (2023)", "Realme 11 Pro+ (2023)", "Realme 12 (2024)", "Realme 12 Pro (2024)", 
                "Realme 12 Pro+ (2024)", "Realme 13 (2024)", "Realme 13 Pro (2024)", "Realme 13 Pro+ (2024)", 
                "Realme 14 (2025)", "Realme 14 Pro (2025)", "Realme 14 Pro+ (2025)",

                // --- Realme Narzo Series ---
                "Narzo 10 (2020)", "Narzo 20 (2020)", "Narzo 20 Pro (2020)", "Narzo 30 (2021)", 
                "Narzo 30 Pro (2021)", "Narzo 50 (2022)", "Narzo 50 Pro (2022)", "Narzo 60 (2023)", 
                "Narzo 60 Pro (2023)", "Narzo 70 (2024)", "Narzo 70 Pro (2024)", "Narzo N53 (2023)", 
                "Narzo N55 (2023)",

                // --- Realme GT / GT Neo / GT Master Series ---
                "Realme GT (2021)", "Realme GT Master Edition (2021)", "Realme GT Explorer Master (2021)", 
                "Realme GT Neo (2021)", "Realme GT Neo 2 (2021)", "Realme GT 2 (2022)", "Realme GT 2 Pro (2022)", 
                "Realme GT Neo 3 (2022)", "Realme GT Neo 5 (2023)", "Realme GT5 (2023)", "Realme GT5 Pro (2023)", 
                "Realme GT6 (2024)", "Realme GT6T (2024)", "Realme GT Neo 6 (2024)", "Realme GT7 Pro (2025)",

                // --- Realme X / Q / V Series ---
                "Realme X (2019)", "Realme X2 (2019)", "Realme X2 Pro (2019)", "Realme X3 (2020)", 
                "Realme X3 SuperZoom (2020)", "Realme X7 (2020)", "Realme X7 Pro (2020)", 
                "Realme Q2 (2020)", "Realme Q3 (2021)", "Realme Q5 (2022)", "Realme V3 (2020)", 
                "Realme V5 (2020)", "Realme V11 (2021)", "Realme V15 (2021)", "Realme V23 (2022)", 
                "Realme V25 (2022)"
            )
            "INFINIX" -> listOf(
                // --- Infinix Smart Series ---
                "Smart 2 (2018)", "Smart 3 Plus (2019)", "Smart 4 (2019)", "Smart 5 (2020)", 
                "Smart 6 (2021)", "Smart 7 (2023)", "Smart 8 (2023)", "Smart 8 Pro (2024)",

                // --- Infinix Hot Series ---
                "Hot 2 (2015)", "Hot 3 (2016)", "Hot 4 (2016)", "Hot S (2016)", 
                "Hot 5 (2017)", "Hot 6 Pro (2018)", "Hot 7 (2019)", "Hot 8 (2019)", 
                "Hot 9 (2020)", "Hot 10 (2020)", "Hot 10 Play (2021)", "Hot 11 (2021)", 
                "Hot 11S (2021)", "Hot 12 (2022)", "Hot 12 Play (2022)", "Hot 20 (2022)", 
                "Hot 30 (2023)", "Hot 30i (2023)", "Hot 40 (2023)", "Hot 40 Pro (2023)", 
                "Hot 50 Pro (2024)",

                // --- Infinix Note Series ---
                "Note 3 (2016)", "Note 4 (2017)", "Note 5 (2018)", "Note 7 (2020)", 
                "Note 8 (2020)", "Note 10 (2021)", "Note 10 Pro (2021)", "Note 11 (2021)", 
                "Note 11 Pro (2021)", "Note 12 (2022)", "Note 12 Pro 5G (2022)", "Note 30 (2023)", 
                "Note 30 Pro (2023)", "Note 30 VIP (2023)", "Note 40 (2024)", "Note 40 Pro (2024)", 
                "Note 40 Pro+ (2024)", "Note 50 Pro (2025)",

                // --- Infinix Zero Series ---
                "Zero 3 (2015)", "Zero 4 (2016)", "Zero 5 (2017)", "Zero 8 (2020)", 
                "Zero X Pro (2021)", "Zero Ultra (2022)", "Zero 20 (2022)", "Zero 30 (2023)", 
                "Zero 30 5G (2023)", "Zero 40 5G (2024)",

                // --- Infinix GT Series ---
                "GT 10 Pro (2023)", "GT 20 Pro (2024)", "GT 30 Pro (2025)"
            )
            "TECNO" -> listOf(
                // --- Tecno Spark Series ---
                "Spark 2 (2018)", "Spark 3 (2019)", "Spark 4 (2019)", "Spark 5 (2020)", 
                "Spark 6 (2020)", "Spark 7 (2021)", "Spark 8 (2021)", "Spark 8 Pro (2021)", 
                "Spark 9 (2022)", "Spark 9 Pro (2022)", "Spark 10 (2023)", "Spark 10 Pro (2023)", 
                "Spark 20 (2023)", "Spark 20 Pro (2023)", "Spark 20 Pro+ (2024)", "Spark 30 (2024)", 
                "Spark 30 Pro (2025)",

                // --- Tecno Pop Series ---
                "Pop 1 (2018)", "Pop 2 (2018)", "Pop 3 (2019)", "Pop 4 (2020)", 
                "Pop 5 (2021)", "Pop 6 (2022)", "Pop 7 (2023)", "Pop 8 (2023)",

                // --- Tecno Camon Series ---
                "Camon CX (2017)", "Camon 11 (2018)", "Camon 12 (2019)", "Camon 15 (2020)", 
                "Camon 16 Premier (2020)", "Camon 17 Pro (2021)", "Camon 18 Premier (2021)", 
                "Camon 19 Pro (2022)", "Camon 20 (2023)", "Camon 20 Pro 5G (2023)", "Camon 20 Premier (2023)", 
                "Camon 30 (2024)", "Camon 30 Pro (2024)", "Camon 30 Premier (2024)",

                // --- Tecno Pova Series ---
                "Pova (2020)", "Pova 2 (2021)", "Pova Neo (2021)", "Pova 3 (2022)", 
                "Pova 4 Pro (2022)", "Pova 5 (2023)", "Pova 5 Pro (2023)", "Pova 6 (2024)", 
                "Pova 6 Pro (2024)",

                // --- Tecno Phantom Series ---
                "Phantom 5 (2015)", "Phantom 6 (2016)", "Phantom 8 (2017)", "Phantom 9 (2019)", 
                "Phantom X (2021)", "Phantom X2 Pro (2022)", "Phantom V Fold (2023)", "Phantom V Flip (2023)", 
                "Phantom V Fold2 (2024)", "Phantom V Flip2 (2024)"
            )
            "OPPO" -> listOf(
                // --- Oppo A Series ---
                "A3s (2018)", "A5 (2018)", "A7 (2018)", "A9 (2019)", 
                "A1k (2019)", "A5s (2019)", "A15 (2020)", "A16 (2021)", 
                "A17 (2022)", "A31 (2020)", "A53 (2020)", "A54 (2021)", 
                "A57 (2022)", "A58 (2023)", "A74 (2021)", "A78 (2023)", 
                "A98 (2023)", "A3 Pro (2024)", "A60 (2024)", "A80 (2024)",

                // --- Oppo F Series ---
                "F1 (2016)", "F1 Plus (2016)", "F3 (2017)", "F5 (2017)", 
                "F7 (2018)", "F9 (2018)", "F9 Pro (2018)", "F11 (2019)", 
                "F11 Pro (2019)", "F15 (2020)", "F17 (2020)", "F17 Pro (2020)", 
                "F19 (2021)", "F19 Pro (2021)", "F21 Pro (2022)", "F23 (2023)", 
                "F25 Pro (2024)", "F27 Pro+ (2024)",

                // --- Oppo Reno Series ---
                "Reno (2019)", "Reno 10x Zoom (2019)", "Reno 2 (2019)", "Reno 2F (2019)", 
                "Reno 3 (2020)", "Reno 4 (2020)", "Reno 4 Pro (2020)", "Reno 5 (2021)", 
                "Reno 5 Pro (2021)", "Reno 6 (2021)", "Reno 6 Pro (2021)", "Reno 7 (2022)", 
                "Reno 8 (2022)", "Reno 8 Pro (2022)", "Reno 9 (2022)", "Reno 10 (2023)", 
                "Reno 10 Pro+ (2023)", "Reno 11 (2024)", "Reno 11 Pro (2024)", "Reno 12 (2024)", 
                "Reno 12 Pro (2024)", "Reno 13 (2025)", "Reno 13 Pro (2025)",

                // --- Oppo Find X Series ---
                "Find X (2018)", "Find X2 (2020)", "Find X2 Pro (2020)", "Find X3 (2021)", 
                "Find X3 Pro (2021)", "Find X5 (2022)", "Find X5 Pro (2022)", "Find X6 (2023)", 
                "Find X6 Pro (2023)", "Find X7 Ultra (2024)", "Find X8 (2024)", "Find X8 Pro (2024)", 
                "Find X8 Ultra (2025)",

                // --- Oppo K Series ---
                "K1 (2018)", "K3 (2019)", "K5 (2019)", "K7 (2020)", 
                "K9 (2021)", "K10 (2022)", "K11 (2023)", "K12 (2024)",

                // --- Oppo Neo, Mirror & Joy Series ---
                "Neo 3 (2014)", "Neo 5 (2015)", "Neo 7 (2015)", "Mirror 3 (2015)", 
                "Mirror 5 (2015)", "Joy (2014)", "Joy 3 (2015)",

                // --- Oppo R & N Series ---
                "R1 (2013)", "R5 (2014)", "R7 (2015)", "R9 (2016)", 
                "R11 (2017)", "R15 (2018)", "R17 (2018)", "N1 (2013)", "N3 (2014)"
            )
            "VIVO" -> listOf(
                // --- Vivo Y Series ---
                "Y11 (2019)", "Y12 (2019)", "Y15 (2019)", "Y17 (2019)", 
                "Y20 (2020)", "Y21 (2021)", "Y30 (2020)", "Y33s (2021)", 
                "Y51 (2020)", "Y53 (2017)", "Y73 (2021)", "Y100 (2023)", 
                "Y200 (2023)", "Y300 (2024)", "Y300 Pro (2024)",

                // --- Vivo T Series ---
                "T1 (2022)", "T1 Pro (2022)", "T2 (2023)", "T2 Pro (2023)", 
                "T3 (2024)", "T3 Pro (2024)",

                // --- Vivo V Series ---
                "V1 (2015)", "V3 (2016)", "V3 Max (2016)", "V5 (2016)", 
                "V7 (2017)", "V9 (2018)", "V11 (2018)", "V15 (2019)", 
                "V15 Pro (2019)", "V17 Pro (2019)", "V19 (2020)", "V20 (2020)", 
                "V21 (2021)", "V23 (2022)", "V25 (2022)", "V25 Pro (2022)", 
                "V27 (2023)", "V27 Pro (2023)", "V29 (2023)", "V29 Pro (2023)", 
                "V30 (2024)", "V30 Pro (2024)", "V40 (2024)", "V40 Pro (2024)", 
                "V50 (2025)", "V50 Pro (2025)",

                // --- Vivo X Series ---
                "X1 (2012)", "Xplay 3S (2013)", "X5Max (2014)", "X9 (2016)", 
                "X20 (2017)", "X21 (2018)", "X30 (2019)", "X30 Pro (2019)", 
                "X50 (2020)", "X50 Pro (2020)", "X60 (2021)", "X60 Pro (2021)", 
                "X70 Pro (2021)", "X70 Pro+ (2021)", "X80 Pro (2022)", "X90 (2022)", 
                "X90 Pro+ (2022)", "X100 (2023)", "X100 Pro (2023)", "X100 Ultra (2024)", 
                "X200 (2024)", "X200 Pro (2024)", "X200 Ultra (2025)",

                // --- Vivo X Fold & X Flip Series ---
                "X Fold (2022)", "X Fold+ (2022)", "X Flip (2023)", "X Fold 2 (2023)", 
                "X Fold 3 (2024)", "X Fold 3 Pro (2024)",

                // --- Vivo S Series ---
                "S1 (2019)", "S1 Pro (2019)", "S5 (2019)", "S6 (2020)", 
                "S7 (2020)", "S9 (2021)", "S10 (2021)", "S12 (2021)", 
                "S15 (2022)", "S16 (2022)", "S17 (2023)", "S18 (2023)", 
                "S19 (2024)", "S20 (2024)"
            )
            "HONOR" -> listOf(
                "Honor 3 (2013)", "Honor 6 (2014)", "Honor 7 (2015)", "Honor 8 (2016)", 
                "Honor 9 (2017)", "Honor 10 (2018)", "Honor 20 Pro (2019)", "Honor 30 Pro+ (2020)", 
                "Honor 50 (2021)", "Honor 60 (2021)", "Honor 70 (2022)", "Magic4 Pro (2022)", 
                "Honor 80 (2022)", "Magic5 Pro (2023)", "Honor 90 (2023)", "Magic V2 (2023)", 
                "Honor 100 (2023)", "Magic6 Pro (2024)", "Honor 200 Pro (2024)", "Magic V3 (2024)", 
                "Magic7 Pro (2025)", "Honor 300 Pro (2025)"
            )
            "ONEPLUS" -> listOf(
                // --- OnePlus Number Series ---
                "OnePlus One (2014)", "OnePlus 2 (2015)", "OnePlus X (2015)", "OnePlus 3 (2016)", 
                "OnePlus 3T (2016)", "OnePlus 5 (2017)", "OnePlus 5T (2017)", "OnePlus 6 (2018)", 
                "OnePlus 6T (2018)", "OnePlus 7 (2019)", "OnePlus 7 Pro (2019)", "OnePlus 7T (2019)", 
                "OnePlus 8 (2020)", "OnePlus 8 Pro (2020)", "OnePlus 8T (2020)", "OnePlus 9 (2021)", 
                "OnePlus 9 Pro (2021)", "OnePlus 10 Pro (2022)", "OnePlus 10T (2022)", "OnePlus 11 (2023)", 
                "OnePlus 12 (2024)", "OnePlus 13 (2025)", "OnePlus 13 Pro (2025)",

                // --- OnePlus R Series ---
                "OnePlus 9R (2021)", "OnePlus 9RT (2021)", "OnePlus 10R (2022)", "OnePlus 11R (2023)", 
                "OnePlus 12R (2024)", "OnePlus 13R (2025)",

                // --- OnePlus Nord / Nord CE / Nord N Series ---
                "OnePlus Nord (2020)", "OnePlus Nord N10 (2020)", "OnePlus Nord N100 (2020)", "OnePlus Nord CE (2021)", 
                "OnePlus Nord 2 (2021)", "OnePlus Nord N200 (2021)", "OnePlus Nord CE 2 (2022)", "OnePlus Nord 2T (2022)", 
                "OnePlus Nord N20 (2022)", "OnePlus Nord CE 3 (2023)", "OnePlus Nord 3 (2023)", "OnePlus Nord CE 3 Lite (2023)", 
                "OnePlus Nord N30 (2023)", "OnePlus Nord 4 (2024)", "OnePlus Nord CE 4 (2024)", "OnePlus Nord CE 4 Lite (2024)",

                // --- OnePlus Open Series ---
                "OnePlus Open (2023)", "OnePlus Open 2 (2025)",

                // --- OnePlus Ace Series ---
                "OnePlus Ace (2022)", "OnePlus Ace Pro (2022)", "OnePlus Ace 2 (2023)", "OnePlus Ace 2 Pro (2023)", 
                "OnePlus Ace 3 (2024)", "OnePlus Ace 3 Pro (2024)"
            )
            "LG" -> listOf(
                "Optimus One (2010)", "Optimus 2X (2011)", "Optimus G (2012)", "Nexus 4 (2012)", 
                "G2 (2013)", "Nexus 5 (2013)", "G3 (2014)", "G4 (2015)", 
                "V10 (2015)", "G5 (2016)", "V20 (2016)", "G6 (2017)", 
                "V30 (2017)", "G7 ThinQ (2018)", "V40 ThinQ (2018)", "G8 ThinQ (2019)", 
                "V50 ThinQ (2019)", "Velvet (2020)", "V60 ThinQ (2020)", "Wing (2020)"
            )
            "ALCATEL" -> listOf(
                "One Touch Easy (2010)", "One Touch Scribe HD (2013)", "One Touch Idol (2013)", "Idol 3 (2015)", 
                "Idol 4S (2016)", "Alcatel 1 (2018)", "Alcatel 3V (2018)", "Alcatel 5 (2018)", 
                "Alcatel 1S (2019)", "Alcatel 3X (2019)", "Alcatel 1S (2020)", "Alcatel 3L (2020)", 
                "Alcatel 1S (2021)", "Alcatel 3H (2021)", "Alcatel 1B (2022)", "Alcatel 1V (2022)"
            )
            "ZTE" -> listOf(
                "Blade (2010)", "Skate (2011)", "Era (2012)", "Grand S (2013)", 
                "Nubia Z5 (2013)", "Nubia Z7 (2014)", "Axon Pro (2015)", "Axon 7 (2016)", 
                "Blade V8 (2017)", "Axon 9 Pro (2018)", "Axon 10 Pro (2019)", "Axon 11 (2020)", 
                "Axon 20 5G (2020)", "Axon 30 Ultra (2021)", "Axon 40 Ultra (2022)", "Axon 50 Ultra (2023)", 
                "Nubia Z50S Pro (2023)", "Nubia Z60 Ultra (2024)", "Axon 60 Ultra (2024)", "Nubia Z70 Ultra (2025)"
            )
            "BLU" -> listOf(
                "Studio 5.3 (2011)", "Life One (2013)", "Vivo IV (2014)", "Pure XL (2015)", 
                "Vivo 5 (2016)", "R1 HD (2016)", "Vivo XI+ (2018)", "G9 Pro (2019)", 
                "Bold N1 (2019)", "G90 Pro (2020)", "G91 Pro (2021)", "Bold N2 (2022)", 
                "F92 5G (2022)", "G93 (2023)", "Bold N3 (2023)", "G94 (2024)", "Bold N4 (2025)"
            )
            "NOKIA" -> listOf(
                // --- Nokia C Series ---
                "C1 (2019)", "C2 (2020)", "C10 (2021)", "C20 (2021)", 
                "C30 (2021)", "C12 (2023)", "C22 (2023)", "C32 (2023)",

                // --- Nokia G Series ---
                "G10 (2021)", "G20 (2021)", "G50 (2021)", "G11 (2022)", 
                "G21 (2022)", "G60 5G (2022)", "G22 (2023)", "G42 5G (2023)",

                // --- Nokia X & XR Series ---
                "X10 (2021)", "X20 (2021)", "X30 5G (2022)", "XR20 (2021)", "XR21 (2023)",

                // --- Nokia T Series ---
                "T20 Tablet (2021)", "T10 Tablet (2022)", "T21 Tablet (2022)",

                // --- Nokia Lumia Series ---
                "Lumia 800 (2011)", "Lumia 900 (2012)", "Lumia 920 (2012)", "Lumia 520 (2013)", 
                "Lumia 1020 (2013)", "Lumia 1520 (2013)", "Lumia 930 (2014)", "Lumia 535 (2014)", 
                "Lumia 640 XL (2015)", "Lumia 950 XL (2015)",

                // --- Nokia Asha Series ---
                "Asha 200 (2011)", "Asha 300 (2011)", "Asha 305 (2012)", "Asha 501 (2013)",

                // --- Nokia N & E Series ---
                "N95 (2007)", "N97 (2009)", "N8 (2010)", "N9 (2011)", 
                "E71 (2008)", "E72 (2009)", "E6 (2011)", "E7 (2011)"
            )
            "TCL" -> listOf(
                "TCL Plex (2019)", "TCL 10 Pro (2020)", "TCL 10 5G (2020)", "TCL 20 Pro 5G (2021)", 
                "TCL 20 SE (2021)", "TCL 30 V 5G (2022)", "TCL 30 SE (2022)", "TCL 40 XE (2023)", 
                "TCL 40 NxtPaper (2023)", "TCL 50 XL (2024)", "TCL 505 (2024)", "TCL 50 Pro (2024)", 
                "TCL 60 Pro (2025)"
            )
            "ASUS" -> listOf(
                "PadFone (2012)", "ZenFone 4 (2014)", "ZenFone 5 (2014)", "ZenFone 6 (2014)", 
                "ZenFone 2 (2015)", "ZenFone 3 (2016)", "ZenFone AR (2017)", "ZenFone 4 Pro (2017)", 
                "ZenFone 5Z (2018)", "ZenFone 6 (2019)", "Zenfone 7 Pro (2020)", "Zenfone 8 (2021)", 
                "Zenfone 9 (2022)", "Zenfone 10 (2023)", "Zenfone 11 Ultra (2024)", "Zenfone 12 Pro (2025)"
            )
            "LENOVO" -> listOf(
                "LePhone (2010)", "K800 (2012)", "K900 (2013)", "Vibe Z (2013)", 
                "Vibe Z2 Pro (2014)", "Phab Plus (2015)", "ZUK Z1 (2015)", "ZUK Z2 Pro (2016)", 
                "Phab 2 Pro (2016)", "K8 Note (2017)", "Z5 Pro (2018)", "Z6 Pro (2019)", 
                "Legion Duel (2020)", "Legion Duel 2 (2021)", "Legion Y70 (2022)", "Legion Y90 (2022)", 
                "K14 Plus (2023)"
            )
            "APPLE" -> listOf(
                "iPhone 4 (2010)", "iPhone 4S (2011)", "iPhone 5 (2012)", "iPhone 5S (2013)", 
                "iPhone 6 (2014)", "iPhone 6S (2015)", "iPhone SE (2016)", "iPhone 7 Plus (2016)", 
                "iPhone X (2017)", "iPhone XR (2018)", "iPhone XS Max (2018)", "iPhone 11 Pro Max (2019)", 
                "iPhone 12 Pro Max (2020)", "iPhone 13 Pro Max (2021)", "iPhone 14 Pro Max (2022)", 
                "iPhone 15 Pro Max (2023)", "iPhone 16 Pro Max (2024)", "iPhone 17 Pro Max (2025)"
            )
            "REDMI" -> listOf(
                // --- Redmi Number & A Series ---
                "Redmi 1 (2013)", "Redmi 2 (2015)", "Redmi 3 (2016)", "Redmi 3S (2016)", 
                "Redmi 4 (2016)", "Redmi 5 (2017)", "Redmi 6 (2018)", "Redmi 7 (2019)", 
                "Redmi 8 (2019)", "Redmi 9 (2020)", "Redmi 10 (2021)", "Redmi 12 (2023)", 
                "Redmi 13C (2023)", "Redmi A1 (2022)", "Redmi A2 (2023)", "Redmi A3 (2024)",

                // --- Redmi Note Series ---
                "Redmi Note (2014)", "Redmi Note 2 (2015)", "Redmi Note 3 (2015)", "Redmi Note 4 (2016)", 
                "Redmi Note 5 (2018)", "Redmi Note 5 Pro (2018)", "Redmi Note 6 Pro (2018)", "Redmi Note 7 Pro (2019)", 
                "Redmi Note 8 Pro (2019)", "Redmi Note 9 Pro (2020)", "Redmi Note 10 Pro (2021)", "Redmi Note 11 Pro (2022)", 
                "Redmi Note 12 Pro+ (2023)", "Redmi Note 13 Pro+ (2024)", "Redmi Note 14 Pro+ (2024)",

                // --- Redmi K Series ---
                "Redmi K20 Pro (2019)", "Redmi K30 Pro (2020)", "Redmi K40 Pro (2021)", "Redmi K50 Pro (2022)", 
                "Redmi K60 Pro (2022)", "Redmi K70 Pro (2023)", "Redmi K80 Pro (2024)",

                // --- Redmi Turbo Series ---
                "Redmi Turbo 3 (2024)"
            )
            "POCO" -> listOf(
                // --- POCO C Series ---
                "POCO C3 (2020)", "POCO C40 (2022)", "POCO C55 (2023)", "POCO C65 (2023)",

                // --- POCO M Series ---
                "POCO M2 Pro (2020)", "POCO M3 (2020)", "POCO M3 Pro (2021)", "POCO M4 Pro (2022)", 
                "POCO M5 (2022)", "POCO M6 Pro (2024)",

                // --- POCO X Series ---
                "POCO X2 (2020)", "POCO X3 NFC (2020)", "POCO X3 Pro (2021)", "POCO X4 Pro 5G (2022)", 
                "POCO X5 Pro 5G (2023)", "POCO X6 Pro (2024)", "POCO X7 Pro (2025)",

                // --- POCO F Series ---
                "POCO F1 (2018)", "POCO F2 Pro (2020)", "POCO F3 (2021)", "POCO F4 GT (2022)", 
                "POCO F5 Pro (2023)", "POCO F6 Pro (2024)", "POCO F7 Ultra (2025)"
            )
            "IQOO" -> listOf(
                // --- iQOO Number Series ---
                "iQOO (2019)", "iQOO 3 (2020)", "iQOO 5 Pro (2020)", "iQOO 7 (2021)", 
                "iQOO 8 Pro (2021)", "iQOO 9 Pro (2022)", "iQOO 10 Pro (2022)", "iQOO 11 Pro (2022)", 
                "iQOO 12 Pro (2023)", "iQOO 13 (2024)",

                // --- iQOO Neo Series ---
                "iQOO Neo (2019)", "iQOO Neo 3 (2020)", "iQOO Neo 5 (2021)", "iQOO Neo 6 (2022)", 
                "iQOO Neo 7 (2023)", "iQOO Neo 9 Pro (2024)", "iQOO Neo 10 (2025)",

                // --- iQOO Z & U Series ---
                "iQOO Z1 (2020)", "iQOO Z3 (2021)", "iQOO Z5 (2021)", "iQOO Z6 (2022)", 
                "iQOO Z7 Pro (2023)", "iQOO Z9 Turbo (2024)", "iQOO U1 (2020)", "iQOO U3 (2021)", 
                "iQOO U5 (2022)"
            )
            "SONY" -> listOf(
                "Xperia X10 (2010)", "Xperia Arc (2011)", "Xperia S (2012)", "Xperia Z (2013)", 
                "Xperia Z2 (2014)", "Xperia Z3 (2014)", "Xperia Z5 Premium (2015)", "Xperia XZ (2016)", 
                "Xperia XZ Premium (2017)", "Xperia XZ2 (2018)", "Xperia 1 (2019)", "Xperia 5 (2019)", 
                "Xperia 1 II (2020)", "Xperia 5 II (2020)", "Xperia 1 III (2021)", "Xperia 1 IV (2022)", 
                "Xperia 1 V (2023)", "Xperia 1 VI (2024)", "Xperia 1 VII (2025)"
            )
            "GOOGLE PIXEL" -> listOf(
                "Galaxy Nexus (2011)", "Nexus 4 (2012)", "Nexus 5 (2013)", "Nexus 6 (2014)", 
                "Nexus 5X (2015)", "Nexus 6P (2015)", "Pixel (2016)", "Pixel XL (2016)", 
                "Pixel 2 XL (2017)", "Pixel 3 XL (2018)", "Pixel 3a XL (2019)", "Pixel 4 XL (2019)", 
                "Pixel 4a (2020)", "Pixel 5 (2020)", "Pixel 6 Pro (2021)", "Pixel 7 Pro (2022)", 
                "Pixel 7a (2023)", "Pixel 8 Pro (2023)", "Pixel 8a (2024)", "Pixel 9 Pro XL (2024)", 
                "Pixel 10 Pro (2025)"
            )
            "NOTHING" -> listOf(
                "Phone (1) (2022)", "Phone (2) (2023)", "Phone (2a) (2024)", "CMF Phone 1 (2024)", 
                "Phone (3) (2025)"
            )
            "MEIZU" -> listOf(
                "M9 (2011)", "MX (2011)", "MX2 (2012)", "MX3 (2013)", 
                "MX4 Pro (2014)", "PRO 5 (2015)", "PRO 6 (2016)", "15 Plus (2018)", 
                "16th (2018)", "Meizu 16s (2019)", "Meizu 17 Pro (2020)", "Meizu 18s Pro (2021)", 
                "Meizu 20 Pro (2023)", "Meizu 21 PRO (2024)", "Meizu 22 Pro (2025)"
            )
            "BLACK SHARK" -> listOf(
                "Black Shark (2018)", "Black Shark Helo (2018)", "Black Shark 2 (2019)", "Black Shark 2 Pro (2019)", 
                "Black Shark 3 Pro (2020)", "Black Shark 3S (2020)", "Black Shark 4 Pro (2021)", "Black Shark 4S Pro (2021)", 
                "Black Shark 5 Pro (2022)", "Black Shark 5S (2023)"
            )
            "REDMAGIC" -> listOf(
                "Nubia Red Magic (2018)", "Red Magic Mars (2018)", "Red Magic 3 (2019)", "Red Magic 3S (2019)", 
                "Red Magic 5G (2020)", "Red Magic 5S (2020)", "Red Magic 6 Pro (2021)", "Red Magic 6R (2021)", 
                "Red Magic 7 Pro (2022)", "Red Magic 7S Pro (2022)", "Red Magic 8 Pro (2023)", "Red Magic 8S Pro (2023)", 
                "Red Magic 9 Pro (2023)", "Red Magic 9S Pro (2024)", "RedMagic 10 Pro (2024)", "RedMagic 11 Pro (2025)"
            )
            "ROG PHONE" -> listOf(
                "ROG Phone (2018)", "ROG Phone II (2019)", "ROG Phone 3 (2020)", "ROG Phone 5 (2021)", 
                "ROG Phone 5s Pro (2021)", "ROG Phone 6 (2022)", "ROG Phone 6D Ultimate (2022)", "ROG Phone 7 Ultimate (2023)", 
                "ROG Phone 8 Pro (2024)", "ROG Phone 9 Pro (2024)", "ROG Phone 10 Pro (2025)"
            )
            "LAVA" -> listOf(
                // --- Lava Blaze Series ---
                "Blaze (2022)", "Blaze Pro (2022)", "Blaze 2 (2023)", "Blaze 2 Pro (2023)", 
                "Blaze Curve 5G (2024)",

                // --- Lava Agni Series ---
                "Agni 5G (2021)", "Agni 2 5G (2023)", "Agni 3 (2024)",

                // --- Lava Yuva Series ---
                "Yuva (2022)", "Yuva 2 Pro (2023)", "Yuva 3 Pro (2023)",

                // --- Lava Storm Series ---
                "Storm 5G (2023)",

                // --- Lava O & Z Series ---
                "O1 (2023)", "O2 (2024)", "Z2 (2021)", "Z4 (2021)", 
                "Z6 (2021)", "Z25 (2017)", "Z90 (2017)", "Z61 (2018)", 
                "Z92 (2019)", "Z66 (2020)",

                // --- Lava Iris Series ---
                "Iris 501 (2013)", "Iris Pro 30 (2014)", "Iris Fuel 50 (2014)"
            )
            "ITEL" -> listOf(
                // --- Itel A Series ---
                "A16 (2018)", "A33 (2019)", "A56 (2020)", "A48 (2021)", 
                "A25 (2021)", "A49 (2022)", "A60 (2023)", "A70 (2023)",

                // --- Itel S Series ---
                "S15 (2019)", "S16 (2020)", "S17 (2021)", "S18 (2022)", 
                "S23 (2023)", "S23+ (2023)", "S24 (2024)",

                // --- Itel P Series ---
                "P36 (2020)", "P37 (2021)", "P40 (2023)", "P55 (2023)", 
                "P55+ (2024)",

                // --- Itel Vision & RS Series ---
                "Vision 1 (2020)", "Vision 1 Pro (2021)", "Vision 2 (2021)", "Vision 3 (2022)", 
                "RS4 (2024)"
            )
            "MICROMAX" -> listOf(
                "Canvas 2 (2012)", "Canvas HD (2013)", "Canvas Knight (2014)", "Canvas Spark (2015)", 
                "Dual 5 (2017)", "Bharat 2 (2017)", "Evok Dual Note (2017)", "Infinity N11 (2018)", 
                "iOne (2019)", "In Note 1 (2020)", "In 1b (2020)", "In Note 2 (2022)", 
                "In 2c (2022)", "In 2b (2021)"
            )
            "SHARP" -> listOf(
                "Aquos Phone SH-12C (2011)", "Aquos Phone Zeta (2012)", "Aquos Crystal (2014)", "Aquos XX (2015)", 
                "Aquos R (2017)", "Aquos R2 (2018)", "Aquos Zero (2018)", "Aquos R3 (2019)", 
                "Aquos Sense3 (2019)", "Aquos Zero2 (2020)", "Aquos R5G (2020)", "Aquos R6 (2021)", 
                "Aquos Sense6 (2021)", "Aquos R7 (2022)", "Aquos Sense7 (2022)", "Aquos R8 Pro (2023)", 
                "Aquos Sense8 (2023)", "Aquos R9 (2024)", "Aquos Wish4 (2024)"
            )
            "COOLPAD" -> listOf(
                "Coolpad F1 (2014)", "Dazen 1 (2015)", "Note 3 (2015)", "Max (2016)", 
                "Cool 1 (2016)", "Note 5 (2016)", "Play 6 (2017)", "Cool 3 (2019)", 
                "Cool 5 (2019)", "Cool 6 (2020)", "Cool 12A (2020)", "Cool 20 (2021)", 
                "Cool 20 Pro (2021)", "Cool 30 (2023)", "Grand View 40 (2023)", "Golden Century (2024)"
            )
            else -> listOf("Plume L3 Plus (2019)", "Plume L2 (2018)", "Plume L1 (2017)", "Griffe T8 (2016)")
        }

        val modelsList = baseModels.map { baseName ->
            val cleanName = cleanModelName(baseName)
            val launchYear = extractYear(baseName)
            PhoneModel(
                name = cleanName,
                settings = getSettingsForModel(brand, cleanName),
                launchYear = launchYear
            )
        }
        modelsCache[uppercaseBrand] = modelsList
        return modelsList
    }

    // Cached list of all phones mapped by brand to easily display timelines and full chronological listings
    val allModelsCache: List<PhoneWithBrand> by lazy {
        brands.flatMap { brand ->
            getModelsForBrand(brand).map { model ->
                PhoneWithBrand(brand = brand, model = model)
            }
        }
    }
}
