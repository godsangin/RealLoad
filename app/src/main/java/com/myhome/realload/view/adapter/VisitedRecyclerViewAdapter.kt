package com.myhome.realload.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myhome.realload.FragmentListener
import com.myhome.realload.databinding.VisitedItemBinding
import com.myhome.realload.databinding.VisitedNamedItemBinding
import com.myhome.realload.model.NamedPlace
import com.myhome.realload.model.Place
import com.myhome.realload.viewmodel.adapterviewmodel.VisitedItemViewModel

class VisitedRecyclerViewAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var items:ArrayList<Place> = ArrayList()
    var listener:FragmentListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 0){
            val visitedItemBinding = VisitedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = ViewHolder(visitedItemBinding)
            return holder
        }
        else{
            val visitedItemBinding = VisitedNamedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val holder = NamedViewHolder(visitedItemBinding)
            return holder
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == 0){
            (holder as ViewHolder).bind(items[position], position + 1)
        }
        else{
            (holder as NamedViewHolder).bind(items[position] as NamedPlace, position + 1)
        }
        holder.itemView.setOnClickListener {
            listener?.moveLocationWithMarker(items[position])
        }
    }
    override fun getItemViewType(position: Int): Int {
        if(items[position] is NamedPlace){
            return 1
        }
        return 0
    }

    class ViewHolder(binding:VisitedItemBinding):RecyclerView.ViewHolder(binding.root){
        val binding = binding
        fun bind(place:Place, index:Int){
            binding.model = VisitedItemViewModel(place, index)
        }
    }

    class NamedViewHolder(binding:VisitedNamedItemBinding):RecyclerView.ViewHolder(binding.root){
        val binding = binding
        fun bind(place:NamedPlace, index:Int){
            binding.model = VisitedItemViewModel(place, index)
        }
    }
}