package pl.pomocnawirus.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import pl.pomocnawirus.PomocNaWirusApplication

object PreferencesManager {

    private const val NIGHT_MODE = "night_mode"
    private val sharedPref: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(PomocNaWirusApplication.instance)

    fun getNightMode() = sharedPref.getBoolean(NIGHT_MODE, false)

    fun setNightMode(newValue: Boolean) {
        sharedPref.edit()
            .putBoolean(NIGHT_MODE, newValue)
            .apply()
    }
}