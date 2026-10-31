
package com.beaste.macro

import android.os.Parcel
import android.util.Log
import java.util.concurrent.TimeUnit

class BeastEUserService : IBeastEUserService.Stub() {

    companion object {
        private const val TRANSACTION_DESTROY = 16777115

        private val ALLOWED_KEYS = setOf(
            "KEYCODE_R",
            "KEYCODE_Z",
            "KEYCODE_X",
            "KEYCODE_C",
            "KEYCODE_V"
        )
    }

    override fun ping(): Boolean {
        return true
    }

    override fun sendKey(keyCode: String): Boolean {
        if (keyCode !in ALLOWED_KEYS) {
            return false
        }

        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("input", "keyevent", keyCode)
            )

            val completed = process.waitFor(
                1500,
                TimeUnit.MILLISECONDS
            )

            if (!completed) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0
            }

        } catch (error: Exception) {
            Log.e("BeastEUserService", "Input failed", error)
            false
        }
    }

    override fun stop() {
        // No persistent key-down state is maintained.
    }

    override fun onTransact(
        code: Int,
        data: Parcel,
        reply: Parcel?,
        flags: Int
    ): Boolean {
        if (code == TRANSACTION_DESTROY) {
            destroyService()
            return true
        }

        return super.onTransact(code, data, reply, flags)
    }

    private fun destroyService() {
        try {
            stop()
        } finally {
            System.exit(0)
        }
    }
}
