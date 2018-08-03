package com.contentful.tea.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.contentful.tea.kotlin.contentful.EditorialFeature
import com.contentful.tea.kotlin.contentful.Parameter
import com.contentful.tea.kotlin.courses.CoursesFragmentDirections
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.routing.MainRouteCallback
import com.contentful.tea.kotlin.routing.RouteCallback
import com.contentful.tea.kotlin.routing.route
import com.contentful.tea.kotlin.routing.separateParameterFromPath

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
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        rerouteIfNeeded()
    }

    private fun rerouteIfNeeded() {
        if (intent != null && intent.action == Intent.ACTION_VIEW) {
            val pathAndParameter = intent.dataString.substringAfter("://")
            val (parameter, path) = separateParameterFromPath(pathAndParameter)

            dependencies().contentful.applyParameter(
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
            putString(getString(R.string.settings_key_api), api.name)
            putString(getString(R.string.settings_key_locale), locale)
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
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> openSettings()
        else -> false
    }

    fun openSettings(): Boolean {
        findNavController(this, R.id.navigation_host_fragment).navigate(R.id.settings)
        return true
    }

    override fun onSupportNavigateUp() =
        findNavController(this, R.id.navigation_host_fragment).navigateUp()

    override fun dependencies(): Dependencies {
        if (dependencies == null) {
            dependencies = Dependencies(applicationContext)
        }

        return dependencies as Dependencies
    }
}