package com.myhome.realload.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myhome.realload.FragmentListener
import com.myhome.realload.PlaceListener
import com.myhome.realload.R
import com.myhome.realload.model.Image
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.model.Place
import com.myhome.realload.view.adapter.PlaceRecyclerViewAdapter
import com.myhome.realload.view.adapter.PlaceViewPagerAdapter
import com.myhome.realload.view.adapter.VisitedRecyclerViewAdapter
import com.myhome.siviewpager.SIViewPager

object DataBindingUtils {
    @BindingAdapter("bind_listener")
    @JvmStatic
    fun bindListener(recyclerView: RecyclerView, listener:ObservableField<FragmentListener>){
        (recyclerView.adapter as VisitedRecyclerViewAdapter).listener = listener.get()
    }
    @BindingAdapter("bind_listener")
    @JvmStatic
    fun bindPlaceListener(recyclerView: RecyclerView, listener:ObservableField<PlaceListener>){
        (recyclerView.adapter as PlaceRecyclerViewAdapter).listener = listener.get()
    }

//    @BindingAdapter("bind_listener")
//    @JvmStatic
//    fun bindPlaceListener(view: SIViewPager, listener:ObservableField<PlaceListener>){
//        if(view.pagerAdapter == null){
//            view.build(PlaceViewPagerAdapter(view.context))
//        }
//        (view.pagerAdapter as PlaceViewPagerAdapter).listener = listener.get()
//    }

//    @BindingAdapter("bind_modify")
//    @JvmStatic
//    fun bindModify(siViewPager: SIViewPager, modify:ObservableField<Boolean>){
//        if(siViewPager.pagerAdapter == null){
//            return
//        }
//        (siViewPager.pagerAdapter as PlaceViewPagerAdapter).modifing = modify.get()!!
//        siViewPager.pagerAdapter?.notifyDataSetChanged()
//    }

//    @BindingAdapter("bind_place")
//    @JvmStatic
//    fun bindPlace(siViewPager: SIViewPager, place:ObservableField<NamedPlace>){
//        if(siViewPager.pagerAdapter == null){
//            siViewPager.build(PlaceViewPagerAdapter(siViewPager.context))
//        }
//        (siViewPager.pagerAdapter as PlaceViewPagerAdapter).place = place.get()
//    }
    @BindingAdapter("bind_item")
    @JvmStatic
    fun bindItem(recyclerView:RecyclerView, items:ObservableArrayList<Place>){
        val adapter = recyclerView.adapter
        val lm = LinearLayoutManager(recyclerView.context)
        if(adapter == null){
            recyclerView.adapter = VisitedRecyclerViewAdapter()
            recyclerView.layoutManager = lm
        }

        (recyclerView.adapter as VisitedRecyclerViewAdapter).items = items
        recyclerView.adapter!!.notifyDataSetChanged()
    }
    @BindingAdapter("bind_item")
    @JvmStatic
    fun bindCustomItem(recyclerView:RecyclerView, items:ObservableArrayList<NamedPlace>){
        val adapter = recyclerView.adapter
        val lm = LinearLayoutManager(recyclerView.context)
        if(adapter == null){
            recyclerView.adapter = PlaceRecyclerViewAdapter()
            recyclerView.layoutManager = lm
        }

        (recyclerView.adapter as PlaceRecyclerViewAdapter).items = items
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    @BindingAdapter(value = ["bind_images", "bind_listener", "bind_place", "bind_modifying"], requireAll = false)
    @JvmStatic
    fun SIViewPager.bindItems(images:ObservableArrayList<Image>, listener: ObservableField<PlaceListener>, place: ObservableField<NamedPlace>, modifying:ObservableField<Boolean>){
        this.clear()
        val padapter = PlaceViewPagerAdapter(this.context, listener.get(), place.get(), modifying.get())
        for(item in images){
            padapter.addItem(item)
        }
        this.build(padapter)
    }

    @BindingAdapter("bind_text")
    @JvmStatic
    fun bindDoubleText(textView:TextView, value:Double){
        textView.text = value.toString()
    }
    @BindingAdapter("bind_text")
    @JvmStatic
    fun bindStringText(textView:TextView, value:String){
        textView.text = value
    }

    @BindingAdapter("bind_image")
    @JvmStatic
    fun bindFavoriteImage(imageView:ImageView, favorite:Boolean){
        if(favorite){
            imageView.setImageResource(R.drawable.ic_star_yellow_24dp)
        }
        else{
            imageView.setImageResource(R.drawable.ic_star_gray_24dp)
        }
    }


}