package dev.beefers.vendetta.manager.domain.manager

import android.app.DownloadManager
import android.content.Context
import androidx.core.content.getSystemService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class DownloadManager(
    private val prefs: PreferenceManager
) {
    private val httpClient = HttpClient {
        install(HttpTimeout) {
            socketTimeoutMillis = 1_000
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = null
        }
    }

    // Discord APK downloading
    suspend fun downloadDiscordApk(version: String, out: File, onProgressUpdate: (Float) -> Unit): File =
        download("${prefs.mirror.baseUrl}/tracker/download/$version/base", out, onProgressUpdate)

    suspend fun downloadSplit(version: String, split: String, out: File, onProgressUpdate: (Float) -> Unit): File =
        download("${prefs.mirror.baseUrl}/tracker/download/$version/$split", out, onProgressUpdate)

    suspend fun downloadVendetta(out: File, onProgressUpdate: (Float) -> Unit) =
        download(
            "https://github.com/vendetta-mod/VendettaXposed/releases/latest/download/app-release.apk",
            out,
            onProgressUpdate
        )

    suspend fun downloadUpdate(out: File) =
        download(
            "https://github.com/vendetta-mod/VendettaManager/releases/latest/download/Manager.apk",
            out
        ) { /* TODO: Update a progress bar in the update dialog */ }

    suspend fun download(url: String, out: File, onProgressUpdate: (Float) -> Unit): File {
        out.parentFile?.mkdirs()
        httpClient.prepareGet(url).execute {
            if(!out.exists()) out.createNewFile()
            val channel = it.body<ByteReadChannel>()
            val contentLength = it.contentLength()?.toInt() ?: 0
            out.outputStream().use { os ->
                channel.toInputStream().use { stream ->
                    println(stream.available())
                    stream.copyToWithProgress(os, bufferSize = 16384 * 2) { progress ->
                        onProgressUpdate(progress / contentLength.toFloat())
                    }
                }
            }
        }
        return out
    }

    private fun InputStream.copyToWithProgress(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, onProgressUpdate: (Long) -> Unit): Long {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = read(buffer)
        while (bytes >= 0) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            onProgressUpdate(bytesCopied)
            bytes = read(buffer)
        }
        return bytesCopied
    }

}