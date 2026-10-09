package com.alessandro.tedesco.data.remote

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class VersionInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String = "",
    val minVersionCode: Int = 0
)

/**
 * Verifica pura dell'integrità di un file APK.
 * Usata sia dal ViewModel sia dai test.
 */
object VerificaApk {

    private const val DIMENSIONE_MINIMA = 1_000_000L // 1 MB

    /**
     * Verifica che un file sia un APK valido controllando:
     * - dimensione > 1 MB
     * - primi 2 byte = 'P' 'K' (magic ZIP/APK)
     */
    fun isApkValido(file: File): Boolean {
        if (!file.exists() || file.length() < DIMENSIONE_MINIMA) return false
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(2)
                val read = input.read(header)
                read == 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Versione pura per test: verifica direttamente header e dimensione.
     */
    fun isApkValido(header: ByteArray, size: Long): Boolean {
        if (size < DIMENSIONE_MINIMA) return false
        if (header.size < 2) return false
        return header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
    }
}
