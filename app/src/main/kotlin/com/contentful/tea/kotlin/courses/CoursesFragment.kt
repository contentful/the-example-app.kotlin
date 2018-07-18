package com.contentful.tea.kotlin.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.contentful.Category
import com.contentful.tea.kotlin.contentful.Course
import com.contentful.tea.kotlin.dependencies.Dependencies
import com.contentful.tea.kotlin.dependencies.DependenciesProvider
import com.contentful.tea.kotlin.home.HomeFragmentDirections
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.course_card.view.*
import kotlinx.android.synthetic.main.fragment_courses.*

class CoursesFragment : Fragment() {
    private var categorySlug: String = ""

    private lateinit var dependencies: Dependencies

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.apply {
            categorySlug = CoursesFragmentArgs.fromBundle(arguments).categorySlug
        }

        if (activity !is DependenciesProvider) {
            throw IllegalStateException("Activity must implement Dependency provider.")
        }

        dependencies = (activity as DependenciesProvider).dependencies()

        return inflater.inflate(R.layout.fragment_courses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dependencies
            .contentful
            .fetchAllCategories { categories ->
                activity?.runOnUiThread {
                    updateCategories(categories)
                    if (!isAllCategory()) {
                        fetchFromCategory(categories.first { it.slug == categorySlug })
                    }
                }
            }

        if (isAllCategory()) {
            dependencies
                .contentful
                .fetchAllCourses { courses ->
                    activity?.runOnUiThread {
                        updateCourses(courses)
                    }
                }
        }

        courses_bottom_navigation.setOnNavigationItemSelectedListener {
            if (activity != null) {
                bottomNavigationItemSelected(it)
                true
            } else {
                false
            }
        }
    }

    private fun fetchFromCategory(category: Category) {
        dependencies
            .contentful
            .fetchAllCoursesOfCategoryId(category.id) { courses ->
                activity?.runOnUiThread {
                    updateCourses(courses)
                }
            }
    }

    private fun isAllCategory() = categorySlug.isEmpty() || categorySlug == "all"

    private fun updateCourses(courses: List<Course>) {
        val navController =
            Navigation.findNavController(activity!!, R.id.navigation_host_fragment)

        val parser = dependencies.markdown

        courses.forEach { course ->
            layoutInflater.inflate(R.layout.course_card, courses_container, false).apply {
                this.card_title.text = parser.parse(course.title)
                this.card_description.text = parser.parse(course.shortDescription)

                this.card_background.setBackgroundColor(
                    resources.getColor(
                        R.color.defaultScrim,
                        null
                    )
                )

                val colors = resources.getIntArray(R.array.rainbow)
                val color = colors[courses_container.childCount % colors.size]
                this.card_scrim.setBackgroundColor(color)

                val l: (View) -> Unit = {
                    val action = HomeFragmentDirections.openCourseOverview(course.slug)
                    navController.navigate(action)
                }

                setOnClickListener(l)
                this.card_call_to_action.setOnClickListener(l)
                courses_container?.addView(this)
            }
        }
    }

    private fun updateCategories(categories: List<Category>) {
        courses_top_navigation.addTab(
            courses_top_navigation
                .newTab()
                .setText(R.string.categories_all)
                .setTag("")

        )

        categories.forEach { category ->
            courses_top_navigation.addTab(
                courses_top_navigation
                    .newTab()
                    .setText(category.title)
                    .setTag(category.slug)
            )
        }

        for (i: Int in 0 until courses_top_navigation.tabCount) {
            val tab = courses_top_navigation.getTabAt(i)!!
            if (tab.tag == categorySlug) {
                tab.select()
            }
        }

        if (courses_top_navigation.selectedTabPosition == -1) {
            courses_top_navigation.getTabAt(0)!!.select()
        }

        courses_top_navigation.addOnTabSelectedListener(UsableOnTabListener { tab ->
            topNavigationItemSelected(tab)
        })
    }

    private fun topNavigationItemSelected(tab: TabLayout.Tab) = if (!isDetached) {
        val navController =
            Navigation.findNavController(activity!!, R.id.navigation_host_fragment)
        navController.navigate(CoursesFragmentDirections.openCategory(tab.tag as String))
    } else {
    }

    private fun bottomNavigationItemSelected(item: MenuItem): Boolean {
        val navController =
            Navigation.findNavController(activity!!, R.id.navigation_host_fragment)
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
        if (navController.currentDestination.id != id) {
            navController.navigate(id)
            true
        } else {
            false
        }
}

class UsableOnTabListener(private val selectDelegate: (TabLayout.Tab) -> Unit) :
    TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.apply { selectDelegate(this) }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {}

    override fun onTabUnselected(tab: TabLayout.Tab?) {}
}
