package dev.beefers.vendetta.manager.domain.manager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import dev.beefers.vendetta.manager.BuildConfig
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DownloadManager(
    private val context: Context
) {
    private val downloadManager = context.getSystemService<DownloadManager>()!!

    // Discord APK downloading
    suspend fun downloadDiscordApk(version: String, out: File): File =
        download("$BACKEND_HOST/tracker/download/$version/base", out)

    suspend fun downloadSplit(version: String, split: String, out: File): File =
        download("$BACKEND_HOST/tracker/download/$version/$split", out)

    suspend fun downloadVendetta(out: File) =
        download(
            "https://github.com/vendetta-mod/VendettaXposed/releases/latest/download/app-release.apk",
            out
        )

    suspend fun downloadUpdate(out: File) =
        download(
            "https://github.com/vendetta-mod/VendettaManager/releases/latest/download/Manager.apk",
            out
        )

    suspend fun download(url: String, out: File): File {
        out.parentFile?.mkdirs()

        return suspendCoroutine { continuation ->
            val receiver = object : BroadcastReceiver() {
                var downloadId = 0L

                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                    if (downloadId != id) return

                    val (status, reason) = DownloadManager.Query().run {
                        setFilterById(downloadId)

                        val cursor = downloadManager.query(this)
                            .apply { moveToFirst() }

                        // Notification's "cancel" was clicked
                        if (cursor.count == 0) {
                            -1 to -1
                        } else {
                            val status =
                                cursor.run { getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)) }
                            val reason =
                                cursor.run { getInt(getColumnIndex(DownloadManager.COLUMN_REASON)) }
                            status to reason
                        }
                    }

                    when (status) {
                        // Cancelled
                        -1 -> {
                            context.unregisterReceiver(this)
                            continuation.resumeWithException(Error("Download was cancelled manually"))
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            context.unregisterReceiver(this)
                            continuation.resume(out)
                        }

                        DownloadManager.STATUS_FAILED -> {
                            context.unregisterReceiver(this)
                            continuation.resumeWithException(
                                Error(
                                    "Failed to download $url because of: ${
                                        reasonToString(
                                            reason
                                        )
                                    }"
                                )
                            )
                        }

                        else -> {}
                    }
                }
            }

            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )

            receiver.downloadId = DownloadManager.Request(url.toUri()).run {
                setTitle("Vendetta Manager: Downloading ${out.name}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationUri(out.toUri())
                addRequestHeader("User-Agent", "Vendetta Manager/${BuildConfig.VERSION_NAME}")
                downloadManager.enqueue(this)
            }
        }
    }

    private fun reasonToString(int: Int): String {
        return when (int) {
            DownloadManager.ERROR_UNKNOWN -> "Unknown Error"
            DownloadManager.ERROR_FILE_ERROR -> "File Error"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP Code"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP Data Error"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found"
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            else -> "Unknown error"
        }
    }

    companion object {
        private const val BACKEND_HOST = "https://vd.k6.tf"
    }

}