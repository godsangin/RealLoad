package com.myhome.realload.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ImageDBController(context:Context) {
    val context = context

    fun createImageFile():File{
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(fileDir?.exists() == false){
            fileDir.mkdir()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val targetFile = File.createTempFile(timeStamp, ".jpg", fileDir)
        return targetFile
    }

    fun removeImageFile(path:String){
        val file = File(path)
        file.delete()
    }

    fun copyFile(source:File):File{
        val targetFile = createImageFile()
        try{
            val inputStream = FileInputStream(source)
            val bufferedInputStream = BufferedInputStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val img = byteArrayOf()
            var current = bufferedInputStream.read()
            while(current != -1){
                byteArrayOutputStream.write(current)
                current = bufferedInputStream.read()
            }
            val fileOutputStream = FileOutputStream(targetFile)
            fileOutputStream.write(byteArrayOutputStream.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            inputStream.close()
            scanMedia(targetFile)
        }catch (e: Exception){
            e.printStackTrace()
        }
        return targetFile
    }

    fun scanMedia(file: File){
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.setData(Uri.fromFile(file))
        context.sendBroadcast(intent)
    }
}