package com.andlit.cloudInterface.synchronizers.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.andlit.cloudInterface.synchronizers.photo.model.SinglePhotoResponse;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.UserLogin;
import com.andlit.database.entities.detected_face;
import com.andlit.database.entities.training_face;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhotoBackup {
    // constants
    private static final String TAG = "PhotoBackup";

    // variables
    private AppDatabase db;
    private FaceBackupAPI a;
    private UserLogin ul;
    private Context c;

    // WARNING! We assume there is a user logged in when this is instantiated!
    public PhotoBackup(Context c) {
        Retrofit api = new Retrofit.Builder().baseUrl("https://andlit.info")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        a = api.create(FaceBackupAPI.class);
        db = AppDatabase.getDatabase(c);
        this.c = c;
        ul = db.userLoginDao().getLoginEntry().get(0);
    }

    public boolean saveAllTrainingData() {
        List<training_face> tfs = db.trainingFaceDao().getAllRecords();

        boolean newSave = false;
        for(training_face tmp:tfs)
            newSave |= saveSingleTrainingFace(tmp);

        return newSave;
    }

    public boolean saveAllDetections() {
        List<detected_face> dfs = db.detectedFacesDao().getAllRecords();

        boolean newSave = false;
        for(detected_face tmp:dfs)
            newSave |= saveSingleDetectedFace(tmp);

        return newSave;
    }

    public boolean saveSingleTrainingFace(training_face tf) {
        File f = new File(tf.path);
        if (!f.exists())
            return false;
        RequestBody file = RequestBody.create(MediaType.parse("image/*"), f);
        MultipartBody.Part fileBody =
                MultipartBody.Part.createFormData("image", f.getName(), file);
        RequestBody hash =
                RequestBody.create( okhttp3.MultipartBody.FORM, tf.hash);
        Call<JsonObject> call = a.savePhoto("Token "+ul.access_token,fileBody,hash);
        try { call.execute(); } catch (IOException e){ return false; }
        return true;
    }

    public boolean saveSingleDetectedFace(detected_face df) {
        File f = new File(df.path);
        if (!f.exists())
            return false;
        RequestBody file = RequestBody.create(MediaType.parse("image/*"), f);
        MultipartBody.Part fileBody =
                MultipartBody.Part.createFormData("image", f.getName(), file);
        RequestBody hash =
                RequestBody.create( okhttp3.MultipartBody.FORM, df.hash);
        Call<JsonObject> call = a.savePhoto("Token "+ul.access_token,fileBody,hash);
        try { call.execute(); } catch (IOException e){ return false; }
        return true;
    }

    public List<SinglePhotoResponse> listAllPhotos() throws IOException {
        ArrayList<SinglePhotoResponse> toReturn = new ArrayList<>();
        Call<JsonArray> call = a.listPhotos("Token "+ul.access_token);
        Response<JsonArray> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonArray body = resp.body();
            if(body == null)
                return null;
            for(JsonElement e:body){
                JsonObject tmp = (JsonObject) e;
                String tmpHash = tmp.get("image_hash").getAsString();
                List<training_face> theInstance =
                        db.trainingFaceDao().getTrainingFaceWithHash(tmpHash);
                SinglePhotoResponse tmpResp;
                if(theInstance.size() > 0)
                    tmpResp = new SinglePhotoResponse(tmpHash,true);
                else
                    tmpResp = new SinglePhotoResponse(tmpHash,false);
                toReturn.add(tmpResp);
            }
        }
        else
            return  null;
        return toReturn;
    }

    public Bitmap getBitmapForHash(String hash) throws IOException {
        Bitmap toReturn;
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"image_hash\": \""+hash+"\"}").getAsJsonObject();
        Call<ResponseBody> call = a.loadPhoto("Token "+ul.access_token,o);
        Response<ResponseBody> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            ResponseBody body = resp.body();
            if(body == null)
                return null;
            InputStream inputStream = body.byteStream();
            toReturn = BitmapFactory.decodeStream(inputStream);
        }
        else
            return null;
        return toReturn;
    }

    public boolean loadSinglePhotoResponse(SinglePhotoResponse r) {
        if(r == null)
            return false;
        if(r.getHash() == null)
            return false;
        if(r.getBmp() == null) {
            try {
                r.setBmp(getBitmapForHash(r.getHash()));
            } catch (IOException e) { return false; }
        }
        String path;
        if(r.isTraining()) {
            List<training_face> tfs = db.trainingFaceDao().getTrainingFaceWithHash(r.getHash());
            if(tfs.size() != 1)
                return false;
            else {
                training_face t = tfs.get(0);
                path = t.path;
            }
        }
        else {
            List<detected_face> dfs = db.detectedFacesDao().getDetectionWithHash(r.getHash());
            if(dfs.size() != 1)
                return false;
            else {
                detected_face t = dfs.get(0);
                path = t.path;
            }
        }
        File f = new File(path);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) { return false; }
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) { return false; }
        Bitmap bmp = r.getBmp();
        bmp.compress(Bitmap.CompressFormat.PNG,100, fos);
        return true;
    }
}