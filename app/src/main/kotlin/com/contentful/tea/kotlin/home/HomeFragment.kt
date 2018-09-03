package com.contentful.tea.kotlin.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.Reloadable
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.LayoutModule
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.isNetworkError
import com.contentful.tea.kotlin.extensions.setImageResourceFromUrl
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.showNetworkError
import kotlinx.android.synthetic.main.course_card.view.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), Reloadable {

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

        main_bottom_navigation.setOnNavigationItemSelectedListener {
            if (activity != null) {
                bottomNavigationItemSelected(it)
                true
            } else {
                false
            }
        }

        loadHomeView()
    }

    override fun onResume() {
        activity?.findViewById<Toolbar>(R.id.main_toolbar)?.findViewById<View>(R.id.logo_image)
            ?.setOnClickListener { navigateUp() }

        super.onResume()
    }

    private fun loadHomeView() {
        dependencies
            .contentInfrastructure
            .fetchHomeLayout(errorCallback = ::errorFetchingLayout) { layout: Layout ->
                layout.contentModules.forEach { module ->
                    activity?.runOnUiThread {
                        layoutInflater.inflate(R.layout.course_card, home_courses, false)
                            .apply {
                                updateModuleView(this, module)
                                home_courses?.addView(this)
                            }
                    }
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
                    val action = HomeFragmentDirections.openCourseOverview(module.course.slug)
                    navController.navigate(action)
                }

                view.setOnClickListener(l)
                view.card_call_to_action.visibility = View.VISIBLE
                view.card_call_to_action.setOnClickListener(l)
            }
            is LayoutModule.HeroImage -> {
                view.card_title.text = parser.parse(module.title)
                view.card_background.setImageResourceFromUrl(module.backgroundImage)
                view.card_scrim.setBackgroundResource(android.R.color.transparent)
                view.card_call_to_action.visibility = View.GONE
            }
            is LayoutModule.Copy -> {
                view.card_title.text = parser.parse(module.headline)
                view.card_description.text = parser.parse(module.copy)
                view.card_background.setBackgroundResource(android.R.color.transparent)
                view.card_scrim.setBackgroundResource(android.R.color.transparent)

                view.card_call_to_action.visibility = View.VISIBLE
                view.card_call_to_action.text = module.ctaTitle
                view.card_call_to_action.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(module.ctaLink)))
                }
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
        if (navController.currentDestination?.id != id) {
            navController.navigate(id)
            true
        } else {
            false
        }

    private fun errorFetchingLayout(throwable: Throwable) {
        activity?.apply {
            if (throwable.isNetworkError()) {
                showNetworkError()
            } else {
                val navController = NavHostFragment.findNavController(this@HomeFragment)
                showError(
                    message = getString(R.string.error_fetching_layout),
                    error = throwable,
                    okHandler = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    private fun navigateUp() {}

    override fun reload() {
        home_courses.removeAllViews()
        loadHomeView()
    }
}