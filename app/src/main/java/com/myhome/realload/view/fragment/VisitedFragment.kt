package com.myhome.realload.view.fragment

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myhome.realload.FragmentListener
import com.myhome.realload.R
import com.myhome.realload.databinding.FragmentVisitedBinding
import com.myhome.realload.db.AppDatabase

import com.myhome.realload.viewmodel.fragment.VisitedViewModel
import com.myhome.realload.viewmodel.fragment.VisitedViewModelListener

class VisitedFragment : Fragment() {
    var fragmentListener:FragmentListener? = null
    val visitedViewModelListener = object:
        VisitedViewModelListener {
        override fun createDatePicker(listener: DatePickerDialog.OnDateSetListener) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val dialog = DatePickerDialog(context!!)
                dialog.setOnDateSetListener(listener)
                dialog.show()
            }
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
        val binding = DataBindingUtil.inflate<FragmentVisitedBinding>(inflater, R.layout.fragment_visited, container, false)
        val viewModel = VisitedViewModel(
            fragmentListener,
            visitedViewModelListener,
            viewLifecycleOwner,
            AppDatabase.getInstance(context!!)
        )
        binding.model = viewModel
        viewModel.getBaseData()
        return binding.root
    }

    companion object {
        private var INSTANCE:VisitedFragment? = null
        @JvmStatic
        fun newInstance(fragmentListener: FragmentListener):VisitedFragment{
            if(INSTANCE == null){
                INSTANCE = VisitedFragment()
            }
            INSTANCE?.fragmentListener = fragmentListener
            return INSTANCE!!
        }
    }
}
