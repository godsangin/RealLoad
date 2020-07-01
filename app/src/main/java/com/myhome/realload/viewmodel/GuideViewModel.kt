package com.myhome.realload.viewmodel;

import androidx.databinding.ObservableField
import com.myhome.realload.view.GuideListener

class GuideViewModel(listener:GuideListener, arrayGuideText:ArrayList<String>) {
    val listener = listener
    val guideIndex = ObservableField<Int>(0)
    val arrayGuideText = arrayGuideText
    val guideText = ObservableField<String>(arrayGuideText.get(guideIndex.get() ?: 0))

    fun getNextGuide(){
        guideIndex.get()?.let {
            if(it >= 3){
                //intent event
                listener.finishGuide()
                return@let
            }
            else{
                listener.nextGuideAnim()
                guideIndex.set(it + 1)
                guideText.set(arrayGuideText.get(guideIndex.get() ?: 0))
            }
        }
    }
}
