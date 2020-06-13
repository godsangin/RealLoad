package com.myhome.realload.viewmodel

import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import com.myhome.realload.R

class MainViewModel(listener:MainViewModelListener) {
    val listener = listener
    val navigationListener = object:NavigationView.OnNavigationItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when(item.itemId){
                R.id.setting -> {
                    listener.doSettingActivity()
                }
                R.id.send_email -> {
                    listener.sendEmail()
                }
            }
            return true
        }
    }
}