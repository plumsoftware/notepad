package ru.plumsoftware.notepad.ui.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.compose.Banner
import com.yandex.mobile.ads.compose.BannerSize
import com.yandex.mobile.ads.compose.rememberBannerAdState
import ru.plumsoftware.notepad.App
import ru.plumsoftware.notepad.ui.theme.Dimens

@Composable
fun YandexStickyBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = App.platformConfig.adsConfig.bannerAdsId,
    reserveBottomNavBarSpace: Boolean = false,
    includeNavigationBarsPadding: Boolean = true,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val bannerState = rememberBannerAdState(
        adSize = BannerSize.Sticky(width = screenWidth)
    )

    LaunchedEffect(adUnitId) {
        bannerState.loadAd(AdRequest.Builder(adUnitId).build())
    }

    Banner(
        state = bannerState,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (reserveBottomNavBarSpace) {
                    Modifier.padding(bottom = Dimens.bottomBarHeight)
                } else {
                    Modifier
                },
            )
            .then(
                if (includeNavigationBarsPadding) {
                    Modifier.navigationBarsPadding()
                } else {
                    Modifier
                },
            ),
    )
}

/** @see HomeNativeAdCard */
@Composable
fun YandexNativeAdCard(
    slotIndex: Int,
    modifier: Modifier = Modifier,
    adUnitId: String = App.platformConfig.adsConfig.nativeAdsId,
) {
    HomeNativeAdCard(
        slotIndex = slotIndex,
        modifier = modifier,
        adUnitId = adUnitId,
    )
}
