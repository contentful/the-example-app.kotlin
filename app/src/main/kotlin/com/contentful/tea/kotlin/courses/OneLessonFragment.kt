package com.contentful.tea.kotlin.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R
import kotlinx.android.synthetic.main.fragment_lesson.*

class OneLessonFragment : Fragment() {
    private var courseId: String = ""
    private var lessonId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.apply {
            courseId = OneLessonFragmentArgs.fromBundle(arguments).courseId
            lessonId = OneLessonFragmentArgs.fromBundle(arguments).lessonId
        }

        return inflater.inflate(R.layout.fragment_lesson, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextLessonId = nextLessonId()
        if (nextLessonId.isEmpty()) {
            lesson_next_button.hide()
        } else {
            lesson_next_button.setOnClickListener {
                val navController = NavHostFragment.findNavController(this)
                val action = OneLessonFragmentDirections.openLesson(courseId, nextLessonId)
                navController.navigate(action)
            }
        }
    }

    private fun nextLessonId() = "NEXT LESSON" // TODO: GENERATE NEXT LESSON ID
}
