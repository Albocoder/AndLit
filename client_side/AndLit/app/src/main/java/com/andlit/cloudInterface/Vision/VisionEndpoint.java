package com.andlit.cloudInterface.Vision;

import android.content.Context;

import com.andlit.cloudInterface.Vision.models.Description;
import com.andlit.cloudInterface.Vision.models.Text;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.UserLogin;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
    private String base64OfImage;

    // WARNING! We assume there is a user logged in when this is instantiated!
    public VisionEndpoint(Context c,String filepath){
        Retrofit api = new Retrofit.Builder().baseUrl("https://andlit.info")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        db = AppDatabase.getDatabase(c);
        ul = db.userLoginDao().getLoginEntry().get(0);
        a = api.create(VisionAPI.class);
        base64OfImage = encodeImageToBase64(filepath);
    }

    public Description getDescriptionOfImage() throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"image\": \""+base64OfImage+"\"").getAsJsonObject();
        Call<JsonObject> call = a.describeContentOfImage("Token "+ul.access_token,o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject reply = resp.body();
            if(reply == null)
                return null;
            Set<String> keys = reply.keySet();
            Set<String> keys2 = new HashSet<>();
            Iterator<String> i = keys.iterator();
            int count = 0;
            while(i.hasNext() && count < 5){
                keys2.add(i.next());
                count++;
            }
            return new Description((String[])keys2.toArray());
        }
        else
            return null;
    }

    public List<Text> getTextFromImage() throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"image\": \""+base64OfImage+"\"").getAsJsonObject();
        Call<JsonObject> call = a.readTextInImage("Token "+ul.access_token,o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject reply = resp.body();
            if(reply == null)
                return null;
            Set<String> blocks = reply.keySet();
            ArrayList<Text> textBlocks = new ArrayList<>(blocks.size());
            for(String s:blocks) {
                JsonObject block = reply.getAsJsonObject(s);
                JsonObject blockBoundary = block.getAsJsonObject("block_boundary");
                JsonArray vertices = blockBoundary.getAsJsonArray("vertices");
                // todo get paragraphs and increment blockTextBuffer
                StringBuffer blockTextBuffer;
//                textBlocks.add(new Text(new opencv_core.Rect(),blockTextBuffer.toString()));
            }
            return textBlocks;
        }
        else{
            return null;
        }
    }
}
