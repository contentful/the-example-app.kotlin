package com.contentful.tea.kotlin

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider

class MainActivity : AppCompatActivity(), DependenciesProvider {

    var dependencies: Dependencies? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))

        supportActionBar?.apply {
            title = ""
        }

        findNavController(
            this,
            R.id.navigation_host_fragment
        ).addOnNavigatedListener { _, _ ->
            invalidateOptionsMenu()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply { clear() }
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_refresh -> reload()
        else -> false
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