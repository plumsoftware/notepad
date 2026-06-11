package ru.plumsoftware.notepad.data.model

import ru.plumsoftware.notepad.BuildConfig

sealed class AdsConfig(
    open val openAdsId: String,
    open val interstitialAdsId: String,
    open val rewardedAdsId: String,
    open val bannerAdsId: String,
    open val nativeAdsId: String
) {
    data class RuStoreAds(
        override val openAdsId: String = if (BuildConfig.DEBUG) "demo-appopenad-yandex" else "R-M-16540014-1",
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-16540014-2",
        override val rewardedAdsId: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else "R-M-16540014-3",
        override val bannerAdsId: String = if (BuildConfig.DEBUG) "demo-banner-yandex" else "R-M-16540014-5",
        override val nativeAdsId: String = if (BuildConfig.DEBUG) "demo-native-content-yandex" else "R-M-16540014-4"
    ) : AdsConfig(
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId,
        rewardedAdsId = rewardedAdsId,
        bannerAdsId = bannerAdsId,
        nativeAdsId = nativeAdsId
    )

    data class HuaweiAppGalleryAds(
        override val openAdsId: String = if (BuildConfig.DEBUG) "demo-appopenad-yandex" else "R-M-13909411-1",
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-13909411-3",
        override val rewardedAdsId: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else "R-M-13909411-4",
        override val bannerAdsId: String = if (BuildConfig.DEBUG) "demo-banner-yandex" else "R-M-13909411-2",
        override val nativeAdsId: String = if (BuildConfig.DEBUG) "demo-native-content-yandex" else "R-M-13909411-5"
    ) : AdsConfig(
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId,
        rewardedAdsId = rewardedAdsId,
        bannerAdsId = bannerAdsId,
        nativeAdsId = nativeAdsId
    )

    data class GooglePlayAds(
        override val openAdsId: String = if (BuildConfig.DEBUG) "demo-appopenad-yandex" else "R-M-17900779-2",
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-17900779-3",
        override val rewardedAdsId: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else "R-M-17900779-1",
        override val bannerAdsId: String = if (BuildConfig.DEBUG) "demo-banner-yandex" else "R-M-17900779-5",
        override val nativeAdsId: String = if (BuildConfig.DEBUG) "demo-native-content-yandex" else "R-M-17900779-4"
    ) : AdsConfig(
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId,
        rewardedAdsId = rewardedAdsId,
        bannerAdsId = bannerAdsId,
        nativeAdsId = nativeAdsId
    )
}
