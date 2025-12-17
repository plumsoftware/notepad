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
        override val adsConfig: AdsConfig = AdsConfig.HuaweiAppGalleryAds(),
        override val appMetricaId: String = "92b04e85-68b9-4d89-8593-15b065b1f297"
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl,
        appMetricaId = appMetricaId
    )

    data class GooglePlayConfig (
        override val rateUrl: String = "https://www.rustore.ru/catalog/app/ru.plumsoftware.notepad",
        override val adsConfig: AdsConfig = AdsConfig.GooglePlayAds(),
        override val appMetricaId: String = "e9b63b76-0f46-42d6-8ddf-647f8b987959"
    ) : PlatformConfig (
        adsConfig = adsConfig,
        rateUrl = rateUrl,
        appMetricaId = appMetricaId
    )
}