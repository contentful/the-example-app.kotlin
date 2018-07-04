package com.contentful.tea.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class CourseOverviewFragment : Fragment() {
    private var courseId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseId = CourseOverviewFragmentArgs.fromBundle(arguments).courseId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Toast.makeText(context, courseId, Toast.LENGTH_LONG).show()
        return inflater.inflate(R.layout.fragment_course_overview, container, false)
    }
}
