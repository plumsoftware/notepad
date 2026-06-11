package ru.plumsoftware.notepad.ui.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.plumsoftware.notepad.App

@Composable
fun HomeNativeAdCard(
    slotIndex: Int,
    modifier: Modifier = Modifier,
    adUnitId: String = App.platformConfig.adsConfig.nativeAdsId,
) {
    NativeAdContainer(
        adUnitId = adUnitId,
        slotKey = "native_$slotIndex",
        modifier = modifier.fillMaxWidth(),
    )
}
