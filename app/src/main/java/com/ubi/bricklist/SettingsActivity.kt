package com.ubi.bricklist

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.PreferenceFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceManager


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

//        val sharedPref = getSharedPreferences(
//            "text", Context.MODE_PRIVATE)
//        summaryToValue()

        title = "Settings"

        if (fragmentManager.findFragmentById(android.R.id.content) == null) {
            fragmentManager.beginTransaction()
                .add(android.R.id.content, SettingsFragment()).commit()
        }
    }

    class SettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
        }
    }

    companion object {
        private val summaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            if (preference is androidx.preference.EditTextPreference) {
                preference.summary = value.toString()
            }
            true
        }

        private fun summaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = summaryToValueListener

            summaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, ""))
        }
    }
}
