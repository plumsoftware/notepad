package ru.plumsoftware.notepad.data.model

sealed class PlatformConfig (
    open val adsConfig: AdsConfig,
    open val rateUrl: String,
    open val appMetricaId: String
) {
    data class RuStoreConfig (
        override val rateUrl: String = "https://www.rustore.ru/catalog/app/ru.plumsoftware.notepad",
        override val adsConfig: AdsConfig = AdsConfig.RuStoreAds(),
        override val appMetricaId: String = "af0558d0-d94d-43ec-b558-3103ff4837ef"
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl,
        appMetricaId = appMetricaId
    )

    data class HuaweiConfig (
        override val rateUrl: String = "https://appgallery.huawei.ru/app/C115075655",
        override val adsConfig: AdsConfig = AdsConfig.HuaweiAppGalleryAds()
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl,
        appMetricaId = ""
    )

    data class GooglePlayConfig (
        override val rateUrl: String = "https://www.rustore.ru/catalog/app/ru.plumsoftware.notepad",
        override val adsConfig: AdsConfig = AdsConfig.GooglePlayAds()
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl,
        appMetricaId = ""
    )
}