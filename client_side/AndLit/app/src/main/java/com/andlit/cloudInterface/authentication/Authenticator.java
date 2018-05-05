package com.andlit.cloudInterface.authentication;

import android.content.Context;

import com.andlit.R;
import com.andlit.cloudInterface.synchronizers.classifier.ClassifierBackup;
import com.andlit.cloudInterface.synchronizers.database.DatabaseBackup;
import com.andlit.cloudInterface.synchronizers.photo.PhotoBackup;
import com.andlit.cron.CronMaster;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.UserLogin;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Authenticator {
    // constants
    private static final String TAG = "Authenticator";

    // variables
    private AuthAPI a;
    private Context c;

    public Authenticator(Context c) {
        Retrofit api = new Retrofit.Builder().baseUrl(c.getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.c = c;
        a = api.create(AuthAPI.class);
    }

    public boolean logoutAndBackup() {
        try {
            ClassifierBackup    cb  = new ClassifierBackup(c);
            DatabaseBackup      dbb = new DatabaseBackup(c);
            PhotoBackup         pb  = new PhotoBackup(c);

            boolean passed = cb.backupClassifier(null);
            passed &= pb.backupAllData();
            passed &= dbb.saveDatabase();
            if(!passed)
                return false;

            cb.deleteAllData();
            pb.deleteAllData();
            dbb.deleteAllData();
            CronMaster.cancelAllJobs(c);
        } catch (Exception e) {
            return false;
        } catch (Throwable throwable) {
            return false;
        }
        return true;
    }

    public boolean logout(){
        new PhotoBackup(c).deleteAllData();
        try { new ClassifierBackup(c).deleteAllData(); } catch (Exception ignored) {}
        try { new DatabaseBackup(c).deleteAllData(); } catch (Exception ignored) {}
        CronMaster.cancelAllJobs(c);
        return true;
    }

    public boolean lock() {
        if(!isLoggedIn())
            return false;
        AppDatabase db = AppDatabase.getDatabase(c);
        UserLogin ul = db.userLoginDao().getLoginEntry().get(0);
        ul.access_token = null;
        db.userLoginDao().updateEntry(ul);
        return true;
    }

    // deletes all possible logged in users and creates 1 entry
    public UserLogin login(String un,String pw) throws IOException {
        if(un.length() <=0 || pw.length() <= 0)
            return null;
        AppDatabase db = AppDatabase.getDatabase(c);
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"username\": \""+un+"\",\"password\":\""+pw+"\"}").getAsJsonObject();
        Call<JsonObject> call = a.getAuthTokenForUser(o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject res = resp.body();
            if(res == null)
                return null;
            String accessToken = res.get("token").getAsString();
            resp = a.getInfoForLoggedInUser("Token "+accessToken).execute();
            code = resp.code();
            if( code >= 200 && code < 300) {
                res = resp.body();
                if(res == null)
                    return null;
                long id = res.get("id").getAsLong();
                UserLogin ul = new UserLogin(id,un,accessToken);
                db.userLoginDao().deleteEntries();
                db.userLoginDao().insertEntry(ul);

                try {
                    DatabaseBackup dbb = new DatabaseBackup(c);
                    boolean passes = dbb.loadDatabase();
                    db = AppDatabase.getDatabase(c);
                    if(passes){
                        db.userLoginDao().deleteEntries();
                        db.userLoginDao().insertEntry(ul);
                    }
                    else { return null; }
                    db = AppDatabase.getDatabase(c);

                    ClassifierBackup    cb = new ClassifierBackup(c);
                    PhotoBackup         pb  = new PhotoBackup(c);

                    passes = pb.retrieveAllData();
                    passes &= cb.loadClassifier();
                    if(!passes)
                        return null;
                } catch (Exception e) {
                    return null;
                }

                return ul;
            }
            else
                return null;
        }
        else
            return null;
    }

    public UserLogin register(String un,String email,String pw) throws IOException {
        AppDatabase db = AppDatabase.getDatabase(c);
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(
                "{\"username\":\""+un+"\",\"email\":\""+email+"\",\"password\":\""+pw+"\"}")
                .getAsJsonObject();
        Response<JsonObject> resp = a.registerNewUser(o).execute();
        int code = resp.code();
        if( code >= 200 && code < 300 ) {
            o = resp.body();
            if(o == null)
                return null;
            try {
                long id = o.get("id").getAsLong();
                String username = o.get("username").getAsString();
                String accessToken = o.get("token").getAsString();
                db.userLoginDao().deleteEntries();
                UserLogin ul = new UserLogin(id,username,accessToken);
                db.userLoginDao().insertEntry(ul);
                return ul;
            }catch (NullPointerException e){ return null; }
        }
        else
            return null;
    }

    public UserLogin getUserData() {
        AppDatabase db = AppDatabase.getDatabase(c);
        if(isLoggedIn() || isLocked())
            return db.userLoginDao().getLoginEntry().get(0);
        return null;
    }

    public boolean changePassword(String newPw) throws IOException {
        AppDatabase db = AppDatabase.getDatabase(c);
        if(newPw.length() <= 0 || !isLoggedIn())
            return false;
        UserLogin ul = db.userLoginDao().getLoginEntry().get(0);
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"password\":\""+newPw+"\"}").getAsJsonObject();
        Response<JsonObject> resp = a.changePasswordForUser
                (o,"Token "+ul.access_token).execute();
        int code = resp.code();
        return code >= 200 && code < 300;
    }

    public boolean isLoggedIn() {
        AppDatabase db = AppDatabase.getDatabase(c);
        List<UserLogin> uls = db.userLoginDao().getLoginEntry();
        if(uls.size() <= 0)
            return false;
        if( uls.get(0).access_token == null || "".equalsIgnoreCase(uls.get(0).access_token) )
            return false;
        return true;
    }

    public boolean isLocked() {
        AppDatabase db = AppDatabase.getDatabase(c);
        List<UserLogin> uls = db.userLoginDao().getLoginEntry();
        if(uls.size() <= 0)
            return false;
        if(uls.get(0).access_token == null || uls.get(0).access_token.equalsIgnoreCase(""))
            return true;
        return false;
    }

    public UserLogin unlock(String pw) throws IOException {
        AppDatabase db = AppDatabase.getDatabase(c);
        if(!isLocked())
            return null;
        String un = db.userLoginDao().getLoginEntry().get(0).username;
        if(un.length() <=0 || pw.length() <= 0)
            return null;
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"username\": \""+un+"\",\"password\":\""+pw+"\"}").getAsJsonObject();
        Call<JsonObject> call = a.getAuthTokenForUser(o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject res = resp.body();
            if(res == null)
                return null;
            String accessToken = res.get("token").getAsString();
            resp = a.getInfoForLoggedInUser("Token "+accessToken).execute();
            code = resp.code();
            if( code >= 200 && code < 300) {
                res = resp.body();
                if(res == null)
                    return null;
                long id = res.get("id").getAsLong();
                UserLogin ul = new UserLogin(id,un,accessToken);
                db.userLoginDao().deleteEntries();
                db.userLoginDao().insertEntry(ul);
                return ul;
            }
            else
                return null;
        }
        else
            return null;
    }
}
