package com.contentful.tea.kotlin.settings

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.contentful.Parameter
import com.contentful.tea.kotlin.contentful.parameterFromBuildConfig
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

        dependencies.contentful.applyParameter(
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
        dependencies.contentful.fetchSpace(
            errorCallback = {
                activity?.showError(getString(R.string.error_settings_cannot_change))
            },
            successCallback = { space ->
                activity?.runOnUiThread {
                    findPreference(getString(R.string.settings_key_space_information))
                        .summary = space.name()

                    setSpaceId(findPreference(R.string.settings_key_space_id))
                    setDeliveryToken(findPreference(R.string.settings_key_delivery_token))
                    setPreviewToken(findPreference(R.string.settings_key_preview_token))
                }
            }
        )
    }

    private fun setSpaceId(preference: EditTextPreference) {
        parameter.spaceId = dependencies.contentful.parameter.spaceId

        preference.summary = dependencies.contentful.parameter.spaceId
        preference.text = dependencies.contentful.parameter.spaceId

        preference.setOnPreferenceChangeListener { _, newValue ->
            parameter.spaceId = newValue.toString()

            checkNewParameter(preference, newValue)
            true
        }
    }

    private fun setDeliveryToken(preference: EditTextPreference) {
        parameter.deliveryToken = dependencies.contentful.parameter.deliveryToken

        preference.summary = dependencies.contentful.parameter.deliveryToken
        preference.text = dependencies.contentful.parameter.deliveryToken

        preference.setOnPreferenceChangeListener { _, newValue ->
            parameter.deliveryToken = newValue.toString()

            checkNewParameter(preference, newValue)
            true
        }
    }

    private fun setPreviewToken(preference: EditTextPreference) {
        parameter.previewToken = dependencies.contentful.parameter.previewToken

        preference.summary = dependencies.contentful.parameter.previewToken
        preference.text = dependencies.contentful.parameter.previewToken

        preference.setOnPreferenceChangeListener { _, newValue ->
            parameter.previewToken = newValue.toString()

            checkNewParameter(preference, newValue)
            true
        }
    }

    private fun checkNewParameter(preference: Preference, newValue: Any) {
        preference.summary = newValue.toString()

        dependencies.contentful.applyParameter(
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

    private fun <T : Preference> findPreference(@StringRes id: Int): T =
        findPreference(getString(id)) as T
}
