package com.beaste.macro

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.SystemClock
import rikka.shizuku.Shizuku

object ShizukuController {

    @Volatile
    private var remote: IBeastEUserService? = null

    @Volatile
    var connected = false
        private set

    @Volatile
    private var bound = false

    private val args by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(
                BuildConfig.APPLICATION_ID,
                BeastEUserService::class.java.name
            )
        )
            .daemon(false)
            .tag("beaste-input-service")
            .version(2)
            .processNameSuffix("beaste_shizuku")
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            val binder = service ?: return

            remote = IBeastEUserService.Stub.asInterface(binder)

            connected = try {
                remote?.ping() == true
            } catch (_: Exception) {
                false
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remote = null
            connected = false
            bound = false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() ==
                PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun bind(): Boolean {
        if (!hasPermission()) return false

        return try {
            if (!bound) {
                Shizuku.bindUserService(args, connection)
                bound = true
            }
            true
        } catch (_: Exception) {
            bound = false
            false
        }
    }

    fun tap(keyCode: Int): Boolean {
        return try {
            remote?.tapKey(
                keyCode,
                SystemClock.uptimeMillis()
            ) == true
        } catch (_: Exception) {
            connected = false
            false
        }
    }

    @Synchronized
    fun unbind() {
        try {
            remote?.stop()

            if (bound) {
                Shizuku.unbindUserService(
                    args,
                    connection,
                    true
                )
            }
        } catch (_: Exception) {
        } finally {
            remote = null
            connected = false
            bound = false
        }
    }
}
