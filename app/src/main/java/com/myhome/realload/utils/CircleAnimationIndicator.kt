package com.myhome.realload.utils

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout

class CircleAnimationIndicator(context:Context, attrs:AttributeSet):LinearLayout(context, attrs){
    var itemMargin = 10
    var animationDuration = 250
    var defaultCircle = 0
    var selectCircle = 0
    lateinit var imageDot:ArrayList<ImageView>
    fun init(count:Int, defaultCircle:Int, selectCircle:Int){
        this.defaultCircle = defaultCircle
        this.selectCircle = selectCircle
        imageDot = ArrayList()
        for(i in 0..count){
            val myImageView = ImageView(context)
            imageDot.add(myImageView)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.topMargin = itemMargin
            params.bottomMargin = itemMargin
            params.leftMargin = itemMargin
            params.rightMargin = itemMargin
            params.gravity = Gravity.CENTER

            myImageView.layoutParams = params
            myImageView.setImageResource(defaultCircle)
            myImageView.setTag(imageDot.get(i).id, false)
            addView(imageDot.get(i))
        }
        selectDot(0)
    }

    fun clear(){
        removeAllViews()
        imageDot.clear()
    }

    fun addItem(){
        val myImageView = ImageView(context)
        imageDot.add(myImageView)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = itemMargin
        params.bottomMargin = itemMargin
        params.leftMargin = itemMargin
        params.rightMargin = itemMargin
        params.gravity = Gravity.CENTER

        myImageView.layoutParams = params
        myImageView.setImageResource(defaultCircle)
        myImageView.setTag(myImageView.id, false)
        addView(myImageView)

    }

    fun removeItem(position:Int){
        val item = imageDot.removeAt(position)
        removeView(item)
        notifyDatasetChangedByPosition(position)
    }

    fun notifyDatasetChangedByPosition(position:Int){
        selectDot(position)
    }

    fun selectDot(position:Int){
        for(i in 0..imageDot.size - 1){
            if(i == position){
                imageDot[i].setImageResource(selectCircle)
                selectScalAnim(imageDot[i], 1f, 1.5f)
            }else{
                if(imageDot[i].getTag(imageDot[i].id) == true){
                    imageDot[i].setImageResource(defaultCircle)
                    defaultScaleAnim(imageDot[i], 1.5f, 1f)
                }
            }
        }
    }

    fun selectScalAnim(view:View, startScale:Float, endScale:Float){
        val anim = ScaleAnimation(startScale, endScale, startScale, endScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.fillAfter = true
        anim.duration = animationDuration.toLong()
        view.startAnimation(anim)
        view.setTag(view.id, true)
    }

    fun defaultScaleAnim(view:View, startScale: Float, endScale: Float){
        val anim = ScaleAnimation(startScale, endScale, startScale, endScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.fillAfter = true
        anim.duration = animationDuration.toLong()
        view.startAnimation(anim)
        view.setTag(view.id, false)
    }

}