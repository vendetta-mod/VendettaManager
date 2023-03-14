package dev.beefers.vendetta.manager.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import dev.beefers.vendetta.manager.BuildConfig

fun Context.copyText(text: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    clipboardManager.setPrimaryClip(ClipData.newPlainText(BuildConfig.APPLICATION_ID, text))
}

fun Context.showToast(@StringRes res: Int, vararg params: Any, short: Boolean = true) {
    Toast.makeText(
        this,
        getString(res, *params),
        if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
    ).show()
}