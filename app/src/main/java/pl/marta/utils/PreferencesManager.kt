package pl.marta.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import pl.marta.MartaApplication

object PreferencesManager {

    private const val NIGHT_MODE = "night_mode"
    private val sharedPref: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(MartaApplication.instance)

    fun getNightMode() = sharedPref.getBoolean(NIGHT_MODE, false)

    fun setNightMode(newValue: Boolean) {
        sharedPref.edit()
            .putBoolean(NIGHT_MODE, newValue)
            .apply()
    }
}