package com.myhome.realload.view.fragment

import android.app.Activity
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.myhome.realload.CustomGeofenceBroadcastReceiver
import com.myhome.realload.CustomGeofenceService
import com.myhome.realload.GeofenceService

import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentPlaceBinding
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.Image
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.utils.ImageDBController
import com.myhome.realload.viewmodel.PlaceViewModel
import com.myhome.realload.viewmodel.PlaceViewModelListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PlaceFragment : Fragment() {

    val REQUESTCODE_ALBUM = 1000
    lateinit var viewModel:PlaceViewModel
    var placeIndex:NamedPlace? = null
    var imageDBController:ImageDBController? = null
    val placeListener = object:PlaceViewModelListener{
        override fun addGeofence(place: NamedPlace) {
            startJobService()
        }

        override fun removeGeofence(place:NamedPlace) {
            startJobService()
        }

        override fun goToAlbum(place: NamedPlace?){
            placeIndex = place
            //place null처리 해야댐
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
            startActivityForResult(intent, REQUESTCODE_ALBUM)
        }

        override fun getDatabase(): AppDatabase? {
            return AppDatabase.getInstance(context!!)
        }

        override fun getOwner(): LifecycleOwner {
            return viewLifecycleOwner
        }

        override fun updateImages(images: ArrayList<Image>) {
            CoroutineScope(Dispatchers.IO).launch {
                for(image in images){
                    if(image.imageRes.equals("")){
                        continue
                    }
                    getDatabase()?.ImageDao()?.insert(image)
                }

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        imageDBController = ImageDBController(context!!)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentPlaceBinding>(inflater, R.layout.fragment_place, container, false)
        viewModel = PlaceViewModel(AppDatabase.getInstance(context!!), placeListener)
        binding.model = viewModel
        viewModel.getBaseData(viewLifecycleOwner, AppDatabase.getInstance(context!!))
        return binding.root
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        if(requestCode == REQUESTCODE_ALBUM){
            val photoUri = data?.data
            viewModel.addItem(placeIndex, copyFile(photoUri))
        }
    }

    fun startJobService(){
        val jobScheduler = context?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context!!, CustomGeofenceService::class.java)
        val builder = JobInfo.Builder(2, componentName)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//        builder.setRequiresBatteryNotLow(true)
        builder.setRequiresCharging(false)
        val jobInfo = builder.build()
        jobScheduler.schedule(jobInfo)
    }

    fun copyFile(photoUri:Uri?):String{
        var cursor: Cursor? = null
        try{
            if(photoUri != null) {
                cursor = context?.getContentResolver()?.query(photoUri, null, null, null, null)
                if(cursor  == null){
                    val myFile = File(photoUri.path)
                    val newFile = imageDBController?.copyFile(myFile)
                    return newFile?.path!!
                }
                else{
                    cursor.moveToFirst()
                    val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if(cursor.getString(idx) == null){
                        return ""
                    }
                    val newFile = File(cursor.getString(idx))
                    return newFile.absolutePath
                }
            }
        }finally {
            if (cursor != null) {
                cursor.close()
            }
        }
        return ""
    }

    companion object {
        var INSTANCE:PlaceFragment? = null
        @JvmStatic
        fun newInstance():PlaceFragment?{
            if(INSTANCE == null){
                INSTANCE = PlaceFragment()
            }
            return INSTANCE
        }
    }
}
