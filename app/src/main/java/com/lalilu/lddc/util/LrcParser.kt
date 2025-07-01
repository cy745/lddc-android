package com.lalilu.lddc.util

import kotlin.math.abs


data class LrcItem(
    val time: Long,
    val content: String
) {
    override fun toString(): String {
        return "[${LrcParser.timeToLrcTime(time, 0)}]$content"
    }
}

fun List<LrcItem>.toLrcContent(): String {
    return joinToString("\n") { it.toString() }
}

/**
 * 将歌词和翻译元素混合，每一句翻译都在歌词数组中找与其时间最接近的一项，将时间改为与其一致然后再进行sorted
 */
fun List<LrcItem>.handleLyricWithTranslation(translation: List<LrcItem>): List<LrcItem> {
    val newTrans = translation.map { trans ->
        val minItem = this.minBy { abs(trans.time - it.time) }
        val absoluteTime = abs(minItem.time - trans.time)

        // 如果时间差距过大，则不处理
        if (absoluteTime >= 100) {
            trans
        } else {
            trans.copy(time = minItem.time)
        }
    }
    return (this + newTrans).sortedBy { it.time }
}

object LrcParser {
    val REGEX_TIME_LRC = Regex("\\[(\\d\\d):(\\d\\d)\\.(\\d{1,5})]")
    val REGEX_TIME_LRC_EX = Regex("<(\\d\\d):(\\d\\d)\\.(\\d{1,5})>")

    fun parseLyric(lyric: String): List<LrcItem> {
        val lines = lyric.lines()

        return lines.mapNotNull {
            val timeTag = REGEX_TIME_LRC.findAll(it)
                .firstOrNull()
                ?: return@mapNotNull null

            val startTime = lrcTimeToTime(timeTag.value)
            var content = it.substring(timeTag.range.last + 1)
            if (content == "//") content = ""

            LrcItem(
                time = startTime,
                content = content
            )
        }
    }

    fun lrcTimeToTime(str: String): Long {
        val timeMatcher =
            // 匹配[00:00.00]格式的时间标签
            REGEX_TIME_LRC.matchEntire(str)
                ?.groupValues
                ?.takeIf { it.isNotEmpty() }
            // 尝试匹配<00:00.00>格式的时间标签
                ?: REGEX_TIME_LRC_EX.matchEntire(str)
                    ?.groupValues
                    ?.takeIf { it.isNotEmpty() }
                ?: return -1L

        val min = timeMatcher.getOrNull(1)!!.toLong()
        val sec = timeMatcher.getOrNull(2)!!.toLong()
        val milString = timeMatcher.getOrNull(3)!!

        var mil = milString.toLong()
        // 如果毫秒是两位数，需要乘以 10，when 新增支持 1 - 6 位毫秒，很多获取的歌词存在不同的毫秒位数
        when (milString.length) {
            1 -> mil *= 100
            2 -> mil *= 10
            4 -> mil /= 10
            5 -> mil /= 100
            6 -> mil /= 1000
        }

        return (min * 60 * 1000 + sec * 1000 + mil)
    }

    /**
     * 将时间转换成[00:00.000]的格式输出
     */
    fun timeToLrcTime(startMs: Long, offsetMs: Long): String {
        val time = startMs + offsetMs

        val minutes = time / 1000 / 60
        val seconds = time / 1000 % 60
        val milliseconds = (time % 1000)
        return "%02d:%02d.%03d".format(minutes, seconds, milliseconds)
    }
}
