package com.myhome.realload.view

import android.Manifest
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kakao.auth.AuthType
import com.kakao.auth.Session
import com.kakao.usermgmt.LoginButton
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.myhome.realload.*
import com.myhome.realload.databinding.ActivityMainBinding
import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.ApplicationUser
import com.myhome.realload.model.Place
import com.myhome.realload.model.User
import com.myhome.realload.utils.BackPressedForFinish
import com.myhome.realload.utils.RetrofitAPI
import com.myhome.realload.utils.SessionCallback
import com.myhome.realload.view.fragment.*
import com.myhome.realload.viewmodel.MainViewModel
import com.myhome.realload.viewmodel.MainViewModelListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, PermissionListener {
    lateinit var fragmentManager:FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    lateinit var backPressedForFinish: BackPressedForFinish
    private val PERMISSION_REQUEST_CODE = 900
    private lateinit var sessionCallback:SessionCallback
    private lateinit var activity:Activity
    private lateinit var loginBt:LoginButton
    private lateinit var profileImage:ImageView
    private lateinit var profileName:TextView
    lateinit var session:Session
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var retrofit:Retrofit
    private lateinit var retrofitAPI:RetrofitAPI

    val loginCallback = object:LoginCallback{
        override fun loginSuccessed(tokenId: Long, nickName: String?, profileUrl: String?) {
            loginBt.visibility = View.GONE
            val editor = sharedPreferences.edit()
            nickName?.let {
                profileName.text = it
                editor.putString("nickName", nickName)
            }
            profileUrl?.let{
                GlideApp.with(applicationContext).load(it).into(profileImage)
                editor.putString("profileUrl", profileUrl)
            }
            editor.putLong("tokenId", tokenId)
            editor.commit()

            val uid = sharedPreferences.getLong("uid", -1)
            if(uid == -1.toLong()){
                val format = SimpleDateFormat("yyyy-MM-dd")
                val user = User()
                user.tokenId = tokenId
                user.name = nickName
                user.tel = getTelNum()
                user.activeDate = format.format(Date())
                val apiResult = retrofitAPI.insertUser(user)
                val retrofitCallback = object:Callback<ApiResponse>{
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        val result = response.body()
                        Log.d("response==", response.toString())
                        Log.d("result==", result.toString())
                        if(result?.resultCode == 200){
                            val uid = ((result.body?.get("uid") ?: "-1") as String).toLong()
                            val editor = sharedPreferences.edit()
                            editor.putLong("uid", uid)
                            editor.commit()
                            setUserInfo()
                        }
                    }
                }
                apiResult.enqueue(retrofitCallback)
            }
        }

        override fun loginFailed() {
            Toast.makeText(applicationContext, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    val fragmentListener = object:FragmentListener{
        override fun moveLocationWithMarker(place: Place) {
            fragmentManager.popBackStack()
            fragmentTransaction = fragmentManager.beginTransaction()
            MapFragment.newInstance(place)?.let { fragmentTransaction.replace(R.id.frame, it) }
            fragmentTransaction.commit()
            bnv.menu.getItem(0).isChecked = true
        }
    }

    val mainViewModelListener = object:MainViewModelListener{
        override fun doSettingActivity() {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
        }

        override fun sendEmail() {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("plain/text")
            val address = arrayOf(getString(R.string.developer_email_address))
            intent.putExtra(Intent.EXTRA_EMAIL, address)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.realload_report) + Build.MODEL + "/" + Build.VERSION.RELEASE)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        getHashKey()
        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()
        backPressedForFinish = BackPressedForFinish(this, getString(R.string.toast_finish_app))
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val mainViewModel = MainViewModel(mainViewModelListener)
        binding.model = mainViewModel
        activity = this
        bnv.setOnNavigationItemSelectedListener(this)

        TedPermission.with(this)
            .setPermissionListener(this)
            .setDeniedMessage(getString(R.string.message_permission_listener))
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS
                )
            .check()
        //권한 획득 후 fragment map으로 초기화

        setRetrofiInit() // retrofit통신을 위해서는 userinfo보다 먼저실행되야함
        setUserInfo()
        setToolbar()
        setDrawerLayoutHeader()
        sessionCallback = SessionCallback(loginCallback)
        session = Session.getCurrentSession()
        session.addCallback(sessionCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        Session.getCurrentSession().removeCallback(sessionCallback)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PERMISSION_REQUEST_CODE){
            TedPermission.with(this)
                .setPermissionListener(this)
                .setDeniedMessage(getString(R.string.message_permission_listener))
                .setPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .check()
        }
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }

        return super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPermissionGranted() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val permissionAccessCoarseLocationApproved = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

            if (permissionAccessCoarseLocationApproved) {
                val backgroundLocationPermissionApproved = ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED

                if (backgroundLocationPermissionApproved) {
                    // App can access location both in the foreground and in the background.
                    // Start your service that doesn't have a foreground service type
                    // defined.
                    startActivityComponent()
                } else {
                    // App can only access location in the foreground. Display a dialog
                    // warning the user that your app must have all-the-time access to
                    // location in order to function properly. Then, request background
                    // location.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        PERMISSION_REQUEST_CODE
                    )
                    AlertDialog.Builder(this)
                        .setTitle("애플리케이션 권한 설정")
                        .setMessage(getString(R.string.permission_background_guide))
                        .setNeutralButton(
                            "설정",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            })
                        .setPositiveButton(
                            "확인",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                TedPermission.with(this)
                                    .setPermissionListener(this)
                                    .setDeniedMessage(getString(R.string.message_permission_listener))
                                    .setPermissions(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                    .check()
                            })
                        .setCancelable(false)
                        .create()
                        .show()
                    Toast.makeText(applicationContext, getString(R.string.toast_permisssion_background_denied), Toast.LENGTH_SHORT).show()
                }
            } else {
                // App doesn't have access to the device's location at all. Make full request
                // for permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
                Toast.makeText(applicationContext, getString(R.string.toast_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
        else{
            startActivityComponent()
        }
    }

    fun startActivityComponent(){
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
            R.id.friend -> {
                fragmentManager.popBackStack()
                fragmentTransaction = fragmentManager.beginTransaction()
//                val mMap = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).map
                FriendFragment.newInstance()?.let { fragmentTransaction.replace(R.id.frame, it) }
                fragmentTransaction.commit()
            }
        }
        return true
    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else{
            backPressedForFinish.onBackPressed()
        }
    }

    private fun setToolbar(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_menu_36dp)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                if(drawer_layout.isDrawerOpen(GravityCompat.START)){
                    drawer_layout.closeDrawer(GravityCompat.START)
                }
                else {
                    drawer_layout.openDrawer(GravityCompat.START)

                }
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun setUserInfo(){
        val uid = sharedPreferences.getLong("uid", -1)
        val tokenId = sharedPreferences.getLong("tokenId", -1)
        val nickName = sharedPreferences.getString("nickName","")
        val format = SimpleDateFormat("yyyy-MM-dd")
        val user = User()
        user.tokenId = tokenId
        user.name = nickName
        user.tel = getTelNum()
        user.activeDate = format.format(Date())
        ApplicationUser.setInstance(user)
        if(uid == -1.toLong()){
            //createUser
        }
        else{
            //activeDate update
            val apiResult = retrofitAPI.updateUser(user.id, user)
            val retrofitCallback = object:Callback<ApiResponse>{
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    t.printStackTrace()
                }

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    val result = response.body()
                    if(result?.resultCode == 200){
                        Log.d("result==", result.toString())
                        val response = ((result.body?.get("result") ?: "false") as String).toBoolean()
                        Log.d("result==", response.toString())
                    }
                }
            }
            apiResult.enqueue(retrofitCallback)
        }


    }

    fun setDrawerLayoutHeader(){
        val headerView = nav_view.getHeaderView(0)
        loginBt = headerView.btn_kakao_login
        profileName = headerView.nav_text
        profileImage = headerView.nav_image
        val logoutBt = headerView.btn_logout

        val profileUrl = sharedPreferences.getString("profileUrl","")
        loginBt.setOnClickListener {
            session.open(AuthType.KAKAO_LOGIN_ALL, activity)
        }
        val applicationUser = ApplicationUser.getInstance()
        if(applicationUser?.tokenId != -1.toLong()){
            loginBt.visibility = View.GONE
        }
        if(!applicationUser?.name.equals("")){
            profileName.text = applicationUser?.name
        }
        if(!profileUrl.equals("")){
            GlideApp.with(applicationContext).load(profileUrl).into(profileImage)
        }

        logoutBt.setOnClickListener{
            UserManagement.getInstance()
                .requestLogout(object: LogoutResponseCallback() {
                    override fun onCompleteLogout() {
                        val editor = sharedPreferences.edit()
                        editor.clear()
                        editor.commit()
                        Toast.makeText(applicationContext, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    }

                })
        }
    }

    fun getTelNum():String?{
        val mTelephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if(mTelephonyManager != null){
            if(ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "전화 권한이 허용되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }else if(mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_UNKNOWN || mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
                Toast.makeText(this, "유심이 없거나, 알 수 없는 유심입니다.", Toast.LENGTH_SHORT).show()
            }else{
                var telNumber = mTelephonyManager.line1Number
                telNumber = telNumber.replace("+82", "0")
                return telNumber
            }
        }
        return null
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
}
