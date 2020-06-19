package com.myhome.realload.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myhome.realload.databinding.FriendItemBinding
import com.myhome.realload.model.Friend
import com.myhome.realload.viewmodel.fragment.FriendListener
import com.myhome.realload.viewmodel.adapterviewmodel.FriendItemViewModel

class FriendRecyclerViewAdapter(listener: FriendListener?) :RecyclerView.Adapter<FriendRecyclerViewAdapter.ViewHolder>(){
    var items = ArrayList<Friend>()
    val listener = listener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val friendItemBinding = FriendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(friendItemBinding, listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(binding:FriendItemBinding, listener: FriendListener?): RecyclerView.ViewHolder(binding.root) {
        val binding = binding
        val listener = listener
        //val listener = listener
        fun bind(friend:Friend){
            val viewModel = FriendItemViewModel(listener, friend)
            binding.model = viewModel
        }

    }
}