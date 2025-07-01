package com.lalilu.lddc.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: Long = -1,
    val mid: String = "",
    val name: String = "",
    val title: String = "",
    val subtitle: String = "",
    val interval: Long = 0,
    val language: Int = -1,
    @SerialName("time_public")
    val releaseTime: String = "",
    @SerialName("album")
    val album: Album? = null,
    @SerialName("singer")
    val singer: List<Singer> = emptyList()
) {
    val duration = interval * 1000
    fun languageName() = languageMap[language] ?: "未知"

    companion object {
        private val languageMap = mapOf<Int, String>(
            0 to "汉语",
            1 to "粤语",
            3 to "日语",
            4 to "韩语",
            5 to "英语",
            9 to "其他"
        )
    }
}

@Serializable
data class Album(
    val id: Long = -1,
    val name: String = "",
    val mid: String = "",
    val pmid: String = "",
    val title: String = "",
    val subtitle: String = "",
    @SerialName("time_public")
    val releaseTime: String = ""
)

@Serializable
data class Singer(
    val id: Long = -1,
    val name: String = "",
    val mid: String = "",
    val pmid: String = "",
    val title: String = ""
)