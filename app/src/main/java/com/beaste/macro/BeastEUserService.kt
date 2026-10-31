
package com.beaste.macro

import android.util.Log
import java.util.concurrent.TimeUnit

class BeastEUserService : IBeastEUserService.Stub() {

    private val allowedKeys = setOf(
        "KEYCODE_R",
        "KEYCODE_Z",
        "KEYCODE_X",
        "KEYCODE_C",
        "KEYCODE_V"
    )

    override fun ping(): Boolean = true

    override fun sendKey(keyCode: String): Boolean {
        if (keyCode !in allowedKeys) return false

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
            Log.e("BeastE", "Input failed", error)
            false
        }
    }

    override fun stop() {
        // No persistent key-down is held.
    }

    override fun destroy() {
        System.exit(0)
    }
}
