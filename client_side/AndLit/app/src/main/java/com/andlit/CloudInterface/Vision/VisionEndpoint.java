package com.andlit.CloudInterface.Vision;

import android.content.Context;

import com.andlit.CloudInterface.Vision.models.Text;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.UserLogin;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.andlit.utils.StorageHelper.encodeImageToBase64;

public class VisionEndpoint {
    // constants
    private static final String TAG = "VisionEndpoint";

    // variables
    private VisionAPI a;
    private UserLogin ul;
    private AppDatabase db;

    // WARNING! We assume there is a user logged in when this is instantiated!
    public VisionEndpoint(Context c){
        Retrofit api = new Retrofit.Builder().baseUrl("https://andlit.info")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        db = AppDatabase.getDatabase(c);
        ul = db.userLoginDao().getLoginEntry().get(0);
        a = api.create(VisionAPI.class);
    }

    public List<Text> getDescriptionOfImage(String filepath) throws IOException {
        String base64OfImage;
        base64OfImage = encodeImageToBase64(filepath);
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"image\": \""+base64OfImage+"\"").getAsJsonObject();
        Call<JsonObject> call = a.describeContentOfImage("Token "+ul.access_token,o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject reply = resp.body();
            Set<String> keys = reply.keySet();
            //todo get top 5 and create a Description object
            return null;
        }
        else
            return null;
    }
}
