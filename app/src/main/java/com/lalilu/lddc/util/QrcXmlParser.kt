package com.lalilu.lddc.util

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.dom2.Element
import nl.adaptivity.xmlutil.serialization.XML

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

    fun parseToLyricContent(str: String): String {
        return runCatching {
            val qrcXml: QrcXml = xml.decodeFromString<QrcXml>(str)
            qrcXml.qrcLyricInfo?.children()?.firstOrNull()?.lyricContent
        }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: str
    }
}