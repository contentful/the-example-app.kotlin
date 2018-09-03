package com.contentful.tea.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.contentful.tea.kotlin.content.Api
import com.contentful.tea.kotlin.content.EditorialFeature
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.content.toApi
import com.contentful.tea.kotlin.content.toEditorialFeature
import com.contentful.tea.kotlin.courses.CoursesFragmentDirections
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.routing.MainRouteCallback
import com.contentful.tea.kotlin.routing.RouteCallback
import com.contentful.tea.kotlin.routing.route

class MainActivity : AppCompatActivity(), DependenciesProvider {

    var dependencies: Dependencies? = null

    private val routeCallback: RouteCallback = MainRouteCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))

        supportActionBar?.apply {
            title = ""
        }

        rerouteIfNeeded()

        findNavController(
            this,
            R.id.navigation_host_fragment
        ).addOnNavigatedListener { _, _ ->
            invalidateOptionsMenu()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        rerouteIfNeeded()
    }

    private fun rerouteIfNeeded() {
        if (intent != null && intent.action == Intent.ACTION_VIEW) {
            val pathAndParameter = intent?.dataString.orEmpty().substringAfter("://")
            val (parameter, path) = separateParameterFromPath(pathAndParameter)

            dependencies().contentInfrastructure.applyParameter(
                parameter = parameter,
                errorHandler = { routingError(pathAndParameter) },
                successHandler = {
                    parameter.storeInSharedPreferences()
                    Navigation.findNavController(
                        this@MainActivity,
                        R.id.navigation_host_fragment
                    ).popBackStack(R.id.home, false)

                    if (!route(path, parameter, routeCallback)) {
                        routingError(pathAndParameter)
                    }
                }
            )
        }
    }

    private fun Parameter.storeInSharedPreferences() {
        val preferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        preferences.edit().apply {
            putString(getString(R.string.settings_key_space_id), spaceId)
            putString(getString(R.string.settings_key_preview_token), previewToken)
            putString(getString(R.string.settings_key_delivery_token), deliveryToken)
            putString(getString(R.string.settings_key_api), (api ?: Api.CDA).name)
            putString(getString(R.string.settings_key_locale), locale)
            putString(getString(R.string.settings_key_host), host)
            putBoolean(
                getString(R.string.settings_key_editorial),
                editorialFeature == EditorialFeature.Enabled
            )
        }.apply()
    }

    private fun routingError(pathAndParameter: String) {
        val navController = findNavController(this, R.id.navigation_host_fragment)
        showError(
            message = getString(R.string.error_parsing_route, pathAndParameter),
            moreTitle = getString(R.string.error_open_settings_button),
            moreHandler = {
                val action = CoursesFragmentDirections.openSettings()
                navController.navigate(action)
            }
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply { clear() }
        val navController = findNavController(this, R.id.navigation_host_fragment)
        if (
            navController.currentDestination?.label != "fragment_settings" &&
            navController.currentDestination?.label != "fragment_qrcode_scanner" &&
            navController.currentDestination?.label != "fragment_space_settings"
        ) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> openSettings()
        R.id.action_refresh -> reload()

        else -> false
    }

    fun openSettings(): Boolean {
        findNavController(this, R.id.navigation_host_fragment).navigate(R.id.settings)
        return true
    }

    private fun reload(): Boolean {
        val controller = findNavController(this, R.id.navigation_host_fragment)
        val destination =
            controller.currentDestination

        if (destination != null) {
            val parentFragment = supportFragmentManager.fragments[0]
            val childFragment = parentFragment.childFragmentManager.fragments[0]

            if (childFragment is Reloadable) {
                childFragment.reload()
            } else {
                Log.e(
                    "Contentful",
                    "Fragment is not reloadable! Please make fragment " +
                        "'${childFragment?.javaClass}' implement ReloadableFragment"
                )
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        invalidateOptionsMenu()
        return findNavController(this, R.id.navigation_host_fragment).navigateUp()
    }

    override fun onBackPressed() {
        invalidateOptionsMenu()
        super.onBackPressed()
    }

    override fun dependencies(): Dependencies {
        if (dependencies == null) {
            dependencies = Dependencies(applicationContext)
        }

        return dependencies as Dependencies
    }
}

fun separateParameterFromPath(uri: String): Pair<Parameter, String> {
    if (!uri.contains("?")) {
        return Pair(Parameter(), uri)
    }

    val (_, parameter) = uri.split("?")

    val parameterMap = parameter
        .split("&")
        .map { it.split("=") }
        .filter { it.size == 2 }
        .map { Pair(it[0], it[1]) }
        .toMap()

    return Pair(
        Parameter(
            spaceId = parameterMap["space_id"].orEmpty(),
            api = parameterMap["api"].toApi(),
            previewToken = parameterMap["preview_token"].orEmpty(),
            deliveryToken = parameterMap["delivery_token"].orEmpty(),
            editorialFeature = parameterMap["editorial_features"].toEditorialFeature(),
            locale = parameterMap["locale"].orEmpty(),
            host = parameterMap["host"].orEmpty()
        ), uri.substringBefore("?")
    )
}