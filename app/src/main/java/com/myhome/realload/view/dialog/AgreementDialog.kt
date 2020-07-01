package com.myhome.realload.view.dialog

import android.app.Activity
import android.os.Bundle
import android.view.Window
import com.myhome.realload.R
import kotlinx.android.synthetic.main.dialog_require_permission.*

class AgreementDialog : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_require_permission)

        agree_checkbox.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }
}