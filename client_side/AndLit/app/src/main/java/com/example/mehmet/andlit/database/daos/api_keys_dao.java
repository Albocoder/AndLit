package com.example.mehmet.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.mehmet.andlit.database.entities.API_key;

import java.util.List;



@Dao
public interface api_keys_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertKeys(API_key... keys);

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertKey(API_key key);

    @Query("select * from api_keys")
    List<API_key> getAllKeys();

    @Query("select `key` from api_keys where website_name = :name")
    String getKeyForWebsite(String name);

    @Query("select distinct website_name from api_keys where `key` = :k")
    List<String> getWebsitesHavingKey(String k);

    @Query("delete from api_keys")
    void purgeData();

    @Delete
    void deleteKey(API_key key);

    @Delete
    void deleteKeys(API_key... keys);

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateKey(API_key toChange);
}
