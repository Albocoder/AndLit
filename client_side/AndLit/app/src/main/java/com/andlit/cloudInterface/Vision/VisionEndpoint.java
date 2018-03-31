package com.andlit.cloudInterface.Vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.andlit.cloudInterface.Vision.models.Description;
import com.andlit.cloudInterface.Vision.models.Text;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.UserLogin;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.andlit.utils.StorageHelper.encodePNGImageToBase64;
import static com.andlit.utils.StorageHelper.writePNGToInternalMemory;

public class VisionEndpoint {
    // constants
    private static final String TAG = "VisionEndpoint";

    // variables
    private VisionAPI a;
    private UserLogin ul;
    private AppDatabase db;
    private String base64OfImage;
    private File imgFile;
    private Context c;

    // WARNING! We assume there is a user logged in when this is instantiated!
    public VisionEndpoint(Context c,Bitmap bm){
        Retrofit api = new Retrofit.Builder().baseUrl("https://andlit.info")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        db = AppDatabase.getDatabase(c);
        ul = db.userLoginDao().getLoginEntry().get(0);
        a = api.create(VisionAPI.class);
        this.c = c;
        try {
            String path = writePNGToInternalMemory(c,bm,"tmp","tmpFile.png");
            imgFile = new File(path);
        } catch (IOException e) {
            base64OfImage = encodePNGImageToBase64(bm); // this doesn't work
        }
    }

    public Description getDescriptionOfImageBinary() throws IOException {
        if(imgFile == null)
            return null;
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/png"),
                        imgFile
                );
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", imgFile.getName(), requestFile);

        Call<JsonObject> call = a.describeContentOfImage("Token "+ul.access_token,body);
        return getDescriptionFromCall(call);
    }

    @Deprecated
    public Description getDescriptionOfImageBase64() throws IOException {
        if(base64OfImage == null)
            return null;
        JsonParser parser = new JsonParser();
        Log.d("test","Fired Description request!");
        JsonObject o = parser.parse("{\"image\": \""+base64OfImage+"\"").getAsJsonObject();
        Call<JsonObject> call = a.describeContentOfImage("Token "+ul.access_token,o);
        return getDescriptionFromCall(call);
    }

    public List<Text> getTextFromImageBinary() throws IOException {
        if(imgFile == null)
            return null;
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/png"),
                        imgFile
                );
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", imgFile.getName(), requestFile);
        Call<JsonObject> call = a.readTextInImage("Token "+ul.access_token,body);
        return getTextFromCall(call);
    }

    @Deprecated
    public List<Text> getTextFromImageBase64() throws IOException {
        if (base64OfImage == null)
            return null;
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"image\": \""+base64OfImage+"\"").getAsJsonObject();
        Call<JsonObject> call = a.readTextInImage("Token "+ul.access_token,o);
        return getTextFromCall(call);
    }

    public void destroy() {
        if(imgFile != null)
            imgFile.delete();
    }


    /* ********************* HELPER FUNCTIONS *********************** */
    private Description getDescriptionFromCall(Call<JsonObject> call) throws IOException {
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject reply = resp.body();
            if(reply == null)
                return null;
            Set<String> keys = reply.keySet();
            Iterator<String> i = keys.iterator();

            // todo get top 5
            int count = 0;
            String [] keysArr = new String[5];
            while(i.hasNext() && count < 5){
                keysArr[count] = i.next();
                count++;
            }
            if(count < 4) {
                String[] keysArr2 = Arrays.copyOf(keysArr,count);
                return new Description(keysArr2);
            }
            return new Description(keysArr);
        }
        else
            return null;
    }

    private List<Text> getTextFromCall(Call<JsonObject> call) throws IOException {
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
                //todo fix this after server response is fixed
                JsonObject blockBoundary = block.getAsJsonObject("block_boundary");
                JsonArray vertices = blockBoundary.getAsJsonArray("vertices");
                int minx = 999999,maxx = -999999,miny = 999999,maxy = -999999;
                for(JsonElement v:vertices) {
                    JsonObject tmpPoint = v.getAsJsonObject();
                    int tmpx = tmpPoint.get("x").getAsInt();
                    int tmpy = tmpPoint.get("y").getAsInt();
                    if (minx > tmpx)
                        minx = tmpx;
                    if (maxx < tmpx)
                        maxx = tmpx;
                    if (miny > tmpy)
                        miny = tmpy;
                    if (maxy < tmpy)
                        maxy = tmpy;
                }
                if (minx < 0)
                    minx = 0;
                if (miny < 0)
                    miny = 0;
                if (maxx < 0)
                    maxx = 0;
                if (maxy < 0)
                    maxy = 0;
                String blockText = block.getAsJsonObject("block_text").getAsString();
                Text tmp = new Text(new opencv_core.Rect(minx,miny,maxx-minx+1,maxy-miny+1),blockText);
                textBlocks.add(tmp);
            }
            return textBlocks;
        }
        else
            return null;
    }
}