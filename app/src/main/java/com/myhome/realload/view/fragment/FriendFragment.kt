package com.myhome.realload.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentFriendBinding
import com.myhome.realload.model.Friend
import com.myhome.realload.view.FindFriendActivity
import com.myhome.realload.viewmodel.FriendListener
import com.myhome.realload.viewmodel.FriendViewModel

class FriendFragment : Fragment() {
    val friendListener = object:FriendListener{
        override fun goSearchFriendActivity() {
            val intent = Intent(context, FindFriendActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val viewModel = FriendViewModel(friendListener)
        val binding = DataBindingUtil.inflate<FragmentFriendBinding>(inflater, R.layout.fragment_friend, container, false)
        binding.model = viewModel
        for(i in 0..3){
            val friend = Friend()
            friend.nickName = "이상인"
            friend.profileUrl = ""
            viewModel.friends.add(friend)
        }
        return binding.root
    }

    companion object {
        private var INSTANCE:FriendFragment? = null
        fun newInstance():FriendFragment?{
            if(INSTANCE == null){
                INSTANCE = FriendFragment()
            }
            return INSTANCE


        }
    }
}
