
package com.beaste.macro

import android.content.Context

object MacroSettings {

    private const val PREFS = "beaste_preferences"
    private const val KEYS = "selected_keys"
    private const val SPEED = "repeat_interval"
    private const val ENABLED = "macro_enabled"

    const val MIN_SPEED = 10
    const val MAX_SPEED = 500

    val availableKeys = setOf("R", "Z", "X", "C", "V")

    private fun prefs(context: Context) =
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

    fun selected(context: Context): Set<String> =
        prefs(context)
            .getStringSet(KEYS, emptySet())
            ?.toSet()
            .orEmpty()
            .intersect(availableKeys)

    fun setSelected(context: Context, value: Set<String>) {
        prefs(context).edit()
            .putStringSet(KEYS, value.intersect(availableKeys))
            .apply()
    }

    fun speed(context: Context): Int =
        prefs(context)
            .getInt(SPEED, 100)
            .coerceIn(MIN_SPEED, MAX_SPEED)

    fun setSpeed(context: Context, value: Int) {
        prefs(context).edit()
            .putInt(SPEED, value.coerceIn(MIN_SPEED, MAX_SPEED))
            .apply()
    }

    fun enabled(context: Context): Boolean =
        prefs(context).getBoolean(ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit()
            .putBoolean(ENABLED, value)
            .apply()
    }
}
