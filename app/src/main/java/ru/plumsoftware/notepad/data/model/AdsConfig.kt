package ru.plumsoftware.notepad.data.model

sealed class AdsConfig(
    open val openAdsId: String,
    open val interstitialAdsId: String
) {
    data class RuStoreAds (
        override val openAdsId: String = "R-M-16540014-1",
        override val interstitialAdsId: String = "R-M-16540014-2"
    ) : AdsConfig (
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId
    )

    data class HuaweiAppGalleryAds (
        override val openAdsId: String = "R-M-13909411-1",
        override val interstitialAdsId: String = "R-M-13909411-3"
    ) : AdsConfig (
        openAdsId = openAdsId,
        interstitialAdsId = interstitialAdsId
    )
}
