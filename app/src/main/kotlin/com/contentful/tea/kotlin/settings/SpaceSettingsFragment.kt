package com.contentful.tea.kotlin.settings

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceFragmentCompat
import com.contentful.tea.kotlin.R

class SpaceSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_space)
        val navController = NavHostFragment.findNavController(this)
        findPreference("scan_qr")?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openScanQR())
                true
            }
        }
    }
}
