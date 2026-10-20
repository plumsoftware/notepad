package ru.plumsoftware.notepad.ui.ads

import android.content.Context
import android.graphics.Outline
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import com.yandex.mobile.ads.common.AdBindingResult
import com.yandex.mobile.ads.nativeads.MediaView
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdAssets
import com.yandex.mobile.ads.nativeads.NativeAdView
import com.yandex.mobile.ads.nativeads.NativeAdViewBinder
import ru.plumsoftware.notepad.R
import kotlin.math.ceil

// Минимальная высота строки предупреждения, при которой текст ещё читается
private const val WARNING_MIN_HEIGHT_DP = 10f

// Запас сверх минимальной площади предупреждения из объявления
private const val WARNING_AREA_SAFETY = 1.25f

// Вертикальные отступы основной строки карточки (paddingTop + paddingBottom в разметке)
private const val CONTENT_VERTICAL_PADDING_DP = 8f

private const val MEDIA_MAX_WIDTH_DP = 120f

/**
 * Инфлейтит и привязывает карточку слайдера высотой [cardHeightDp].
 * null — если объявление нельзя корректно показать в такой карточке
 * (видео требует MediaView от 300×160 dp) или SDK отказался биндить.
 */
internal fun inflateNativeAdBanner(
    context: Context,
    nativeAd: NativeAd,
    isDarkTheme: Boolean,
    cardHeightDp: Float,
): NativeAdView? {
    val assets = nativeAd.adAssets ?: return null
    if (assets.media?.hasVideo == true) return null

    val nativeAdView = LayoutInflater.from(context)
        .inflate(R.layout.view_native_ad_banner, null) as NativeAdView
    if (!prepareBannerLayout(nativeAdView, assets, cardHeightDp)) return null

    val binder = NativeAdViewBinder.Builder(nativeAdView)
        .setMediaView(nativeAdView.findViewById(R.id.native_ad_media))
        .setIconView(nativeAdView.findViewById(R.id.native_ad_icon))
        .setFaviconView(nativeAdView.findViewById(R.id.native_ad_favicon))
        .setTitleView(nativeAdView.findViewById(R.id.native_ad_title))
        .setBodyView(nativeAdView.findViewById(R.id.native_ad_body))
        .setCallToActionView(nativeAdView.findViewById(R.id.native_ad_call_to_action))
        .setWarningView(nativeAdView.findViewById(R.id.native_ad_warning))
        .setDomainView(nativeAdView.findViewById(R.id.native_ad_domain))
        .setSponsoredView(nativeAdView.findViewById(R.id.native_ad_sponsored))
        .setFeedbackView(nativeAdView.findViewById(R.id.native_ad_feedback))
        .setAgeView(nativeAdView.findViewById(R.id.native_ad_age))
        .setPriceView(nativeAdView.findViewById(R.id.native_ad_price))
        .build()

    // Failure — в объявлении есть обязательный ассет, для которого у нас нет View (например, рейтинг)
    if (nativeAd.bindNativeAd(binder) is AdBindingResult.Failure) return null

    applyBannerStyle(nativeAdView, isDarkTheme)
    return nativeAdView
}

/**
 * Показывает ровно те элементы, которые пришли в объявлении, и подгоняет размеры
 * медиа и предупреждения под карточку. false — если предупреждение не помещается.
 */
