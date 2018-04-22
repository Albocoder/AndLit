package com.andlit.cloudInterface.synchronizers.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.andlit.R;
import com.andlit.cloudInterface.synchronizers.photo.model.SinglePhotoResponse;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.UserLogin;
import com.andlit.database.entities.detected_face;
import com.andlit.database.entities.training_face;
import com.andlit.face.FaceOperator;
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
        Retrofit api = new Retrofit.Builder().baseUrl(c.getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.c = c;
        a = api.create(FaceBackupAPI.class);
        db = AppDatabase.getDatabase(c);
        ul = db.userLoginDao().getLoginEntry().get(0);
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
            for(JsonElement e:body) {
                JsonObject tmp = (JsonObject) e;
                String tmpHash = tmp.get("image_hash").getAsString();
                boolean isTraining = !tmp.get("image_label").getAsString().equalsIgnoreCase("-1");
                int size = tmp.get("image_size").getAsInt();
                SinglePhotoResponse tmpResp = new SinglePhotoResponse(tmpHash,isTraining,size);
                toReturn.add(tmpResp);
            }
        }
        else
            return  null;
        return toReturn;
    }

    public boolean saveAllTrainingData() throws IOException {
        List<training_face> tfs = db.trainingFaceDao().getAllRecords();

        boolean newSave = false;
        for(training_face tmp:tfs)
            newSave |= saveSingleTrainingFace(tmp);

        return newSave;
    }

    public boolean saveAllDetections() throws IOException {
        List<detected_face> dfs = db.detectedFacesDao().getAllRecords();

        boolean newSave = false;
        for(detected_face tmp:dfs)
            newSave |= saveSingleDetectedFace(tmp);

        return newSave;
    }

    public boolean saveSingleTrainingFace(training_face tf) throws IOException {
        File f = new File(FaceOperator.getAbsolutePath(c,tf));
        if (!f.exists())
            return false;
        RequestBody file = RequestBody.create(MediaType.parse("image/*"), f);
        MultipartBody.Part fileBody =
                MultipartBody.Part.createFormData("image", f.getName(), file);
        RequestBody hash =
                RequestBody.create( okhttp3.MultipartBody.FORM, tf.hash);
        RequestBody trainingLabel =
                RequestBody.create( okhttp3.MultipartBody.FORM, ""+tf.label);
        Call<JsonObject> call = a.savePhoto("Token "+ul.access_token,fileBody,hash,trainingLabel);
        Response resp = call.execute();
        int code = resp.code();
        return code >= 200 && code < 300;
    }

    public boolean saveSingleTrainingFaceFromHash(String hash) throws IOException {
        List<training_face> tf = db.trainingFaceDao().getTrainingFaceWithHash(hash);
        if(tf.size() != 1)
            return false;
        else
            return saveSingleTrainingFace(tf.get(0));
    }

    public boolean saveSingleDetectedFace(detected_face df) throws IOException {
        File f = new File(FaceOperator.getAbsolutePath(c,df));
        if (!f.exists())
            return false;
        RequestBody file = RequestBody.create(MediaType.parse("image/*"), f);
        MultipartBody.Part fileBody =
                MultipartBody.Part.createFormData("image", f.getName(), file);
        RequestBody hash =
                RequestBody.create( okhttp3.MultipartBody.FORM, df.hash);
        RequestBody isTraining =
                RequestBody.create( okhttp3.MultipartBody.FORM, "-1");
        Call<JsonObject> call = a.savePhoto("Token "+ul.access_token,fileBody,hash,isTraining);
        Response resp = call.execute();
        int code = resp.code();
        return code >= 200 && code < 300;
    }

    public boolean saveSingleDetectedFaceFromHash(String hash) throws IOException {
        List<detected_face> df = db.detectedFacesDao().getDetectionWithHash(hash);
        if(df.size() != 1)
            return false;
        else
            return saveSingleDetectedFace(df.get(0));
    }

    public SinglePhotoResponse getImageFromHash(String hash) throws IOException {
        Call<JsonObject> call = a.getImageStats("Token "+ul.access_token,hash);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject tmp = resp.body();
            if(tmp == null)
                return null;
            String tmpHash = tmp.get("image_hash").getAsString();
            boolean isTraining = !tmp.get("image_label").getAsString().equalsIgnoreCase("-1");
            int size = tmp.get("image_size").getAsInt();
            return new SinglePhotoResponse(tmpHash,isTraining,size);
        }
        else
            return  null;
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

    public boolean loadSinglePhotoResponse(SinglePhotoResponse r) throws IOException {
        if(r == null)
            return false;
        if(r.getHash() == null)
            return false;
        if(r.getBmp() == null)
            r.setBmp(getBitmapForHash(r.getHash()));
        String path;
        if(r.isTraining()) {
            List<training_face> tfs = db.trainingFaceDao().getTrainingFaceWithHash(r.getHash());
            if(tfs.size() != 1)     // entry was deleted from database
                return false;
            else {
                training_face t = tfs.get(0);
                path = FaceOperator.getAbsolutePath(c,t);
            }
        }
        else {
            List<detected_face> dfs = db.detectedFacesDao().getDetectionWithHash(r.getHash());
            if(dfs.size() != 1)     // entry was deleted from database
                return false;
            else {
                detected_face t = dfs.get(0);
                path = FaceOperator.getAbsolutePath(c,t);
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

    public boolean backupDetections( List<SinglePhotoResponse> inCloud ) throws IOException {
        List<detected_face> dfs = db.detectedFacesDao().getAllRecords();
        if (dfs == null)
            return true;
        if (inCloud == null)
            return saveAllDetections();
        for (SinglePhotoResponse s : inCloud) {
            for (detected_face d : dfs) {
                if (s.getHash().equals(d.hash)){
                    dfs.remove(d);
                    break;
                }
            }
        }
        boolean saved = true;
        for(detected_face d:dfs)
            saved &= saveSingleDetectedFace(d);
        return saved;
    }

    public boolean backupTrainings( List<SinglePhotoResponse> inCloud ) throws IOException {
        List<training_face> tfs = db.trainingFaceDao().getAllRecords();
        if (tfs == null)
            return true;
        if (inCloud == null)
            return saveAllTrainingData();
        for (SinglePhotoResponse s : inCloud) {
            for (training_face t : tfs) {
                if (s.getHash().equals(t.hash)){
                    tfs.remove(t);
                    break;
                }
            }
        }
        boolean saved = true;
        for(training_face t:tfs)
            saved &= saveSingleTrainingFace(t);
        return saved;
    }

    public boolean backupBoth( List<SinglePhotoResponse> inCloud ) throws IOException {
        if(inCloud == null)
            return saveAllDetections() && saveAllTrainingData();
        List<training_face> tfs = db.trainingFaceDao().getAllRecords();
        List<detected_face> dfs = db.detectedFacesDao().getAllRecords();
        if (tfs == null && dfs == null)
            return true;
        if (tfs == null)
            tfs = new ArrayList<>();
        if (dfs == null)
            dfs = new ArrayList<>();
        boolean found = false;
        for(SinglePhotoResponse tmps:inCloud) {
            for(detected_face tmpd:dfs){
                if(tmpd.hash.equals(tmps.getHash())) {
                    dfs.remove(tmpd);
                    found = true;
                    break;
                }
            }
            if(found){
                found = false;
                continue;
            }
            for (training_face tmpt : tfs) {
                if(tmpt.hash.equals(tmps.getHash())) {
                    tfs.remove(tmpt);
                    break;
                }
            }
            found = false;
        }
        boolean saved = true;
        for(training_face tmp:tfs)
            saved &= saveSingleTrainingFace(tmp);
        for(detected_face tmp:dfs)
            saved &= saveSingleDetectedFace(tmp);
        return saved;
    }

    public void deleteAllData() {
        try {
            backupBoth(listAllPhotos());
        } catch (IOException ignored) {}
        db.trainingFaceDao().purgeData();
        db.detectedFacesDao().purgeData();
    }
}