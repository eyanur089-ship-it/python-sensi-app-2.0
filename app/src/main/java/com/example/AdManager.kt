package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // ===================================================================================
    // ADMOB PRODUCTION CONFIGURATION
    // PASTE YOUR REAL ADMOB PRODUCTION AD UNIT IDS HERE!
    // ===================================================================================
    
    // STEP 2: PASTE YOUR REAL ADMOB PRODUCTION BANNER AD UNIT ID
    const val BANNER_AD_UNIT_ID = "ca-app-pub-4710248480448695/5129352957"

    // STEP 3: PASTE YOUR REAL ADMOB PRODUCTION APP OPEN AD UNIT ID
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-4710248480448695/3850141863"

    // STEP 4: PASTE YOUR REAL ADMOB PRODUCTION INTERSTITIAL AD UNIT ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4710248480448695/7762566799"
    
    // ===================================================================================

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdLoading = false
    private var hasShownAppOpenAdOnce = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    /**
     * Safely loads the App Open Ad.
     */
    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isAppOpenAdLoading || hasShownAppOpenAdOnce) return

        isAppOpenAdLoading = true
        val request = AdRequest.Builder().build()
        
        try {
            AppOpenAd.load(
                context.applicationContext,
                APP_OPEN_AD_UNIT_ID,
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isAppOpenAdLoading = false
                        Log.d(TAG, "App Open Ad Loaded Successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isAppOpenAdLoading = false
                        appOpenAd = null
                        Log.e(TAG, "App Open Ad Failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isAppOpenAdLoading = false
            e.printStackTrace()
        }
    }

    /**
     * Shows the App Open Ad exactly once after the Splash Screen.
     */
    fun showAppOpenAdIfLoaded(activity: Activity, onAdClosed: () -> Unit) {
        if (hasShownAppOpenAdOnce) {
            onAdClosed()
            return
        }

        val ad = appOpenAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    hasShownAppOpenAdOnce = true
                    Log.d(TAG, "App Open Ad dismissed")
                    onAdClosed()
                    // Preload next ads
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    hasShownAppOpenAdOnce = true
                    Log.e(TAG, "App Open Ad failed to show: ${adError.message}")
                    onAdClosed()
                    // Preload next ads
                    loadInterstitialAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App Open Ad showed")
                }
            }
            try {
                ad.show(activity)
            } catch (e: Exception) {
                e.printStackTrace()
                hasShownAppOpenAdOnce = true
                onAdClosed()
            }
        } else {
            // Ad not loaded, continue immediately to Entry Screen (No lag, no crashes)
            Log.d(TAG, "App Open Ad not loaded yet. Transitioning directly.")
            hasShownAppOpenAdOnce = true
            onAdClosed()
            // Load interstitial for subsequent navigation
            loadInterstitialAd(activity)
        }
    }

    /**
     * Safely loads the Interstitial Ad.
     */
    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        val request = AdRequest.Builder().build()
        
        try {
            InterstitialAd.load(
                context.applicationContext,
                INTERSTITIAL_AD_UNIT_ID,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        Log.d(TAG, "Interstitial Ad Loaded Successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isInterstitialLoading = false
                        interstitialAd = null
                        Log.e(TAG, "Interstitial Ad Failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isInterstitialLoading = false
            e.printStackTrace()
        }
    }

    /**
     * Shows Interstitial Ad every time before entering Sensitivity page.
     */
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    Log.d(TAG, "Interstitial Ad dismissed")
                    onAdClosed()
                    // Preload next interstitial ad
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    onAdClosed()
                    // Preload next interstitial ad
                    loadInterstitialAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showed")
                }
            }
            try {
                ad.show(activity)
            } catch (e: Exception) {
                e.printStackTrace()
                onAdClosed()
                loadInterstitialAd(activity)
            }
        } else {
            Log.d(TAG, "Interstitial Ad not loaded yet. Continuing normally.")
            onAdClosed()
            loadInterstitialAd(activity)
        }
    }
}
