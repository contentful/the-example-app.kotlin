package com.contentful.tea.kotlin.routing

import android.app.Activity
import android.widget.Toast
import androidx.navigation.Navigation
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.toHtml
import com.contentful.tea.kotlin.home.HomeFragmentDirections

class MainRouteCallback(private val activity: Activity) : RouteCallback() {

    init {
        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement from DependencyProvider.")
        }
    }

    private fun applyParameter(parameter: Parameter, successHandler: () -> Unit) {
        val dependencies = (activity as DependenciesProvider).dependencies()
        dependencies.contentInfrastructure.applyParameter(parameter, ::error) {
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    activity.getString(
                        R.string.settings_connected_successfully_to_space,
                        it.name()
                    ).toHtml(),
                    Toast.LENGTH_LONG
                ).show()
                successHandler()
            }
        }
    }

    override fun openHome(parameter: Parameter) {
        applyParameter(parameter) {
            val action = HomeFragmentDirections.openHome()
            Navigation.findNavController(
                activity,
                R.id.navigation_host_fragment
            ).navigate(action)
        }
    }

    override fun goToCourse(courseSlug: String, parameter: Parameter) {
        applyParameter(parameter) {
            val action = HomeFragmentDirections.openCourseOverview(courseSlug)
            Navigation.findNavController(
                activity,
                R.id.navigation_host_fragment
            ).navigate(action)
        }
    }

    override fun goToCategory(categorySlug: String, parameter: Parameter) {
        applyParameter(parameter) {
            val action = HomeFragmentDirections.openCategory(categorySlug)
            Navigation.findNavController(
                activity,
                R.id.navigation_host_fragment
            ).navigate(action)
        }
    }

    override fun goToLesson(courseSlug: String, lessonSlug: String, parameter: Parameter) {
        applyParameter(parameter) {
            val action = HomeFragmentDirections.openLesson(courseSlug, lessonSlug)
            Navigation.findNavController(
                activity,
                R.id.navigation_host_fragment
            ).navigate(action)
        }
    }

    private fun error(throwable: Throwable) {
        activity.showError(
            message = activity.getString(R.string.error_settings_cannot_change),
            error = throwable
        )
    }
}
