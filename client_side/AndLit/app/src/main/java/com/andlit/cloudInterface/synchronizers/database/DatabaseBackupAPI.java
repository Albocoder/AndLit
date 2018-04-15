package com.andlit.cloudInterface.synchronizers.database;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface DatabaseBackupAPI {

    @Multipart
    @POST("files/db/upload/")
    Call<JsonObject> uploadDatabase(@Header("Authorization") String auth,
                                      @Part MultipartBody.Part file);

    @GET("files/cl/list/")
    Call<JsonObject> showDatabaseStats(@Header("Authorization") String auth);

    @GET("files/cl/get/")
    Call<ResponseBody> downloadDatabase(@Header("Authorization") String auth);

}
