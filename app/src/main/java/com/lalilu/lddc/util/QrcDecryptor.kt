package com.lalilu.lddc.util

import java.io.ByteArrayInputStream
import java.util.zip.InflaterInputStream

/**
 *  把 C# 版完全搬到 Kotlin，没有用 JCE Cipher。
 */
@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
object QrcDecryptor {

    /** 24‑byte fixed key */
    private val QQ_KEY = "!@#)(*$%123ZXC!@!@#)(NHL".toByteArray()
    private val schedule by lazy {
        Array(3) { Array(16) { UByteArray(6) } }.also {
            // 生成 3 张子密钥表（与 C# 完全一致）
            DesHelper.tripleDESKeySetup(QQ_KEY, it, DesHelper.DECRYPT)
        }
    }

    /**
     * @param encrypted Hex 字符串（大小写均可、长度需为 16 的倍数）
     * @return UTF‑8 歌词；失败返回 null
     */
    fun decryptLyrics(encrypted: String): String? = try {
        val cipherBytes = encrypted.hexToByteArray()
        val plain = ByteArray(cipherBytes.size)

        // 按 8 字节块解密（ECB，无填充）
        /* === 3DES(EDE) === */
        var i = 0
        val tmp = ByteArray(8)
        while (i < cipherBytes.size) {
            val input = cipherBytes.copyOfRange(i, i + 8)
            DesHelper.tripleDESCrypt(input, tmp, schedule)

            System.arraycopy(tmp, 0, plain, i, 8)
            i += 8
        }

        /* === inflate === */
        val inflater = InflaterInputStream(ByteArrayInputStream(plain))
        val unzipped = inflater.readBytes()

        unzipped.decodeToString()
    } catch (e: Throwable) {
        e.printStackTrace()
        null // 解密 / 解压失败
    }
}