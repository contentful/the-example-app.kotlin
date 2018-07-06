package com.contentful.tea.kotlin.courses

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R
import kotlinx.android.synthetic.main.fragment_course_overview.*

class CourseOverviewFragment : Fragment() {
    private var courseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseId = CourseOverviewFragmentArgs.fromBundle(arguments).courseId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_overview, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Toast.makeText(context, courseId, Toast.LENGTH_LONG).show()
        overview_next.setOnClickListener { onNextButtonClicked() }

        // TODO: Generate lessons from contentful
        for (i in 1 until 10) {
            overview_container.addView(TextView(activity).apply {
                text = "Course $courseId.$i"
                setCompoundDrawables(
                    resources.getDrawable(R.mipmap.ic_launcher_foreground, resources.newTheme()),
                    null,
                    null,
                    null
                )
                setOnClickListener { lessonClicked(i.toString()) }
            })
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun lessonClicked(id: String) {
        val navController = NavHostFragment.findNavController(this)
        val action = CourseOverviewFragmentDirections.openLesson(courseId, id)
        navController.navigate(action)
    }

    private fun onNextButtonClicked() = lessonClicked(nextLessonId())

    private fun nextLessonId(): String = "TODO" // TODO: find first lesson id
}
