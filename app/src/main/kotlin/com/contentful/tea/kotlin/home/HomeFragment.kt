package com.contentful.tea.kotlin.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.contentful.tea.kotlin.BuildConfig
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.Reloadable
import com.contentful.tea.kotlin.content.Api
import com.contentful.tea.kotlin.content.ContentInfrastructure
import com.contentful.tea.kotlin.content.EditorialFeature
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.LayoutModule
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.isNetworkError
import com.contentful.tea.kotlin.extensions.setImageResourceFromUrl
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.showNetworkError
import kotlinx.android.synthetic.main.course_card.view.*
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * This fragment will be the actual starting point of the app: Showing modules and offering
 * settings.
 */
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

        activity?.apply {
            val preferences =
                getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
            dependencies.contentInfrastructure.applyParameterFromSharedPreferences(preferences) {
                loadHomeView()
            }
        }
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
                    moreTitle = getString(R.string.error_open_settings_button),
                    error = throwable,
                    moreHandler = {
                        val action = HomeFragmentDirections.openSettings()
                        navController.navigate(action)
                    },
                    okHandler = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    private fun ContentInfrastructure.applyParameterFromSharedPreferences(
        preferences: SharedPreferences,
        successCallback: () -> Unit
    ) {
        val parameter = Parameter(
            editorialFeature =
            if (preferences.getBoolean(getString(R.string.settings_key_editorial), false)) {
                EditorialFeature.Enabled
            } else {
                EditorialFeature.Disabled
            },
            api = Api.valueOf(
                preferences.getString(
                    getString(R.string.settings_key_api),
                    Api.CDA.name
                )!!
            ),
            locale = preferences.getString(getString(R.string.settings_key_locale), "en-US")!!,
            spaceId = preferences.getString(
                getString(R.string.settings_key_space_id),
                BuildConfig.CONTENTFUL_SPACE_ID
            )!!,
            deliveryToken = preferences.getString(
                getString(R.string.settings_key_delivery_token),
                BuildConfig.CONTENTFUL_DELIVERY_TOKEN
            )!!,
            previewToken = preferences.getString(
                getString(R.string.settings_key_preview_token),
                BuildConfig.CONTENTFUL_PREVIEW_TOKEN
            )!!,
            host = preferences.getString(
                getString(R.string.settings_key_host),
                BuildConfig.CONTENTFUL_HOST
            )!!
        )

        applyParameter(
            parameter,
            errorHandler = { },
            successHandler = { space ->
                Log.d(
                    "HomeFragment.kt",
                    getString(
                        R.string.settings_connected_successfully_to_space,
                        space.name()
                    )
                )

                successCallback()
            }
        )
    }

    private fun navigateUp() {}

    override fun reload() {
        home_courses.removeAllViews()
        loadHomeView()
    }
}