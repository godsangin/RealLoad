package com.myhome.realload.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myhome.realload.databinding.ContactItemBinding
import com.myhome.realload.model.Contact

class ContactRecyclerViewAdapter :RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>(){
    var items = ArrayList<Contact>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(binding:ContactItemBinding):RecyclerView.ViewHolder(binding.root){
        val binding = binding
        fun bind(contact:Contact){
            binding.model = contact
        }
    }
}