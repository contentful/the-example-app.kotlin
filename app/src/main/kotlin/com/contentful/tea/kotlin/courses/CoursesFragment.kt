package com.contentful.tea.kotlin.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.extensions.forEach
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_courses.*

class CoursesFragment : Fragment() {
    private var categoryId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val navController = NavHostFragment.findNavController(this)
        val root = inflater.inflate(R.layout.fragment_courses, container, false)
        arguments?.apply {
            categoryId = CoursesFragmentArgs.fromBundle(arguments).categoryId
        }

        var navigation = root.findViewById<TabLayout>(R.id.top_navigation)
        navigation.addOnTabSelectedListener(UsableOnTabListener { topNavigationItemSelected(it) })

        // TODO: Make dynamic
        val list: ViewGroup = root.findViewById(R.id.courses_list)
        list.forEach { index, view ->
            view.setOnClickListener {
                val action = CoursesFragmentDirections.openCourseOverview("course " + index)
                navController.navigate(action)
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Make dynamic
        top_navigation.addTab(top_navigation.newTab().setText(categoryId))
    }

    private fun topNavigationItemSelected(tab: TabLayout.Tab) = if (!isDetached) {
        val navController = Navigation.findNavController(activity!!, R.id.navigation_host_fragment)
        navController.navigate(CoursesFragmentDirections.openCategory(tab.tag as String))
    } else {
    }
}

class UsableOnTabListener(private val selectDelegate: (TabLayout.Tab) -> Unit) :
    TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab ?: selectDelegate(tab!!)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {}
}
