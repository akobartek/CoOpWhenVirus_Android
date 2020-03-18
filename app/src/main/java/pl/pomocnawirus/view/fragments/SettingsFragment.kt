package pl.pomocnawirus.view.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import pl.pomocnawirus.R
import pl.pomocnawirus.utils.PreferencesManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        preferenceManager
            .findPreference<Preference>(getString(R.string.night_mode_key))
            ?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            activity?.let {
                AppCompatDelegate.setDefaultNightMode(
                    if (newValue as Boolean) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                PreferencesManager.setNightMode(newValue)
            }
            true
        }

        preferenceManager
            .findPreference<Preference>("signOut")
            ?.setOnPreferenceClickListener {
                // TODO() Sign out from Firebase Auth
                true
            }
    }
}
