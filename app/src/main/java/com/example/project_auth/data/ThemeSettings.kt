package com.example.project_auth.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** The user's light/dark choice, kept on the device. SYSTEM follows the phone's setting. */
class ThemeSettings(private val prefs: SharedPreferences) {

    private val _mode = MutableStateFlow(read())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun set(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _mode.value = mode
    }

    private fun read(): ThemeMode =
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_MODE, null).orEmpty()) }
            .getOrDefault(ThemeMode.SYSTEM)

    companion object {
        private const val FILE_NAME = "settings"
        private const val KEY_MODE = "theme_mode"

        lateinit var instance: ThemeSettings
            private set

        /** Called once from [com.example.project_auth.TechWatchApp]. */
        fun init(context: Context) {
            instance = ThemeSettings(context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE))
        }
    }
}