private fun prepareBannerLayout(
    nativeAdView: NativeAdView,
    assets: NativeAdAssets,
    cardHeightDp: Float,
): Boolean {
    val density = nativeAdView.resources.displayMetrics.density
    fun View.showIf(present: Boolean) {
        visibility = if (present) View.VISIBLE else View.GONE
    }

    // Предупреждение растягиваем на всю ширину: его доля площади = высота строки / высота карточки
    val warningView = nativeAdView.findViewById<TextView>(R.id.native_ad_warning)
    val warning = assets.warning?.takeIf { it.value.isNotBlank() }
    var warningHeightDp = 0f
    if (warning != null) {
        val required = warning.minimumRequiredArea
        // SDK может отдать долю (0.1) или проценты (10) — приводим к доле
        val fraction = if (required > 1f) required / 100f else required
        warningHeightDp = maxOf(
            WARNING_MIN_HEIGHT_DP,
            ceil(fraction * cardHeightDp * WARNING_AREA_SAFETY),
        )
        // Если предупреждение съедает больше половины карточки — объявление в слайдер не берём
        if (warningHeightDp > cardHeightDp / 2) return false
        warningView.layoutParams = warningView.layoutParams.apply {
            height = (warningHeightDp * density).toInt()
        }
    }
    warningView.showIf(warning != null)

    // Картинка медийного объявления — во всю высоту строки, ширина по пропорциям креатива
    val mediaView = nativeAdView.findViewById<MediaView>(R.id.native_ad_media)
    val media = assets.media
    if (media != null) {
        val rowHeightDp = cardHeightDp - warningHeightDp - CONTENT_VERTICAL_PADDING_DP
        val ratio = media.aspectRatio.takeIf { it > 0f } ?: 1f
        val widthDp = (rowHeightDp * ratio).coerceIn(rowHeightDp * 0.5f, MEDIA_MAX_WIDTH_DP)
        mediaView.layoutParams = mediaView.layoutParams.apply {
            width = (widthDp * density).toInt()
        }
    }
    mediaView.showIf(media != null)

    nativeAdView.findViewById<View>(R.id.native_ad_icon).showIf(assets.icon != null)
    nativeAdView.findViewById<View>(R.id.native_ad_favicon).showIf(assets.favicon != null)

    val hasTitle = !assets.title.isNullOrBlank()
    nativeAdView.findViewById<View>(R.id.native_ad_title).showIf(hasTitle)
    nativeAdView.findViewById<TextView>(R.id.native_ad_body).apply {
        showIf(!assets.body.isNullOrBlank())
        // Без заголовка текст объявления получает его место
        maxLines = if (hasTitle) 1 else 2
    }
    nativeAdView.findViewById<View>(R.id.native_ad_sponsored).showIf(!assets.sponsored.isNullOrBlank())
    nativeAdView.findViewById<View>(R.id.native_ad_age).showIf(!assets.age.isNullOrBlank())
    nativeAdView.findViewById<View>(R.id.native_ad_domain).showIf(!assets.domain.isNullOrBlank())
    nativeAdView.findViewById<View>(R.id.native_ad_price).showIf(!assets.price.isNullOrBlank())
    nativeAdView.findViewById<View>(R.id.native_ad_call_to_action)
        .showIf(!assets.callToAction.isNullOrBlank())
    return true
}

private fun applyBannerStyle(nativeAdView: NativeAdView, isDarkTheme: Boolean) {
    val context = nativeAdView.context
    val density = context.resources.displayMetrics.density
    val textColors = nativeAdTextColors(isDarkTheme)

    val cornerRadiusPx = 6f * density
    val roundedOutline = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
        }
    }
    listOf(R.id.native_ad_icon, R.id.native_ad_media).forEach { id ->
        nativeAdView.findViewById<View>(id).apply {
            clipToOutline = true
            outlineProvider = roundedOutline
        }
    }

    nativeAdView.findViewById<TextView>(R.id.native_ad_title).setTextColor(textColors.primary)
    listOf(
        R.id.native_ad_body,
        R.id.native_ad_sponsored,
        R.id.native_ad_age,
        R.id.native_ad_domain,
        R.id.native_ad_price,
    ).forEach { id -> nativeAdView.findViewById<TextView>(id).setTextColor(textColors.secondary) }

    // Юридический текст — контрастным основным цветом
    nativeAdView.findViewById<TextView>(R.id.native_ad_warning).setTextColor(textColors.primary)

    nativeAdView.findViewById<TextView>(R.id.native_ad_call_to_action).apply {
        setBackgroundResource(R.drawable.bg_native_ad_cta_light)
        setTextColor(0xFF007AFF.toInt())
    }

    nativeAdView.findViewById<ImageView>(R.id.native_ad_feedback).apply {
        alpha = 1f
        visibility = View.VISIBLE
        val feedbackColors = nativeAdFeedbackColors(isDarkTheme)
        elevation = 0f
        translationZ = 0f
        background = createFeedbackBackground(feedbackColors.background)
        // SDK подставляет свою круглую иконку — из‑за неё появляется «кольцо» вокруг фона.
        setImageDrawable(null)
        setImageResource(R.drawable.ic_native_ad_more)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        val insetPx = (4f * density).toInt()
        setPadding(insetPx, insetPx, insetPx, insetPx)
        setColorFilter(feedbackColors.icon, PorterDuff.Mode.SRC_IN)
    }
}

private data class NativeAdTextColors(
    val primary: Int,
    val secondary: Int,
)

private data class NativeAdFeedbackColors(
    val background: Int,
    val icon: Int,
)

private fun nativeAdFeedbackColors(isDarkTheme: Boolean): NativeAdFeedbackColors =
    if (isDarkTheme) {
        NativeAdFeedbackColors(
            background = 0xFF000000.toInt(),
            icon = 0xFFFFFFFF.toInt(),
        )
    } else {
        NativeAdFeedbackColors(
            background = 0xFFFFFFFF.toInt(),
            icon = 0xFF000000.toInt(),
        )
    }

private fun createFeedbackBackground(color: Int): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

private fun nativeAdTextColors(isDarkTheme: Boolean): NativeAdTextColors = if (isDarkTheme) {
    NativeAdTextColors(
        primary = 0xFFFFFFFF.toInt(),
        secondary = 0xFF8E8E93.toInt(),
    )
} else {
    NativeAdTextColors(
        primary = 0xFF1C1C1E.toInt(),
        secondary = 0xFF8E8E93.toInt(),
    )
}
