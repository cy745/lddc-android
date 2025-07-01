package com.lalilu.lddc.util

import com.blankj.utilcode.util.SPUtils
import com.lalilu.lddc.entity.Lyric
import kotlinx.serialization.json.Json

object LyricResultCache {
    private val lyricCache = mutableMapOf<String, Lyric>()
    private val sp by lazy { SPUtils.getInstance() }
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    suspend fun getLyric(key: String, doFetch: suspend () -> Lyric? = { null }): Lyric? {
        return lyricCache[key]
            ?: getLyricFromSp(key)?.also { lyricCache[key] = it }
            ?: doFetch()?.also { updateLyric(key, it) }
    }

    fun updateLyric(key: String, lyric: Lyric) {
        lyricCache[key] = lyric
        saveIntoSp(key, lyric)
    }

    private fun getLyricFromSp(key: String): Lyric? = runCatching {
        val lyricStr = sp.getString(key)
            ?.takeIf { it.isNotBlank() }
            ?: return@runCatching null

        json.decodeFromString<Lyric>(lyricStr)
    }.getOrNull()

    private fun saveIntoSp(key: String, lyric: Lyric) = runCatching {
        sp.put(key, json.encodeToString(lyric))
    }
}