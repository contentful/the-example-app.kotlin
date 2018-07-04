package com.contentful.tea.kotlin.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R

/**
 * This fragment will be the actual starting point of the app: Showing the high lighted courses and
 * offering settings.
 */
class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val navController = NavHostFragment.findNavController(this)

        val list: ViewGroup = root.findViewById(R.id.home_courses)
        list.forEach { index, view ->
            view.setOnClickListener {
                val action = HomeFragmentDirections.openCourseOverview("course " + index)
                navController.navigate(action)
            }
        }

        return root
    }
}

fun ViewGroup.forEach(action: (index: Int, view: View) -> Any?) {
    for (i in 0 until this.childCount) {
        action(i, this.getChildAt(i))
    }
}