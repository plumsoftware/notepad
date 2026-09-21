package ru.plumsoftware.notepad.ui.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.nativeads.NativeAd

/**
 * Кэш нативной рекламы в ленте заметок в рамках сессии приложения.
 *
 * Важно: кэшируется ТОЛЬКО объект [NativeAd], но не готовый View.
 * View инфлейтится заново на каждый показ — иначе при переиспользовании
 * одного и того же View между композициями (или после пересоздания Activity,
 * например при переключении темы) приложение падает
 * («The specified child already has a parent» / устаревший контекст).
 */
object NativeAdSession {
    var cacheRevision by mutableIntStateOf(0)
        private set

    private data class CacheEntry(val adUnitId: String, val ad: NativeAd)

    private val cache = mutableMapOf<String, CacheEntry>()
    private val loading = mutableSetOf<String>()
    private val failed = mutableSetOf<String>()

    fun getCached(slotKey: String, adUnitId: String): NativeAd? {
        val entry = cache[slotKey] ?: return null
        if (entry.adUnitId != adUnitId) return null
        return entry.ad
    }

    fun beginLoad(slotKey: String, adUnitId: String): Boolean {
        if (getCached(slotKey, adUnitId) != null) return false
        if (failed.contains(slotKey)) return false
        if (loading.contains(slotKey)) return false
        loading.add(slotKey)
        return true
    }

    fun cache(slotKey: String, adUnitId: String, ad: NativeAd) {
        loading.remove(slotKey)
        failed.remove(slotKey)
        cache[slotKey] = CacheEntry(adUnitId, ad)
        cacheRevision++
    }

    fun onLoadFailed(slotKey: String) {
        loading.remove(slotKey)
        failed.add(slotKey)
        cacheRevision++
    }
}
