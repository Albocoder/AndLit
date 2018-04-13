package com.andlit.cloudInterface.vision;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface VisionAPI {
    @Headers( "Content-Type: application/json" )
    @POST("vision/describe/")
    Call<JsonObject> describeContentOfImage(@Header("Authorization") String auth, @Body JsonObject image);

    @Multipart
    @POST("vision/describe/")
    Call<JsonObject> describeContentOfImage(@Header("Authorization") String auth, @Part MultipartBody.Part file);

    @Headers( "Content-Type: application/json" )
    @POST("vision/read/")
    Call<JsonObject> readTextInImage(@Header("Authorization") String auth, @Body JsonObject image);

    @Multipart
    @POST("vision/read/")
    Call<JsonObject> readTextInImage(@Header("Authorization") String auth, @Part MultipartBody.Part file);
}
