package com.myhome.realload.view

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.myhome.realload.*
import com.myhome.realload.model.Place
import com.myhome.realload.view.fragment.MapFragment
import com.myhome.realload.view.fragment.PlaceFragment
import com.myhome.realload.view.fragment.SettingFragment
import com.myhome.realload.view.fragment.VisitedFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, PermissionListener {
    lateinit var fragmentManager:FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    private val PERMISSION_REQUEST_CODE = 900
    val fragmentListener = object:FragmentListener{
        override fun moveLocationWithMarker(place: Place) {
            fragmentManager.popBackStack()
            fragmentTransaction = fragmentManager.beginTransaction()
            MapFragment.newInstance(place)?.let { fragmentTransaction.replace(R.id.frame, it) }
            fragmentTransaction.commit()
            bnv.menu.getItem(0).isChecked = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()
        bnv.setOnNavigationItemSelectedListener(this)

        TedPermission.with(this)
            .setPermissionListener(this)
            .setDeniedMessage(getString(R.string.message_permission_listener))
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
        //권한 획득 후 fragment map으로 초기화
    }

    fun startJobService(){
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(this, GeofenceService::class.java)
        val builder = JobInfo.Builder(1, componentName)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//        builder.setRequiresBatteryNotLow(true)
        builder.setRequiresCharging(false)
        val jobInfo = builder.build()
        jobScheduler.schedule(jobInfo)
    }
    fun startGeofenceJobService(){
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(this, CustomGeofenceService::class.java)
        val builder = JobInfo.Builder(2, componentName)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//        builder.setRequiresBatteryNotLow(true)
        builder.setRequiresCharging(false)
        val jobInfo = builder.build()
        jobScheduler.schedule(jobInfo)
    }
    override fun onPermissionGranted() {
        fragmentManager.popBackStack()
        fragmentTransaction = fragmentManager.beginTransaction()
        MapFragment.newInstance()?.let { fragmentTransaction.replace(R.id.frame, it) }
        fragmentTransaction.commit()
        Toast.makeText(applicationContext, getString(R.string.toast_permission_granted), Toast.LENGTH_SHORT).show()
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val guide = sharedPreferences.getBoolean("guide", false)
        if(!guide){
            val intent = Intent(this, GuideActivity::class.java)
            startActivity(intent)
            val editor = sharedPreferences.edit()
            editor.putBoolean("guide", true)
            editor.commit()
        }

        startJobService()
//        startGeofenceJobService()
//        startBackgroudLocationJobService()
//        val geofenceService = com.myhome.realload.utils.GeofenceService(applicationContext)
//        geofenceService.startService()
    }

    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
        if(deniedPermissions != null){
            for(str in deniedPermissions!!){
            }
        }

        Toast.makeText(applicationContext, getString(R.string.toast_permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.map -> {
                fragmentManager.popBackStack()
                fragmentTransaction = fragmentManager.beginTransaction()
//                val mMap = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).map
                MapFragment.newInstance()?.let { fragmentTransaction.replace(R.id.frame, it) }
                fragmentTransaction.commit()
            }
            R.id.visited -> {
                fragmentManager.popBackStack()
                fragmentTransaction = fragmentManager.beginTransaction()
//                val mMap = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).map
                VisitedFragment.newInstance(fragmentListener).let { fragmentTransaction.replace(R.id.frame, it) }
                fragmentTransaction.commit()
            }
            R.id.place -> {
                fragmentManager.popBackStack()
                fragmentTransaction = fragmentManager.beginTransaction()
//                val mMap = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).map
                PlaceFragment.newInstance()?.let { fragmentTransaction.replace(R.id.frame, it) }
                fragmentTransaction.commit()
            }
            R.id.setting -> {
                fragmentManager.popBackStack()
                fragmentTransaction = fragmentManager.beginTransaction()
//                val mMap = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).map
                SettingFragment.newInstance()?.let { fragmentTransaction.replace(R.id.frame, it) }
                fragmentTransaction.commit()
            }
        }
        return true
    }
}
