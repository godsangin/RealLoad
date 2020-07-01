package com.myhome.realload.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.myhome.realload.R
import com.myhome.realload.databinding.ActivityGuideBinding
import com.myhome.realload.viewmodel.GuideViewModel
import kotlinx.android.synthetic.main.activity_guide.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.bnv

class GuideActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    lateinit var fragmentManager: FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    lateinit var viewModel:GuideViewModel
    val listener = object:GuideListener{
        override fun finishGuide() {
            finish()
        }

        override fun nextGuideAnim() {
            when(viewModel.guideIndex.get() ?: 0){
                0 -> {
                    first_image.clearAnimation()
                    second_image.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_down))
                }
                1 -> {
                    second_image.clearAnimation()
                    third_image.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_down))
                }
                2 ->{
                    third_image.clearAnimation()
                    fourth_image.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_down))
                }
                else -> {
                    finish()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        val binding = DataBindingUtil.setContentView<ActivityGuideBinding>(this, R.layout.activity_guide)
        viewModel = GuideViewModel(listener, arrayListOf(getString(R.string.guide_text_map), getString(R.string.guide_text_visited), getString(R.string.guide_text_place), getString(R.string.guide_text_setting)))
        binding.model = viewModel

        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()
        bnv.setOnNavigationItemSelectedListener(this)
        first_image.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_down))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.map -> {
                viewModel.getNextGuide()
            }
            R.id.visited -> {
                viewModel.getNextGuide()
            }
            R.id.place -> {
                viewModel.getNextGuide()
            }
            R.id.setting -> {
                viewModel.getNextGuide()
            }
        }
        return true
    }
}
