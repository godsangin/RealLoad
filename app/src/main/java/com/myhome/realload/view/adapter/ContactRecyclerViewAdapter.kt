package com.myhome.realload.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myhome.realload.databinding.ContactItemBinding
import com.myhome.realload.model.Friend
import com.myhome.realload.viewmodel.FindFriendListener
import com.myhome.realload.viewmodel.adapterviewmodel.ContactItemViewModel

class ContactRecyclerViewAdapter(listener:FindFriendListener?) :RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>(){
    var items = ArrayList<Friend>()
    val listener = listener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(binding:ContactItemBinding, listener:FindFriendListener?):RecyclerView.ViewHolder(binding.root){
        val binding = binding
        val listener = listener
        fun bind(friend:Friend){
            val viewModel = ContactItemViewModel(listener, friend)
            binding.model = viewModel
        }
    }
}