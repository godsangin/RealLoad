package com.myhome.realload.viewmodel

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.myhome.realload.PlaceListener
import com.myhome.realload.db.AppDatabase
import com.myhome.realload.model.Image
import com.myhome.realload.model.NamedPlace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaceViewModel(database: AppDatabase?, listener: PlaceViewModelListener) {
    val database = database
    val places = ObservableArrayList<NamedPlace>()
    val listener = listener
    val placeListener = object:PlaceListener{
        override fun favorite(place: NamedPlace) {
            CoroutineScope(Dispatchers.IO).launch {
                if(place.favorite){
                    database?.CustomPlaceDao()?.updateFavorite(place.id,false)
                    listener.removeGeofence(place)
                }
                else{
                    database?.CustomPlaceDao()?.updateFavorite(place.id,true)
                    listener.addGeofence(place)
                }
            }

        }

        override fun getDatabase(): AppDatabase? {
            return listener.getDatabase()
        }

        override fun getOwner(): LifecycleOwner {
            return listener.getOwner()
        }

        override fun goToAlbum(place: NamedPlace?) {
            listener.goToAlbum(place)
        }

        override fun updateImages(images: ArrayList<Image>) {
            listener.updateImages(images)
        }

        override fun removeItem(place: NamedPlace?, position: Int) {
            var index = 0
            for(myPlace in places){
                if(myPlace.equals(place) == true){
                    myPlace.images.removeAt(position)
                    places.set(index, myPlace)
                }
                index++
            }
        }
    }
    val listenerObserver = ObservableField<PlaceListener>(placeListener)
    fun getBaseData(owner: LifecycleOwner, database: AppDatabase?){
        if(database == null){
            return
        }
        database?.CustomPlaceDao()?.selectAll().observe(owner, Observer {
            CoroutineScope(Dispatchers.IO).launch {
                places.clear()
                for(place in it){
                    val myPlace = NamedPlace()
                    myPlace.id = place.id
                    myPlace.name = place.name
                    myPlace.favorite = place.favorite
                    myPlace.latitude = place.latitude
                    myPlace.longitude = place.longitude
                    val images = database?.ImageDao()?.getImagesByPid(myPlace.id)
                    myPlace.images = ArrayList(images)
                    places.add(myPlace)
                }
            }
        })
    }

    fun addItem(place:NamedPlace?, copyFilePath:String){
        var index = 0
        for(myPlace in places){
            if(place?.equals(myPlace) == true){
                val image = Image()
                image.pid = place.id
                image.imageRes = copyFilePath
                myPlace.images.add(image)
                places.set(index, myPlace)
            }
            index++
        }
    }

}