package com.myhome.realload.viewmodel.fragment

import android.app.DatePickerDialog

interface VisitedViewModelListener {
    fun createDatePicker(listener:DatePickerDialog.OnDateSetListener)
}