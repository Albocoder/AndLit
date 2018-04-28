package com.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andlit.database.entities.Pool;

import java.util.List;

@Dao
public interface pools_dao {
    @Insert
    void insertPool(Pool g);

    @Delete
    void deletePool(Pool g);

    @Update
    void updatePool(Pool g);

    @Query("delete from pools")
    void purgeData();

    @Query("select * from pools where creator_un = :un")
    List<Pool> getAllPoolsCreatedByUsername(String un);

    @Query("select * from pools where creator_id = :id")
    List<Pool> getAllPoolsCreatedByID(Long id);

    @Query("select * from pools where is_creator=1 ")
    List<Pool> getAllPoolsCreatedByMe();

    @Query("select * from pools where is_creator=0 ")
    List<Pool> getAllPoolsCreatedByOthers();

    @Query("select * from pools")
    List<Pool> getAllPools();

    @Query("select count(*) from pools")
    int getNumberOfPools();

    @Query("select creator_un from pools")
    List<String> getAllAdmins();

    @Query("select creator_un from pools where id = :id")
    String getAdminOfPoolID(String id);

    @Query("select * from pools where id = :id")
    Pool getPoolWithID(String id);

    @Query("select creator_un from pools where name = :name")
    List<String> getAdminOfPoolsName(String name);

    @Query("select * from pools where name = :n")
    List<Pool> getAllPoolsWithName(String n);
}