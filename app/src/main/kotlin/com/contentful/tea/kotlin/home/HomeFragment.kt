package com.contentful.tea.kotlin.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.contentful.Contentful
import com.contentful.tea.kotlin.contentful.Layout
import com.contentful.tea.kotlin.contentful.LayoutModule
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.setImageResourceFromUrl
import kotlinx.android.synthetic.main.course_card.view.*
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * This fragment will be the actual starting point of the app: Showing modules and offering
 * settings.
 */
class HomeFragment : Fragment() {
    private var contentful: Contentful = Contentful()

    private lateinit var dependencies: Dependencies

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement Dependency provider.")
        }

        dependencies = (activity as DependenciesProvider).dependencies()

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentful.fetchHomeLayout { layout: Layout ->
            layout.contentModules.forEach { module ->
                activity?.runOnUiThread {
                    layoutInflater.inflate(R.layout.course_card, home_courses, false).apply {
                        updateModuleView(this, module)
                        home_courses?.addView(this)
                    }
                }
            }
        }

        main_bottom_navigation.setOnNavigationItemSelectedListener {
            if (activity != null) {
                bottomNavigationItemSelected(it)
                true
            } else {
                false
            }
        }
    }

    private fun updateModuleView(view: View, module: LayoutModule) {
        val parser = dependencies.markdown
        when (module) {
            is LayoutModule.HightlightedCourse -> {
                view.card_title.text = parser.parse(module.course.title)
                view.card_description.text = parser.parse(module.course.shortDescription)
                view.card_background.setImageResourceFromUrl(module.course.image)

                val l: (View) -> Unit = {
                    val navController = NavHostFragment.findNavController(this@HomeFragment)
                    val action = HomeFragmentDirections.openCourseOverview(module.course.id)
                    navController.navigate(action)
                }

                view.setOnClickListener(l)
                view.card_call_to_action.setOnClickListener(l)
            }
            is LayoutModule.HeroImage -> {
                view.card_title.text = parser.parse(module.title)
                view.card_background.setImageResourceFromUrl(module.backgroundImage)
                view.card_scrim.setBackgroundResource(android.R.color.transparent)
            }
            is LayoutModule.Copy -> {
                view.card_title.text = parser.parse(module.headline)
                view.card_description.text = parser.parse(module.copy)
                view.card_background.setBackgroundResource(android.R.color.transparent)
                view.card_scrim.setBackgroundResource(android.R.color.transparent)
            }
        }
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