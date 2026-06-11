package ru.plumsoftware.notepad.ui.ads

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeAdLoader
import com.yandex.mobile.ads.nativeads.NativeAdView
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.ui.theme.Dimens

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
        mutableStateOf(NativeAdSession.getCached(slotKey, adUnitId)?.first)
    }
    var boundView by remember(slotKey) {
        mutableStateOf(NativeAdSession.getCached(slotKey, adUnitId)?.second)
    }

    LaunchedEffect(cacheRevision, slotKey, adUnitId) {
        NativeAdSession.getCached(slotKey, adUnitId)?.let { (ad, view) ->
            nativeAd = ad
            boundView = view
        }
    }

    DisposableEffect(slotKey, adUnitId) {
        if (!NativeAdSession.beginLoad(slotKey, adUnitId)) {
            onDispose { }
            return@DisposableEffect onDispose { }
        }

        val loader = NativeAdLoader(activity)
        loader.loadAd(
            AdRequest.Builder(adUnitId).build(),
            object : NativeAdLoadListener {
                override fun onAdLoaded(ad: NativeAd) {
                    val view = inflateAndBindNativeAd(activity, ad, isDarkTheme)
                    if (view != null) {
                        NativeAdSession.cache(slotKey, adUnitId, ad, view)
                        nativeAd = ad
                        boundView = view
                    } else {
                        NativeAdSession.onLoadFailed(slotKey)
                        nativeAd = null
                        boundView = null
                    }
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    NativeAdSession.onLoadFailed(slotKey)
                    nativeAd = null
                    boundView = null
                }
            },
        )
        onDispose {
            loader.cancelLoading()
        }
    }

    val ad = nativeAd
    val view = boundView

    AnimatedVisibility(
        visible = ad != null && view != null,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        modifier = modifier,
    ) {
        if (ad == null || view == null) return@AnimatedVisibility

        val isImageLayout = view.getTag(R.id.native_ad_is_image_layout) as? Boolean == true

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.spacingM))
                .background(colors.surface),
            factory = {
                (view.parent as? ViewGroup)?.removeView(view)
                view.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view
            },
            update = { nativeAdView ->
                nativeAdView.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                bindNativeAd(nativeAdView, ad, isImageLayout, isDarkTheme)
            },
        )
    }
}
