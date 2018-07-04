package com.contentful.tea.kotlin

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener { bottomNavigationItemSelected(it) }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
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

    private fun bottomNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(this, R.id.navigation_host_fragment)
        return when (item.itemId) {
            R.id.bottom_navigation_home -> {
                navigateIfNotAlreadyThere(navController, R.id.home)
            }
            R.id.bottom_navigation_courses -> {
                navigateIfNotAlreadyThere(navController, R.id.courses)
                true
            }
            else -> false
        }
    }

    private fun navigateIfNotAlreadyThere(navController: NavController, @IdRes id: Int): Boolean =
        if (navController.currentDestination.id != id) {
            navController.navigate(id)
            true
        } else {
            false
        }
}