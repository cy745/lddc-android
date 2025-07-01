package com.lalilu.lddc.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.dom2.Node
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

object QrcXmlParser {
    @OptIn(ExperimentalXmlUtilApi::class)
    private val xml = XML(
        serializersModule = SerializersModule {
            polymorphic(Any::class) {
                defaultDeserializer { String.serializer() }
                subclass(String::class)
                subclass(Element::class)
            }
        }
    ) {
        defaultPolicy {
            autoPolymorphic = true
            ignoreUnknownChildren()
            fast_0_90_2 { }
        }
    }

    fun parse(str: String): QrcXml? {
        return xml.decodeFromString<QrcXml>(str)
    }
}

@XmlSerialName("QrcInfos")
@Serializable
data class QrcXml(
    @XmlSerialName("QrcHeadInfo")
    val qrcHeadInfo: QrcHeadInfo? = null,
    @XmlSerialName("LyricInfo")
    val qrcLyricInfo: QrcLyricInfo? = null,
)

@Serializable
data class QrcHeadInfo(
    @XmlSerialName("SaveTime")
    val saveTime: String = "",
    @XmlSerialName("Version")
    val version: String = ""
)

@Serializable
data class QrcLyricInfo(
    @XmlSerialName("LyricCount")
    val lyricCount: Int = 0,
    @XmlValue
    private val lyric: List<Node> = emptyList()
) {
    fun children(): List<QrcLyricItem> {
        return lyric.mapNotNull {
            if (it !is Element) return@mapNotNull null

            QrcLyricItem(
                tagName = it.getTagName(),
                lyricType = it.getAttribute("LyricType")?.toInt() ?: 0,
                lyricContent = it.getAttribute("LyricContent") ?: ""
            )
        }
    }
}

@Serializable
data class QrcLyricItem(
    val tagName: String? = "",
    @XmlSerialName("LyricType")
    val lyricType: Int = 0,
    @XmlSerialName("LyricContent")
    val lyricContent: String = ""
)