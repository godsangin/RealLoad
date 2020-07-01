package com.myhome.realload.view

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.myhome.realload.R
import com.myhome.realload.model.Friend
import com.myhome.realload.view.fragment.FriendMapFragment

class FriendLocationActivity : AppCompatActivity() {
    lateinit var fragmentManager: FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var friend:Friend
    private lateinit var mInterstitialAd:InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_location)
        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val intent = getIntent()
        friend = intent.getParcelableExtra("friend")

        fragmentManager.popBackStack()
        fragmentTransaction = fragmentManager.beginTransaction()
        FriendMapFragment.newInstance(friend)?.let { fragmentTransaction.replace(R.id.frame, it) }
        fragmentTransaction.commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(friend.nickName + "님의 방문정보")

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.show()
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
