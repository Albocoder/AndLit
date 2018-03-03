package com.example.mehmet.andlit.CloudInterface;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

interface AuthAPI {

    @Headers( "Content-Type: application/json" )
    @POST("users/create")
    Call<JsonObject> registerNewUser(@Body JsonObject body);

    @Headers( "Content-Type: application/json" )
    @GET("users/getprofile")
    Call<JsonObject> getInfoForLoggedInUser(@Header("Authorization") String auth);

    @Headers( "Content-Type: application/json" )
    @PUT("users/changepass")
    Call<JsonObject> changePasswordForUser(@Body JsonObject body,
                                           @Header("Authorization") String auth);

    @Headers( "Content-Type: application/json" )
    @POST("users/gettoken")
    Call<JsonObject> getAuthTokenForUser(@Body JsonObject body);
}