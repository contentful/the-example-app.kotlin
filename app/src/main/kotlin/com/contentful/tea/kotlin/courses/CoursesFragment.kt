package com.contentful.tea.kotlin.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.extensions.forEach
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_courses.*

class CoursesFragment : Fragment() {
    private var categoryId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.apply {
            categoryId = CoursesFragmentArgs.fromBundle(arguments).categoryId
        }

        return inflater.inflate(R.layout.fragment_courses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Make dynamic
        courses_top_navigation.addTab(courses_top_navigation.newTab().setText(categoryId))

        courses_bottom_navigation.setOnNavigationItemSelectedListener {
            if (activity != null) {
                bottomNavigationItemSelected(it)
                true
            } else {
                false
            }
        }
        courses_top_navigation.addOnTabSelectedListener(UsableOnTabListener {
            topNavigationItemSelected(
                it
            )
        })

        val navController = Navigation.findNavController(activity!!, R.id.navigation_host_fragment)
        courses_list.forEach { index, view ->
            view.setOnClickListener {
                val action = CoursesFragmentDirections.openCourseOverview("course " + index)
                navController.navigate(action)
            }
        }
    }

    private fun topNavigationItemSelected(tab: TabLayout.Tab) = if (!isDetached) {
        val navController = Navigation.findNavController(activity!!, R.id.navigation_host_fragment)
        navController.navigate(CoursesFragmentDirections.openCategory(tab.tag as String))
    } else {
    }

    private fun bottomNavigationItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(activity!!, R.id.navigation_host_fragment)
        return when (item.itemId) {
            R.id.bottom_navigation_home -> {
                navigateIfNotAlreadyThere(navController, R.id.home)
                true
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

class UsableOnTabListener(private val selectDelegate: (TabLayout.Tab) -> Unit) :
    TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab ?: selectDelegate(tab!!)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {}
}
