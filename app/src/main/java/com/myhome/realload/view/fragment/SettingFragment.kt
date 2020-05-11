package com.myhome.realload.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.myhome.realload.GlideApp

import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentSettingBinding
import com.myhome.realload.utils.LogSemaphore
import com.myhome.realload.viewmodel.SettingViewModel
import com.myhome.realload.viewmodel.SettingViewModelListener
import kotlinx.android.synthetic.main.fragment_setting.view.*
import java.io.BufferedReader
import java.io.File
import java.io.IOException

class SettingFragment : Fragment() {
    lateinit var viewModel:SettingViewModel
    lateinit var sharedPreferences:SharedPreferences
    val viewModelListener = object:SettingViewModelListener{
        override fun setPushNoti(agree: Boolean) {
            val editor = sharedPreferences?.edit()
            editor?.putBoolean("push", agree)
            editor?.commit()
        }

        override fun showStayConditionDialog() {
            val dialog = AlertDialog.Builder(activity)
                .setTitle(getString(R.string.dialog_stay_time_title))
                .setItems(R.array.array_stay_time, object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val staytime = arrayOf(600000, 1200000, 1800000, 3600000, 7200000)
                        val editor = sharedPreferences?.edit()
                        editor?.putLong("stayCondition", staytime[which].toLong())
                        editor?.commit()
                        viewModel.stayCondition.set(staytime[which].toLong())
                        viewModel.stayConditionText.set(findStayCondition(staytime[which].toLong()))
                    }
                }).create()
            dialog.show()
        }

        override fun showDistanceConditionDialog() {
            val dialog = AlertDialog.Builder(activity)
                .setTitle(getString(R.string.dialog_distance_title))
                .setItems(R.array.array_distance, object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val distances = arrayOf(10F, 20F, 30F, 40F, 50F)
                        val editor = sharedPreferences?.edit()
                        editor?.putFloat("distanceCondition", distances[which])
                        editor?.commit()
                        viewModel.distanceCondition.set(distances[which])
                        viewModel.distanceConditionText.set(findDistanceCondition(distances[which]))
                    }
                }).create()
            dialog.show()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = context?.getSharedPreferences("setting", Context.MODE_PRIVATE)!!
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = DataBindingUtil.inflate<FragmentSettingBinding>(inflater, R.layout.fragment_setting, container, false)
        viewModelInit()
        view.model = viewModel
//        val text = readTxt()
//        view.root.log.setText(text ?: "null")
        return view.root
    }

    fun readTxt():String?{
        val file = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            File(context?.dataDir?.path + "/realload")
        }else{
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/realload")
        }
        if(file?.exists() == false){
            file.mkdir()
        }
        val myTxt = File(file?.path + "/log.txt")
        val allText = myTxt.bufferedReader().use(BufferedReader::readText)
        return allText
    }
    fun viewModelInit(){
        val pushCondition = sharedPreferences?.getBoolean("push", true)
        val stayCondition = sharedPreferences?.getLong("stayCondition", 600000)
        val distanceCondition = sharedPreferences?.getFloat("distanceCondition", 10F)
        viewModel = SettingViewModel(viewModelListener, pushCondition, stayCondition, distanceCondition)
        viewModel.stayConditionText.set(findStayCondition(stayCondition))
        viewModel.distanceConditionText.set(findDistanceCondition(distanceCondition))
    }

    fun findStayCondition(stayCondition:Long?):String{
        when(stayCondition){
            600000.toLong() -> {
                return "10분"
            }
            1200000.toLong() -> {
                return "20분"
            }
            1800000.toLong() -> {
                return "30분"
            }
            3600000.toLong() -> {
                return "1시간"
            }
            7200000.toLong() -> {
                return "2시간"
            }
        }
        return ""
    }

    fun findDistanceCondition(distanceCondition:Float?):String{
        when(distanceCondition){
            10F -> {
                return "10m"
            }
            20F -> {
                return "20m"
            }
            30F -> {
                return "30m"
            }
            40F -> {
                return "40m"
            }
            50F -> {
                return "50m"
            }
        }
        return ""
    }

    companion object {
        var INSTANCE:SettingFragment? = null
        @JvmStatic
        fun newInstance():SettingFragment{
            if(INSTANCE == null){
                INSTANCE = SettingFragment()
            }
            return INSTANCE!!
        }

    }
}
