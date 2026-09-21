package ru.plumsoftware.notepad.ui.ads

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeAdLoader
import com.yandex.mobile.ads.nativeads.NativeAdView

@Composable
fun NativeAdContainer(
    adUnitId: String,
    slotKey: String,
    modifier: Modifier = Modifier,
) {
    if (adUnitId.isBlank()) return

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = colors.background.luminance() < 0.5f
    val cacheRevision = NativeAdSession.cacheRevision

    var nativeAd by remember(slotKey) {
        mutableStateOf(NativeAdSession.getCached(slotKey, adUnitId))
    }

    // Подхватываем рекламу, если её загрузил другой слот / прошлый показ
    LaunchedEffect(cacheRevision, slotKey, adUnitId) {
        NativeAdSession.getCached(slotKey, adUnitId)?.let { nativeAd = it }
    }

    DisposableEffect(slotKey, adUnitId) {
        if (!NativeAdSession.beginLoad(slotKey, adUnitId)) {
            return@DisposableEffect onDispose { }
        }

        val loader = try {
            NativeAdLoader(activity)
        } catch (e: Throwable) {
            NativeAdSession.onLoadFailed(slotKey)
            null
        }

        loader?.let {
            try {
                it.loadAd(
                    AdRequest.Builder(adUnitId).build(),
                    object : NativeAdLoadListener {
                        override fun onAdLoaded(ad: NativeAd) {
                            NativeAdSession.cache(slotKey, adUnitId, ad)
                            nativeAd = ad
                        }

                        override fun onAdFailedToLoad(error: AdRequestError) {
                            NativeAdSession.onLoadFailed(slotKey)
                        }
                    },
                )
            } catch (e: Throwable) {
                NativeAdSession.onLoadFailed(slotKey)
            }
        }

        onDispose {
            try {
                loader?.cancelLoading()
            } catch (_: Throwable) {
            }
        }
    }

    val ad = nativeAd ?: return

    // Свежий View на каждый показ (не переиспользуем закешированный между композициями)
    val boundView: NativeAdView? = remember(ad, isDarkTheme) {
        try {
            inflateAndBindNativeAd(activity, ad, isDarkTheme)
        } catch (e: Throwable) {
            null
        }
    }
    val view = boundView ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.surface)
            .border(
                width = 0.5.dp,
                color = colors.outlineVariant,
                shape = MaterialTheme.shapes.large,
            )
            .padding(4.dp),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                try {
                    (view.parent as? ViewGroup)?.removeView(view)
                } catch (_: Throwable) {
                }
                view.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view as View
            },
            // Повторно НЕ биндим и НЕ пересоздаём — View уже связан с рекламой при инфлейте.
            update = { },
        )
    }
}
