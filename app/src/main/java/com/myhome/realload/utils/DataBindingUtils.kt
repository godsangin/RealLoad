package com.myhome.realload.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.myhome.realload.FragmentListener
import com.myhome.realload.GlideApp
import com.myhome.realload.PlaceListener
import com.myhome.realload.R
import com.myhome.realload.model.*
import com.myhome.realload.view.adapter.*
import com.myhome.realload.viewmodel.FindFriendListener
import com.myhome.realload.viewmodel.fragment.FriendListener
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

    @BindingAdapter("bind_navlistener")
    @JvmStatic
    fun setNavListener(view:NavigationView, listener:NavigationView.OnNavigationItemSelectedListener){
        view.setNavigationItemSelectedListener(listener)
    }

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

    @BindingAdapter(value=["bind_friend_item", "bind_listener"], requireAll = false)
    @JvmStatic
    fun bindFriendItem(recyclerView:RecyclerView, items:ObservableArrayList<Friend>, listener:ObservableField<FriendListener>){
        val adapter = recyclerView.adapter
        val lm = LinearLayoutManager(recyclerView.context)
        if(adapter == null){
            recyclerView.adapter = FriendRecyclerViewAdapter(listener.get())
            recyclerView.layoutManager = lm
        }

        (recyclerView.adapter as FriendRecyclerViewAdapter).items = items
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    @BindingAdapter(value = ["bind_item", "bind_listener"], requireAll = false)
    @JvmStatic
    fun bindContactItem(recyclerView:RecyclerView, items:ObservableArrayList<Friend>, listener:ObservableField<FindFriendListener>){
        val adapter = recyclerView.adapter
        val lm = LinearLayoutManager(recyclerView.context)
        if(adapter == null){
            recyclerView.adapter = ContactRecyclerViewAdapter(listener.get())
            recyclerView.layoutManager = lm
        }
//        if(items.size % 10 != 0){
//            return
//        }
        (recyclerView.adapter as ContactRecyclerViewAdapter).items = items
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

    @BindingAdapter("bind_image")
    @JvmStatic
    fun bindImage(imageView:ImageView, imageUrl:String){
        GlideApp.with(imageView.context).load(imageUrl).placeholder(R.drawable.ic_account_circle_gray_24dp).into(imageView)
    }


}