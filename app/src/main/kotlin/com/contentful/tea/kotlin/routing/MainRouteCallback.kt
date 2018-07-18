package com.contentful.tea.kotlin.routing

import android.app.Activity
import androidx.navigation.Navigation
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.home.HomeFragmentDirections

class MainRouteCallback(val activity: Activity) : RouteCallback() {

    init {
        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement from DependencyProvider.")
        }
    }

    private fun applyParameter(parameter: Parameter) {
        val dependencies = (activity as DependenciesProvider).dependencies()
        dependencies.contentful.applyParameter(parameter)
    }

    override fun openHome(parameter: Parameter) {
        applyParameter(parameter)

        val action = HomeFragmentDirections.openHome()
        Navigation.findNavController(
            activity,
            R.id.navigation_host_fragment
        ).navigate(action)
    }

    override fun goToCourse(courseSlug: String, parameter: Parameter) {
        applyParameter(parameter)

        val action = HomeFragmentDirections.openCourseOverview(courseSlug)
        Navigation.findNavController(
            activity,
            R.id.navigation_host_fragment
        ).navigate(action)
    }

    override fun goToCategory(categorySlug: String, parameter: Parameter) {
        applyParameter(parameter)

        val action = HomeFragmentDirections.openCategory(categorySlug)
        Navigation.findNavController(
            activity,
            R.id.navigation_host_fragment
        ).navigate(action)
    }

    override fun goToLesson(courseSlug: String, lessonSlug: String, parameter: Parameter) {
        applyParameter(parameter)

        val action = HomeFragmentDirections.openLesson(courseSlug, lessonSlug)
        Navigation.findNavController(
            activity,
            R.id.navigation_host_fragment
        ).navigate(action)
    }
}
