package com.myhome.realload.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.myhome.realload.R
import com.myhome.realload.view.fragment.SettingFragment

class SettingActivity : AppCompatActivity() {
    lateinit var fragmentManager: FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()

        fragmentManager.popBackStack()
        fragmentTransaction = fragmentManager.beginTransaction()
        SettingFragment.newInstance()?.let { fragmentTransaction.replace(R.id.frame, it) }
        fragmentTransaction.commit()

        setToolbar()
    }

    private fun setToolbar(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
