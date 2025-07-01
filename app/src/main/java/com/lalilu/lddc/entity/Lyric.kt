package com.lalilu.lddc.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Lyric(
    val songID: Long = -1,
    val songName: String = "",
    val songType: Int = -1,
    val singerName: String = "",
    val lyric: String = "",
    val trans: String = "",
    val roma: String = "",
    @SerialName("lrc_t")
    val lrcT: Long = 0,
    @SerialName("qrc_t")
    val qrcT: Long = 0,
    @SerialName("trans_t")
    val transT: Long = 0,
    @SerialName("roma_t")
    val romaT: Long = 0,
    @SerialName("lyric_style")
    val lyricStyle: Int = 0,
    val classical: Int = 0,
    val introduceTitle: String = "",
    val startTs: Long = 0,
    val transSource: Int = 0
)