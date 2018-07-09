package com.contentful.tea.kotlin.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.contentful.Contentful
import com.contentful.tea.kotlin.contentful.Layout
import com.contentful.tea.kotlin.contentful.LayoutModule
import com.contentful.tea.kotlin.extensions.setImageResourceFromUrl
import kotlinx.android.synthetic.main.course_card.view.*
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * This fragment will be the actual starting point of the app: Showing modules and offering
 * settings.
 */
class HomeFragment : Fragment() {
    private var contentful: Contentful = Contentful()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentful.fetchHomeLayout { layout: Layout ->
            layout.contentModules.forEach { module ->
                activity?.runOnUiThread {
                    layoutInflater.inflate(R.layout.course_card, home_courses, false).apply {
                        updateModuleView(this, module)
                        home_courses.addView(this)
                    }
                }
            }
        }
    }

    private fun updateModuleView(view: View, module: LayoutModule) {
        when (module) {
            is LayoutModule.HightlightedCourse -> {
                view.card_title.text = module.course.title
                view.card_description.text = module.course.shortDescription
                view.card_background.setImageResourceFromUrl(module.course.image)

                val l: (View) -> Unit = {
                    val navController = NavHostFragment.findNavController(this@HomeFragment)
                    val action = HomeFragmentDirections.openCourseOverview(module.course.id)
                    navController.navigate(action)
                }

                view.setOnClickListener(l)
                view.card_call_to_action.setOnClickListener(l)
            }
            is LayoutModule.HeroImage -> {
                view.card_title.text = module.title
                view.card_background.setImageResourceFromUrl(module.backgroundImage)
                view.card_scrim.setBackgroundResource(android.R.color.transparent)
            }
            is LayoutModule.Copy -> {
                view.card_title.text = module.headline
                view.card_description.text = module.copy
                view.card_background.setBackgroundResource(android.R.color.transparent)
                view.card_scrim.setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}