package com.example.data.local

import android.content.Context

data class PreparationOptions(
    val autoPrepare: Boolean = true,
    val cleanCache: Boolean = true,
    val checkNetwork: Boolean = true,
    val showResults: Boolean = true,
    val dndOnLaunch: Boolean = false
)

class PreparationPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("glassboost_preparation", Context.MODE_PRIVATE)
    fun read() = PreparationOptions(
        prefs.getBoolean("auto", true),
        prefs.getBoolean("clean", true),
        prefs.getBoolean("network", true),
        prefs.getBoolean("results", true),
        prefs.getBoolean("dnd", false)
    )
    fun save(options: PreparationOptions) {
        prefs.edit()
            .putBoolean("auto", options.autoPrepare)
            .putBoolean("clean", options.cleanCache)
            .putBoolean("network", options.checkNetwork)
            .putBoolean("results", options.showResults)
            .putBoolean("dnd", options.dndOnLaunch)
            .apply()
    }
}
