package com.example.mehmet.andlit.CloudInterface;

import android.content.Context;
import android.util.Log;

import com.example.mehmet.andlit.database.AppDatabase;
import com.example.mehmet.andlit.database.entities.UserLogin;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

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
    private AppDatabase db;
    private AuthAPI a;

    public Authenticator(Context c) {
        db = AppDatabase.getDatabase(c);
        Retrofit api = new Retrofit.Builder().baseUrl("https://andlit.info")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        a = api.create(AuthAPI.class);
    }

    public UserLogin login() {
        List<UserLogin> u = db.userLoginDao().getLoginEntry();
        if( u.size() <= 0 )
            return null;
        return u.get(0);
    }

    public void logout() {
        db.userLoginDao().deleteEntries();
    }

    // deletes all possible logged in users and creates 1 entry
    public UserLogin login(String un,String pw) throws IOException {
        if(un.length() <=0 || pw.length() <= 0)
            return null;
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"username\": \""+un+"\",\"password\":\""+pw+"\"}").getAsJsonObject();
        Call<JsonObject> call = a.getAuthTokenForUser(o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject res = resp.body();
            String accessToken = res.get("token").getAsString();
            resp = a.getInfoForLoggedInUser("Token "+accessToken).execute();
            code = resp.code();
            if( code >= 200 && code < 300) {
                res = resp.body();
                long id = res.get("id").getAsLong();
                UserLogin ul = new UserLogin(id,un,accessToken);
                logout();
                db.userLoginDao().insertEntry(ul);
                return ul;
            }
            else
                return null;
        }
        else
            return null;
    }

    public UserLogin register(String un,String email,String pw) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(
                "{\"username\":\""+un+"\",\"email\":\""+email+"\",\"password\":\""+pw+"\"}")
                .getAsJsonObject();
        Response<JsonObject> resp = a.registerNewUser(o).execute();
        int code = resp.code();
        if( code >= 200 && code < 300 ) {
            o = resp.body();
            try {
                long id = o.get("id").getAsLong();
                String username = o.get("username").getAsString();
                String accessToken = o.get("token").getAsString();
                logout();
                UserLogin ul = new UserLogin(id,username,accessToken);
                db.userLoginDao().insertEntry(ul);
                return ul;
            }catch (NullPointerException e){ return null; }
        }
        else
            return null;
    }

    public UserLogin getUserData() {
        if(isLoggedIn())
            return db.userLoginDao().getLoginEntry().get(0);
        return null;
    }

    public boolean changePassword(String newPw) throws IOException {
        if(newPw.length() <= 0 || !isLoggedIn())
            return false;
        UserLogin ul = db.userLoginDao().getLoginEntry().get(0);
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"password\":\""+newPw+"\"}").getAsJsonObject();
        Response<JsonObject> resp = a.changePasswordForUser
                (o,"Token "+ul.access_token).execute();
        int code = resp.code();
        if(code >= 200 && code < 300)
            return true;
        return false;
    }

    public boolean isLoggedIn() { return db.userLoginDao().getLoginEntry().size() > 0; }
}
