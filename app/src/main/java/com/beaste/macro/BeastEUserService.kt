package com.beaste.macro

import android.hardware.input.InputManager
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent

class BeastEUserService : IBeastEUserService.Stub() {

    companion object {
        private const val TAG = "BeastEUserService"
        private const val INJECT_ASYNC = 0

        private val ALLOWED_KEYS = setOf(
            KeyEvent.KEYCODE_R,
            KeyEvent.KEYCODE_Z,
            KeyEvent.KEYCODE_X,
            KeyEvent.KEYCODE_C,
            KeyEvent.KEYCODE_V
        )
    }

    private val inputManager by lazy {
        val managerClass =
            Class.forName("android.hardware.input.InputManagerGlobal")

        val instance = managerClass
            .getDeclaredMethod("getInstance")
            .invoke(null)

        managerClass.getDeclaredMethod(
            "injectInputEvent",
            InputEvent::class.java,
            Int::class.javaPrimitiveType
        ).apply {
            isAccessible = true
        }.let { method ->
            Pair(instance, method)
        }
    }

    override fun ping(): Boolean = true

    override fun tapKey(
        keyCode: Int,
        eventTime: Long
    ): Boolean {
        if (keyCode !in ALLOWED_KEYS) return false

        return try {
            val now = SystemClock.uptimeMillis()
            val downTime = now
            val safeEventTime = eventTime.coerceAtLeast(now)

            val down = KeyEvent(
                downTime,
                safeEventTime,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0,
                0,
                KeyEvent.VIRTUAL_KEYBOARD,
                0,
                KeyEvent.FLAG_FROM_SYSTEM,
                InputDevice.SOURCE_KEYBOARD
            )

            val up = KeyEvent(
                downTime,
                safeEventTime + 1,
                KeyEvent.ACTION_UP,
                keyCode,
                0,
                0,
                KeyEvent.VIRTUAL_KEYBOARD,
                0,
                KeyEvent.FLAG_FROM_SYSTEM,
                InputDevice.SOURCE_KEYBOARD
            )

            inject(down) && inject(up)
        } catch (error: Throwable) {
            Log.e(TAG, "Key injection failed", error)
            false
        }
    }

    private fun inject(event: InputEvent): Boolean {
        val (instance, method) = inputManager
        return method.invoke(
            instance,
            event,
            INJECT_ASYNC
        ) as? Boolean ?: false
    }

    override fun stop() {
        // Each tap sends its own DOWN and UP pair.
        // No key is intentionally left held.
    }
}
