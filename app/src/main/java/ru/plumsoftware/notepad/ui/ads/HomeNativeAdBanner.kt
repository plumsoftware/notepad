package ru.plumsoftware.notepad.ui.ads

import android.view.ViewGroup
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdView
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoader
import kotlinx.coroutines.delay
import ru.plumsoftware.notepad.App
import ru.plumsoftware.notepad.ui.theme.Dimens

private const val HOME_BANNER_SLOT = "home_banner"

// Сколько объявлений запрашиваем одним запросом (максимум, выставленный для блока в кабинете)
private const val HOME_BANNER_MAX_ADS = 4

private const val AUTO_SCROLL_INTERVAL_MS = 6_000L

/** Высота слайдера фиксирована заранее, чтобы контент не прыгал при загрузке (защита от мисклика). */
private const val HOME_BANNER_HEIGHT_DP = 55f
private val HomeNativeAdBannerHeight = HOME_BANNER_HEIGHT_DP.dp

/**
 * Слайдер нативной рекламы под строкой поиска на главном экране.
 * Каждая карточка — по размерам, отступам и скруглениям как [ru.plumsoftware.notepad.ui.elements.NotepadSearchBar].
 * Пока реклама грузится, место занято шиммером; если рекламы нет — блок плавно схлопывается.
 */
@Composable
fun HomeNativeAdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = App.platformConfig.adsConfig.nativeAdsId,
) {
    if (adUnitId.isBlank()) return
    val activity = LocalActivity.current ?: return
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = colors.background.luminance() < 0.5f
    val cacheRevision = NativeAdSession.cacheRevision

    val ads = remember(cacheRevision, adUnitId) {
        NativeAdSession.getCached(HOME_BANNER_SLOT, adUnitId)
    }
    val loadFailed = remember(cacheRevision) { NativeAdSession.isFailed(HOME_BANNER_SLOT) }

    DisposableEffect(adUnitId) {
        if (!NativeAdSession.beginLoad(HOME_BANNER_SLOT, adUnitId)) {
            return@DisposableEffect onDispose { }
        }

        val loader = try {
            NativeBulkAdLoader(activity)
        } catch (e: Throwable) {
            NativeAdSession.onLoadFailed(HOME_BANNER_SLOT)
            null
        }

        try {
            loader?.loadAds(
                AdRequest.Builder(adUnitId).build(),
                HOME_BANNER_MAX_ADS,
                object : NativeBulkAdLoadListener {
                    override fun onAdsLoaded(nativeAds: List<NativeAd>) {
                        if (nativeAds.isEmpty()) {
                            NativeAdSession.onLoadFailed(HOME_BANNER_SLOT)
                        } else {
                            NativeAdSession.cache(HOME_BANNER_SLOT, adUnitId, nativeAds)
                        }
                    }

                    override fun onAdsFailedToLoad(error: AdRequestError) {
                        NativeAdSession.onLoadFailed(HOME_BANNER_SLOT)
                    }
                },
            )
        } catch (e: Throwable) {
            NativeAdSession.onLoadFailed(HOME_BANNER_SLOT)
        }

        onDispose {
            try {
                loader?.cancelLoading()
            } catch (_: Throwable) {
            }
        }
    }

    // Свежие View на каждый показ; объявления, которые нельзя корректно показать
    // в карточке (видео, обязательный ассет без View), пропускаем
    val adViews: List<NativeAdView>? = remember(ads, isDarkTheme) {
        ads?.mapNotNull { ad ->
            try {
                inflateNativeAdBanner(activity, ad, isDarkTheme, HOME_BANNER_HEIGHT_DP)
            } catch (e: Throwable) {
                null
            }
        }
    }
    val nothingToShow = loadFailed || adViews?.isEmpty() == true

    AnimatedVisibility(
        visible = !nothingToShow,
        exit = shrinkVertically() + fadeOut(),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.spacingM)
                .height(HomeNativeAdBannerHeight),
        ) {
            Crossfade(targetState = adViews, label = "native_banner") { views ->
                if (views == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.screenPaddingHorizontal)
                            .clip(MaterialTheme.shapes.medium)
                            .background(colors.surfaceVariant),
                    ) {
                        NativeAdBannerShimmer()
                    }
                } else {
                    NativeAdSlider(views)
                }
            }
        }
    }
}

@Composable
private fun NativeAdSlider(views: List<NativeAdView>) {
    val pagerState = rememberPagerState(pageCount = { views.size })

    if (views.size > 1) {
        // Таймер перезапускается после каждого перелистывания, в том числе ручного
        LaunchedEffect(pagerState.settledPage) {
            delay(AUTO_SCROLL_INTERVAL_MS)
            pagerState.animateScrollToPage((pagerState.settledPage + 1) % views.size)
        }
    }

    HorizontalPager(
        state = pagerState,
        // Карточка шириной как поиск; соседняя чуть выглядывает справа — видно, что это слайдер
        contentPadding = PaddingValues(horizontal = Dimens.screenPaddingHorizontal),
        pageSpacing = Dimens.spacingS,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        val view = views[page]
        AndroidView(
            factory = {
                // Страница могла пересоздаться, пока View ещё числится за прошлым контейнером
                (view.parent as? ViewGroup)?.removeView(view)
                view
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}

@Composable
private fun NativeAdBannerShimmer() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )
    val base = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f)
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    val placeholder = Modifier.shimmer({ progress }, base, highlight, RoundedCornerShape(6.dp))

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(36.dp).then(placeholder))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxWidth(0.65f).height(11.dp).then(placeholder))
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth(0.35f).height(8.dp).then(placeholder))
        }
        Box(modifier = Modifier.width(64.dp).height(28.dp).then(placeholder))
    }
}

/** Блик, пробегающий слева направо. progress читается в фазе отрисовки — без рекомпозиций. */
private fun Modifier.shimmer(
    progress: () -> Float,
    base: Color,
    highlight: Color,
    shape: Shape,
): Modifier = clip(shape).drawBehind {
    drawRect(base)
    val band = size.width * 0.6f
    val x = -band + (size.width + 2 * band) * progress()
    drawRect(
        Brush.linearGradient(
            colors = listOf(Color.Transparent, highlight, Color.Transparent),
            start = Offset(x - band, 0f),
            end = Offset(x, 0f),
        ),
    )
}
