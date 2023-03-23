package dev.beefers.vendetta.manager.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toBitmap
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

private val cachedBitmaps: MutableMap<Int, MutableMap<Int, Bitmap>> = mutableMapOf()

context(Context)
private val Int.dp: Int
    get() = (45 * this@Context.resources.displayMetrics.density + 0.5f).toInt()

fun Context.getBitmap(@DrawableRes icon: Int, size: Int): Bitmap {
    cachedBitmaps[icon]?.let { it[size]?.let { bitmap -> return bitmap } }
    val sizePx = size.dp

    val bitmap = getDrawable(icon)!!.toBitmap(
        height = sizePx,
        width = sizePx
    )

    cachedBitmaps[icon] = mutableMapOf()
    cachedBitmaps[icon]?.let {
        it[size] = bitmap
    }

    return bitmap
}