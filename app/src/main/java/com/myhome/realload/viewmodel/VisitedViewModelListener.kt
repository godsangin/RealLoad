package com.myhome.realload.viewmodel

import android.app.DatePickerDialog

interface VisitedViewModelListener {
    fun createDatePicker(listener:DatePickerDialog.OnDateSetListener)
}