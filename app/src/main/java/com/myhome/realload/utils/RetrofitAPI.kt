package com.myhome.realload.utils

import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.Place
import com.myhome.realload.model.User
import retrofit2.Call
import retrofit2.http.*

interface RetrofitAPI {
    @GET("/users/{id}")
    fun getUser(@Path("id") id:Long):Call<ApiResponse>
    @POST("/users")
    fun insertUser(@Body user:User):Call<ApiResponse>
    @PUT("/users/{id}")
    fun updateUser(@Path("id") id:Long, @Body user:User):Call<ApiResponse>
    @DELETE("/users/{id}")
    fun deleteUser(@Path("id") id:Long):Call<ApiResponse>

    @GET("/places/{uid}/{startDate}/{endDate}")
    fun getPlaces(@Path("uid") uid:Long, @Path("startDate") startDate:String, @Path("endDate") endDate:String):Call<ApiResponse>
    @POST("/places/{uid}")
    fun insertPlace(@Path("uid") uid:Long, @Body place:Place):Call<ApiResponse>

    @GET("users/{tel}")
    fun getUserByTel(@Path("tel") tel:String):Call<ApiResponse>

}