package com.andlit.cloudInterface.pools;

import android.content.Context;
import android.util.Log;

import com.andlit.R;
import com.andlit.cloudInterface.pools.models.PoolMember;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.Pool;
import com.andlit.database.entities.UserLogin;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PoolOps {

    // constants
    private static final String TAG = "PoolOps";

    // variables
    private PoolsAPI a;
    private UserLogin ul;
    private AppDatabase db;
    private Context c;

    public PoolOps(Context c) throws Exception {
        Retrofit api = new Retrofit.Builder().baseUrl(c.getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.c = c;
        db = AppDatabase.getDatabase(c);
        List<UserLogin> uls = db.userLoginDao().getLoginEntry();
        if(uls.size() == 0) {
            Log.d(TAG,"User not logged in!");
            throw new Exception("User not logged in!");
        }
        ul = uls.get(0);

        a = api.create(PoolsAPI.class);
    }

    public Pool createPool(String poolName) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"pool_name\": \""+poolName+"\"}").getAsJsonObject();
        Call<JsonObject> call = a.createPool("Token "+ul.access_token,o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject body = resp.body();
            if(body == null)
                return null;
            Pool createdPool = parsePoolFromResponse(body);
            db.poolsDao().insertPool(createdPool);
            return createdPool;
        }
        return null;
    }

    public Pool changePoolName(String newName,Pool p) throws IOException {
        return changePoolName(newName,p.id);
    }
    public Pool changePoolName(String newName,String id) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"pool_name\": \""+ newName +"\", " +
                "\"pool_id\": \""+id+"\"}").getAsJsonObject();
        Call<JsonObject> call = a.changePoolName("Token "+ul.access_token,o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            Pool existingPool = db.poolsDao().getPoolWithID(id);
            if(existingPool != null) {
                existingPool.name = newName;
                db.poolsDao().updatePool(existingPool);
                return existingPool;
            }
            else{
                JsonObject body = resp.body();
                if(body == null)
                    return null;
                Pool createdPool = parsePoolFromResponse(body);
                db.poolsDao().insertPool(createdPool);
                return createdPool;
            }
        }
        return null;
    }

    public Pool changePoolPassword(String id) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"pool_id\": \""+id+"\"}").getAsJsonObject();
        Call<JsonObject> call = a.changePoolPw("Token "+ul.access_token,o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject body = resp.body();
            if(body == null)
                return null;
            String poolPw = body.get("pool_password").getAsString();
            Pool existingPool = db.poolsDao().getPoolWithID(id);
            if(existingPool != null) {
                existingPool.password = poolPw;
                db.poolsDao().updatePool(existingPool);
                return existingPool;
            }
            else {
                Pool changedPool = parsePoolFromResponse(body);
                db.poolsDao().insertPool(changedPool);
                return changedPool;
            }
        }
        return null;
    }
    public Pool changePoolPassword(Pool p) throws IOException {
        return changePoolPassword(p.id);
    }

    public Pool joinPool(String id,String pw) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"pool_id\": \""+id+"\", \"pool_password\":\""+
                pw+"\" }").getAsJsonObject();
        Call<JsonObject> call = a.joinPool("Token "+ul.access_token,o);
        Response<JsonObject> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonObject body = resp.body();
            if(body == null)
                return null;
            Pool joinedPool = parsePoolFromResponse(body);
            Pool p = db.poolsDao().getPoolWithID(id);
            if (p==null)
                db.poolsDao().insertPool(joinedPool);
            else
                db.poolsDao().updatePool(joinedPool);
            return joinedPool;
        }
        return null;
    }

    // todo: test this method the moment it will work
    public List<Pool> listPools() throws IOException {
        Call<JsonArray> call = a.listPools("Token "+ul.access_token);
        Response<JsonArray> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonArray arr = resp.body();
            if(arr == null)
                return null;
            ArrayList<Pool> toReturn = new ArrayList<>();
            db.poolsDao().purgeData();
            for (JsonElement e: arr) {
                JsonObject tmp = (JsonObject)e;
                Pool tmpPool = parsePoolFromResponse(tmp);
                toReturn.add(tmpPool);
                db.poolsDao().insertPool(tmpPool);
            }
            return toReturn;
        }
        return null;
    }

    public List<PoolMember> getMembersOfPool(Pool p) throws IOException {
        return getMembersOfPool(p.id);
    }
    public List<PoolMember> getMembersOfPool(String id) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"pool_id\": \""+id+"\"}").getAsJsonObject();
        Call<JsonArray> call = a.listUsersInPool("Token "+ul.access_token,o);
        Response<JsonArray> resp = call.execute();
        int code = resp.code();
        if ( code >= 200 && code < 300) {
            JsonArray arr = resp.body();
            if(arr == null)
                return null;
            ArrayList<PoolMember> toReturn = new ArrayList<>();
            for (JsonElement e: arr) {
                JsonObject tmpMember = (JsonObject)e;
                PoolMember tmp = parsePoolMemberFromResponse(tmpMember);
                toReturn.add(tmp);
            }
            return toReturn;
        }
        return null;
    }

    public boolean kickFromPool(Pool p,String username) throws IOException {
        return kickFromPool(p.id,username);
    }
    public boolean kickFromPool(String poolID,String username) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{ \"pool_id\" : \""+poolID+"\" , \"username\" : \""
                +username+"\" }").getAsJsonObject();
        Call<String> call = a.kickUserFromPool("Token "+ul.access_token,o);
        Response<String> resp = call.execute();
        int code = resp.code();
        return code >= 200 && code < 300;
    }

    public boolean leavePool (Pool p) throws IOException {
        return leavePool(p.id);
    }
    public boolean leavePool (String poolID) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse("{\"pool_id\": \""+poolID+"\" }").getAsJsonObject();
        Call<String> call = a.leavePool("Token "+ul.access_token,o);
        Response<String> resp = call.execute();
        int code = resp.code();
        if(code >= 200 && code < 300){
            try {
                db.poolsDao().deletePoolWithID(poolID);
            }catch (Exception ignored){}
            return true;
        }
        return false;
    }

    // *************************** PRIVATE FUNCTIONS ******************************* //
    private Pool parsePoolFromResponse(JsonObject body) {
        if (body == null)
            return null;
        JsonObject creator = body.getAsJsonObject("pool_creator");
        long creatorID = creator.get("pk").getAsLong();
        String creatorUn = creator.get("username").getAsString();
        JsonObject group = body.getAsJsonObject("pool_group");
        String poolID = group.get("name").getAsString();
        String name = body.get("pool_name").getAsString();
        String password = body.get("pool_password").getAsString();
        return new Pool(poolID,name,password,creatorID,creatorUn,
                creatorUn.equalsIgnoreCase(ul.username));
    }
    private PoolMember parsePoolMemberFromResponse(JsonObject body) {
        if(body == null)
            return null;
        long id = body.get("pk").getAsLong();
        String un = body.get("username").getAsString();
        return new PoolMember(id,un);
    }
}
