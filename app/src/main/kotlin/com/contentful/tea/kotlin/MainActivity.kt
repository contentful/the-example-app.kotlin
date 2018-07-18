package com.contentful.tea.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
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
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        rerouteIfNeeded()
    }

    private fun rerouteIfNeeded() {
        if (intent != null && intent.action == Intent.ACTION_VIEW) {
            route(intent.data.host + intent.data.path, routeCallback)
        }
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