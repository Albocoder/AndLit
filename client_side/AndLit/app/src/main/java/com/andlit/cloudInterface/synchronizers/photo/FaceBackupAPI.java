package com.andlit.cloudInterface.synchronizers.photo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

interface FaceBackupAPI {

    @GET("images/list/")
    Call<JsonArray> listPhotos(@Header("Authorization") String auth);


    @Multipart
    @POST("images/upload/")
    Call<JsonObject> savePhoto(@Header("Authorization") String auth,
                               @Part MultipartBody.Part file,
                               @Part("image_hash") RequestBody hash,
                               @Part("image_label") RequestBody isTrain);

    @POST("images/get/")
    @Headers( "Content-Type: application/json" )
    Call<ResponseBody> loadPhoto(@Header("Authorization") String auth, @Body JsonObject body);

    @Multipart
    @POST("images/getdetail/")
    Call<JsonObject> getImageStats(@Header("Authorization") String auth,
                                     @Part("image_hash") String hash);
}
