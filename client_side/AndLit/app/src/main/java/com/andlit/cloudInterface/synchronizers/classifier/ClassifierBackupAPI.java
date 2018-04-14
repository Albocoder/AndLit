package com.andlit.cloudInterface.synchronizers.classifier;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

interface ClassifierBackupAPI {
    @Multipart
    @POST("files/cl/upload/")
    Call<JsonObject> uploadClassifier(@Header("Authorization") String auth,
                                      @Part MultipartBody.Part file,
                                      @Part("file_hash") RequestBody description);

}
