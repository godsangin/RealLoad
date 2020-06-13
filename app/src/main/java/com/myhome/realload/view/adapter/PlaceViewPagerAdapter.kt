package com.myhome.realload.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.myhome.realload.GlideApp
import com.myhome.realload.PlaceListener
import com.myhome.realload.R
import com.myhome.realload.model.Image
import com.myhome.realload.model.NamedPlace
import com.myhome.siviewpager.SIPagerAdapter
import kotlinx.android.synthetic.main.place_image_item.view.*

class PlaceViewPagerAdapter(context:Context, listener:PlaceListener?, place:NamedPlace?, modifying:Boolean?) :SIPagerAdapter(){
    val context = context
    var listener:PlaceListener? = listener
    var modifying = modifying
    var place:NamedPlace? = place


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.place_image_item, container, false)
        val addView = LayoutInflater.from(context).inflate(R.layout.image_pager_item_add, container, false)

        if(position == items?.size!! - 1 && modifying == true){
            addView.setOnClickListener {
                if(listener == null){
                }
                assert(listener != null)
                listener?.goToAlbum(place)
            }
            container.addView(addView)
            addView.setTag(position)
            return addView
        }else{
            if(modifying == true){
                view.cancel_action.visibility = View.VISIBLE
                view.cancel_action.setOnClickListener {
                    listener?.removeItem(place, position)
                }
            }
            else{
                view.cancel_action.visibility = View.GONE
            }
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            val requestOption : RequestOptions = RequestOptions()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform()
            //Log.d("log==pageradapter", items.get(position).toString())
            GlideApp.with(context).load((items.get(position) as Image).imageRes).apply(requestOption).into(view.image)
            container.addView(view)
            view.setTag(position)
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}