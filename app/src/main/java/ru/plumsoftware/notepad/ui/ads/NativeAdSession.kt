package ru.plumsoftware.notepad.ui.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdView

/**
 * Кэш нативной рекламы в ленте заметок в рамках сессии приложения.
 */
object NativeAdSession {
    var cacheRevision by mutableIntStateOf(0)
        private set

    private data class CacheEntry(
        val adUnitId: String,
        val ad: NativeAd,
        val view: NativeAdView,
    )

    private val cache = mutableMapOf<String, CacheEntry>()
    private val loading = mutableSetOf<String>()
    private val failed = mutableSetOf<String>()

    fun getCached(slotKey: String, adUnitId: String): Pair<NativeAd, NativeAdView>? {
        val entry = cache[slotKey] ?: return null
        if (entry.adUnitId != adUnitId) return null
        return entry.ad to entry.view
    }

    fun beginLoad(slotKey: String, adUnitId: String): Boolean {
        if (getCached(slotKey, adUnitId) != null) return false
        if (failed.contains(slotKey)) return false
        if (loading.contains(slotKey)) return false
        loading.add(slotKey)
        return true
    }

    fun cache(slotKey: String, adUnitId: String, ad: NativeAd, view: NativeAdView) {
        loading.remove(slotKey)
        failed.remove(slotKey)
        cache[slotKey] = CacheEntry(adUnitId, ad, view)
        cacheRevision++
    }

    fun onLoadFailed(slotKey: String) {
        loading.remove(slotKey)
        failed.add(slotKey)
        cacheRevision++
    }
}
