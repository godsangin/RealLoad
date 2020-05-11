package com.myhome.realload.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myhome.realload.FragmentListener
import com.myhome.realload.PlaceListener
import com.myhome.realload.databinding.PlaceItemBinding
import com.myhome.realload.databinding.VisitedItemBinding
import com.myhome.realload.databinding.VisitedNamedItemBinding
import com.myhome.realload.model.CustomPlace
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.viewmodel.adapterviewmodel.PlaceRecyclerViewModel
import kotlinx.android.synthetic.main.place_item.view.*

class PlaceRecyclerViewAdapter :RecyclerView.Adapter<PlaceRecyclerViewAdapter.ViewHolder>(){
    var items:ArrayList<NamedPlace> = ArrayList()
    var listener:PlaceListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val placeItemBinding = PlaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(placeItemBinding, listener)

        return holder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
//        holder.itemView.favorite.setOnClickListener {
//            listener?.favorite(items[position])
//        }
    }

    class ViewHolder(binding:PlaceItemBinding, listener:PlaceListener?):RecyclerView.ViewHolder(binding.root){
        val binding = binding
        val listener = listener
        lateinit var viewModel:PlaceRecyclerViewModel
        fun bind(place:NamedPlace){
            viewModel = PlaceRecyclerViewModel(place, listener)
            binding.model = viewModel

        }
    }

}