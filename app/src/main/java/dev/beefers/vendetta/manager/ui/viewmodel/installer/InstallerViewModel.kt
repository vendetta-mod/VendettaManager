package dev.beefers.vendetta.manager.ui.viewmodel.installer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.github.diamondminer88.zip.ZipCompression
import com.github.diamondminer88.zip.ZipReader
import com.github.diamondminer88.zip.ZipWriter
import dev.beefers.vendetta.manager.BuildConfig
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.domain.manager.DownloadManager
import dev.beefers.vendetta.manager.domain.manager.InstallManager
import dev.beefers.vendetta.manager.domain.manager.InstallMethod
import dev.beefers.vendetta.manager.domain.manager.PreferenceManager
import dev.beefers.vendetta.manager.installer.Installer
import dev.beefers.vendetta.manager.installer.session.SessionInstaller
import dev.beefers.vendetta.manager.installer.shizuku.ShizukuInstaller
import dev.beefers.vendetta.manager.installer.util.ManifestPatcher
import dev.beefers.vendetta.manager.installer.util.Patcher
import dev.beefers.vendetta.manager.installer.util.Signer
import dev.beefers.vendetta.manager.utils.DiscordVersion
import dev.beefers.vendetta.manager.utils.copyText
import dev.beefers.vendetta.manager.utils.isMiui
import dev.beefers.vendetta.manager.utils.mainThread
import dev.beefers.vendetta.manager.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.patch.util.Logger
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class InstallerViewModel(
    private val context: Context,
    private val downloadManager: DownloadManager,
    private val preferences: PreferenceManager,
    private val discordVersion: DiscordVersion,
    val installManager: InstallManager
) : ScreenModel {
    var backDialogOpened by mutableStateOf(false)
        private set

    var failedOnDownload by mutableStateOf(false)

    private val installationRunning = AtomicBoolean(false)
    private val cacheDir = context.externalCacheDir ?: context.cacheDir
    private var debugInfo = """
            Vendetta Manager v${BuildConfig.VERSION_NAME}
            Built from commit ${BuildConfig.GIT_COMMIT} on ${BuildConfig.GIT_BRANCH} ${if (BuildConfig.GIT_LOCAL_CHANGES || BuildConfig.GIT_LOCAL_COMMITS) "(Changes Present)" else ""}
            
            Running Android ${Build.VERSION.RELEASE}, API level ${Build.VERSION.SDK_INT}
            Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString()}
            
            
        """.trimIndent()

    private val logger = object : Logger() {
        override fun d(msg: String?) {
            if (msg != null) {
                Log.d("Installer", msg)
                debugInfo += "$msg\n"
            }
        }

        override fun i(msg: String?) {
            if (msg != null) {
                Log.i("Installer", msg)
                debugInfo += "$msg\n"
            }
        }

        override fun e(msg: String?) {
            if (msg != null) {
                Log.e("Installer", msg)
                debugInfo += "$msg\n"
            }
        }
    }

    fun addLogError(msg: String) = logger.e("\n$msg")

    fun copyDebugInfo() {
        context.copyText(debugInfo)
    }

    fun clearCache() {
        cacheDir.deleteRecursively()
        context.showToast(R.string.msg_cleared_cache)
    }

    fun openBackDialog() {
        backDialogOpened = true
    }

    fun closeBackDialog() {
        backDialogOpened = false
    }

    fun launchVendetta() {
        installManager.current?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val job = coroutineScope.launch(Dispatchers.Main) {
        if (installationRunning.getAndSet(true)) {
            return@launch
        }

        withContext(Dispatchers.IO) {
            install()
        }
    }

    fun cancelInstall() {
        runCatching {
            job.cancel("User exited the installer")
        }
    }

    private suspend fun install() {
        steps += listOf(
            InstallStep.DL_BASE_APK to InstallStepData(
                InstallStep.DL_BASE_APK.nameRes,
                InstallStatus.QUEUED
            ),
            InstallStep.DL_LIBS_APK to InstallStepData(
                InstallStep.DL_LIBS_APK.nameRes,
                InstallStatus.QUEUED
            ),
            InstallStep.DL_LANG_APK to InstallStepData(
                InstallStep.DL_LANG_APK.nameRes,
                InstallStatus.QUEUED
            ),
            InstallStep.DL_RESC_APK to InstallStepData(
                InstallStep.DL_RESC_APK.nameRes,
                InstallStatus.QUEUED
            ),
            InstallStep.DL_VD to InstallStepData(InstallStep.DL_VD.nameRes, InstallStatus.QUEUED),
            InstallStep.ADD_VD to InstallStepData(InstallStep.ADD_VD.nameRes, InstallStatus.QUEUED),
            InstallStep.PATCH_MANIFESTS to InstallStepData(
                InstallStep.PATCH_MANIFESTS.nameRes,
                InstallStatus.QUEUED
            ),
            InstallStep.SIGN_APK to InstallStepData(
                InstallStep.SIGN_APK.nameRes,
                InstallStatus.QUEUED
            ),
            InstallStep.INSTALL_APK to InstallStepData(
                InstallStep.INSTALL_APK.nameRes,
                InstallStatus.QUEUED
            ),
        )

        if (preferences.patchIcon) steps += InstallStep.CHANGE_ICON to InstallStepData(
            InstallStep.CHANGE_ICON.nameRes,
            InstallStatus.QUEUED
        )

        val version = preferences.discordVersion.ifBlank { discordVersion.toVersionCode() }
        val arch = Build.SUPPORTED_ABIS.first()
        val discordCacheDir = cacheDir.resolve(version)
        val patchedDir = discordCacheDir.resolve("patched").also { it.deleteRecursively() }
        val signedDir = discordCacheDir.resolve("signed").also { it.deleteRecursively() }
        val lspatchedDir = patchedDir.resolve("lspatched").also { it.deleteRecursively() }

        // Download base.apk
        val baseApk = step(InstallStep.DL_BASE_APK) {
            discordCacheDir.resolve("base-$version.apk").let { file ->
                logger.i("Checking if base-$version.apk is cached")
                if (file.exists()) {
                    cached = true
                    logger.i("base-$version.apk is cached")
                } else {
                    logger.i("base-$version.apk is not cached, downloading now")
                    downloadManager.downloadDiscordApk(version, file) {
                        progress = it
                    }
                }

                logger.i("Move base-$version.apk to working directory")
                file.copyTo(
                    patchedDir.resolve(file.name),
                    true
                )
            }
        }

        // Download libs apk
        val libsApk = step(InstallStep.DL_LIBS_APK) {
            val libArch = arch.replace("-v", "_v")
            discordCacheDir.resolve("config.$libArch-$version.apk").let { file ->
                logger.i("Checking if config.$libArch-$version.apk is cached")
                if (file.exists()) {
                    logger.i("config.$libArch-$version.apk is cached")
                    cached = true
                } else {
                    logger.i("config.$libArch-$version.apk is not cached, downloading now")
                    downloadManager.downloadSplit(
                        version = version,
                        split = "config.$libArch",
                        out = file
                    ) {
                        progress = it
                    }
                }

                logger.i("Move config.$libArch-$version.apk to working directory")
                file.copyTo(
                    patchedDir.resolve(file.name),
                    true
                )
            }
        }

        // Download locale apk
        val langApk = step(InstallStep.DL_LANG_APK) {
            discordCacheDir.resolve("config.en-$version.apk").let { file ->
                logger.i("Checking if config.en-$version.apk is cached")
                if (file.exists()) {
                    logger.i("config.en-$version.apk is cached")
                    cached = true
                } else {
                    logger.i("config.en-$version.apk is not cached, downloading now")
                    downloadManager.downloadSplit(
                        version = version,
                        split = "config.en",
                        out = file
                    ) {
                        progress = it
                    }
                }

                logger.i("Move config.en-$version.apk to working directory")
                file.copyTo(
                    patchedDir.resolve(file.name),
                    true
                )
            }
        }

        // Download resources apk
        val resApk = step(InstallStep.DL_RESC_APK) {
            discordCacheDir.resolve("config.xxhdpi-$version.apk").let { file ->
                logger.i("Checking if config.xxhdpi-$version.apk is cached")
                if (file.exists()) {
                    logger.i("config.xxhdpi-$version.apk is cached")
                    cached = true
                } else {
                    logger.i("config.xxhdpi-$version.apk is not cached, downloading now")
                    downloadManager.downloadSplit(
                        version = version,
                        split = "config.xxhdpi",
                        out = file
                    ) {
                        progress = it
                    }
                }

                logger.i("Move config.xxhdpi-$version.apk to working directory")
                file.copyTo(
                    patchedDir.resolve(file.name),
                    true
                )
            }
        }

        // Download vendetta apk
        val vendetta = step(InstallStep.DL_VD) {
            preferences.moduleLocation.let { file ->
                logger.i("Checking if vendetta.apk is cached")
                if (file.exists()) {
                    logger.i("vendetta.apk is cached")
                    cached = true
                } else {
                    logger.i("vendetta.apk is not cached, downloading now")
                    downloadManager.downloadVendetta(file) {
                        progress = it
                    }
                }

                logger.i("Move vendetta.apk to working directory")
                file.copyTo(
                    patchedDir.resolve(file.name),
                    true
                )
            }
        }

        if (preferences.patchIcon) {
            step(InstallStep.CHANGE_ICON) {
                ZipWriter(baseApk, true).use { baseApk ->
                    val mipmaps =
                        arrayOf("mipmap-xhdpi-v4", "mipmap-xxhdpi-v4", "mipmap-xxxhdpi-v4")
                    val icons = arrayOf(
                        "ic_logo_foreground.png",
                        "ic_logo_square.png",
                        "ic_logo_foreground.png"
                    )

                    for (icon in icons) {
                        val newIcon = context.assets.open("icons/$icon")
                            .use { it.readBytes() }

                        for (mipmap in mipmaps) {
                            logger.i("Replacing $mipmap with $icon")
                            val path = "res/$mipmap/$icon"
                            baseApk.deleteEntry(path)
                            baseApk.writeEntry(path, newIcon)
                        }
                    }
                }
            }
        }

        // Patch manifests
        step(InstallStep.PATCH_MANIFESTS) {
            arrayOf(baseApk, libsApk, langApk, resApk).forEach { apk ->
                logger.i("Reading AndroidManifest.xml from ${apk!!.name}")
                val manifest = ZipReader(apk)
                    .use { zip -> zip.openEntry("AndroidManifest.xml")?.read() }
                    ?: throw IllegalStateException("No manifest in ${apk.name}")

                ZipWriter(apk, true).use { zip ->
                    logger.i("Changing package and app name in ${apk.name}")
                    val patchedManifestBytes = if (apk == baseApk) {
                        ManifestPatcher.patchManifest(
                            manifestBytes = manifest,
                            packageName = preferences.packageName,
                            appName = preferences.appName,
                            debuggable = preferences.debuggable,
                        )
                    } else {
                        logger.i("Changing package name in ${apk.name}")
                        ManifestPatcher.renamePackage(manifest, preferences.packageName)
                    }

                    logger.i("Deleting old AndroidManifest.xml in ${apk.name}")
                    zip.deleteEntry(
                        "AndroidManifest.xml",
                        apk == libsApk
                    ) // Preserve alignment in libs apk
                    logger.i("Adding patched AndroidManifest.xml in ${apk.name}")
                    zip.writeEntry("AndroidManifest.xml", patchedManifestBytes)
                }
            }
        }

        step(InstallStep.SIGN_APK) {
            // Align resources.arsc due to targeting api 30 for silent install
            logger.i("Creating dir for signed apks")
            signedDir.mkdir()
            val apks = arrayOf(baseApk, libsApk, langApk, resApk)
            if (Build.VERSION.SDK_INT >= 30) {
                for (file in apks) {
                    logger.i("Byte aligning ${file!!.name}")
                    val bytes = ZipReader(file).use {
                        if (it.entryNames.contains("resources.arsc")) {
                            it.openEntry("resources.arsc")?.read()
                        } else {
                            null
                        }
                    } ?: continue

                    ZipWriter(file, true).use {
                        logger.i("Removing old resources.arsc")
                        it.deleteEntry("resources.arsc", true)
                        logger.i("Adding aligned resources.arsc")
                        it.writeEntry("resources.arsc", bytes, ZipCompression.NONE, 4096)
                    }
                }
            }

            apks.forEach {
                logger.i("Signing ${it!!.name}")
                Signer.signApk(it, File(signedDir, it.name))
            }
        }

        step(InstallStep.ADD_VD) {
            val files = mutableListOf<File>()
            signedDir.list { _, name ->
                files.add(signedDir.resolve(name))
            }
            Patcher.patch(
                logger,
                outputDir = lspatchedDir,
                apkPaths = files.map { it.absolutePath },
                embeddedModules = listOf(vendetta!!.absolutePath)
            )
        }

        step(InstallStep.INSTALL_APK) {
            logger.i("Gathering final apks")
            val files = mutableListOf<File>()
            lspatchedDir.list { _, name ->
                files.add(lspatchedDir.resolve(name))
            }
            logger.i("Installing apks")

            val installer: Installer = when (preferences.installMethod) {
                InstallMethod.DEFAULT -> SessionInstaller(context)
                InstallMethod.SHIZUKU -> ShizukuInstaller(context)
            }

            installer.installApks(silent = !isMiui, *files.toTypedArray())

            isFinished = true
        }
    }

    @OptIn(ExperimentalTime::class)
    private inline fun <T> step(step: InstallStep, block: InstallStepData.() -> T): T? {
        if (isFinished) return null
        steps[step]!!.status = InstallStatus.ONGOING
        currentStep = step

        try {
            val value = measureTimedValue { block.invoke(steps[step]!!) }
            val millis = value.duration.inWholeMilliseconds

            steps[step]!!.apply {
                duration = millis.div(1000f)
                status = InstallStatus.SUCCESSFUL
            }
            return value.value
        } catch (e: Throwable) {
            steps[step]!!.status = InstallStatus.UNSUCCESSFUL

            if(e.message?.contains("InvalidArchive") == true) mainThread {
                context.showToast(R.string.msg_invalid_apk)
            }

            logger.e("\nFailed on step ${step.name}\n")
            logger.e(e.stackTraceToString())
            if(step.group == InstallStepGroup.DL) failedOnDownload = true

            currentStep = step
            isFinished = true
            return null
        }
    }

    enum class InstallStepGroup(@StringRes val nameRes: Int) {
        DL(R.string.group_download),
        PATCHING(R.string.group_patch),
        INSTALLING(R.string.group_installing)
    }

    enum class InstallStep(
        val group: InstallStepGroup,
        @StringRes val nameRes: Int
    ) {
        DL_BASE_APK(InstallStepGroup.DL, R.string.step_dl_base),
        DL_LIBS_APK(InstallStepGroup.DL, R.string.step_dl_lib),
        DL_LANG_APK(InstallStepGroup.DL, R.string.step_dl_lang),
        DL_RESC_APK(InstallStepGroup.DL, R.string.step_dl_res),
        DL_VD(InstallStepGroup.DL, R.string.step_dl_vd),

        CHANGE_ICON(InstallStepGroup.PATCHING, R.string.step_change_icon),
        PATCH_MANIFESTS(InstallStepGroup.PATCHING, R.string.step_patch_manifests),
        SIGN_APK(InstallStepGroup.PATCHING, R.string.step_signing),
        ADD_VD(InstallStepGroup.PATCHING, R.string.step_add_vd),

        INSTALL_APK(InstallStepGroup.INSTALLING, R.string.step_installing)
    }

    enum class InstallStatus {
        ONGOING,
        SUCCESSFUL,
        UNSUCCESSFUL,
        QUEUED
    }

    @Stable
    class InstallStepData(
        @StringRes val nameRes: Int,
        status: InstallStatus,
        duration: Float = 0f,
        cached: Boolean = false,
        progress: Float? = null
    ) {
        var status by mutableStateOf(status)
        var duration by mutableStateOf(duration)
        var cached by mutableStateOf(cached)
        var progress by mutableStateOf(progress)
    }

    var currentStep by mutableStateOf<InstallStep?>(null)
    val steps = mutableStateMapOf<InstallStep, InstallStepData>()
    var isFinished by mutableStateOf(false)

    fun getSteps(group: InstallStepGroup): List<InstallStepData> {
        return steps
            .filterKeys { it.group === group }.entries
            .sortedBy { it.key.ordinal }
            .map { it.value }
    }

}