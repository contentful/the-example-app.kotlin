package com.contentful.tea.kotlin.settings

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceFragmentCompat
import com.contentful.tea.kotlin.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_main)

        val navController = NavHostFragment.findNavController(this)
        findPreference("licences")?.apply {
            setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        activity,
                        OssLicensesMenuActivity::class.java
                    )
                )
                true
            }
        }
        findPreference("imprint")?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openImprint())
                true
            }
        }
        findPreference("about")?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openAbout())
                true
            }
        }
        findPreference("connect_to_space")?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openSpaceSettings())
                true
            }
        }
        findPreference("scan_qr")?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openScanQR())
                true
            }
        }
    }
}
