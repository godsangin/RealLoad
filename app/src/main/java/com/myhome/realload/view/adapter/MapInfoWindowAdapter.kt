package com.myhome.realload.view.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.myhome.realload.GlideApp
import com.myhome.realload.R
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.model.Place
import kotlinx.android.synthetic.main.marker_window_info_item.view.*
import kotlinx.coroutines.*

class MapInfoWindowAdapter(context:Context, inflater: LayoutInflater) : GoogleMap.InfoWindowAdapter, RequestListener<Drawable>{
    val context = context
    val inflater = inflater
    var CurrentMarker:Marker? = null
    var loaded = false
    var places:ArrayList<Place>? = null
    val loadedListener = object : ImageLoadListener{
        override fun loadFinished() {
            Log.d("log==", "excuted" + CurrentMarker.toString())
            Handler().post {
//                CurrentMarker?.hideInfoWindow()
                CurrentMarker?.showInfoWindow()
                loaded = true
            }
        }
    }
    val requestListener = this
    override fun getInfoContents(marker: Marker?): View? {

//        GlideApp.with(context).load(R.drawable.ic_star_gray_24dp)
//            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(view.image)
        return null
    }




    override fun getInfoWindow(marker: Marker?): View? {// 처음 이후에 변경된 것은 맵에 반영안됨.(데이터바인딩 사용불가)
        val view = inflater.inflate(R.layout.marker_window_info_item, null)
        view.layoutParams = LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT)
        marker?.let {
            view.title.text = it.title
            view.snippet.text = it.snippet
            CurrentMarker = it
        }
        view.image.visibility = View.GONE
        if(places != null){
            for(place in places!!){
                if(place is NamedPlace && place.latitude == marker?.position?.latitude && place.longitude == marker?.position?.longitude){
                    val index = (Math.random() * place.images.size).toInt()
                    if(place.images.size != 0){
                        view.image.visibility = View.VISIBLE
                        GlideApp.with(context)
                            .load(place.images.get(index).imageRes)
                            .error(R.drawable.ic_brightness_gray_12dp)
                            .listener(requestListener)
                            .into(view.image)
                    }

                }
            }
        }
        return view
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        Log.d("log==", "" + e?.causes)
        Log.d("log==", "" + e?.message)
        return true
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        if (loaded) {
            loaded = false
            return false
        }
        Handler().post {
            Log.d("log==","showinfowindow, " + CurrentMarker)
            CurrentMarker?.showInfoWindow()

        }
        loaded = true
        return false
    }
}