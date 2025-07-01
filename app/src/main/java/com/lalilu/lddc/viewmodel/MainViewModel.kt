package com.lalilu.lddc.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lddc.entity.Lyric
import com.lalilu.lddc.entity.Song
import com.lalilu.lddc.util.LyricResultCache
import com.lalilu.lddc.util.QrcDecryptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random

@OptIn(ExperimentalStdlibApi::class, ExperimentalSerializationApi::class)
class MainViewModel : ViewModel() {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
        install(ContentEncoding) {
            gzip()
            identity()
            deflate()
        }
    }
    private val headersMap = mapOf<String, Any>(
        "Cookie" to "tmeLoginType=-1;",
        "Content-Type" to "application/json",
        "Accept-encoding" to "gzip, deflate",
        "User-Agent" to "okhttp/3.14.9"
    )
    private val initialized = mutableStateOf(false)
    private val extraCommDataMap = mutableMapOf<String, String?>(
        "uid" to null,
        "sid" to null,
        "userip" to null,
    )

    val songs = mutableStateOf<List<Song>>(emptyList())

    init {
        viewModelScope.launch { checkInit() }
    }


    suspend fun search(keyword: String) = runCatching {
        checkInit()

        fun genSearchId(): Long {
            val time = System.currentTimeMillis()
            return (1..20).random() * 18014398509481984 + (0..4194304).random() * 4294967296 + time % 86400000
        }

        val response = request(
            method = "DoSearchForQQMusicLite",
            module = "music.search.SearchCgiService",
            param = buildJsonObject {
                put("search_id", JsonPrimitive("${genSearchId()}"))
                put("remoteplace", JsonPrimitive("search.android.keyboard"))
                put("query", JsonPrimitive(keyword))
                put("search_type", JsonPrimitive(0))
                put("num_per_page", JsonPrimitive(20))
                put("page_num", JsonPrimitive(0))
                put("highlight", JsonPrimitive(0))
                put("nqc_flag", JsonPrimitive(0))
                put("page_id", JsonPrimitive(1))
                put("grp", JsonPrimitive(1))
            }
        )

        val jsonElement = response.bodyJson()
        val bodyElement = jsonElement.jsonObject["body"]?.jsonObject
        val songsArray = bodyElement?.get("item_song")?.jsonArray
        songs.value = songsArray
            ?.mapNotNull { runCatching { json.decodeFromJsonElement<Song>(it) }.getOrNull() }
            ?: emptyList()
    }

    suspend fun getLyric(
        songId: Long,
        albumName: String,
        interval: Long, // 单位为秒
        singerName: String,
        songName: String,
    ) = runCatching {
        LyricResultCache.getLyric(key = "$songId-$albumName-${singerName}-${songName}") {
            val response = request(
                method = "GetPlayLyricInfo",
                module = "music.musichallSong.PlayLyricInfo",
                param = buildJsonObject {
                    put("albumName", JsonPrimitive(albumName.encodeBase64()))
                    put("crypt", JsonPrimitive(1))
                    put("ct", JsonPrimitive(19))
                    put("cv", JsonPrimitive(2111))
                    put("interval", JsonPrimitive(interval))
                    put("lrc_t", JsonPrimitive(0))
                    put("qrc", JsonPrimitive(1))
                    put("qrc_t", JsonPrimitive(0))
                    put("roma", JsonPrimitive(1))
                    put("roma_t", JsonPrimitive(0))
                    put("singerName", JsonPrimitive(singerName.encodeBase64()))
                    put("songID", JsonPrimitive(songId))
                    put("songName", JsonPrimitive(songName.encodeBase64()))
                    put("trans", JsonPrimitive(1))
                    put("trans_t", JsonPrimitive(0))
                    put("type", JsonPrimitive(0))
                }
            )

            val jsonElement = response.bodyJson()
            json.decodeFromJsonElement<Lyric>(jsonElement).run {
                copy(
                    lyric = QrcDecryptor.decryptLyrics(lyric) ?: lyric,
                    trans = QrcDecryptor.decryptLyrics(trans) ?: trans,
                    roma = QrcDecryptor.decryptLyrics(roma) ?: roma,
                )
            }
        }
    }

    private suspend fun checkInit() {
        if (initialized.value) return

        runCatching {
            val response = request(
                method = "GetSession",
                module = "music.getSession.session",
                param = buildJsonObject {
                    put("caller", JsonPrimitive(0))
                    put("uid", JsonPrimitive("0"))
                    put("vkey", JsonPrimitive(0))
                }
            )
            val jsonElement = response.bodyJson()
            val requestObj = jsonElement.jsonObject["session"]?.jsonObject
            extraCommDataMap["uid"] = requestObj?.get("uid")?.jsonPrimitive?.content
            extraCommDataMap["sid"] = requestObj?.get("sid")?.jsonPrimitive?.content
            extraCommDataMap["userip"] = requestObj?.get("userip")?.jsonPrimitive?.content

            initialized.value = true
        }.getOrElse {
            LogUtils.e(it)
        }
    }

    private suspend fun request(method: String, module: String, param: JsonElement): HttpResponse {
        return client.post("https://u.y.qq.com/cgi-bin/musicu.fcg") {
            headersMap.forEach { header(it.key, it.value) }
            val json = buildRequestJson(method, module, param)
            LogUtils.i(json.toString())
            setBody(json)
        }
    }

    private fun buildRequestJson(method: String, module: String, param: JsonElement): JsonObject {
        return buildJsonObject {
            put("comm", buildCommData())
            put("request", buildJsonObject {
                put("method", JsonPrimitive(method))
                put("module", JsonPrimitive(module))
                put("param", param)
            })
        }
    }

    private fun buildCommData(): JsonObject {
        val romStr = "Redmi/miro/miro:15/AE3A.240806.005/OS2.0.10${Random.nextInt(2, 5)}" +
                ".0.VOMCNXM:user/release-keys"

        return buildJsonObject {
            put("ct", JsonPrimitive(11))
            put("cv", JsonPrimitive("1003006"))
            put("v", JsonPrimitive("1003006"))
            put("os_ver", JsonPrimitive("15"))
            put("phonetype", JsonPrimitive("24122RKC7C"))
            put("rom", JsonPrimitive(romStr))
            put("tmeAppID", JsonPrimitive("qqmusiclight"))
            put("nettype", JsonPrimitive("NETWORK_WIFI"))
            put("udid", JsonPrimitive("0"))
            extraCommDataMap.forEach {
                if (it.value != null) {
                    put(it.key, JsonPrimitive(it.value))
                }
            }
        }
    }

    private suspend fun HttpResponse.bodyJson(): JsonElement {
        return json.decodeFromStream<JsonElement>(bodyAsChannel().toInputStream())
            .jsonObject.getValue("request")
            .jsonObject.getValue("data")
    }
}