package com.andlit.cloudInterface.pools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

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

    @Multipart
    @POST("pools/query/")
    Call<JsonObject> queryPoolMember(@Header("Authorization") String auth,@Part MultipartBody.Part image,
                                     @Part("pool_id") RequestBody pid,
                                     @Part("target_user_id") RequestBody id);
}
