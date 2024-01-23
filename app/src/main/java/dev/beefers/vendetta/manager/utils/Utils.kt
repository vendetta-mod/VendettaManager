package dev.beefers.vendetta.manager.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import dev.beefers.vendetta.manager.BuildConfig
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

val mainThread = Handler(Looper.getMainLooper())

fun mainThread(block: () -> Unit) {
    mainThread.post(block)
}

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

    val bitmap = AppCompatResources.getDrawable(this, icon)!!.toBitmap(
        height = sizePx,
        width = sizePx
    )

    cachedBitmaps[icon] = mutableMapOf()
    cachedBitmaps[icon]?.let {
        it[size] = bitmap
    }

    return bitmap
}

// Thanks to https://gist.github.com/Muyangmin/e8ec1002c930d8df3df46b306d03315d
fun getSystemProp(prop: String): String? {
    val line: String
    var input = null as BufferedReader?
    try {
        val proc = Runtime.getRuntime().exec("getprop $prop")
        input = BufferedReader(InputStreamReader(proc.inputStream), 1024)
        line = input.readLine()
        input.close()
    } catch (e: IOException) {
        return null
    } finally {
        input?.let {
            try {
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return line
}

val isMiui: Boolean
    get() = getSystemProp("ro.miui.ui.version.name")?.isNotEmpty() ?: false