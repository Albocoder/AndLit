package com.andlit.cloudInterface.pools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface PoolsAPI {
    @Headers( "Content-Type: application/json" )
    @POST("pools/create/")
    Call<JsonObject> createPool(@Header("Authorization") String auth, @Body JsonObject poolName);

    @Headers( "Content-Type: application/json" )
    @POST("pools/change_name/")
    Call<JsonObject> changePoolName(@Header("Authorization") String auth, @Body JsonObject poolData);

    @Headers( "Content-Type: application/json" )
    @POST("pools/change_password/")
    Call<JsonObject> changePoolPw(@Header("Authorization") String auth, @Body JsonObject poolData);

    @Headers( "Content-Type: application/json" )
    @POST("pools/join/")
    Call<JsonObject> joinPool(@Header("Authorization") String auth, @Body JsonObject poolData);

    @Headers( "Content-Type: application/json" )
    @GET("pools/list_pools/")
    Call<JsonArray> listPools(@Header("Authorization") String auth);

    @Headers( "Content-Type: application/json" )
    @POST("pools/list_users/")
    Call<JsonArray> listUsersInPool(@Header("Authorization") String auth, @Body JsonObject poolData);

    @Headers( "Content-Type: application/json" )
    @POST("pools/kick_user/")
    Call<String> kickUserFromPool(@Header("Authorization") String auth, @Body JsonObject userAndPoolData);

    @Headers( "Content-Type: application/json" )
    @POST("pools/leave/")
    Call<String> leavePool(@Header("Authorization") String auth, @Body JsonObject poolData);
}
