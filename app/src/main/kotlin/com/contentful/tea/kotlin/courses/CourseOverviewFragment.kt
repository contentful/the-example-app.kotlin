package com.contentful.tea.kotlin.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.Reloadable
import com.contentful.tea.kotlin.content.Course
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.extensions.isNetworkError
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.showNetworkError
import kotlinx.android.synthetic.main.fragment_course_overview.*
import kotlinx.android.synthetic.main.item_lesson.view.*

class CourseOverviewFragment : Fragment(), Reloadable {
    private var courseSlug: String? = null
    private var firstLessonSlug: String? = null

    private lateinit var dependencies: Dependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseSlug = CourseOverviewFragmentArgs.fromBundle(arguments).courseSlug
        }

        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement Dependency provider.")
        }

        dependencies = (activity as DependenciesProvider).dependencies()
    }

    override fun onResume() {
        super.onResume()

        activity?.findViewById<Toolbar>(R.id.main_toolbar)?.findViewById<View>(R.id.logo_image)
            ?.setOnClickListener { goHome() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        overview_next.setOnClickListener { onNextButtonClicked() }

        updateViews()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun reload() {
        overview_container.removeAllViews()
        updateViews()
    }

    private fun updateViews() {
        courseSlug?.let {
            dependencies
                .contentInfrastructure
                .fetchCourseBySlug(
                    courseSlug!!,
                    errorCallback = ::errorFetchingCourseBySlug
                ) { course ->
                    activity?.runOnUiThread {
                        updateData(course)
                    }
                }
        }
    }

    private fun updateData(course: Course) {
        if (overview_title == null) {
            return
        }

        firstLessonSlug = if (course.lessons.isNotEmpty()) course.lessons.first().slug else null
        val parser = dependencies.markdown

        overview_title.text = parser.parse(course.title)
        overview_description.text = parser.parse(course.description)
        overview_duration.text = parser.parse(
            getString(
                R.string.lesson_duration,
                course.duration,
                course.skillLevel
            )
        )

        val inflater = LayoutInflater.from(context)
        course.lessons.forEach { lesson ->
            val index = course.lessons.indexOf(lesson)
            inflater
                .inflate(R.layout.item_lesson, overview_container, false)
                .apply {
                    this.lesson_item_title.text = parser.parse(lesson.title)
                    this.lesson_item_description.text = parser.parse(
                        getString(R.string.lesson_number, index + 1)
                    )
                    setOnClickListener {
                        lessonClicked(lesson.slug)
                    }

                    overview_container.addView(this)
                }
        }
    }

    private fun lessonClicked(lessonSlug: String) {
        val navController = NavHostFragment.findNavController(this)
        val action = CourseOverviewFragmentDirections.openLesson(courseSlug!!, lessonSlug)
        navController.navigate(action)
    }

    private fun onNextButtonClicked() = firstLessonSlug?.let { lessonClicked(it) }

    private fun errorFetchingCourseBySlug(throwable: Throwable) {
        activity?.apply {
            if (throwable.isNetworkError()) {
                showNetworkError()
            } else {
                val navController = NavHostFragment.findNavController(this@CourseOverviewFragment)
                showError(
                    message = getString(R.string.error_fetching_course_from_slug, courseSlug),
                    moreTitle = getString(R.string.error_open_settings_button),
                    error = throwable,
                    moreHandler = {
                        val action = CourseOverviewFragmentDirections.openSettings()
                        navController.navigate(action)
                    },
                    okHandler = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    private fun goHome() {
        val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
        val navController = NavHostFragment.findNavController(this)
        val action = CourseOverviewFragmentDirections.openHome()
        navController.navigate(action, navOptions)
    }
}
