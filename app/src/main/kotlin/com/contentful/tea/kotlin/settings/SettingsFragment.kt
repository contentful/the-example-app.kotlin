package com.contentful.tea.kotlin.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.contentful.java.cda.CDALocale
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.content.Api
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.content.toUrl
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.toast
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

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

    override fun onResume() {
        activity?.findViewById<Toolbar>(R.id.main_toolbar)?.findViewById<View>(R.id.logo_image)
            ?.setOnClickListener { goToParent() }

        super.onResume()
    }

    private fun fillPreferences() {
        parameter = dependencies.contentInfrastructure.parameter.copy()
        fillInApi(
            findPreference(getString(R.string.settings_key_api)) as ListPreference
        )

        fillInLocales(
            findPreference(getString(R.string.settings_key_locale)) as ListPreference
        )

        dependencies.contentInfrastructure.fetchSpace(errorCallback = {}) { space ->
            activity?.runOnUiThread {
                findPreference(getString(R.string.settings_key_space_connect))?.summary =
                    space.name()
            }
        }
    }

    private fun fillInApi(listPreference: ListPreference) =
        listPreference.apply {
            val apiName = (parameter.api ?: Api.CDA).name
            setDefaultValue(apiName)
            summary = apiName
            value = apiName
            entries = Api.values().map { it.toString() }.toTypedArray()
            entryValues = entries

            setOnPreferenceChangeListener { _, newValue ->
                parameter.api = Api.valueOf(newValue as String)
                summary = newValue
                preferenceChanged()
            }
        }

    private fun fillInLocales(listPreference: ListPreference) =
        listPreference.apply {
            setDefaultValue(parameter.locale)
            summary = parameter.locale
            value = parameter.locale

            dependencies.contentInfrastructure.fetchAllLocales(errorCallback = { _ ->
                activity?.toast(getString(R.string.error_could_not_fetch_locales))
            }, successCallback = { locales ->
                entries = locales.map(CDALocale::code).toTypedArray()
                entryValues = entries
                setOnPreferenceChangeListener { _, newValue ->
                    parameter.locale = newValue as String
                    summary = newValue
                    preferenceChanged()
                }
            })
        }

    private fun preferenceChanged(): Boolean {
        dependencies.contentInfrastructure.applyParameter(
            parameter = parameter,
            errorHandler = {
                activity?.runOnUiThread {
                    activity?.showError(
                        message = "${getString(R.string.error_settings_cannot_change)}<br/>" +
                            "<tt>${it.message}</tt>",
                        moreTitle = getString(R.string.error_settings_reset),
                        moreHandler = {
                            fillPreferences()
                        }
                    )
                }
            },
            successHandler = { space ->
                activity?.runOnUiThread {
                    activity?.toast(
                        getString(
                            R.string.settings_connected_successfully_to_space,
                            space.name()
                        ),
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
        findPreference(getString(R.string.settings_key_licences))?.apply {
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

        findPreference(getString(R.string.settings_key_imprint))?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openImprint())
                true
            }
        }

        findPreference(getString(R.string.settings_key_about))?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openAbout())
                true
            }
        }

        findPreference(getString(R.string.settings_key_space_connect))?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openSpaceSettings())
                true
            }
        }

        findPreference(getString(R.string.settings_key_qr))?.apply {
            setOnPreferenceClickListener {
                navController.navigate(SettingsFragmentDirections.openScanQR())
                true
            }
        }

        findPreference(getString(R.string.settings_key_share_qr))?.apply {
            setOnPreferenceClickListener {
                shareQrCode()
                true
            }
        }
    }

    private fun shareQrCode() {
        if (ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE_EXTERNAL_REQUEST_ID
            )
        } else {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(encodeSettings(), BarcodeFormat.QR_CODE, 512, 512)

            val path = MediaStore.Images.Media.insertImage(
                activity?.contentResolver,
                bitmap,
                "QR Code TEA",
                "QR Code encapsulating settings from Contentful."
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
            startActivity(Intent.createChooser(intent, "Share QR Code"))
        }
    }

    private fun encodeSettings(): String = dependencies.contentInfrastructure.parameter.toUrl()

    private fun goToParent() {
        val navController = NavHostFragment.findNavController(this)
        navController.navigateUp()
    }
}

private const val PERMISSION_WRITE_EXTERNAL_REQUEST_ID: Int = 2
