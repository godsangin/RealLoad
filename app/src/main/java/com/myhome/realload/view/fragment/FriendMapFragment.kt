package com.myhome.realload.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentFriendMapBinding
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.Friend
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.model.Place
import com.myhome.realload.utils.RetrofitAPI
import com.myhome.realload.view.adapter.MapInfoWindowAdapter
import com.myhome.realload.viewmodel.fragment.FriendMapListener
import com.myhome.realload.viewmodel.fragment.FriendMapViewModel
import kotlinx.android.synthetic.main.fragment_friend_map.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class FriendMapFragment(friend:Friend) :Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    companion object {
        private var INSTANCE:FriendMapFragment? = null
        fun newInstance(friend:Friend):FriendMapFragment?{
            if(INSTANCE == null){
                INSTANCE = FriendMapFragment(friend)
            }
            INSTANCE?.moveMapByLocation = true
            return INSTANCE

        }
    }
    var currentMarker: Marker? = null
    var mMap: GoogleMap? = null
    lateinit var currentPosition:LatLng
    val markers = ArrayList<Marker>()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var location: Location
    private lateinit var sharedPreferences:SharedPreferences
    private var mapFragment:SupportMapFragment? = null
    private var mapInfoWindowAdapter:MapInfoWindowAdapter? = null

    var moveMapByLocation = true
    var moveMapByUser = true
    var mRequestingLocationUpdates = false
    var viewModel: FriendMapViewModel? = null
    var type = 0
    var marker_root_view:View? = null
    val arrayPoints = ArrayList<LatLng>()
    val friend = friend
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitAPI: RetrofitAPI

    private val UPDATE_INTERVAL_MS = 1000.toLong()
    private val FASTEST_UPDATE_INTERVAL_MS = 500.toLong()

    val locationCallback:LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList =
                locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                //location = locationList.get(0);
                currentPosition = LatLng(location.getLatitude(), location.getLongitude())
                val markerSnippet =
                    "위도:" + location.getLatitude().toString() + " 경도:" + location.getLongitude().toString()
                //db저장작업
                if(moveMapByLocation){
                    setCurrentLocation(location)
                }
            }
        }
    }

    val mapListener = object:FriendMapListener{
        override fun callPlaces(start: String, end: String) {
            val uid = sharedPreferences.getLong("uid", -1)
            callPlaces(start, end, friend.uid)
        }
        override fun showDatePicker() {
            showDatePickerDialog()
        }
    }
    val datepickerCallback = object: DatePickerDialog.OnDateSetListener{
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            if (type == 0) {
                val monthText = if(month + 1 < 10) "0" + (month+1).toString() else (month+1).toString()
                val dayText = if(dayOfMonth < 10) "0" + (dayOfMonth).toString() else (dayOfMonth).toString()
                val text = year.toString() + "-" + monthText + "-" + dayText
                viewModel?.customDateFrom?.set(text)
                type++
                val cal = GregorianCalendar()
                datepickerDialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    DatePickerDialog(
                        context!!,
                        this,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                } else {
                    Toast.makeText(
                        context!!,
                        getString(R.string.toast_not_enough_required_api_datepicker),
                        Toast.LENGTH_SHORT
                    ).show()
                    null
                }
                datepickerDialog?.setTitle(getString(R.string.dialog_datepicker_to))
                datepickerDialog?.show()
            } else {
                val monthText = if(month + 1 < 10) "0" + (month+1).toString() else (month+1).toString()
                val dayText = if(dayOfMonth < 10) "0" + (dayOfMonth).toString() else (dayOfMonth).toString()
                val text = year.toString() + "-" + monthText + "-" + dayText
                viewModel?.customDateTo?.set(text)
                viewModel?.getCustomItems()
                type = 0
            }
        }
    }
    var datepickerDialog:DatePickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val view = FragmentFriendMapBinding.inflate(inflater, container, false)
        marker_root_view = LayoutInflater.from(context).inflate(R.layout.marker_layout, null)
        viewModel = FriendMapViewModel(mapListener)
        view.model = viewModel

        MapsInitializer.initialize(context)
        //배너광고
        MobileAds.initialize(context) {}
        val adRequest = AdRequest.Builder().build()
        view.root.adView.loadAd(adRequest)

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
        }
        mapFragment?.getMapAsync(this)
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS)
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        childFragmentManager.beginTransaction().replace(R.id.map, mapFragment!!).commit()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        sharedPreferences = context!!.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        setRetrofiInit()
        return view.root
    }


    override fun onStart() {
//        placeLog()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onPause() {
        super.onPause()
        mFusedLocationClient.removeLocationUpdates(locationCallback)
        mRequestingLocationUpdates = false
    }
    override fun onStop() {
        mapFragment?.onStop()
        super.onStop()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onMapReady(map: GoogleMap?){
//        if(map != null){
//            mMap = map
//        }
        mMap = map
        startLocationUpdates()
        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(15F))
        mMap?.setOnMapClickListener {

        }

        mMap?.setOnMyLocationButtonClickListener{
            moveMapByLocation = true
            mMap?.setOnCameraMoveStartedListener {
                if (moveMapByUser == true && mRequestingLocationUpdates){
                    moveMapByLocation = false
                    mMap?.setOnCameraMoveStartedListener(null)
                }

                moveMapByUser = true
            }
            return@setOnMyLocationButtonClickListener true
        }
        mMap?.setOnCameraMoveStartedListener {
            if (moveMapByUser == true && mRequestingLocationUpdates){
                moveMapByLocation = false
                mMap?.setOnCameraMoveStartedListener(null)
            }

            moveMapByUser = true
        }
        mMap?.setOnMarkerClickListener(this)
        mMap?.setOnInfoWindowClickListener(this)
        mapInfoWindowAdapter = MapInfoWindowAdapter(context!!, layoutInflater)
        mMap?.setInfoWindowAdapter(mapInfoWindowAdapter)
    }



    override fun onMarkerClick(marker: Marker?): Boolean {
        mapInfoWindowAdapter?.loaded = false
        return false
    }

    override fun onInfoWindowClick(marker: Marker?) {
        if(marker?.title.equals("정의되지 않은 장소입니다.")){
            showCreateCustomPlaceDialog(marker?.position)
        }
    }

    private fun startLocationUpdates() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
            hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
        mRequestingLocationUpdates = true
        mMap?.isMyLocationEnabled = true
