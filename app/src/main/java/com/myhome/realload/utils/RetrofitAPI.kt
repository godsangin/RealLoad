package com.myhome.realload.utils

import com.google.gson.JsonObject
import com.myhome.realload.model.ApiArrayResponse
import com.myhome.realload.model.ApiResponse
import com.myhome.realload.model.Place
import com.myhome.realload.model.User
import retrofit2.Call
import retrofit2.http.*

@JvmSuppressWildcards
interface RetrofitAPI {
    @GET("/users/{id}")
    fun getUser(@Path("id") id:Long):Call<ApiResponse>
    @POST("/users")
    fun insertUser(@Body user:User):Call<ApiResponse>
    @PUT("/users")
    fun updateUser(@Body user:User):Call<ApiResponse>
    @DELETE("/users/{id}")
    fun deleteUser(@Path("id") id:Long):Call<ApiResponse>

    @GET("/places/{uid}/{startDate}/{endDate}")
    fun getPlaces(@Path("uid") uid:Long, @Path("startDate") startDate:String, @Path("endDate") endDate:String):Call<JsonObject>
    @POST("/places/{uid}")
    fun insertPlace(@Path("uid") uid:Long, @Body place:Place):Call<ApiResponse>

    @GET("/users/tel/{tel}")
    fun getUserByTel(@Path("tel") tel:String):Call<ApiResponse>

    @POST("/friends")
    fun insertFriend(@Body map:Map<String, Long>):Call<JsonObject>
    @GET("/friends/uid/{uid}")
    fun getFriends(@Path("uid") uid:Long):Call<JsonObject>
    @GET("/friends/requests/{uid}")
    fun getFriendRequests(@Path("uid") uid:Long):Call<JsonObject>
    @PUT("/friends/{fromUid}/{toUid}")
    fun allowFriendRequest(@Path("fromUid") fromUid:Long, @Path("toUid") toUid:Long):Call<JsonObject>
    @PUT("/friends")
    fun updateFriend(@Body map:Map<String, Any>):Call<JsonObject>

}