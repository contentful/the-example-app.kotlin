package com.contentful.tea.kotlin.settings

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.content.parameterFromBuildConfig
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.toHtml
import com.contentful.tea.kotlin.extensions.toast

class SettingsCredentialsFragment : PreferenceFragmentCompat() {
    private lateinit var dependencies: Dependencies
    private var parameter: Parameter = Parameter()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_credentials)

        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement Dependency provider.")
        }

        dependencies = (activity as DependenciesProvider).dependencies()

        setupStaticRoutes()

        setupCurrentValues()
    }

    private fun setupStaticRoutes() {
        val navController = NavHostFragment.findNavController(this)
        findPreference(getString(R.string.settings_key_qr))?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openScanQR())
                true
            }
        }

        findPreference(getString(R.string.settings_key_reset))?.apply {
            setOnPreferenceClickListener {
                resetParameter()
                true
            }
        }
    }

    private fun resetParameter() {
        parameter = parameterFromBuildConfig()

        dependencies.contentInfrastructure.applyParameter(
            parameter = parameter,
            errorHandler = {
                activity?.showError(getString(R.string.error_settings_cannot_change))
            },
            successHandler = { space ->
                setupCurrentValues()

                activity?.toast(
                    getString(
                        R.string.settings_connected_successfully_to_space,
                        space.name()
                    )
                )
            }
        )
    }

    private fun setupCurrentValues() {
        dependencies.contentInfrastructure.fetchSpace(
            errorCallback = {
                activity?.showError(getString(R.string.error_settings_cannot_change))
            },
            successCallback = { space ->
                activity?.runOnUiThread {
                    findPreference(getString(R.string.settings_key_space_information))
                        .summary = space.name()

                    setEditPreference(
                        R.string.settings_key_space_id,
                        dependencies.contentInfrastructure.parameter.spaceId
                    ) { parameter.spaceId = it }
                    setEditPreference(
                        R.string.settings_key_delivery_token,
                        dependencies.contentInfrastructure.parameter.deliveryToken
                    ) { parameter.deliveryToken = it }
                    setEditPreference(
                        R.string.settings_key_preview_token,
                        dependencies.contentInfrastructure.parameter.previewToken
                    ) { parameter.previewToken = it }
                    setEditPreference(
                        R.string.settings_key_host,
                        dependencies.contentInfrastructure.parameter.host
                    ) { parameter.host = it }
                }
            }
        )
    }

    private fun setEditPreference(
        @StringRes preferenceId: Int,
        value: String,
        update: (String) -> Unit
    ) {
        val preference = findPreference(getString(preferenceId)) as EditTextPreference
        update(value)

        preference.summary = value
        preference.text = value
        preference.setOnPreferenceChangeListener { _, newValue ->
            update(newValue.toString())

            checkNewParameter(preference, newValue)
            true
        }
    }

    private fun checkNewParameter(preference: Preference, newValue: Any) {
        preference.summary = newValue.toString()

        dependencies.contentInfrastructure.applyParameter(
            parameter = parameter,
            errorHandler = {
                highlightError(preference)
            },
            successHandler = { space ->
                activity?.toast(
                    getString(
                        R.string.settings_connected_successfully_to_space,
                        space.name()
                    )
                )
            }
        )
    }

    private fun highlightError(preference: Preference) = activity?.runOnUiThread {
        preference.summary = """<font color="red">⚠️ ${preference.summary} ⚠️</font>️️""".toHtml()

        activity?.showError(
            message = getString(R.string.error_settings_cannot_change),
            moreTitle = getString(R.string.error_settings_reset),
            moreHandler = ::resetParameter
        )
    }

    private fun goToParent() {
        val navController = NavHostFragment.findNavController(this)
        navController.popBackStack()
    }
}
