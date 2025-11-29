package ru.plumsoftware.notepad.data.model

import ru.plumsoftware.notepad.BuildConfig

sealed class AdsConfig(
    open val openAdsId: String,
    open val interstitialAdsId: String,
    open val rewardedAdsId: String
) {
    data class RuStoreAds (
        override val openAdsId: String = if (BuildConfig.DEBUG) "demo-appopenad-yandex" else "R-M-16540014-1",
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-16540014-2",
        override val rewardedAdsId: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else "R-M-16540014-3"
    ) : AdsConfig (
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId,
        rewardedAdsId = rewardedAdsId
    )

    data class HuaweiAppGalleryAds (
        override val openAdsId: String =  if (BuildConfig.DEBUG) "demo-appopenad-yandex" else "R-M-13909411-1",
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-13909411-3",
        override val rewardedAdsId: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else "R-M-13909411-4"
    ) : AdsConfig (
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId,
        rewardedAdsId = rewardedAdsId
    )

    data class GooglePlayAds (
        override val openAdsId: String =  if (BuildConfig.DEBUG) "demo-appopenad-yandex" else "R-M-13909411-1",
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-13909411-3",
        override val rewardedAdsId: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else "R-M-13909411-4"
    ) : AdsConfig (
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId,
        rewardedAdsId = rewardedAdsId
    )
}
