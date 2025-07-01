package com.lalilu.lddc.util

import com.lalilu.lddc.util.LrcParser.timeToLrcTime


object QrcParser {
    private val REGEX_TIME = Regex("\\[(\\d+),\\s?(\\d+)]")
    private val REGEX_TIME_EX = Regex("\\((\\d+),\\s?(\\d+)\\)")

    fun parse(lyric: String): List<QrcItem> {
        val lines = lyric.lines()
        val items = lines.map { parseLine(it) }
            .flatten()
        return items
    }

    fun parseLine(line: String): List<QrcItem> {
        // 查找开头的时间标签
        val timeTag = REGEX_TIME.findAll(line)
            .firstOrNull()
            ?: return emptyList()

        val startTime = timeTag.groupValues[1].toLong()
        val duration = timeTag.groupValues[2].toLong()

        val sentenceContent = line.substring(timeTag.range.last + 1)

        // 查找句子里的时间标签
        val wordsTimeTag = REGEX_TIME_EX.findAll(sentenceContent)
            .toList()
            .takeIf { it.isNotEmpty() }
            ?: return emptyList()

        val textSplits = mutableListOf<QrcContentItem>()
        for (i in wordsTimeTag.indices) {
            val item = wordsTimeTag[i]

            val startIndex = wordsTimeTag.getOrNull(i - 1)?.range?.last?.plus(1) ?: 0
            val endIndex = item.range.start

            if (startIndex <= endIndex) {
                val text = sentenceContent.substring(startIndex, endIndex)
                if (text.isNotEmpty()) {
                    textSplits.add(QrcContentItem.Text(text))
                    textSplits.add(
                        QrcContentItem.TimeTag(
                            time = item.groupValues[1].toLong(),
                            duration = item.groupValues[2].toLong()
                        )
                    )
                }
            }
        }

        // 为歌词单词文本添加开始时间和结束时间
        val words = textSplits.mapIndexedNotNull { index, item ->
            if (item is QrcContentItem.TimeTag) return@mapIndexedNotNull null
            val text = item as? QrcContentItem.Text ?: return@mapIndexedNotNull null

            val timeTag = textSplits.getOrNull(index + 1) as? QrcContentItem.TimeTag
            val startTime = timeTag?.time ?: return@mapIndexedNotNull null

            QrcItem.QrcWordWithTiming(
                content = text.text,
                startTime = startTime,
                duration = timeTag.duration
            )
        }

        // 若无结果则尽早返回
        if (words.isEmpty()) return emptyList()

        return listOf(
            QrcItem(
                words = words,
                translation = emptyList(),
                startTime = startTime,
                duration = duration,
            )
        )
    }


    private sealed interface QrcContentItem {
        data class TimeTag(val time: Long, val duration: Long) : QrcContentItem
        data class Text(val text: String) : QrcContentItem
    }
}

data class QrcItem(
    val words: List<QrcWordWithTiming>,
    val translation: List<String>,
    val startTime: Long,
    val duration: Long,
) {
    fun endTime() = startTime + duration
    fun sentenceContent() = words.joinToString("") { it.content }
    fun toLrcLine(): String = "[${timeToLrcTime(startTime, 0)}]${toLrcContent()}"
    fun toLrcContent(): String = words.joinToString("") { it.toLrcContent() }

    data class QrcWordWithTiming(
        val content: String,
        val startTime: Long,
        val duration: Long
    ) {
        fun endTime() = startTime + duration
        fun toLrcContent(): String = "$content[${timeToLrcTime(startTime, duration)}]"
    }

    fun toLrcItem(): LrcItem = LrcItem(
        time = startTime,
        content = toLrcContent()
    )
}

