package com.myhome.realload.view

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.myhome.realload.GlideApp
import com.myhome.realload.R
import kotlinx.android.synthetic.main.activity_splash.*
import javax.sql.DataSource

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        var stringArray = ArrayList<String>()
        var splashStrings = resources.getStringArray(R.array.splash_strings)
        for(str in splashStrings){
            stringArray.add(str)
        }
        text1.setText(popString(stringArray))
        text2.setText(popString(stringArray))
        startAnimation()
    }

    fun popString(strings:ArrayList<String>):String{
        val random = (Math.random() * strings.size).toInt()
        val string = strings.removeAt(random)
        return string
    }

    fun startAnimation(){
        val listener= object:RequestListener<GifDrawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<GifDrawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: GifDrawable?,
                model: Any?,
                target: Target<GifDrawable>?,
                dataSource: com.bumptech.glide.load.DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                resource?.setLoopCount(1)
                val callback = object : Animatable2Compat.AnimationCallback(){
                    override fun onAnimationEnd(drawable: Drawable?) {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        super.onAnimationEnd(drawable)
                    }

                }
                resource?.registerAnimationCallback(callback)
                return false
            }
        }
        val requestOptions = RequestOptions()
        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
        requestOptions.skipMemoryCache(false)
        requestOptions.signature(ObjectKey(System.currentTimeMillis()))
        GlideApp.with(applicationContext).asGif().listener(listener).load(R.drawable.gif_realload).apply(
            requestOptions).into(logo_image)
    }
}
