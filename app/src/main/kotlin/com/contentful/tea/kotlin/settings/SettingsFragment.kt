package com.contentful.tea.kotlin.settings

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.contentful.java.cda.CDALocale
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.contentful.Api
import com.contentful.tea.kotlin.contentful.EditorialFeature
import com.contentful.tea.kotlin.contentful.Parameter
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.toHtml
import com.contentful.tea.kotlin.extensions.toast
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var dependencies: Dependencies
    private var parameter: Parameter = Parameter()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_main)

        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement Dependency provider.")
        }

        dependencies = (activity as DependenciesProvider).dependencies()

        fillPreferences()

        setupStaticRoutes()
    }

    private fun fillPreferences() {
        val currentParameter = dependencies.contentful.parameter
        fillInApi(
            currentParameter,
            findPreference(getString(R.string.settings_key_api)) as ListPreference
        )

        fillInLocales(
            currentParameter,
            findPreference(getString(R.string.settings_key_locale)) as ListPreference
        )

        fillInEditorials(
            currentParameter,
            findPreference(getString(R.string.settings_key_editorial)) as SwitchPreference
        )

        dependencies.contentful.fetchSpace(errorCallback = {}) { space ->
            activity?.runOnUiThread {
                findPreference(getString(R.string.settings_key_space_connect))?.summary =
                    space.name()
            }
        }
    }

    private fun fillInApi(currentParameter: Parameter, listPreference: ListPreference) =
        listPreference.apply {
            setDefaultValue(currentParameter.api.name)
            summary = currentParameter.api.name
            value = currentParameter.api.name
            entries = Api.values().map { it.toString() }.toTypedArray()
            entryValues = entries

            setOnPreferenceChangeListener { preference, newValue ->
                parameter.api = Api.valueOf(newValue as String)
                summary = newValue
                preferenceChanged(preference, newValue)
            }
        }

    private fun fillInLocales(currentParameter: Parameter, listPreference: ListPreference) =
        listPreference.apply {
            setDefaultValue(currentParameter.locale)
            summary = currentParameter.locale
            value = currentParameter.locale

            dependencies.contentful.fetchAllLocales(errorCallback = { _ ->
                activity?.toast(getString(R.string.error_could_not_fetch_locales))
            }, successCallback = { locales ->
                entries = locales.map(CDALocale::code).toTypedArray()
                entryValues = entries
                setOnPreferenceChangeListener { preference, newValue ->
                    parameter.locale = newValue as String
                    summary = newValue
                    preferenceChanged(preference, newValue)
                }
            })
        }

    private fun fillInEditorials(
        currentParameter: Parameter,
        switchPreference: SwitchPreference
    ) =
        switchPreference.apply {
            isChecked = currentParameter.editorialFeature == EditorialFeature.Enabled

            setOnPreferenceChangeListener { preference, newValue ->
                parameter.editorialFeature =
                    if (newValue == true) EditorialFeature.Enabled else EditorialFeature.Disabled
                preferenceChanged(preference, newValue)
            }
        }

    private fun preferenceChanged(preference: Preference, newValue: Any?): Boolean {
        dependencies.contentful.applyParameter(
            parameter = parameter,
            errorHandler = {
                activity?.showError(
                    message = "${getString(R.string.error_settings_cannot_change)}<br/>" +
                        "<tt>${it.message}</tt>",
                    moreTitle = getString(R.string.error_settings_reset),
                    moreHandler = {
                        fillPreferences()
                    }
                )
            },
            successHandler = { space ->
                activity?.runOnUiThread {
                    activity?.toast(
                        getString(
                            R.string.settings_connected_successfully_to_space,
                            space.name()
                        ).toHtml(),
                        false
                    )

                    findPreference(getString(R.string.settings_key_space_connect))?.summary =
                        space.name()
                }
            }
        )

        return true
    }

    private fun setupStaticRoutes() {
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
