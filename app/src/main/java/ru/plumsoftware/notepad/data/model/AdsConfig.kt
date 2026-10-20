package ru.plumsoftware.notepad.data.model

import ru.plumsoftware.notepad.BuildConfig

sealed class AdsConfig(
    open val interstitialAdsId: String,
    open val bannerAdsId: String,
    open val nativeAdsId: String
) {
    data class RuStoreAds(
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-16540014-2",
        override val bannerAdsId: String = if (BuildConfig.DEBUG) "demo-banner-yandex" else "R-M-16540014-5",
        override val nativeAdsId: String = if (BuildConfig.DEBUG) "demo-native-content-yandex" else "R-M-16540014-4"
    ) : AdsConfig(
        interstitialAdsId = interstitialAdsId,
        bannerAdsId = bannerAdsId,
        nativeAdsId = nativeAdsId
    )

    data class HuaweiAppGalleryAds(
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-13909411-3",
        override val bannerAdsId: String = if (BuildConfig.DEBUG) "demo-banner-yandex" else "R-M-13909411-2",
        override val nativeAdsId: String = if (BuildConfig.DEBUG) "demo-native-content-yandex" else "R-M-13909411-5"
    ) : AdsConfig(
        interstitialAdsId = interstitialAdsId,
        bannerAdsId = bannerAdsId,
        nativeAdsId = nativeAdsId
    )

    data class GooglePlayAds(
        override val interstitialAdsId: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-17900779-3",
        override val bannerAdsId: String = if (BuildConfig.DEBUG) "demo-banner-yandex" else "R-M-17900779-5",
        override val nativeAdsId: String = if (BuildConfig.DEBUG) "demo-native-content-yandex" else "R-M-17900779-4"
    ) : AdsConfig(
        interstitialAdsId = interstitialAdsId,
        bannerAdsId = bannerAdsId,
        nativeAdsId = nativeAdsId
    )
}
