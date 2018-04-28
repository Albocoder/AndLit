package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.andlit.database.AppDatabase;

import java.util.List;

@Entity(tableName = "pools")
public class Pool {
    @NonNull
    @PrimaryKey
    public String id;
    @NonNull
    public String name;
    @NonNull
    public String password;
    @NonNull
    public Long creator_id;
    @NonNull
    public String creator_un;
    @NonNull
    public Boolean is_creator;

    public Pool(String id,String name,String password,Long creator_id,String creator_un,Boolean is_creator) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.creator_id = creator_id;
        this.creator_un = creator_un;
        this.is_creator = is_creator;
    }

    public Pool(String id, String name, String password, Long creator_id, String creator_un, AppDatabase db) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.creator_id = creator_id;
        this.creator_un = creator_un;
        List<UserLogin> uls = db.userLoginDao().getLoginEntry();
        if(uls.size() == 0)
            this.is_creator = false;
        this.is_creator = uls.get(0).username.equalsIgnoreCase(creator_un);
    }

    public Pool(String id, String name, String password, Long creator_id, String creator_un, String myUsername) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.creator_id = creator_id;
        this.creator_un = creator_un;
        this.is_creator = myUsername.equalsIgnoreCase(creator_un);
    }
}
