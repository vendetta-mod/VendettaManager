package dev.beefers.vendetta.manager.ui.activity

import android.Manifest
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.beefers.vendetta.manager.BuildConfig
import dev.beefers.vendetta.manager.installer.shizuku.IShizukuInstallerService
import dev.beefers.vendetta.manager.installer.shizuku.ShizukuInstallerService
import dev.beefers.vendetta.manager.ui.screen.installer.InstallerScreen
import dev.beefers.vendetta.manager.ui.screen.main.MainScreen
import dev.beefers.vendetta.manager.ui.theme.VendettaManagerTheme
import dev.beefers.vendetta.manager.utils.DiscordVersion
import dev.beefers.vendetta.manager.utils.Intents
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs


class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {
    companion object {
        var serviceBinder: IShizukuInstallerService? = null
            private set
    }

    private val acRequestCode = 1

    private val REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionResult

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult != PackageManager.PERMISSION_GRANTED) {
            checkAndRequestPermission()
        }
    }

    private fun checkAndRequestPermission() {
        if (Shizuku.pingBinder()) {
            Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(acRequestCode)
            }
        }
    }

    private val userServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            val res = StringBuilder()
            res.append("onServiceConnected: ").append(componentName.className).append('\n')
            if (binder.pingBinder()) {
                val service: IShizukuInstallerService = IShizukuInstallerService.Stub.asInterface(binder)
                try {
                    serviceBinder = service
                    res.append(service.doSomething())
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    res.append(Log.getStackTraceString(e))
                }
            } else {
                res.append("invalid binder for ").append(componentName).append(" received")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBinder = null
        }
    }

    private val userServiceArgs = UserServiceArgs(
        ComponentName(
            BuildConfig.APPLICATION_ID,
            ShizukuInstallerService::class.java.getName()
        )
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    private fun bindService() {
        val res = StringBuilder()
        try {
            if (Shizuku.getVersion() < 10) {
                res.append("requires Shizuku API 10")
            } else {
                Shizuku.bindUserService(userServiceArgs, userServiceConnection)
            }
        } catch (tr: Throwable) {
            tr.printStackTrace()
            res.append(tr.toString())
        }
    }

    private fun unbindService() {
        val res = StringBuilder()
        try {
            if (Shizuku.getVersion() < 10) {
                res.append("requires Shizuku API 10")
            } else {
                Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
            }
        } catch (tr: Throwable) {
            tr.printStackTrace()
            res.append(tr.toString())
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val version = intent.getStringExtra(Intents.Extras.VERSION)

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf("android.permission.POST_NOTIFICATIONS"),
                0
            )
        }

        checkAndRequestPermission()

        val screen = if (intent.action == Intents.Actions.INSTALL && version != null) {
            InstallerScreen(DiscordVersion.fromVersionCode(version)!!)
        } else {
            MainScreen()
        }

        setContent {
            VendettaManagerTheme {
                Navigator(screen) {
                    SlideTransition(it)
                }
            }
        }

        bindService()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
        unbindService()
        super.onDestroy()
    }
}
