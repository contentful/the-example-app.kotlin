package com.contentful.tea.kotlin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController

class MainActivity : AppCompatActivity(), DependenciesProvider {

    var dependencies: Dependencies? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))

        supportActionBar?.apply {
            title = ""
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