package com.myhome.realload.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.myhome.realload.MapListener

import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentMapBinding
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.model.Place
import com.myhome.realload.model.PlaceLog
import com.myhome.realload.view.adapter.MapInfoWindowAdapter
import com.myhome.realload.viewmodel.MapViewModel
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    companion object {
        private var INSTANCE:MapFragment? = null
        fun newInstance():MapFragment?{
            if(INSTANCE == null){
                INSTANCE = MapFragment()
            }
            INSTANCE?.moveMapByLocation = true
            return INSTANCE

        }

        fun newInstance(place:Place):MapFragment?{
            if(INSTANCE == null){
                INSTANCE = MapFragment()
            }
            INSTANCE?.moveMapByLocation = false
            INSTANCE?.defaultPlace = place

            return INSTANCE
        }
    }
    var currentMarker: Marker? = null
    var mMap: GoogleMap? = null
    lateinit var currentPosition:LatLng
    val markers = ArrayList<Marker>()
    var defaultPlace:Place? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var location: Location
    private var mapFragment:SupportMapFragment? = null
    private var mapInfoWindowAdapter:MapInfoWindowAdapter? = null
    var moveMapByLocation = true
    var moveMapByUser = true
    var mRequestingLocationUpdates = false
    var viewModel:MapViewModel? = null
    var type = 0
    var marker_root_view:View? = null
    val arrayPoints = ArrayList<LatLng>()

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

    val mapListener = object:MapListener{
        override fun setMarker(places: ArrayList<Place>) {
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
        override fun showDatePicker() {
            showDatePickerDialog()
        }

        override fun setPolyLine(logs: List<PlaceLog>) {
            arrayPoints.clear()
            CoroutineScope(Dispatchers.Main).launch {
                mMap?.clear()

                for(marker in markers){
                    marker.remove()
                }
                markers.clear()
                var index = 1
                for(place in logs){
                    addMarkerWithPolyLine(place, index.toString(), place.date, index++)
                }
            }
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
        val view = FragmentMapBinding.inflate(inflater, container, false)
        marker_root_view = LayoutInflater.from(context).inflate(R.layout.marker_layout, null)
        viewModel = MapViewModel(mapListener, AppDatabase.getInstance(context!!), viewLifecycleOwner)
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
        setDefaultLocation()
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
        if(marker?.snippet.equals("추가")){
            showCreateCustomPlaceDialog(marker?.position)
        }
    }

    @SuppressLint("MissingPermission")
    fun setDefaultLocation() {
        //디폴트 위치, Seoul
        moveMapByUser = false
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        var DEFAULT_LOCATION = LatLng(location.latitude, location.longitude)
        if(defaultPlace == null){
            val markerTitle = "위치정보 가져올 수 없음"
            val markerSnippet = "위치 퍼미션과 GPS 활성 여부 확인하세요"
            if (currentMarker != null) currentMarker?.remove()

            val markerOptions =  MarkerOptions()
            markerOptions.position(DEFAULT_LOCATION)
            markerOptions.title(markerTitle)
            markerOptions.snippet(markerSnippet)
            markerOptions.draggable(true)

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        }
        else{
            DEFAULT_LOCATION = LatLng(defaultPlace!!.latitude, defaultPlace!!.longitude)
            if(defaultPlace is NamedPlace){
                addMarker(defaultPlace!!, (defaultPlace as NamedPlace).name, (defaultPlace as NamedPlace).name!!, -1)
            }
            else{
                addMarker(defaultPlace!!, "nonamed", "nonamed", -1)
            }

        }

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15F)
        mMap?.moveCamera(cameraUpdate)
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

    fun addMarkerWithPolyLine(log:PlaceLog, markerTitle: String?, markerSnippet: String?, position:Int){
        val currentLatLng = LatLng(log.latitude, log.longitude)
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
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.RED)
        arrayPoints.add(currentLatLng)
        polylineOptions.addAll(arrayPoints)
        mMap?.addPolyline(polylineOptions)
    }

    fun showCreateCustomPlaceDialog(position:LatLng?){
        if(position == null) return
        val edittext = EditText(context)
        val builder = AlertDialog.Builder(activity)
            .setTitle(getString(R.string.dialog_create_place_title))
            .setMessage(getString(R.string.dialog_create_place_content))
            .setView(edittext)
            .setPositiveButton(getString(R.string.submit), object:DialogInterface.OnClickListener{
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
            .setNegativeButton(getString(R.string.cancel), object:DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            })
        builder.show()
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


}
