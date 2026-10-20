package ru.plumsoftware.notepad.ui.ads

import android.app.Activity
import android.os.SystemClock
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import ru.plumsoftware.notepad.App

/**
 * Межстраничная реклама после создания заметки — не чаще раза в 2 минуты.
 *
 * Время последнего показа хранится только в памяти процесса: после закрытия приложения
 * оно обнуляется, и первое подходящее создание заметки снова показывает рекламу.
 * Реклама грузится только в момент, когда её можно показать (без предзагрузки).
 */
object InterstitialAdPacing {
    private const val MIN_INTERVAL_MS = 2 * 60 * 1000L

    // Показываем, только если до создания у пользователя было больше стольких заметок
    private const val MIN_PREVIOUS_NOTES = 3L

    // Опоздавшую рекламу не показываем: она всплыла бы посреди других действий пользователя
    private const val LOAD_TIMEOUT_MS = 5_000L

    private var lastShownAt: Long? = null
    private var loader: InterstitialAdLoader? = null

    fun onNoteCreated(activity: Activity, previousNotesCount: Long) {
        if (previousNotesCount <= MIN_PREVIOUS_NOTES) return
        if (loader != null || !isIntervalPassed()) return

        val startedAt = SystemClock.elapsedRealtime()
        val adLoader = try {
            InterstitialAdLoader(activity)
        } catch (e: Throwable) {
            return
        }
        loader = adLoader

        adLoader.loadAd(
            AdRequest.Builder(App.platformConfig.adsConfig.interstitialAdsId).build(),
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loader = null
                    val isLate = SystemClock.elapsedRealtime() - startedAt > LOAD_TIMEOUT_MS
                    if (isLate || activity.isFinishing || activity.isDestroyed) return

                    interstitialAd.setAdEventListener(object : InterstitialAdEventListener {
                        override fun onAdShown() {
                            // Отсчёт 2 минут — с момента реального показа
                            lastShownAt = SystemClock.elapsedRealtime()
                        }

                        override fun onAdFailedToShow(adError: AdError) {}
                        override fun onAdDismissed() {}
                        override fun onAdClicked() {}
                        override fun onAdImpression(impressionData: ImpressionData?) {}
                    })
                    interstitialAd.show(activity)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    loader = null
                }
            },
        )
    }

    private fun isIntervalPassed(): Boolean {
        val last = lastShownAt ?: return true
        return SystemClock.elapsedRealtime() - last >= MIN_INTERVAL_MS
    }
}