//        if (checkPermission()) mMap.isMyLocationEnabled = true
    }

    fun setCurrentLocation(location:Location) {
        moveMapByUser = false
//        if (currentMarker != null) currentMarker?.remove()
        val currentLatLng = LatLng(location.getLatitude(), location.getLongitude())

        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        mMap?.moveCamera(cameraUpdate)
    }

    fun addMarker(place:Place, markerTitle: String?, markerSnippet: String?, position:Int){
        val currentLatLng = LatLng(place.latitude, place.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(currentLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        if(position > 0){
            marker_root_view?.findViewById<TextView>(R.id.index_tv)?.setText(position.toString())
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, marker_root_view)));
        }
        else{
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        }

        mMap?.addMarker(markerOptions)?.let {
            markers.add(it)
        }
    }

    fun showDatePickerDialog(){

        val cal = GregorianCalendar()
        datepickerDialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DatePickerDialog(context!!, datepickerCallback, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        } else {
            Toast.makeText(context!!, getString(R.string.toast_not_enough_required_api_datepicker), Toast.LENGTH_SHORT).show()
            null
        }
        val titleView = TextView(activity)
        titleView.setText(getString(R.string.dialog_datepicker_from))
        datepickerDialog?.setCustomTitle(titleView)
        datepickerDialog?.show()
    }
    // View를 Bitmap으로 변환
    private fun createDrawableFromView(context:Context?, view:View?):Bitmap? {
        if(view == null){
            return null
        }
        val displayMetrics = DisplayMetrics()
        (context as Activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        view.setLayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    fun showCreateCustomPlaceDialog(position:LatLng?){
        if(position == null) return
        val edittext = EditText(context)
        val builder = AlertDialog.Builder(activity)
            .setTitle(getString(R.string.dialog_create_place_title))
            .setMessage(getString(R.string.dialog_create_place_content))
            .setView(edittext)
            .setPositiveButton(getString(R.string.submit), object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if(edittext.text.toString().equals("")){
                        Toast.makeText(context, getString(R.string.toast_place_not_inputted_text), Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val myPlace = CustomPlace()
                        myPlace.latitude = position.latitude
                        myPlace.longitude = position.longitude
                        myPlace.name = edittext.text.toString()
                        CoroutineScope(Dispatchers.IO).launch {
                            AppDatabase.getInstance(context!!)?.CustomPlaceDao()?.insert(myPlace)
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, getString(R.string.toast_place_saved), Toast.LENGTH_SHORT).show()
                                viewModel?.refreshData()
                            }
                        }
                    }
                }
            })
            .setNegativeButton(getString(R.string.cancel), object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            })
        builder.show()
    }

    fun setPlaces(places:ArrayList<Place>){
        CoroutineScope(Dispatchers.Main).launch {
            mMap?.clear()
            for(marker in markers){
                marker.remove()
            }
            markers.clear()
            mapInfoWindowAdapter?.places = places
            var index = 1
            for(place in places){
                if(place is NamedPlace){
                    addMarker(place, place.name, place.startDate + " ~\n" + place.endDate, index++)
                }
                else{
                    addMarker(place, "정의되지 않은 장소입니다.", place.startDate + " ~\n" + place.endDate + "\n" + "추가하기", index++)
                }
            }
        }
    }

    fun setRetrofiInit(){
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS).build()
        retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.apiUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitAPI = retrofit.create(RetrofitAPI::class.java)
    }

    fun callPlaces(start:String, end:String, uid:Long){
        val apiResult = retrofitAPI.getPlaces(uid, start, end)
        val retrofitCallback = object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(context, context?.getString(R.string.toast_network_enabled), Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                val result = response.body()
                if((result?.get("responseCode")?.asInt) == 200) {
                    val bodyArray = result?.get("body")?.asJsonArray ?: JsonArray()
                    val places = ArrayList<Place>()
                    for(placeMap in bodyArray){
                        val place = Place()
                        place.longitude = placeMap.asJsonObject.get("longitude").asDouble
                        place.latitude = placeMap.asJsonObject.get("latitude").asDouble
                        place.startDate = placeMap.asJsonObject.get("startDate").asString
                        place.endDate = placeMap.asJsonObject.get("endDate").asString
                        places.add(place)
                    }
                    setPlaces(places)
                }
                else{
                    Toast.makeText(context, context?.getString(R.string.toast_network_enabled), Toast.LENGTH_SHORT).show()
                }
            }
        }
        apiResult.enqueue(retrofitCallback)
    }
}
