package ru.plumsoftware.notepad.data.model

sealed class PlatformConfig (
    open val adsConfig: AdsConfig,
    open val rateUrl: String
) {
    data class RuStoreConfig (
        override val rateUrl: String = "https://www.rustore.ru/catalog/app/ru.plumsoftware.notepad",
        override val adsConfig: AdsConfig = AdsConfig.RuStoreAds()
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl
    )

    data class HuaweiConfig (
        override val rateUrl: String = "https://appgallery.huawei.ru/app/C115075655",
        override val adsConfig: AdsConfig = AdsConfig.HuaweiAppGalleryAds()
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl
    )

    data class GooglePlayConfig (
        override val rateUrl: String = "",
        override val adsConfig: AdsConfig = AdsConfig.GooglePlayAds()
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl
    )
}