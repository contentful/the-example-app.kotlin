package com.contentful.tea.kotlin.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.Dependencies
import com.contentful.tea.kotlin.DependenciesProvider
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.contentful.Course
import kotlinx.android.synthetic.main.fragment_course_overview.*
import kotlinx.android.synthetic.main.item_lesson.view.*

class CourseOverviewFragment : Fragment() {
    private var courseId: String? = null
    private var firstLessonId: String? = null

    private lateinit var dependencies: Dependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseId = CourseOverviewFragmentArgs.fromBundle(arguments).courseId
        }

        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement Dependency provider.")
        }

        dependencies = (activity as DependenciesProvider).dependencies()
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

        courseId?.let {
            dependencies
                .contentful
                .fetchCourse(courseId!!) { course ->
                    activity?.runOnUiThread {
                        updateData(course)
                    }
                }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun updateData(course: Course) {
        firstLessonId = if (course.lessons.isNotEmpty()) course.lessons.first().id else null

        overview_title.text = course.title
        overview_description.text = course.description
        overview_duration.text = getString(
            R.string.lesson_duration,
            course.duration,
            course.skillLevel
        )

        val inflater = LayoutInflater.from(context)
        course.lessons.forEach { lesson ->
            val index = course.lessons.indexOf(lesson)
            inflater
                .inflate(R.layout.item_lesson, overview_container, false)
                .apply {
                    this.lesson_item_title.text = lesson.title
                    this.lesson_item_description.text =
                        getString(R.string.lesson_number, index + 1)
                    setOnClickListener {
                        lessonClicked(lesson.id)
                    }

                    overview_container.addView(this)
                }
        }
    }

    private fun lessonClicked(lessonId: String) {
        val navController = NavHostFragment.findNavController(this)
        val action = CourseOverviewFragmentDirections.openLesson(courseId, lessonId)
        navController.navigate(action)
    }

    private fun onNextButtonClicked() = firstLessonId?.let { lessonClicked(it) }
}
