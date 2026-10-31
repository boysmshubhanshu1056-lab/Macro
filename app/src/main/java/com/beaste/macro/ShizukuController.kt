
package com.beaste.macro

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuController {

    @Volatile
    private var remote: IBeastEUserService? = null

    @Volatile
    var connected = false
        private set

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
            .version(1)
            .processNameSuffix("beaste_shizuku")
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            remote = IBeastEUserService.Stub.asInterface(service)

            connected = try {
                remote?.ping() == true
            } catch (_: Exception) {
                false
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remote = null
            connected = false
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

    fun bind(): Boolean {
        if (!hasPermission()) return false

        return try {
            if (!bound) {
                Shizuku.bindUserService(args, connection)
                bound = true
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun send(keyCode: String): Boolean {
        return try {
            remote?.sendKey(keyCode) == true
        } catch (_: Exception) {
            connected = false
            false
        }
    }

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
