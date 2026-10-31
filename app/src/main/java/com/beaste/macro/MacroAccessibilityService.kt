
package com.beaste.macro

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MacroAccessibilityService : AccessibilityService() {

    private val scheduler =
        Executors.newScheduledThreadPool(2)

    private val jobs =
        ConcurrentHashMap<Int, ScheduledFuture<*>>()

    private val keyMap = mapOf(
        KeyEvent.KEYCODE_R to ("R" to "KEYCODE_R"),
        KeyEvent.KEYCODE_Z to ("Z" to "KEYCODE_Z"),
        KeyEvent.KEYCODE_X to ("X" to "KEYCODE_X"),
        KeyEvent.KEYCODE_C to ("C" to "KEYCODE_C"),
        KeyEvent.KEYCODE_V to ("V" to "KEYCODE_V")
    )

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) = Unit

    override fun onInterrupt() {
        stopAll()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {

        val pair = keyMap[event.keyCode]
            ?: return false

        if (!MacroSettings.enabled(this)) {
            return false
        }

        if (pair.first !in MacroSettings.selected(this)) {
            return false
        }

        if (!ShizukuController.connected) {
            return false
        }

        when (event.action) {

            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    startRepeating(
                        event.keyCode,
                        pair.second
                    )
                }
            }

            KeyEvent.ACTION_UP -> {
                stopRepeating(event.keyCode)
            }
        }

        return false
    }

    private fun startRepeating(
        keyCode: Int,
        shellKeyCode: String
    ) {
        if (jobs.containsKey(keyCode)) return

        val period = MacroSettings.speed(this).toLong()

        val future = scheduler.scheduleAtFixedRate({

            if (!MacroSettings.enabled(this) ||
                !ShizukuController.connected
            ) {
                stopRepeating(keyCode)
            } else {
                ShizukuController.send(shellKeyCode)
            }

        }, period, period, TimeUnit.MILLISECONDS)

        jobs[keyCode] = future
    }

    private fun stopRepeating(keyCode: Int) {
        jobs.remove(keyCode)?.cancel(false)
    }

    private fun stopAll() {
        jobs.values.forEach {
            it.cancel(false)
        }

        jobs.clear()
    }

    override fun onDestroy() {
        stopAll()
        scheduler.shutdownNow()
        super.onDestroy()
    }
}
